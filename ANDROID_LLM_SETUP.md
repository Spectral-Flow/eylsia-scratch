# Android Phone LLM Setup with Mistral Model

This guide will help you set up Ollama with the Mistral 7B model on your Android phone to work with the Elysia application.

## Prerequisites

- Android phone with at least 8GB RAM (recommended)
- Termux app installed from F-Droid or GitHub releases
- At least 10GB free storage space
- Phone and computer on the same Wi-Fi network

## Step 1: Install Termux

1. **Download Termux** from F-Droid (recommended) or GitHub releases
   - F-Droid: https://f-droid.org/en/packages/com.termux/
   - GitHub: https://github.com/termux/termux-app/releases
   
2. **Open Termux** and update packages:
   ```bash
   pkg update && pkg upgrade
   ```

## Step 2: Create the Elysia Directory

```bash
# Create the directory structure as specified
mkdir -p ~/storage/shared/ELYSIA/elysia
cd ~/storage/shared/ELYSIA/elysia

# Verify the path
pwd
# Should show: /data/data/com.termux/files/home/storage/shared/ELYSIA/elysia
```

## Step 3: Install Ollama

```bash
# Install required packages
pkg install curl git golang

# Download and install Ollama for Android
curl -fsSL https://ollama.com/install.sh | sh

# Alternative if the above doesn't work:
# Download the Ollama binary manually
curl -L https://github.com/ollama/ollama/releases/download/v0.1.17/ollama-linux-arm64 -o ollama
chmod +x ollama
mv ollama /data/data/com.termux/files/usr/bin/
```

## Step 4: Configure Ollama Service

1. **Start Ollama server**:
   ```bash
   # Start Ollama in background
   ollama serve &
   
   # Or use screen to keep it running
   pkg install screen
   screen -S ollama
   ollama serve
   # Press Ctrl+A then D to detach
   ```

2. **Test Ollama is running**:
   ```bash
   curl http://localhost:11434/api/tags
   ```

## Step 5: Download Mistral Model

```bash
# Navigate to your Elysia directory
cd ~/storage/shared/ELYSIA/elysia

# Pull the specific Mistral model you requested
ollama pull mistral:7b-instruct-v0.2-q4_k_m

# Alternative: If the above tag doesn't work, try:
ollama pull mistral:7b-instruct
ollama pull mistral:7b-instruct-q4_k_m

# List downloaded models to verify
ollama list
```

## Step 6: Test the Model

```bash
# Test the model is working
ollama run mistral:7b-instruct-v0.2-q4_k_m "Hello, how are you?"

# Or test via API
curl -X POST http://localhost:11434/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "model": "mistral:7b-instruct-v0.2-q4_k_m",
    "messages": [
      {"role": "user", "content": "Hello, how are you?"}
    ],
    "stream": false
  }'
```

## Step 7: Configure Network Access

1. **Find your phone's IP address**:
   ```bash
   # In Termux, run:
   ip route get 1 | sed 's/^.*src \([^ ]*\).*$/\1/;q'
   
   # Or use:
   hostname -I
   ```

2. **Configure Ollama for network access**:
   ```bash
   # Create Ollama config directory
   mkdir -p ~/.ollama
   
   # Set environment variable for network binding
   export OLLAMA_HOST=0.0.0.0:11434
   
   # Add to your shell profile
   echo 'export OLLAMA_HOST=0.0.0.0:11434' >> ~/.bashrc
   ```

3. **Restart Ollama with network access**:
   ```bash
   # Kill existing Ollama process
   pkill ollama
   
   # Start with network binding
   OLLAMA_HOST=0.0.0.0:11434 ollama serve &
   ```

## Step 8: Configure Elysia Server

1. **Update server configuration**:
   - Edit `/server/.env` file
   - Change `LLM_ENDPOINT` to `http://YOUR_PHONE_IP:11434`
   - Change `LLM_MODEL` to `mistral:7b-instruct-v0.2-q4_k_m`

2. **Example configuration**:
   ```bash
   # If your phone IP is 192.168.1.100
   LLM_ENDPOINT=http://192.168.1.100:11434
   LLM_MODEL=mistral:7b-instruct-v0.2-q4_k_m
   LLM_ENABLED=true
   ```

## Step 9: Keep Ollama Running

To keep Ollama running when you're not actively using Termux:

1. **Using screen** (recommended):
   ```bash
   # Install screen
   pkg install screen
   
   # Start a screen session
   screen -S ollama-server
   
   # Start Ollama
   OLLAMA_HOST=0.0.0.0:11434 ollama serve
   
   # Detach from screen: Ctrl+A then D
   # Reattach later: screen -r ollama-server
   ```

2. **Using nohup**:
   ```bash
   nohup OLLAMA_HOST=0.0.0.0:11434 ollama serve > ollama.log 2>&1 &
   ```

3. **Create a startup script**:
   ```bash
   # Create startup script
   cat > ~/start-ollama.sh << 'EOF'
#!/bin/bash
export OLLAMA_HOST=0.0.0.0:11434
cd ~/storage/shared/ELYSIA/elysia
screen -dm -S ollama-server ollama serve
echo "Ollama started in screen session 'ollama-server'"
echo "Your phone IP: $(hostname -I)"
echo "Attach to session: screen -r ollama-server"
EOF
   
   # Make executable
   chmod +x ~/start-ollama.sh
   
   # Run the script
   ./start-ollama.sh
   ```

## Step 10: Verification

1. **Test from your computer**:
   ```bash
   # Replace 192.168.1.100 with your phone's actual IP
   curl http://192.168.1.100:11434/api/tags
   
   # Test chat
   curl -X POST http://192.168.1.100:11434/api/chat \
     -H "Content-Type: application/json" \
     -d '{
       "model": "mistral:7b-instruct-v0.2-q4_k_m",
       "messages": [{"role": "user", "content": "Hello!"}],
       "stream": false
     }'
   ```

2. **Start your Elysia server** and check the health endpoint for LLM status.

## Troubleshooting

### Model Not Found
If `mistral:7b-instruct-v0.2-q4_k_m` doesn't work, try these alternatives:
```bash
ollama pull mistral:7b-instruct
ollama pull mistral
ollama list  # Check available models
```

### Network Connection Issues
```bash
# Check if Ollama is listening on all interfaces
netstat -tlnp | grep 11434

# Check firewall (if you have one)
# Make sure port 11434 is open
```

### Memory Issues
```bash
# Check available memory
free -h

# If low on memory, try a smaller model:
ollama pull mistral:7b-instruct-q2_k
```

### Performance Tips
- Close other apps on your phone to free up RAM
- Keep your phone plugged in during long conversations
- Consider using a phone cooler if it gets hot
- Monitor battery usage

## File Locations Summary

- **Termux home**: `/data/data/com.termux/files/home/`
- **Your specified path**: `~/storage/shared/ELYSIA/elysia`
- **Model storage**: `~/.ollama/models/`
- **Ollama binary**: `/data/data/com.termux/files/usr/bin/ollama`

## Next Steps

After completing this setup:
1. Configure ElevenLabs API key in your server
2. Start the Elysia server
3. Build and run the Android app
4. Test the complete voice + LLM integration

The server will now use your phone as the LLM backend while running the API and WebSocket services on your computer or server.