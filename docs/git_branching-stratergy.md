# Git Branching and Deployment Strategy



## Visual Workflow Diagram

```
feature/*
   |
   v
develop
   |
   v
[Dev Environment]
   |
   v
release/*        
   |                         
   |                        
   v
main  <----------------------+         hotfix/*
   |                         |             |
   |                         +-------------+
   v
[UAT Environment]
   |
   v
[Production Environment]
   (Promote UAT image, tag commit)
```

Step-by-step flow:
1. Developers create `feature/*` branches from `develop` and merge back after review.
2. `develop` is deployed to the **dev** environment.
3. After QA signoff, a `release/*` branch is created and merged to `main` via PR (Once got approved by client).
4. `main` is deployed to **uat** environment.
5. After final signoff, the exact image tested in UAT is promoted to **production** (no rebuild), and a tag is created on `main` for reference.
6. Hotfixes:
    - For UAT: use `hotfix-uat-<description>` from `release/*` or `main`, merge back to both `main` and `release/*` (if open).
    - For Production: use `hotfix-v<version>-<description>` from `main`, merge back to both `main`.


## Branches
- **main**: Production-ready code (protected)
- **develop**: Latest development code (protected)
- **feature/***: Feature branches from `develop`
- **release/***: Release branches for UAT signoff
- **hotfix/***: For urgent fixes on `main` or UAT

## Environments
- **dev**: Deploy from `develop`
- **uat**: Deploy from `main` (after release PR is merged and signed off)
- **production**: Deploy the exact image tested in UAT (no rebuild)

## Workflow
1. **Feature Development**
    - Developers create `feature/*` branches from `develop`.
    - Merge feature branches into `develop` after review.
    - Code in `develop` is deployed to the **dev** environment.

2. **Release Preparation**
    - After QA signoff, create a `release/*` branch from `develop`.
    - Raise a PR from `release/*` to `main`.
    - Share the PR with the client for review.

3. **UAT Deployment**
    - After client signoff, merge the PR to `main`.
    - Deploy the code from `main` to the **uat** environment.

4. **Production Release**
    - After final signoff (QA & client), promote the exact image tested in UAT to **production** (no new build).
    - Create a Git tag (e.g., `v1.2.3`) on the corresponding commit in `main` for code reference.

5. **Hotfixes**
    - For UAT: Create `hotfix/uat-*` from `main` or `release/*`, merge back to both `main` and `develop`.
    - For Production: Create `hotfix/v<version>-*` from `main`, merge back to both `main` and `develop`.

## Best Practices
- Always merge hotfixes back to both `main` and `develop` to avoid code drift.
- Use clear naming conventions for branches and tags.


---

**Note:**
- The production deployment always uses the image that was tested and signed off in UAT. The Git tag is for code traceability only.
- No new image is built for production after UAT signoff.
