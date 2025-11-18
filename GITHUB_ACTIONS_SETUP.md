# GitHub Actions Setup Guide

This guide explains how to set up and use the GitHub Actions CI/CD pipeline for the Social Events Microservices project.

## 🎯 What the CI/CD Pipeline Does

The pipeline automatically:

1. **Builds** all backend services (Maven + Java 21)
2. **Tests** backend services with JUnit
3. **Builds** frontend dashboard (Node.js + Vite)
4. **Creates** Docker images for all services
5. **Deploys** dashboard to GitHub Pages (optional)
6. **Pushes** Docker images to Docker Hub (optional)

## ✅ Required Setup (Minimal)

### Step 1: Enable GitHub Actions

GitHub Actions is enabled by default for public repositories. For private repos:

1. Go to your repository on GitHub
2. Click **Settings** → **Actions** → **General**
3. Under "Actions permissions", select **Allow all actions and reusable workflows**
4. Click **Save**

### Step 2: Workflow File is Already Included

The workflow file is already in your repository:
```
.github/workflows/ci-cd.yml
```

### Step 3: Trigger the Workflow

The workflow triggers automatically on:
- Push to `main` branch
- Push to branches matching `claude/dev-server-ready-*`
- Pull requests to `main`

**Manual trigger:**
1. Go to **Actions** tab in GitHub
2. Select **CI/CD Pipeline** workflow
3. Click **Run workflow**
4. Select branch and click **Run workflow**

## 🚀 Optional Setup (GitHub Pages Deployment)

### Enable GitHub Pages for Dashboard

1. Go to **Settings** → **Pages**
2. Under "Source", select **GitHub Actions**
3. Click **Save**

**Your dashboard will be deployed to:**
```
https://<your-username>.github.io/<repository-name>/
```

### Configure API URL for Dashboard

If you want the dashboard to connect to a different API:

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Name: `API_BASE_URL`
4. Value: `https://your-api-url.com` (your REST API URL)
5. Click **Add secret**

If not set, defaults to `http://localhost:8084`

## 🐳 Optional Setup (Docker Hub Push)

### Prerequisites

