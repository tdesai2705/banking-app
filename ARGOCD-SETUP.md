# ArgoCD Integration Guide - IGSL Implementation

Complete guide for setting up ArgoCD GitOps with CloudBees Unify CI/CD pipeline.

---

## Architecture Overview

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  CloudBees   │────▶│  Docker Hub  │     │   GitHub     │────▶│   ArgoCD     │
│     CI/CD    │     │   (Images)   │     │  (GitOps)    │     │  (Sync)      │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────────┘
       │                                           │                     │
       │                                           │                     ▼
       └───────────────────────────────────────────┴──────────▶┌──────────────┐
                     (Updates image tag)                       │  Kubernetes  │
                                                                └──────────────┘
```

**Flow:**
1. Developer pushes code to GitHub
2. CloudBees CI builds Docker image with version tag (v1.0.123-abc1234)
3. CloudBees pushes image to Docker Hub
4. CloudBees updates `gitops/{env}/values.yaml` with new image tag
5. CloudBees commits and pushes GitOps change to GitHub
6. ArgoCD detects change in GitHub (auto-sync every 3 minutes)
7. ArgoCD applies new Helm values to Kubernetes
8. Kubernetes rolls out new version

---

## Prerequisites

- [x] Kubernetes cluster with cluster-admin access
- [x] kubectl configured with cluster access
- [x] Git repository with banking-app code
- [x] CloudBees Unify organization
- [x] Docker Hub account

---

## Step 1: Install ArgoCD

### 1.1 Create ArgoCD Namespace

```bash
kubectl create namespace argocd
```

### 1.2 Install ArgoCD

```bash
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

Wait for all pods to be ready:

```bash
kubectl wait --for=condition=Ready pods --all -n argocd --timeout=600s
```

### 1.3 Access ArgoCD UI

**Get admin password:**

```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo
```

**Port forward to access UI:**

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

**Open browser:**
- URL: https://localhost:8080
- Username: `admin`
- Password: (from above command)

---

## Step 2: Configure ArgoCD Applications

### 2.1 Apply Banking App Applications

```bash
# QA Application
kubectl apply -f https://raw.githubusercontent.com/tdesai2705/banking-app/main/gitops/applications/banking-app-qa.yaml

# Staging Application
kubectl apply -f https://raw.githubusercontent.com/tdesai2705/banking-app/main/gitops/applications/banking-app-staging.yaml

# Production Application
kubectl apply -f https://raw.githubusercontent.com/tdesai2705/banking-app/main/gitops/applications/banking-app-prod.yaml
```

### 2.2 Verify Applications in ArgoCD UI

