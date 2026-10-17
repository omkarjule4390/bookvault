# Deploy on Render (free)

1. Push this repo to GitHub (run `github_push.bat`, or use the git commands below).
2. Go to https://render.com, sign in with GitHub.
3. **New > Blueprint**, select this repo. Render reads `render.yaml` and creates
   the PostgreSQL database and the web service together.
4. Wait for the build (about 5-10 min). Then open:
   - `https://bookvault.onrender.com/`            (website)
   - `https://bookvault.onrender.com/swagger-ui.html` (API docs)

Notes
- Free web service sleeps after 15 min idle; first request after that takes ~1 min.
- Free Render Postgres is deleted after 30 days. Verify current limits on Render's site.

## Manual git commands
```bash
git init
git add .
git commit -m "BookVault: initial commit"
git branch -M main
git remote add origin https://github.com/<username>/bookvault.git
git push -u origin main
```