1. Create a Docker Hub account at https://hub.docker.com/
2. Create an access token:
   - Go to Account Settings → Security → Access Tokens
   - Click **New Access Token**
   - Description: "GitHub Actions"
   - Permissions: **Read, Write, Delete**
   - Click **Generate**
   - **Copy the token** (you won't see it again!)

### Configure Secrets

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**

**Add two secrets:**

**Secret 1: DOCKERHUB_USERNAME**
- Name: `DOCKERHUB_USERNAME`
- Value: Your Docker Hub username
- Click **Add secret**

**Secret 2: DOCKERHUB_TOKEN**
- Name: `DOCKERHUB_TOKEN`
- Value: Your Docker Hub access token (from prerequisites)
- Click **Add secret**

### Verify Secrets are Set

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. You should see:
   - `DOCKERHUB_USERNAME`
   - `DOCKERHUB_TOKEN`
   - (Optional) `API_BASE_URL`

## 📋 Workflow Jobs Explained

### Job 1: build-backend

**What it does:**
- Checks out code
- Sets up Java 21
- Builds with Maven
- Runs tests
- Builds Docker images
- Saves images as artifacts

**When it runs:** On every push and pull request

**Duration:** ~5-10 minutes

**Artifacts:**
- `backend-images` (Docker image tarballs)

### Job 2: build-frontend

**What it does:**
- Checks out code
- Sets up Node.js 18
- Installs npm dependencies
- Builds React dashboard
- Saves build as artifact

**When it runs:** On every push and pull request

**Duration:** ~2-5 minutes

**Artifacts:**
- `dashboard-dist` (Built React app)

### Job 3: deploy-dashboard-pages

**What it does:**
- Downloads dashboard artifact
- Deploys to GitHub Pages

**When it runs:** Only on push to `main` branch

**Duration:** ~1 minute

**Requirements:**
- GitHub Pages must be enabled
- Workflow must have `pages: write` permission (already configured)

### Job 4: push-to-dockerhub

**What it does:**
- Checks if Docker Hub credentials are configured
- Logs in to Docker Hub
- Downloads Docker images
- Tags and pushes to Docker Hub

**When it runs:** Only on push to `main` branch

**Duration:** ~3-5 minutes

**Requirements:**
- `DOCKERHUB_USERNAME` secret
- `DOCKERHUB_TOKEN` secret

**Note:** This job gracefully skips if secrets are not configured.

## 🔍 Monitoring Workflow Runs

### View Workflow Runs

1. Go to **Actions** tab in your repository
2. You'll see a list of workflow runs
3. Click on any run to see details

### View Job Logs

1. Click on a workflow run
2. Click on a job name (e.g., "build-backend")
3. Expand any step to see detailed logs

### Download Artifacts

1. Go to a workflow run
2. Scroll to "Artifacts" section at the bottom
3. Click artifact name to download

**Available artifacts:**
- `backend-images` - Docker images (1 day retention)
- `dashboard-dist` - Built React app (7 days retention)

## ✅ Verifying Successful Deployment

### Check Build Status

Look for green checkmarks ✅ in:
- README.md (badge at top, if added)
- Pull requests
- Commits list
- Actions tab

### Verify GitHub Pages Deployment

1. After push to `main`, wait 2-3 minutes
2. Go to `https://<username>.github.io/<repo>/`
3. You should see the dashboard UI

### Verify Docker Hub Push

1. Go to https://hub.docker.com/
2. Log in with your account
3. You should see repositories:
   - `<username>/event-stream-service`
   - `<username>/elastic-query-service`

## 🐛 Troubleshooting

### ❌ Build fails on "Build with Maven"

**Possible causes:**
- Java version mismatch
- Maven dependencies unavailable
- Code compilation errors

**Solution:**
1. Check the error in job logs
2. Ensure code compiles locally: `mvn clean install`
3. Check if any dependencies need updating

### ❌ Build fails on "Run tests"

**Possible causes:**
- Test failures
- Missing test dependencies

**Solution:**
1. Run tests locally: `mvn test`
2. Fix any failing tests
3. Push the fixes

### ❌ Frontend build fails

**Possible causes:**
- npm dependencies conflicts
- Build errors in React code

**Solution:**
1. Test locally:
   ```bash
   cd dashboard-ui
   npm install
   npm run build
   ```
2. Fix any errors
3. Push the fixes

### ❌ GitHub Pages deployment fails

**Possible causes:**
- Pages not enabled in settings
- Permissions issue

**Solution:**
1. Enable GitHub Pages: Settings → Pages → Source: GitHub Actions
2. Ensure workflow has correct permissions (already configured)
3. Re-run the workflow

### ❌ Docker Hub push fails with "unauthorized"

**Possible causes:**
- Incorrect username or token
- Token expired or revoked

**Solution:**
1. Verify secrets are correctly set
2. Generate new Docker Hub access token
3. Update `DOCKERHUB_TOKEN` secret

### ⚠️ Docker Hub push shows "skipped"

**This is normal!** It means Docker Hub secrets are not configured.

**To enable:**
1. Follow "Optional Setup (Docker Hub Push)" above
2. Add both secrets
3. Push to `main` again

## 📊 Build Status Badge (Optional)

Add a build status badge to your README:

```markdown
[![CI/CD Pipeline](https://github.com/<username>/<repo>/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/<username>/<repo>/actions/workflows/ci-cd.yml)
```

Replace `<username>` and `<repo>` with your GitHub username and repository name.

## 🔐 Security Best Practices

### Secrets Management

✅ **DO:**
- Use GitHub Secrets for sensitive data
- Rotate Docker Hub tokens regularly
- Use least-privilege access tokens
- Keep tokens confidential

❌ **DON'T:**
- Commit secrets to code
- Share tokens publicly
- Use personal credentials in shared repos
- Grant unnecessary permissions

### Workflow Security

✅ **DO:**
- Review workflow changes in pull requests
- Use specific action versions (e.g., `@v4`)
- Limit workflow permissions
- Enable dependency scanning

❌ **DON'T:**
- Accept workflows from untrusted sources
- Use `@latest` for action versions
- Grant excessive permissions

## 🎓 Advanced Configuration

### Customize Workflow Triggers

Edit `.github/workflows/ci-cd.yml`:

```yaml
on:
  push:
    branches: [ main, develop ]  # Add more branches
    paths-ignore:              # Ignore certain files
      - '**.md'
      - 'docs/**'
  schedule:                    # Run on schedule
    - cron: '0 0 * * 0'       # Every Sunday at midnight
```

### Add More Jobs

Add custom jobs to the workflow:

```yaml
  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run security scan
        run: mvn dependency-check:check
```

### Parallel Jobs

Jobs run in parallel by default unless they have `needs`:

```yaml
job-a:
  runs-on: ubuntu-latest
  steps: [...]

job-b:  # Runs in parallel with job-a
  runs-on: ubuntu-latest
  steps: [...]

job-c:  # Runs after job-a completes
  needs: job-a
  runs-on: ubuntu-latest
  steps: [...]
```

## 📈 Workflow Performance Tips

### 1. Use Caching

Already configured for:
- Maven dependencies
- npm packages

### 2. Parallel Jobs

Jobs run in parallel when possible:
- `build-backend` and `build-frontend` run together
- Saves 5-10 minutes total

### 3. Artifacts Cleanup

Artifacts auto-delete:
- `backend-images`: 1 day
- `dashboard-dist`: 7 days

Adjust in workflow if needed:
```yaml
retention-days: 30  # Keep for 30 days
```

## 🆘 Getting Help

### GitHub Actions Documentation

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

### Common Issues

- [GitHub Actions Status](https://www.githubstatus.com/)
- [Community Forum](https://github.community/)

### Repository Specific

- Open an issue in the repository
- Check the Actions tab for error logs
- Review recent commits for changes

## ✅ Quick Reference

### Enable GitHub Pages
```
Settings → Pages → Source: GitHub Actions
```

### Add Secret
```
Settings → Secrets and variables → Actions → New repository secret
```

### View Workflows
```
Actions tab → Select workflow → Click run
```

### Re-run Failed Jobs
```
Actions → Click run → Re-run failed jobs
```

### Download Artifacts
```
Actions → Click run → Scroll to Artifacts → Download
```

---

## 🎉 Summary

**Minimum Setup (Always Works):**
1. ✅ Push code to GitHub
2. ✅ Workflow runs automatically
3. ✅ Build and test complete

**Optional Enhancements:**
- 🌐 Enable GitHub Pages for dashboard hosting
- 🐳 Add Docker Hub secrets for image publishing
- 🔧 Customize workflow for your needs

**The workflow is designed to work out-of-the-box with graceful degradation if optional features aren't configured.**
