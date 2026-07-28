# CI/CD

## CI/CD

### CI/CD

Two pipelines, two repos:

| Pipeline | Repo | Does |
| --- | --- | --- |
| 1. Image build | `E4H_Digital_Platform/.github/workflows/` | Builds a Docker image per app, pushes to Docker Hub |
| 2. Cluster deployment | [DIGIT-DevOps](https://github.com/Selco-Foundation/DIGIT-DevOps) `.github/workflows/Dev.yaml`, `Prod.yaml` | Applies Helmfile releases to the target cluster |

Pipeline 1 builds/pushes the image; Pipeline 2 applies the Helmfile release to deploy it to dev. `Prod.yaml` is separate and manually run. Helm/Helmfile mechanics are covered in [Deployment](deployment.md).

**Note:** this two-pipeline setup is the current (older) approach. Migration to ArgoCD (GitOps-based deployment) is planned, which will change how deployments are driven.

#### Pipeline 1 — Image build

One workflow per app — 30+ total. Same shape everywhere (triggers, versioning, image build/push); only the build step differs by app type.

GHA layer caching (and the Maven cache, for backend services) is enabled on these builds, which reduces build time.

**Triggers:** push to `develop`/`staging` (scoped by `paths:` to that app's own directory), push of a `v*` tag, or manual `workflow_dispatch`.

**`Docker_Image_Build` job — common steps:**

1. Checkout, set up Docker Buildx.
2. Resolve `VERSION` — tag name if pushed via `v*` tag, else branch name (`/` → `-`), else `latest`.
3. Capture `commit_hash` — last commit touching the app's path.
4. Log in to Docker Hub.
5. Build and push with `docker/build-push-action`, GHA layer caching. Tag: **`selcohub/<app>:<VERSION>-<commit_hash>`**.

| | Backend — ref: `amc-service.yaml` | Frontend — ref: `digit-ui.yaml` |
| --- | --- | --- |
| Path filter | `backend/e4h-services/amc-scheduler-service/**` | `frontend/micro-ui/**` |
| Extra setup | JDK 17 (Temurin) + Maven cache (`~/.m2`, keyed by `pom.xml`) | None — build runs inside the Dockerfile |
| Build | `mvn package` before the Docker step | N/A |
| Dockerfile | shared `build/maven/Dockerfile`, `build-args: JAR_FILE=<jar path>` | `frontend/micro-ui/web/docker/testfile/Dockerfile`, `build-args: WORK_DIR=frontend/micro-ui/`, `GA_MEASUREMENT_ID` (resolved from branch/tag: dev/staging/prod measurement ID) |
| Extras | Some services also build a companion DB image (`selcohub/<service>-db:...`) | State UIs add `build-args: PUBLIC_PATH=/<state>/` and push `selcohub/<state>-ui:...` |

`sonarcloud.yml` is the exception — static analysis only, no build/push step.

`staging`/tag builds only build and push the image — no auto-deploy.

#### Pipeline 2 — Cluster deployment

`Dev.yaml` and `Prod.yaml` share one structure; differences noted inline.

**Triggers:** `workflow_dispatch` (manual) and `repository_dispatch`.

**`check-changed-files` job** — flags whether `deploy-as-code/**` changed.

**`DIGIT-deployment` job:**

1. Checkout.
2. AWS auth — `Prod`: assume IAM role via OIDC. `Dev`: static `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`.
3. `aws eks update-kubeconfig --name <cluster_name>` for the target cluster.
4. Install `sops`, decrypt the env's secrets file in place (not restored afterwards — the manual flow in [Deployment](deployment.md#part-2-application-deployment-helm-helmfile) restores it; this workflow doesn't).
5. Install Helm (+ `helm-diff`) and Helmfile.
6. `yq`-parse the release's version manifest (e.g. `dependency_chart-selco-v3.13.36.yaml`) into per-service env vars.
7. `helmfile -f digit-helmfile.yaml apply --include-needs=true --set <service>.image.tag=... [--set <service>.initContainers.dbMigration.image.tag=...]` for every service, with `HELMFILE_ENV=selco-prod`/`selco-dev`.

**Maintenance point:** the manifest filename and every `--set` flag are hardcoded. New release → add the manifest **and** update the filename in the `yq` step. New service → add its `--set` flag(s) too.

| Secret / variable | Used for |
| --- | --- |
| `DOCKER_USERNAME` / `DOCKER_PASSWORD` | Docker Hub login |
| `AWS_REGION`, `CLUSTER_NAME_PROD`/dev equivalent | EKS targeting |
| IAM role (Prod) or `AWS_ACCESS_KEY_ID`+`SECRET` (Dev) | AWS auth for deployment |
| `PUBLIC_KMS_KEY_PROD`/dev equivalent | Decrypting `sops` secrets |

#### End-to-end flow

1. Push to `develop`/`staging`, or a `v*` tag, under an app's path filter.
2. Its workflow builds and pushes `selcohub/<app>:<VERSION>-<commit_hash>`.
3. `Dev.yaml` is run to deploy to dev: auths to AWS/EKS, decrypts dev secrets, resolves tags from the version manifest, runs `helmfile apply`.
4. Prod is a separate, manual `Prod.yaml` run against the prod cluster and `selco-prod` secrets.

#### Build configuration

Shared build assets:

* `build/build-config.yml`, `frontend/build/build-config.yml`
* `build/maven`, `build/maven-java8`, `build/python`, `build/adhoc`

#### Documentation guidance

When adding a service or app pipeline:

1. Add/update the workflow, scoping `paths:` to the app's directory.
2. Update its README build instructions.
3. Add its image tag (and `dbMigration` tag, if applicable) to the next `product-release-charts` manifest and to `Dev.yaml`/`Prod.yaml`'s `--set` flags.
4. Update this page if the change affects platform-level CI/CD behavior.