1. Open ArgoCD UI (https://localhost:8080)
2. You should see 3 applications:
   - `banking-app-qa` (deploying to tejas-qa namespace)
   - `banking-app-staging` (deploying to tejas-staging namespace)
   - `banking-app-prod` (deploying to tejas-prod namespace)

---

## Step 3: Verify ArgoCD Sync

### 3.1 Check Application Status

```bash
# Via kubectl
kubectl get applications -n argocd

# Via argocd CLI (optional)
argocd app list
```

### 3.2 Watch Sync Progress

```bash
# Watch specific application
kubectl get application banking-app-qa -n argocd -w

# Or use argocd CLI
argocd app get banking-app-qa --watch
```

### 3.3 Verify Deployment in Kubernetes

```bash
# Check QA deployment
kubectl get all -n tejas-qa -l app.kubernetes.io/name=banking-app

# Check Staging deployment
kubectl get all -n tejas-staging -l app.kubernetes.io/name=banking-app

# Check Production deployment
kubectl get all -n tejas-prod -l app.kubernetes.io/name=banking-app
```

---

## Step 4: Trigger GitOps Deployment from CloudBees

### 4.1 Run GitOps Workflow

1. Go to CloudBees Unify UI
2. Navigate to **Components** → **banking-app**
3. Go to **Workflows** tab
4. Find **"GitOps Deployment"** workflow
5. Click **Run workflow**

### 4.2 What the Workflow Does

**Stage 1: Build & Test**
- Runs unit tests
- Builds Docker image with version `v1.0.{run_number}-{short_sha}`
- Pushes to Docker Hub

**Stage 2: Update GitOps (QA)**
- Updates `gitops/qa/values.yaml` with new image tag
- Commits and pushes to GitHub main branch
- Example commit: `🚀 Deploy v1.0.123-abc1234 to QA`

**Stage 3: Verify QA**
- Waits 3 minutes for ArgoCD to sync
- Verifies deployment status
- Runs smoke tests

**Stage 4: Update GitOps (Staging)** (requires approval)
- Updates `gitops/staging/values.yaml`
- Commits and pushes to GitHub

**Stage 5: Update GitOps (Production)** (requires approval)
- Updates `gitops/prod/values.yaml`
- Commits and pushes to GitHub

---

## Step 5: Monitor ArgoCD Sync

### 5.1 Watch ArgoCD Detect Changes

After CloudBees updates the GitOps repo:

```bash
# Watch application sync status
argocd app get banking-app-qa --watch

# Or check in UI
# https://localhost:8080 → banking-app-qa → Refresh
```

### 5.2 ArgoCD Sync Behavior

- **Auto-sync interval**: Every 3 minutes
- **Self-heal**: Enabled (reverts manual kubectl changes)
- **Prune**: Enabled (removes resources not in Git)

### 5.3 Manual Sync (if needed)

```bash
# Force immediate sync
argocd app sync banking-app-qa

# Or via kubectl
kubectl patch application banking-app-qa -n argocd --type merge -p '{"operation":{"sync":{}}}'
```

---

## Step 6: Verify End-to-End Flow

### 6.1 Make a Code Change

```bash
# Edit some code
echo "// Test change" >> src/main/java/com/example/banking/BankingApplication.java

# Commit and push
git add .
git commit -m "Test: Trigger GitOps pipeline"
git push origin main
```

### 6.2 Watch the Pipeline

1. **CloudBees Unify**: Monitor workflow run
2. **GitHub**: Watch for GitOps commits to `gitops/qa/values.yaml`
3. **ArgoCD UI**: Watch application sync
4. **Kubernetes**: See new pods rolling out

```bash
# Watch pods rolling out
kubectl get pods -n tejas-qa -w
```

---

## Step 7: Rollback (if needed)

### 7.1 Via ArgoCD UI

1. Open application in ArgoCD UI
2. Click **History**
3. Select previous revision
4. Click **Rollback**

### 7.2 Via Git Revert

```bash
# Revert the GitOps commit
git revert HEAD
git push origin main

# ArgoCD will auto-sync to previous version
```

### 7.3 Via ArgoCD CLI

```bash
# Rollback to previous version
argocd app rollback banking-app-qa
```

---

## Troubleshooting

### Issue: ArgoCD Not Syncing

**Check sync status:**
```bash
argocd app get banking-app-qa
```

**Common causes:**
- Repository not reachable (check ArgoCD logs)
- Invalid Helm values (check app events)
- Namespace doesn't exist (ArgoCD creates if `CreateNamespace=true`)

**Fix:**
```bash
# Check ArgoCD logs
kubectl logs -n argocd deployment/argocd-repo-server

# Force sync
argocd app sync banking-app-qa --prune
```

### Issue: Image Pull Errors

**Check pod events:**
```bash
kubectl describe pod <pod-name> -n tejas-qa
```

**Common causes:**
- Image tag doesn't exist in Docker Hub
- Image pull credentials missing
- Wrong image repository

**Fix:**
```bash
# Verify image exists
docker pull tejasdesai27/banking-app:v1.0.123-abc1234

# Check GitOps values
cat gitops/qa/values.yaml | grep -A2 "image:"
```

### Issue: Helm Chart Errors

**Check application events:**
```bash
kubectl describe application banking-app-qa -n argocd
```

**Common causes:**
- Invalid Helm syntax in chart/
- Missing required values
- Namespace permissions

**Fix:**
```bash
# Test Helm chart locally
helm template banking-app ./chart -f gitops/qa/values.yaml

# Apply manually to debug
helm upgrade --install banking-app ./chart \
  -f gitops/qa/values.yaml \
  -n tejas-qa \
  --dry-run --debug
```

---

## Best Practices for IGSL

### 1. Environment Strategy

- **QA**: Auto-sync enabled, no approval needed
- **Staging**: Auto-sync enabled, approval gate in CloudBees
- **Production**: Auto-sync enabled, approval gate + change request

### 2. Git Branch Strategy

**Option A: Main branch only** (current setup)
- All environments deploy from `main`
- Simple, works for small teams

**Option B: Environment branches**
```
main → qa-branch → staging-branch → prod-branch
```
- More control, good for large teams
- Requires branch promotion strategy

### 3. Image Tagging Strategy

**Current**: `v1.0.{run_number}-{short_sha}`
- Semantic versioning with build metadata
- Easy to track in ArgoCD history

**Alternatives**:
- `v{major}.{minor}.{patch}` - Manual semantic versioning
- `{short_sha}` - Commit-based tagging
- `{date}-{sha}` - Date-based versioning

### 4. Approval Gates

Configure in CloudBees Unify:
```yaml
environment: staging  # or prod
# Requires manual approval in UI
```

Or integrate with ServiceNow/Jira for automated approval workflows.

### 5. Monitoring & Alerts

**ArgoCD Notifications** (optional):
```bash
# Configure Slack notifications
kubectl apply -f argocd-notifications-config.yaml
```

**Example**: Get notified when sync fails or succeeds.

---

## Security Considerations

### 1. Repository Access

ArgoCD needs read access to GitHub repository:
- Use Deploy Keys (recommended for single repo)
- Use Personal Access Token (for multiple repos)
- Use SSH keys

### 2. Image Registry

- Use private Docker registry for production
- Configure image pull secrets in Kubernetes
- Scan images for vulnerabilities before deployment

### 3. RBAC

Configure ArgoCD RBAC:
```yaml
# argocd-rbac-cm ConfigMap
policy.csv: |
  p, qa-team, applications, sync, banking-app-qa, allow
  p, prod-team, applications, sync, banking-app-prod, allow
```

---

## Maintenance

### Upgrade ArgoCD

```bash
# Backup current installation
kubectl get all -n argocd -o yaml > argocd-backup.yaml

# Apply new version
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Clean Up Test Deployments

```bash
# Delete applications
kubectl delete application banking-app-qa -n argocd

# Delete namespace resources
kubectl delete all --all -n tejas-qa
```

---

## For IGSL Demo (May 13th)

### Demo Script

1. **Show GitOps Structure**
   - Walk through `gitops/` directory
   - Explain environment-specific values

2. **Show ArgoCD Applications**
   - Open ArgoCD UI
   - Show 3 applications (qa, staging, prod)
   - Show current sync status

3. **Trigger Deployment**
   - Run CloudBees GitOps workflow
   - Watch it update `gitops/qa/values.yaml` on GitHub
   - Watch ArgoCD detect change and sync
   - Show new pods rolling out in Kubernetes

4. **Show Evidence Trail**
   - CloudBees evidence (build, test, GitOps update)
   - Git history (commits to GitOps repo)
   - ArgoCD history (deployment revisions)
   - Kubernetes events (pod rollouts)

5. **Show Rollback**
   - Trigger rollback in ArgoCD UI
   - Watch it revert to previous version

### Key Points to Emphasize

✅ **No manual kubectl needed** - ArgoCD handles everything  
✅ **Git as single source of truth** - All changes in Git history  
✅ **Automatic sync** - 3-minute sync interval  
✅ **Self-healing** - Reverts manual changes  
✅ **Multi-environment** - QA → Staging → Production  
✅ **Approval gates** - Built into CloudBees environments  
✅ **Full audit trail** - Evidence at every step  

---

## Resources

- **ArgoCD Docs**: https://argo-cd.readthedocs.io/
- **GitOps Principles**: https://opengitops.dev/
- **Banking App Repo**: https://github.com/tdesai2705/banking-app
- **CloudBees Unify**: https://docs.cloudbees.com/

---

**Setup Complete!** 🎉

You now have a fully automated GitOps pipeline:
- CloudBees CI/CD for build and test
- GitOps repository for declarative config
- ArgoCD for automated Kubernetes deployments
- Multi-environment promotion with approval gates
