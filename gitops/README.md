# Banking App - GitOps Repository Structure

This directory contains the GitOps configuration for deploying the Banking App across multiple environments using ArgoCD.

## Directory Structure

```
gitops/
├── applications/          # ArgoCD Application definitions
│   ├── banking-app-qa.yaml
│   ├── banking-app-staging.yaml
│   └── banking-app-prod.yaml
├── qa/                    # QA environment configuration
│   └── values.yaml
├── staging/               # Staging environment configuration
│   └── values.yaml
└── prod/                  # Production environment configuration
    └── values.yaml
```

## GitOps Workflow

1. **CloudBees CI** builds Docker image and pushes to registry
2. **CloudBees Workflow** updates the appropriate `values.yaml` file with new image tag
3. **ArgoCD** detects the change and syncs the application
4. **Kubernetes** deploys the updated application

## ArgoCD Setup

### Install ArgoCD (if not already installed)

```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Create Applications

```bash
kubectl apply -f gitops/applications/
```

### Access ArgoCD UI

```bash
# Get admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# Port forward
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Open browser: https://localhost:8080
# Username: admin
# Password: (from above command)
```

## Environment Configuration

Each environment has its own `values.yaml` file that overrides the Helm chart defaults:

- **QA**: 2 replicas, tejas-qa namespace
- **Staging**: 3 replicas, tejas-staging namespace
- **Production**: 5 replicas, tejas-prod namespace

## Manual Deployment (without ArgoCD)

If you want to deploy manually without ArgoCD:

```bash
# QA
helm upgrade --install banking-app ../chart \
  --namespace tejas-qa \
  --values gitops/qa/values.yaml

# Staging
helm upgrade --install banking-app ../chart \
  --namespace tejas-staging \
  --values gitops/staging/values.yaml

# Production
helm upgrade --install banking-app ../chart \
  --namespace tejas-prod \
  --values gitops/prod/values.yaml
```

## For IGSL Implementation

1. Point your ArgoCD instance to this repository
2. Apply the Application manifests in `gitops/applications/`
3. Configure CloudBees workflow to update image tags in `values.yaml` files
4. ArgoCD will automatically sync changes

**Repository URL**: https://github.com/tdesai2705/banking-app.git  
**Path**: `gitops/{environment}/`  
**Target Revision**: `main`
