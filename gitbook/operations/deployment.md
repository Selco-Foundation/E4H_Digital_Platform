# Deployment

## Deployment

### Deployment

E4H deployment is managed from [DIGIT-DevOps](https://github.com/Selco-Foundation/DIGIT-DevOps). It uses two stages: AWS infrastructure provisioning and Kubernetes application deployment.

#### Deployment architecture

The [Selco AWS Architecture diagram](https://app.eraser.io/workspace/86s3qP7CeCWNYIZjJDIR) documents ingress into the AWS-hosted E4H platform. It shows application workloads in EKS and managed data services.

![Selco AWS deployment architecture](https://files.gitbook.com/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FdOIYSDzObZCmTnRmkwhn%2Fuploads%2FH8VDL2acgHrLWnP5y3Ab%2Fselco-aws-architecture.png?alt=media\&token=76decc75-672a-45ce-81f0-a36d7fc5cfc3)

Selco AWS architecture showing ingress, EKS workloads, managed data services, object storage, access control, and outbound connectivity.

#### Infrastructure provisioning

`infra-as-code/terraform/sample-aws` provisions VPCs, public and private subnets, NAT, EKS, RDS/PostgreSQL, S3, IAM, and Terraform remote state.

Apply and validate infrastructure changes before deploying Helm releases.

#### Application deployment

`deploy-as-code/digit-helmfile.yaml` and `deploy-as-code/charts` coordinate Helm and Helmfile releases.

| Layer          | Components                                                                                                                                                                                                                                   |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Backbone       | PostgreSQL/RDS, Kafka, Elasticsearch master/data, Kibana, Redis, ingress-nginx, cert-manager, MinIO, pgAdmin                                                                                                                                 |
| Core platform  | eGov Gateway, MDMS, Workflow, Filestore, IDGen, Notification, User, Persister, Indexer, Boundary, and supporting DIGIT services                                                                                                              |
| E4H and domain | AMC Service, Asset Registry, Field Planner, Field Planner Activity, Ingestion Service, Project Service, Vendor Registry, RMS Service, IM Services, IM Services Analytics, Processor Services, Facility Service, Installation QC, PDF Service |
| Interfaces     | DIGIT UI, state UIs, Workbench UI                                                                                                                                                                                                            |
| Operations     | Monitoring and auxiliary services                                                                                                                                                                                                            |

#### Backbone dependencies

* **PostgreSQL/RDS:** Service persistence and Flyway migrations.
* **Kafka:** Service events, asynchronous processing, Persister topics, and Indexer topics.
* **Elasticsearch:** Inbox, search, reporting, and operational indexes.
* **eGov Persister:** Consumes configured Kafka topics and writes validated records to PostgreSQL.
* **eGov Indexer:** Consumes Kafka topics and maintains Elasticsearch indexes.
* **Redis:** Shared caching and transient state where configured.
* **Ingress and cert-manager:** External routing and TLS.
* **MinIO/S3:** Object and document storage.

#### Environments and secrets

`selco-dev.yaml`, `selco-uat.yaml`, and `selco-prod.yaml` are under `deploy-as-code/charts/environments`.

Environment-specific values select endpoints, namespaces, image tags, and integration settings. SOPS-encrypted secret files hold sensitive values. Plaintext secrets must not be committed.

#### Release composition

The inspected product release manifest is `deploy-as-code/charts/product-release-charts/dependency_chart-selco-v3.11.35.yaml`, version `v3.11.35`.

`backbone-dev` is a prerequisite for `core-dev`. The release manifest is the authoritative service and image composition.

**Note:** Some includes in the root Helmfile are currently commented out. Use the environment-specific release manifest and enabled Helmfiles to determine the actual deployment graph.

#### Deployment sequence

1. Provision or update AWS infrastructure with Terraform.
2. Validate EKS access, RDS connectivity, storage, IAM, and networking.
3. Deploy and validate backbone services.
4. Deploy core DIGIT services, including Persister and Indexer.
5. Deploy E4H and domain services and interfaces.
6. Run Flyway migrations and confirm Kafka topics and Elasticsearch indexes.
7. Execute health checks, smoke tests, and rollback checks.

#### Release checklist

* Changed services and apps.
* API and schema changes.
* Database and master-data migrations.
* Kafka topic, Persister, and Indexer changes.
* Workflow and cron changes.
* Environment variables and encrypted secrets.
* Smoke tests.
* Rollback notes.
