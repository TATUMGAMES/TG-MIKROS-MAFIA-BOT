# Deployment Guide: TG-MIKROS Discord Bot on Google Cloud Platform

This guide provides step-by-step instructions for deploying the TG-MIKROS Discord Bot on Google Cloud Platform (GCP) using a Compute Engine Virtual Machine.

---

## 🧰 1. Prerequisites

Before starting, ensure you have:

- ✅ **Google Cloud Account** with billing enabled
- ✅ **GCP Project** created and active
- ✅ **Google Cloud SDK** installed locally (optional, for CLI access)
- ✅ **Discord Bot Token** from [Discord Developer Portal](https://discord.com/developers/applications)
- ✅ **Java 17+** runtime (will be installed on VM)
- ✅ **Git** access to the repository
- ✅ **SSH client** for connecting to the VM

### Required Information
- Discord Bot Token
- GCP Project ID
- Preferred VM region/zone
- SSH key pair (or use GCP's browser SSH)

---

## 🖥️ 2. Create a Compute Engine VM

### Step 1: Access Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project from the project dropdown
3. Navigate to **Compute Engine → VM Instances**

### Step 2: Create VM Instance

1. Click **"Create Instance"** button

2. **Configure the VM:**
   - **Name:** `mikros-bot-vm` (or your preferred name)
   - **Region:** Choose closest to your users (e.g., `us-central1`)
   - **Zone:** Any zone in selected region

3. **Machine Configuration:**
   - **Machine family:** General-purpose
   - **Machine type:** `e2-micro` (free tier eligible) or `e2-small` for better performance
     - **Note:** `e2-micro` may be throttled under load; consider `e2-small` for production

4. **Boot Disk:**
   - **Operating System:** Ubuntu
   - **Version:** Ubuntu 22.04 LTS
   - **Boot disk type:** Standard persistent disk
   - **Size:** 20 GB (minimum) or 30 GB (recommended)

5. **Firewall:**
   - ✅ **Allow HTTP traffic** (optional, for future webhooks)
   - ✅ **Allow HTTPS traffic** (optional, for future webhooks)
   - **Note:** Discord bot doesn't require incoming HTTP/HTTPS, but enabling won't hurt

6. **Advanced Options (Optional):**
   - **Networking:** Default VPC is fine
   - **Access scopes:** Default (full access) or "Allow default access"

7. Click **"Create"** and wait for the VM to be created (1-2 minutes)

### Step 3: SSH into the Instance

1. Once the VM is running, click **"SSH"** button next to the instance
2. This opens a browser-based SSH terminal
3. Alternatively, use `gcloud` CLI:
   ```bash
   gcloud compute ssh mikros-bot-vm --zone=YOUR_ZONE
   ```

---

## 📦 3. Set Up the Bot Environment

### Step 1: Update System Packages

```bash
sudo apt update
sudo apt upgrade -y
```

### Step 2: Install Java 17

```bash
# Install OpenJDK 17
sudo apt install openjdk-17-jdk -y

# Verify installation
java -version
# Should show: openjdk version "17.x.x"
```

### Step 3: Install Git

```bash
sudo apt install git -y

# Verify installation
git --version
```

### Step 4: Install Gradle (Optional but Recommended)

```bash
# Install Gradle using SDKMAN or download directly
# Option 1: Using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle 8.5

# Option 2: Manual installation
wget https://services.gradle.org/distributions/gradle-8.5-bin.zip
sudo unzip gradle-8.5-bin.zip -d /opt
sudo ln -s /opt/gradle-8.5/bin/gradle /usr/local/bin/gradle

# Verify installation
gradle -v
```

**Note:** The project includes Gradle Wrapper (`gradlew`), so Gradle installation is optional.

### Step 5: Clone the Repository

```bash
# Navigate to home directory
cd ~

# Clone the repository
git clone https://github.com/TATUMGAMES/TG-MIKROS-BOT-discord.git

# Navigate to project directory
cd TG-MIKROS-BOT-discord
```

**Alternative:** If using private repository:
```bash
# Use SSH or HTTPS with authentication
git clone git@github.com:TATUMGAMES/TG-MIKROS-BOT-discord.git
```

### Step 6: Create Configuration File

```bash
# Create .env file in project root
nano .env
```

Add the following content:
```env
DISCORD_BOT_TOKEN=your_bot_token_here
BOT_OWNER_ID=your_discord_user_id_here
```

**Security:** Set proper file permissions:
```bash
chmod 600 .env
```

### Step 7: Build the Project

```bash
# Make gradlew executable
chmod +x gradlew

# Build the project
./gradlew build -x test

# Verify JAR was created
ls -lh build/libs/
# Should see: TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar
```

---

## ▶️ 4. Run the Bot (Manually)

### Option A: Direct Execution

```bash
# Navigate to project directory
cd ~/TG-MIKROS-BOT-discord

# Run the bot
java -jar build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar
```

### Option B: Using Gradle

```bash
# Run directly with Gradle
./gradlew run
```

### Option C: Background Execution with nohup

```bash
# Run in background with output to log file
nohup java -jar build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar > bot.log 2>&1 &

# Check if running
ps aux | grep java

# View logs
tail -f bot.log
```

### Option D: Using screen (Recommended for Testing)

```bash
# Install screen if not available
sudo apt install screen -y

# Start a new screen session
screen -S mikros-bot

# Run the bot
java -jar build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar

# Detach from screen: Press Ctrl+A, then D
# Reattach: screen -r mikros-bot
```

### Option E: Using tmux

```bash
# Install tmux if not available
sudo apt install tmux -y

# Start a new tmux session
tmux new -s mikros-bot

# Run the bot
java -jar build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar

# Detach: Press Ctrl+B, then D
# Reattach: tmux attach -t mikros-bot
```

---

## 🔁 5. Auto-Restart with systemd

For production deployment, use systemd to ensure the bot starts automatically and restarts on failure.

### Step 1: Create systemd Service File

```bash
sudo nano /etc/systemd/system/mikros-bot.service
```

Add the following content:

```ini
[Unit]
Description=TG-MIKROS Discord Bot
After=network.target

[Service]
Type=simple
User=YOUR_USERNAME
WorkingDirectory=/home/YOUR_USERNAME/TG-MIKROS-BOT-discord
ExecStart=/usr/bin/java -jar /home/YOUR_USERNAME/TG-MIKROS-BOT-discord/build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=mikros-bot

# Environment variables (if not using .env file)
# Environment="DISCORD_BOT_TOKEN=your_token_here"
# Environment="BOT_OWNER_ID=your_id_here"

[Install]
WantedBy=multi-user.target
```

**Important:** Replace `YOUR_USERNAME` with your actual username (check with `whoami`).

### Step 2: Reload systemd and Enable Service

```bash
# Reload systemd to recognize new service
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable mikros-bot

# Start the service
sudo systemctl start mikros-bot

# Check status
sudo systemctl status mikros-bot
```

### Step 3: View Logs

```bash
# View recent logs
sudo journalctl -u mikros-bot -n 50

# Follow logs in real-time
sudo journalctl -u mikros-bot -f

# View logs since boot
sudo journalctl -u mikros-bot -b

# View logs from specific time
sudo journalctl -u mikros-bot --since "2025-10-08 10:00:00"
```

### Step 4: Service Management Commands

```bash
# Start the bot
sudo systemctl start mikros-bot

# Stop the bot
sudo systemctl stop mikros-bot

# Restart the bot
sudo systemctl restart mikros-bot

# Check status
sudo systemctl status mikros-bot

# Disable auto-start on boot
sudo systemctl disable mikros-bot
```

---

## 🐳 6. Optional: Docker Deployment

### Step 1: Create Dockerfile

Create `Dockerfile` in the project root:

```dockerfile
# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Install Gradle (optional, we'll use wrapper)
# Or copy Gradle wrapper from project
COPY gradlew .
COPY gradle/ gradle/

# Copy project files
COPY build.gradle.kts settings.gradle.kts ./
COPY src/ ./src/

# Build the application
RUN chmod +x gradlew && ./gradlew build -x test

# Expose any ports if needed (Discord bot doesn't need ports)
# EXPOSE 8080

# Set environment variables (or use .env file)
# ENV DISCORD_BOT_TOKEN=""
# ENV BOT_OWNER_ID=""

# Run the bot
CMD ["java", "-jar", "build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar"]
```

### Step 2: Build Docker Image

```bash
# Build the image
docker build -t mikros-bot:latest .

# Verify image was created
docker images | grep mikros-bot
```

### Step 3: Run Docker Container

```bash
# Run with environment variables
docker run -d \
  --name mikros-bot \
  --restart=always \
  -v $(pwd)/.env:/app/.env:ro \
  mikros-bot:latest

# Or with environment variables directly
docker run -d \
  --name mikros-bot \
  --restart=always \
  -e DISCORD_BOT_TOKEN="your_token_here" \
  -e BOT_OWNER_ID="your_id_here" \
  mikros-bot:latest
```

### Step 4: Docker Management Commands

```bash
# View running containers
docker ps

# View logs
docker logs mikros-bot

# Follow logs
docker logs -f mikros-bot

# Stop container
docker stop mikros-bot

# Start container
docker start mikros-bot

# Restart container
docker restart mikros-bot

# Remove container
docker rm mikros-bot
```

### Step 5: Docker Compose (Optional)

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mikros-bot:
    build: .
    container_name: mikros-bot
    restart: always
    environment:
      - DISCORD_BOT_TOKEN=${DISCORD_BOT_TOKEN}
      - BOT_OWNER_ID=${BOT_OWNER_ID}
    volumes:
      - ./logs:/app/logs
      - ./.env:/app/.env:ro
    # No ports needed for Discord bot
```

Run with:
```bash
docker-compose up -d
```

---

## 🔐 7. TODO: Secrets Management (Future)

### Current Implementation
- Secrets stored in `.env` file
- File permissions set to `600` (owner read/write only)
- `.env` file should be in `.gitignore` (already configured)

### Future Enhancement: Google Secret Manager

**TODO:** Integrate with Google Secret Manager for secure secret storage.

#### Benefits:
- Centralized secret management
- Automatic rotation support
- Audit logging
- Fine-grained access control
- No secrets in file system

#### Implementation Steps (Future):
1. Enable Secret Manager API in GCP
2. Create secrets in Secret Manager:
   ```bash
   echo -n "your_bot_token" | gcloud secrets create discord-bot-token --data-file=-
   echo -n "your_owner_id" | gcloud secrets create bot-owner-id --data-file=-
   ```
3. Grant VM service account access:
   ```bash
   gcloud secrets add-iam-policy-binding discord-bot-token \
     --member="serviceAccount:PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
     --role="roles/secretmanager.secretAccessor"
   ```
4. Modify bot to read from Secret Manager API
5. Update systemd service or Dockerfile to use Secret Manager

#### Manual Fallback (Current):
```bash
# Secure .env file
chmod 600 .env
chown $USER:$USER .env

# Verify permissions
ls -la .env
# Should show: -rw------- (600)
```

---

## 📊 8. TODO: Logging & Monitoring

### Current Implementation
- Logging via SLF4J/Logback
- Logs written to `logs/bot.log` (if configured)
- Console output available
- systemd journal integration (if using systemd)

### Future Enhancement: Cloud Logging

**TODO:** Integrate with Google Cloud Logging (formerly Stackdriver).

#### Benefits:
- Centralized log aggregation
- Log retention and search
- Alerting on errors
- Integration with monitoring

#### Implementation Steps (Future):
1. Enable Cloud Logging API
2. Install Cloud Logging agent:
   ```bash
   curl -sSO https://dl.google.com/cloudagents/add-logging-agent-repo.sh
   sudo bash add-logging-agent-repo.sh
   sudo apt-get update
   sudo apt-get install google-fluentd
   ```
3. Configure log collection
4. View logs in Cloud Console

### Future Enhancement: Stackdriver Monitoring

**TODO:** Set up monitoring and alerting.

#### Metrics to Monitor:
- Bot uptime
- Command execution rate
- Error rate
- Memory usage
- CPU usage
- Discord API rate limits

#### Implementation Steps (Future):
1. Enable Cloud Monitoring API
2. Install monitoring agent
3. Create custom metrics
4. Set up alerting policies

### Manual Fallback (Current):
```bash
# View application logs
tail -f logs/bot.log

# View systemd logs
sudo journalctl -u mikros-bot -f

# View Docker logs
docker logs -f mikros-bot

# Rotate logs (if needed)
logrotate -f /etc/logrotate.d/mikros-bot
```

---

## 🔧 9. Troubleshooting

### Bot Not Starting

1. **Check Java version:**
   ```bash
   java -version
   # Should be Java 17 or higher
   ```

2. **Check .env file:**
   ```bash
   cat .env
   # Verify DISCORD_BOT_TOKEN is set
   ```

3. **Check logs:**
   ```bash
   # systemd
   sudo journalctl -u mikros-bot -n 100
   
   # Docker
   docker logs mikros-bot
   
   # Direct execution
   tail -f bot.log
   ```

4. **Verify JAR exists:**
   ```bash
   ls -lh build/libs/TG-MIKROS-BOT-discord-1.0-SNAPSHOT.jar
   ```

### Bot Disconnects Frequently

1. **Check VM resources:**
   ```bash
   # Check memory
   free -h
   
   # Check CPU
   top
   
   # Check disk space
   df -h
   ```

2. **Upgrade VM instance:**
   - Consider upgrading from `e2-micro` to `e2-small` or higher

3. **Check network connectivity:**
   ```bash
   ping discord.com
   ```

### Permission Errors

1. **Check file permissions:**
   ```bash
   ls -la .env
   chmod 600 .env
   ```

2. **Check systemd service user:**
   ```bash
   # Ensure user in service file matches actual user
   whoami
   sudo nano /etc/systemd/system/mikros-bot.service
   ```

### Build Failures

1. **Clean and rebuild:**
   ```bash
   ./gradlew clean build
   ```

2. **Check Java version:**
   ```bash
   java -version
   ./gradlew --version
   ```

3. **Verify dependencies:**
   ```bash
   ./gradlew dependencies
   ```

---

## 🔄 10. Updating the Bot

### Method 1: Manual Update

```bash
# Navigate to project directory
cd ~/TG-MIKROS-BOT-discord

# Pull latest changes
git pull origin main

# Rebuild
./gradlew clean build -x test

# Restart service
sudo systemctl restart mikros-bot
```

### Method 2: Automated Update Script

Create `update-bot.sh`:

```bash
#!/bin/bash
set -e

cd ~/TG-MIKROS-BOT-discord
git pull origin main
./gradlew clean build -x test
sudo systemctl restart mikros-bot
echo "Bot updated and restarted successfully"
```

Make executable:
```bash
chmod +x update-bot.sh
```

Run:
```bash
./update-bot.sh
```

### Method 3: Docker Update

```bash
# Rebuild image
docker build -t mikros-bot:latest .

# Stop and remove old container
docker stop mikros-bot
docker rm mikros-bot

# Start new container
docker run -d --name mikros-bot --restart=always \
  -v $(pwd)/.env:/app/.env:ro \
  mikros-bot:latest
```

---

## 🛡️ 11. Security Best Practices

### File Permissions
```bash
# Secure .env file
chmod 600 .env
chown $USER:$USER .env

# Secure project directory (optional)
chmod 700 ~/TG-MIKROS-BOT-discord
```

### Firewall Rules
- Discord bot doesn't require incoming connections
- Consider restricting SSH access to specific IPs
- Use GCP firewall rules for additional security

### Service Account
- Use dedicated service account for bot (future enhancement)
- Grant minimum required permissions
- Rotate credentials regularly

### Regular Updates
- Keep system packages updated: `sudo apt update && sudo apt upgrade`
- Keep Java updated
- Monitor security advisories

---

## 📈 12. Performance Optimization

### VM Sizing
- **Development/Testing:** `e2-micro` (free tier)
- **Small Community:** `e2-small` (1 vCPU, 2 GB RAM)
- **Medium Community:** `e2-medium` (2 vCPU, 4 GB RAM)
- **Large Community:** `e2-standard-2` (2 vCPU, 8 GB RAM)

### JVM Tuning (Optional)

Add to systemd service `ExecStart`:
```ini
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar /path/to/bot.jar
```

Or create `jvm-options.txt`:
```
-Xms512m
-Xmx1024m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
```

Use with:
```bash
java @jvm-options.txt -jar bot.jar
```

---

## ✅ 13. Verification Checklist

After deployment, verify:

- [ ] Bot appears online in Discord
- [ ] Commands respond correctly
- [ ] Logs show no errors
- [ ] Bot reconnects after VM restart (if using systemd)
- [ ] Resource usage is acceptable
- [ ] .env file is secured (600 permissions)
- [ ] Service is enabled (if using systemd)
- [ ] Logs are accessible

### Test Commands
```bash
# In Discord, test:
/warn @user test
/rpg-register name:Test class:WARRIOR
/game-setup channel:#test-channel
```

---

## 📝 14. Additional Resources

- [Google Cloud Compute Engine Documentation](https://cloud.google.com/compute/docs)
- [Discord Bot Development Guide](https://discord.com/developers/docs)
- [JDA Documentation](https://docs.jda.wiki/)
- [systemd Service Documentation](https://www.freedesktop.org/software/systemd/man/systemd.service.html)
- [Docker Documentation](https://docs.docker.com/)

---

## 🆘 Support

For issues or questions:
1. Check logs first: `sudo journalctl -u mikros-bot -n 100`
2. Review this documentation
3. Check project README.md
4. Review BEST_CODING_PRACTICES.md
5. Contact development team

---

**Last Updated:** 2025-10-08  
**Version:** 1.0  
**Status:** Production Ready





