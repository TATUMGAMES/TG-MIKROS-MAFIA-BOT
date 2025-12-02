# TASKS_10.md

## Objective

Generate complete deployment instructions for running the MIKROS Bot on **Google Cloud Platform (GCP)** using a *
*Virtual Machine (VM)**. Cursor AI should **not attempt to deploy the bot**, but instead provide detailed documentation
for human operators to follow.

---

## Requirements for Cursor AI

- ✅ DO generate a markdown documentation file: `/docs/DEPLOYMENT_GOOGLE_CLOUD.md`
- ❌ DO NOT deploy the bot automatically
- ✅ DO include optional Docker instructions
- ✅ DO include optional `systemd` service unit
- ✅ Add `TODO` markers for future upgrades (secrets manager, logging, etc.)
- ✅ Use clear, reproducible steps

---

## Deliverable: `/docs/DEPLOYMENT_GOOGLE_CLOUD.md`

Cursor AI must generate a markdown file with the following structure and sections:

---

### 🧰 1. Prerequisites

- A Google Cloud account
- A GCP project with billing enabled
- Google Cloud SDK installed locally
- Discord bot token available
- Java 17+ runtime requirement
- GitHub repository already cloned or accessible

---

### 🖥️ 2. Create a Compute Engine VM

- Step-by-step instructions to:
    - Go to **Google Cloud Console**
    - Navigate to **Compute Engine → VM Instances**
    - Click “Create Instance”
    - Choose:
        - Machine type: `e2-micro` (or higher)
        - OS: Ubuntu 22.04 LTS (or similar)
        - Firewall: Allow HTTP & HTTPS
    - Create and SSH into the instance

---

### 📦 3. Set Up the Bot Environment

- Install required packages:
    - Java 17
    - Git
    - (Optional) Docker & Docker Compose
- Clone the GitHub repo:
    - `git clone https://github.com/TATUMGAMES/TG-MIKROS-BOT-discord.git`
- Prepare config:
    - Create `.env` or `settings.json`
    - Add bot token and required secrets

---

### ▶️ 4. Run the Bot (Manually)

- Command to run:
    - `./gradlew build && java -jar build/libs/mikros-bot.jar`
- Tips for background execution:
    - Use `screen`, `tmux`, or `nohup`

---

### 🔁 5. Optional: Auto-Restart with systemd

- Cursor AI should generate a `mikros-bot.service` unit file with:
    - Working directory
    - ExecStart command
    - Restart policy
- Instructions to:
    - Place the service file in `/etc/systemd/system/`
    - Enable with `sudo systemctl enable mikros-bot`
    - Start with `sudo systemctl start mikros-bot`
    - View logs with `journalctl -u mikros-bot`

---

### 🐳 6. Optional: Docker Deployment

- Generate `Dockerfile` with Java 17 base image
- Example `docker run` command with volume mounts for configs
- Recommend `--restart=always` for self-healing
- (Optional) Suggest Docker Compose for future environments

---

### 🔐 7. TODO: Secrets Management (Future)

- Add placeholder for:
    - Integrating with **Google Secret Manager**
    - Creating secure access for bot token
- Manual fallback:
    - Store secrets in `.env`
    - Secure file permissions (`chmod 600`)

---

### 📊 8. TODO: Logging & Monitoring

- Add placeholder for:
    - Cloud Logging
    - Stackdriver Monitoring
- Manual fallback:
    - Log to file with rotation (via SLF4J config)

---

## Additional Notes

- This documentation is for humans. **Cursor AI must not automate or execute any cloud commands.**
- Cursor AI should format `/docs/DEPLOYMENT_GOOGLE_CLOUD.md` cleanly, using:
    - Headings
    - Code blocks
    - Bullet points
    - Clearly marked `TODO` sections

---

## When Done

✅ Ensure `/docs/DEPLOYMENT_GOOGLE_CLOUD.md` exists  
✅ File must match the architecture and tools defined in `README.md` and `BEST_CODING_PRACTICES.md`
