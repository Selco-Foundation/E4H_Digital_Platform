# Deployment

## Deployment

### Deployment

E4H deployment is managed from [DIGIT-DevOps](https://github.com/Selco-Foundation/DIGIT-DevOps). It uses two stages: **Part 1 — infrastructure provisioning** (Terraform) and **Part 2 — application deployment** (Helm/Helmfile). Provision the cluster first, then deploy the application; if a cluster already exists, you can skip straight to Part 2.

#### Deployment architecture

The [Selco AWS Architecture diagram](https://app.eraser.io/workspace/86s3qP7CeCWNYIZjJDIR) documents ingress into the AWS-hosted E4H platform. It shows application workloads in EKS and managed data services.

![Selco AWS deployment architecture](https://files.gitbook.com/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FdOIYSDzObZCmTnRmkwhn%2Fuploads%2FH8VDL2acgHrLWnP5y3Ab%2Fselco-aws-architecture.png?alt=media\&token=76decc75-672a-45ce-81f0-a36d7fc5cfc3)

Selco AWS architecture showing ingress, EKS workloads, managed data services, object storage, access control, and outbound connectivity.

#### Third-party dependencies

The install depends on several external services the project does not control. If any are down, misconfigured, or unreachable, the corresponding step **will fail**. Check these first when troubleshooting.

| # | Dependency | Used for | Impact if unavailable |
|---|---|---|---|
| 1 | **GoDaddy / Cloudflare** (or your domain registrar/DNS provider) | Hosting the DNS zone; creating the CNAME record that points to the ingress load balancer | Domain won't resolve to the cluster; cert-manager's ACME challenge (SSL) will fail |
| 2 | **Docker Hub** | Pulling public base images used by Helm charts and CI build steps | Pod images fail to pull (`ImagePullBackOff`); Helmfile/Helm releases stay pending |
| 3 | **GitHub Actions** | CI/CD pipelines that build/push images and, in some workflows, run the Helmfile deploy | Automated builds/deploys don't run; manual `helm`/`helmfile` deploys are unaffected |
| 4 | **[Nexus repository](https://nexus-repo.egovernments.org/nexus)** | Hosts build artifacts for E4H **backend services** (JARs/Maven artifacts) consumed during image builds | Backend image builds fail; deploying a **new** image tag is blocked (already-built/pushed images are unaffected) |
| 5 | **AWS** | EKS, RDS, S3, IAM, VPC networking (Part 1), plus the registry/storage deployed services use at runtime | Terraform apply fails outright; an outage can affect RDS/S3-backed services and new provisioning even on an existing cluster |
| 6 | **Image repositories for backbone & monitoring services** (`quay.io`, `ghcr.io`, `bitnami`, `grafana`, or a private registry — see each chart's `values.yaml` `image.repository`) | Pulling images for Kafka, Postgres, Elasticsearch, ingress-nginx, cert-manager, Loki, Promtail, etc. | Those pods fail to start, blocking any core service that depends on them (DB, Kafka, ingress) |

Verify connectivity/credentials to all of the above **before** a fresh install, and re-check them first if a previously working deploy suddenly fails.

#### Part 1 — Infrastructure provisioning (Terraform)

`infra-as-code/terraform/sample-aws` provisions the AWS infrastructure the platform runs on. Application/service deployment is not covered here — see [Part 2](#part-2-application-deployment-helm-helmfile).

The `terraform/` folder also contains other configs (`egov-cicd`, `quickstart-aws-ec2`, `node-pool`, `sample-azure`, `sample-gke`, ...). These are generic/legacy templates from the eGov DIGIT framework used by other projects and are **not** used for E4H — ignore them unless you know you need them.

**What gets created:**

* A VPC with public + private subnets, an Internet Gateway, a NAT Gateway, and route tables.
* An EKS cluster and managed node group, with `vpc-cni`, `kube-proxy`, `coredns`, and `aws-ebs-csi-driver` addons, gated by `create_eks`.
* An IAM role (IRSA) for the EBS CSI driver, and a default `gp3` encrypted storage class.
* A PostgreSQL RDS instance, gated by `create_rds`.
* Two S3 buckets (`<cluster_name>-assets-bucket`, `<cluster_name>-filestore-bucket`) with public read policies, plus an IAM user/access key/policy scoped to the filestore bucket.
* A Kubernetes secret (`egov-filestore`) containing the filestore IAM credentials (only when `create_eks = true`).

**Prerequisites:**

* **Terraform** >= 1.5.7, **AWS CLI v2**, **kubectl**, and **git** installed locally or on the build box.
* An AWS account with credentials resolvable by the Terraform AWS provider (`aws configure` or `aws configure sso`), and regional capacity/quota for 1 VPC, 1 NAT Gateway + 1 Elastic IP, 1 EKS cluster, and the chosen worker instance type.
* IAM permissions to create VPC networking, IAM roles/users/policies (including an OIDC provider for IRSA), EKS, RDS, S3, plus the state backend's S3 bucket and DynamoDB table. **AdministratorAccess** is the simplest option; otherwise the identity needs at minimum `ec2:*`, `eks:*`, the IAM role/user/policy/OIDC/`PassRole` actions, `rds:*` (if `create_rds = true`), `s3:*`, `dynamodb:*`, and `sts:GetCallerIdentity`.
* `enable_cluster_creator_admin_permissions = true` in `main.tf` grants the identity that creates the cluster admin access to it automatically.

**Steps:**

1. **Set up the remote state backend** (one-time, per environment) — Terraform state is stored in S3 with DynamoDB locking:
   ```bash
   cd infra-as-code/terraform/sample-aws/remote-state
   terraform init
   terraform apply -var="bucket_name=<your-unique-state-bucket-name>"
   ```
   The AWS region is hardcoded in `main.tf` (`ap-south-2`) — edit it if you need a different region. Do this once per environment with a distinct bucket name, and note the bucket name, key, region, and DynamoDB table name for the next step.
2. **Configure the environment** — from `infra-as-code/terraform/sample-aws`, start from an existing tfvars file (`tfvars/dev.tfvars`, `tfvars/prod.tfvars`) or create a new one. Key variables: `aws_region`, `cluster_name` (must be unique per environment), `vpc_cidr_block`, `network_availability_zones`/`availability_zones`, `kubernetes_version`, `architecture` (`x86_64`/`arm64`), `instance_types`, node group scaling (`min_worker_nodes`/`desired_worker_nodes`/`max_worker_nodes`), `max_pods_per_node`, `create_eks`, `create_rds`, RDS settings (`db_name`, `db_username`, `db_version`, `db_instance_class`), and `filestore_namespace`. `db_password` has no default — Terraform prompts for it interactively, or pass `TF_VAR_db_password` in CI.
3. **Initialize and apply**, pointing at the state backend from step 1:
   ```bash
   terraform init \
     -backend-config="bucket=<state-bucket-name>" \
     -backend-config="key=terraform/terraform.tfstate" \
     -backend-config="region=<state-bucket-region>" \
     -backend-config="dynamodb_table=<state-bucket-name>" \
     -backend-config="encrypt=true"

   terraform plan  -var-file=tfvars/dev.tfvars
   terraform apply -var-file=tfvars/dev.tfvars
   ```
   Takes ~15-20 minutes (EKS cluster + node group creation is the slowest part).
4. **Verify:**
   ```bash
   aws eks update-kubeconfig --name <cluster_name> --region <aws_region>
   kubectl get nodes
   ```
   Terraform outputs also expose `vpc_id`, `private_subnets`, `public_subnets`, `db_instance_endpoint`, `s3_assets_bucket`, and `s3_filestore_bucket` for use in Part 2.
5. **Tear down** (if needed): `terraform destroy -var-file=tfvars/<env>.tfvars`. The remote-state S3 bucket has `prevent_destroy = true` and is managed separately in `remote-state/` — destroying `sample-aws` does not remove it.

#### Part 2 — Application deployment (Helm/Helmfile)

`deploy-as-code/digit-helmfile.yaml` and `deploy-as-code/charts` coordinate Helm and Helmfile releases onto the cluster from Part 1 (or any existing cluster with `kubectl` access, a reachable Postgres instance, and an ingress controller).

**Repository layout (`deploy-as-code/`):**

```
deploy-as-code/
├── digit-helmfile.yaml               # root helmfile, includes the sub-helmfiles below
├── charts/
│   ├── backbone-services/            # kafka, postgresql, redis, elasticsearch, ingress-nginx, cert-manager, minio, pgadmin ...
│   ├── core-services/                # all DIGIT microservices (egov-user, egov-mdms-service, workflow, UI, state modules, ...)
│   ├── monitoring/                   # loki, promtail
│   ├── auxiliary-services/           # oauth2-proxy, pgadmin, kafka-connect, s3-proxy ...
│   ├── common / common-chart-template/  # shared helpers + boilerplate chart used to scaffold new services
│   ├── environments/                 # per-environment values + secrets (sops encrypted)
│   │   ├── <env>.yaml                #   non-secret config
│   │   └── <env>-secrets.yaml        #   secret config, encrypted with sops
│   ├── product-release-charts/       # version manifests: service -> image tag, per release
│   └── .sops.yaml                    # sops encryption rules (KMS key per environment)
```

Each service under `core-services/` (and the other categories) is a standalone Helm chart with its own `Chart.yaml` and `values.yaml`. Helmfile orchestrates applying a selected set of these charts against an environment's values/secrets files.

| Layer          | Components                                                                                                                                                                                                                                   |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Backbone       | PostgreSQL/RDS, Kafka, Elasticsearch master/data, Kibana, Redis, ingress-nginx, cert-manager, MinIO, pgAdmin                                                                                                                                 |
| Core platform  | eGov Gateway, MDMS, Workflow, Filestore, IDGen, Notification, User, Persister, Indexer, Boundary, and supporting DIGIT services                                                                                                              |
| E4H and domain | AMC Service, Asset Registry, Field Planner, Field Planner Activity, Ingestion Service, Project Service, Vendor Registry, RMS Service, IM Services, IM Services Analytics, Processor Services, Facility Service, Installation QC, PDF Service |
| Interfaces     | DIGIT UI, state UIs, Workbench UI                                                                                                                                                                                                            |
| Operations     | Monitoring and auxiliary services                                                                                                                                                                                                            |

**Prerequisites:**

Tools: `kubectl` (configured with a context pointing at the target cluster), `helm` v3, `helmfile` (+ the `helm-diff` plugin), [`sops`](https://github.com/mozilla/sops) (encrypt/decrypt `*-secrets.yaml`), `yq` (parse image tags from `product-release-charts` manifests), and AWS CLI if secrets use AWS KMS and/or the cluster is EKS.

Cluster/environment prerequisites (already in place if Part 1 is complete): `ingress-nginx` and `cert-manager` (or your own ingress/TLS solution); a reachable Postgres database; object storage (S3 or MinIO) for `egov-filestore`; the target namespaces (`core-dev`, `backbone-dev` by default — these are namespace names carried over from the dev environment, not an environment indicator, and can be renamed); SMS/Email gateway credentials for `egov-notification-sms`/`egov-notification-mail`; a reachable git repo with the DIGIT MDMS/persister/indexer config YAMLs (referenced via `initContainers.gitSync`); a KMS or GPG key matching `charts/.sops.yaml`; and a domain name you can point at the cluster (see [DNS and TLS after deployment](#dns-and-tls-after-deployment)).

**Configure the environment** — environment-specific configuration lives in `deploy-as-code/charts/environments/`. For a new environment, copy an existing pair of files (e.g. `selco-uat.yaml` / `selco-uat-secrets.yaml`) and update:

* `<env>.yaml` (non-secret values): `global.domain`, `root-ingress.cert-issuer`, `configmaps.egov-config.data.*` (`db-host`, `db-name`, `db-url`, `db-otel-url`, `es-host`, `es-indexer-host`, `kafka-brokers`, `egov-services-fqdn-name`, `egov-state-level-tenant-id`, S3 bucket names, etc.), and any per-service overrides (heap size, java-args, feature flags, `custom-js-injection` for UI charts, tenant-specific config).
* `<env>-secrets.yaml` (sops-encrypted): database credentials, `egov-filestore` access/secret keys, `egov-enc-service` master password/salt/IV, notification gateway credentials, default admin credentials, payment gateway keys, map/geocoding keys, etc.

`charts/.sops.yaml` defines the KMS key for files matching `charts/environments/env-secrets.yaml`; since the real files are named `<env>-secrets.yaml`, pass the KMS ARN explicitly:

```bash
sops --encrypt --kms <kms-key-arn> --in-place deploy-as-code/charts/environments/<env>-secrets.yaml
sops --decrypt --kms <kms-key-arn> deploy-as-code/charts/environments/<env>-secrets.yaml
```

Never commit an unencrypted secrets file.

**Select which services to deploy** — each helmfile (`coreservices-helmfile.yaml`, `backboneservices-helmfile.yaml`, `monitoring-helmfile.yaml`) declares one `releases` entry per chart, most commented out.

1. Uncomment its entry in the relevant `*-helmfile.yaml`.
2. Make sure the corresponding sub-helmfile is included (uncommented) in `deploy-as-code/digit-helmfile.yaml`.
3. Add `needs:` entries if the service depends on another release being installed first (e.g. `egov-user` needs `egov-enc-service`).

**Note:** Some includes in the root Helmfile are currently commented out. Use the environment-specific release manifest and enabled Helmfiles to determine the actual deployment graph.

**Pin image versions** — `charts/product-release-charts/dependency_chart-<product>-v<version>.yaml` is a version manifest: for a given release it lists every service and the exact image tag to deploy. Use the latest file as reference, or create a new one when cutting a release, then pass the tags via `--set <service>.image.tag=<tag>` (and `--set <service>.initContainers.dbMigration.image.tag=<tag>` for services with a Flyway/DB-migration init container). See `.github/workflows/Prod.yaml` for the current, full list of `--set` flags — every service on the helmfile with an entry in the version manifest needs its own tag flag(s).

**Deploy:**

```bash
cd deploy-as-code

# decrypt secrets in place (sops-encrypted files must be plaintext for helmfile to read them)
sops --decrypt --kms <kms-key-arn> charts/environments/<env>-secrets.yaml > /tmp/env-secrets.yaml
cp /tmp/env-secrets.yaml charts/environments/<env>-secrets.yaml

export HELMFILE_ENV=<env>       # e.g. selco-prod
helmfile -f digit-helmfile.yaml apply --include-needs=true \
  --set <service>.image.tag=<tag> \
  ...
```

Afterwards, restore the encrypted version (`git checkout -- charts/environments/<env>-secrets.yaml`) so the plaintext copy never gets committed.

**Post-deploy verification:**

* Verify pods are healthy: `kubectl get pods -n core-dev -n backbone-dev`.
* Hit each chart's health endpoint (defined per-chart under `healthChecks` in `values.yaml`, e.g. `/user/health`) through the ingress domain, once DNS/TLS is set up (see below).

**Onboarding a new microservice:**

1. Scaffold a chart from `charts/common-chart-template/` under the right category (`core-services/`, `municipal-services/`, etc.).
2. Set in its `values.yaml`: `image.repository`/`image.tag`, `ingress.context`, `replicas`, `memory_requests`/`memory_limits`, `heap`/`java-args`, `healthChecks.livenessProbePath`/`readinessProbePath`, and any `initContainers.dbMigration` or `initContainers.gitSync` (repo/branch) it needs.
3. Add a `releases` entry for it in `coreservices-helmfile.yaml` (with `needs:` if it depends on another service).
4. Add its hostname to `configmaps.egov-service-host` in `core-services/configmaps/values.yaml` (and the environment's `<env>.yaml` if overridden) so other services can discover it.
5. Add its image tag to the next `product-release-charts` version manifest and to the `--set` flags in the relevant GitHub Actions workflow(s).

#### Backbone dependencies

* **PostgreSQL/RDS:** Service persistence and Flyway migrations.
* **Kafka:** Service events, asynchronous processing, Persister topics, and Indexer topics.
* **Elasticsearch:** Inbox, search, reporting, and operational indexes.
* **eGov Persister:** Consumes configured Kafka topics and writes validated records to PostgreSQL.
* **eGov Indexer:** Consumes Kafka topics and maintains Elasticsearch indexes.
* **Redis:** Shared caching and transient state where configured.
* **Ingress and cert-manager:** External routing and TLS.
* **MinIO/S3:** Object and document storage.

#### Deployment sequence

1. Provision or update AWS infrastructure with Terraform (Part 1).
2. Validate EKS access, RDS connectivity, storage, IAM, and networking.
3. Deploy and validate backbone services.
4. Deploy core DIGIT services, including Persister and Indexer.
5. Deploy E4H and domain services and interfaces.
6. Run Flyway migrations and confirm Kafka topics and Elasticsearch indexes.
7. Complete DNS and TLS setup (see below).
8. Execute health checks, smoke tests, and rollback checks.

#### DNS and TLS after deployment

Once the `ingress-nginx` chart (part of `backbone-services`) is deployed, the cloud provider provisions a load balancer for it. The domain won't serve traffic until these steps are complete:

1. **Get the load balancer hostname:**
   ```bash
   kubectl get svc ingress-nginx-controller -n backbone-dev -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
   ```
2. **Map your domain to the load balancer.** In your DNS provider (GoDaddy, Cloudflare, etc.), create a **CNAME record** for `global.domain` (as set in `charts/environments/<env>.yaml`) pointing at the load balancer hostname from step 1. Propagation can take a few minutes to a few hours depending on the provider and TTL.
3. **SSL/TLS is handled automatically by cert-manager.** The `cert-manager` backbone chart watches Ingress resources annotated with the `ClusterIssuer` configured in `root-ingress.cert-issuer` (e.g. `letsencrypt-prod`), and requests/renews a certificate for `global.domain` once the CNAME resolves — cert-manager needs the domain to already point at the load balancer to complete the ACME challenge. No manual certificate handling is required; if a certificate stays pending, check that the CNAME has propagated and that `kubectl describe certificate -n <namespace>` doesn't show a challenge error.

#### Release checklist

* Changed services and apps.
* API and schema changes.
* Database and master-data migrations.
* Kafka topic, Persister, and Indexer changes.
* Workflow and cron changes.
* Environment variables and encrypted secrets.
* Smoke tests.
* Rollback notes.
