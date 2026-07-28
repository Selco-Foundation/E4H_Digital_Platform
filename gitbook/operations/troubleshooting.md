# Troubleshooting

## Troubleshooting

### Troubleshooting

Use this page as the first checklist when local setup, integration, or deployment behavior is unclear.

#### Backend

* Check the service-level `README.md` and `LOCALSETUP.md`.
* Confirm required environment variables and external dependencies.
* Verify API contracts under `docs`.
* Check related workflow or cron manifests if behavior is scheduled or asynchronous.

#### Frontend

* Confirm Yarn dependencies are installed in the correct app directory.
* Check `.env` values for backend and asset endpoints.
* Confirm `REACT_APP_USER_TYPE` is correct for the flow being tested.
* Compare UI behavior with sequence diagrams under `docs/ui-sequence-diagrams` where available.

#### Mobile

* Run dependency resolution from the `mobile` directory.
* Confirm Flutter SDK and platform tooling are installed.
* Validate backend endpoint and environment configuration.
* Test cache-backed workflows on a device or emulator when changing field flows.

#### Infrastructure and DNS

Use these when a domain won't resolve, TLS won't issue, or an environment seems unreachable — see [Deployment → DNS and TLS after deployment](deployment.md#dns-and-tls-after-deployment) for the full setup this validates.

* **Check the load balancer hostname the ingress controller actually got:**
  ```bash
  kubectl get svc ingress-nginx-controller -n backbone-dev -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
  ```
* **Confirm the domain's CNAME points at that load balancer:**
  ```bash
  dig CNAME <domain>
  # or, for a quick resolved-address check:
  nslookup <domain>
  ```
  If this doesn't return the load balancer hostname, the CNAME record is missing, wrong, or hasn't propagated yet at your DNS provider (GoDaddy, Cloudflare, etc.).
* **Check the domain against a public resolver**, to rule out local/ISP DNS caching:
  ```bash
  dig @8.8.8.8 <domain>
  ```
* **Once DNS resolves, check whether the app actually responds:**
  ```bash
  curl -v https://<domain>/<service-health-path>
  # e.g. curl -v https://<domain>/user/health
  ```
  * A connection/timeout error with correct DNS points at the load balancer, security group, or ingress rule rather than DNS.
  * An SSL error (certificate name mismatch, self-signed, etc.) points at cert-manager/`ClusterIssuer` rather than DNS — see below.
  * An HTTP error (4xx/5xx) from a valid TLS connection points at the service itself — check `kubectl get pods`/`kubectl logs` for that service.
* **If TLS/certificate issuance is stuck**, confirm the CNAME has propagated first (cert-manager's ACME challenge needs the domain to already resolve to the load balancer), then check the certificate resource:
  ```bash
  kubectl describe certificate -n <namespace>
  ```

#### Documentation

* If a GitBook page does not appear, check the sidebar structure.
* If GitBook sync fails, check `.gitbook.yaml` and the configured branch.
* If a link breaks, verify the path relative to the page location.
