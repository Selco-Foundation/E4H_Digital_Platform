# Security Policy

## Supported Versions

We currently provide security updates for the following versions:

| Version | Supported | Notes |
| :--- | :---: | :--- |
| Latest release (`v2.x`) | :white_check_mark: | Receives all security patches |
| Older releases (`< v2.0`) | :x: | No longer supported; please upgrade |

---

## Reporting a Vulnerability

If you discover a security vulnerability in this project, **please report it privately.** Do **not** create a public GitHub issue, pull request, or discussion for security vulnerabilities.

You can report a vulnerability using one of the following methods:

1. **GitHub Security Advisories (Preferred):** Go to the repository's **Security** tab -> **Advisories** -> Click **"Report a vulnerability"**.
2. **Email:** Send a report directly to **security@selco-foundation.org**.

### What to Include in Your Report
To help us triage and resolve the issue quickly, please provide:
* A clear description of the vulnerability and its potential impact
* The affected version(s)
* Step-by-step instructions or proof-of-concept (PoC) script to reproduce the issue
* Any suggested mitigation or fix (if available)

---

## Expected Response Timeline

When a report is submitted, we aim to adhere to the following schedule:

* **Acknowledgment:** We will acknowledge receipt of your report within **48 hours**.
* **Triage & Assessment:** We will investigate and confirm the report within **5 business days**.
* **Remediation & Disclosure:** If confirmed, we will work on a fix and coordinate disclosure once the patch is ready for release.

---

## Out of Scope

To save time for both reporters and maintainers, the following are generally considered **out of scope**:

* Vulnerabilities dependent on outdated or unsupported browsers/environments
* Spam, automated scan outputs without proof-of-concept, or missing best practices without clear exploitability
* Denial of Service (DoS) / Distributed Denial of Service (DDoS) attacks
* Social engineering or phishing targeting maintainers or users

---

## Security Best Practices for Contributors

When contributing code to this repository:
* **Never commit secrets:** Avoid committing passwords, API keys, access tokens, or private keys.
* **Keep dependencies updated:** Monitor and update direct dependencies regularly.
* **Filter sensitive logs:** Do not include sensitive information in public issue logs or pull requests.

*Thank you for helping keep this project secure!*