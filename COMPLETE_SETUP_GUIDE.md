# Complete Setup Guide: Elysia with Phone-based LLM and Sci-Fi UI

This guide provides step-by-step instructions to set up the Elysia application with a local LLM running on your Android phone and enhanced voice synthesis with a futuristic cyber interface.

## 🚀 Quick Overview

You'll set up:
1. **Ollama with Mistral 7B** on your Android phone (using Termux)
2. **Elysia server** configured to connect to your phone's LLM
3. **ElevenLabs voice synthesis** for AI voice responses
4. **Enhanced Android app** with a sci-fi cyber UI theme

## 📱 Part 1: Setting Up LLM on Your Android Phone

### Step 1: Install Termux

1. **Download Termux**:
   - **Recommended**: F-Droid - https://f-droid.org/packages/com.termux/
   - **Alternative**: GitHub Releases - https://github.com/termux/termux-app/releases
   - **DO NOT** use Google Play Store version (outdated)

2. **Initial Setup**:
   ```bash
   # Open Termux and update
   pkg update && pkg upgrade -y
   
   # Install required packages
   pkg install curl git golang python nodejs-lts
   ```

### Step 2: Create Directory Structure

```bash
# Create the exact path you specified
mkdir -p ~/storage/shared/ELYSIA/elysia
cd ~/storage/shared/ELYSIA/elysia

# Verify path
pwd
# Should show: /data/data/com.termux/files/home/storage/shared/ELYSIA/elysia
```

### Step 3: Install Ollama

```bash
# Method 1: Official installer (recommended)
curl -fsSL https://ollama.com/install.sh | sh

# Method 2: If above fails, manual install
curl -L https://github.com/ollama/ollama/releases/latest/download/ollama-linux-arm64 -o ollama
chmod +x ollama
mv ollama $PREFIX/bin/

# Verify installation
ollama --version
```

### Step 4: Download and Set Up Mistral Model

```bash
# Start Ollama service
ollama serve &

# Wait a few seconds, then pull the Mistral model
ollama pull mistral:7b-instruct

# If the exact model variant isn't available, try:
# ollama pull mistral:7b
# ollama pull mistral

# Verify model is downloaded
ollama list
```

### Step 5: Configure Network Access

```bash
# Find your phone's IP address
ip route get 1 | sed 's/^.*src \([^ ]*\).*$/\1/;q'
# Or: hostname -I

# Stop existing Ollama process
pkill ollama

# Configure Ollama for network access
export OLLAMA_HOST=0.0.0.0:11434
echo 'export OLLAMA_HOST=0.0.0.0:11434' >> ~/.bashrc

# Restart Ollama with network binding
ollama serve &
```

### Step 6: Test LLM from Phone

```bash
# Test locally first
ollama run mistral:7b-instruct "Hello, how are you?"

# Test via API
curl -X POST http://localhost:11434/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "model": "mistral:7b-instruct",
    "messages": [{"role": "user", "content": "Hello!"}],
    "stream": false
  }'
```

### Step 7: Keep Ollama Running

Create a startup script:
```bash
cat > ~/start-elysia-llm.sh << 'EOF'
#!/bin/bash
export OLLAMA_HOST=0.0.0.0:11434
cd ~/storage/shared/ELYSIA/elysia

# Start Ollama in a screen session
pkg install screen -y
screen -dm -S elysia-ollama ollama serve

echo "🤖 Elysia LLM Server Started!"
echo "📍 Location: $(pwd)"
echo "🌐 Your Phone IP: $(hostname -I)"
echo "🔌 Endpoint: http://$(hostname -I | tr -d ' '):11434"
echo "📱 Model: mistral:7b-instruct"
echo ""
echo "💡 To attach to session: screen -r elysia-ollama"
echo "💡 To stop: screen -r elysia-ollama then Ctrl+C"
EOF

chmod +x ~/start-elysia-llm.sh

# Run the script
./start-elysia-llm.sh
```

## 🖥️ Part 2: Configure Elysia Server

### Step 1: Find Your Phone's IP

From your computer, find your phone's IP:
```bash
# If you're on the same WiFi network
# Check your router's admin panel or use:
nmap -sn 192.168.1.0/24 | grep -E "192.168.1.[0-9]+"
# Look for your phone's device name
```

### Step 2: Configure Server Environment

```bash
cd server
cp .env.example .env
```

Edit `.env` file:
```bash
# Server Configuration
PORT=3000
NODE_ENV=development

# ElevenLabs Configuration (Get your API key from elevenlabs.io)
ELEVENLABS_API_KEY=your_actual_elevenlabs_api_key_here
ELEVENLABS_VOICE_ID=21m00Tcm4TlvDq8ikWAM

# Local LLM Configuration (Replace with your phone's IP)
LLM_ENDPOINT=http://YOUR_PHONE_IP:11434
LLM_MODEL=mistral:7b-instruct
LLM_ENABLED=true

# Security
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,https://*.vercel.app

# Logging
LOG_LEVEL=info
```

**Example** (if your phone IP is 192.168.1.150):
```bash
LLM_ENDPOINT=http://192.168.1.150:11434
```

### Step 3: Test Connection

```bash
# Test from your computer to phone
curl http://YOUR_PHONE_IP:11434/api/tags

# If this works, you should see your models listed
```

### Step 4: Start Elysia Server

```bash
# Install dependencies
bun install

# Start server
bun run dev
```

You should see:
```
🚀 Elysia server is running at http://localhost:3000
📖 Swagger documentation available at http://localhost:3000/swagger
🎤 Voice synthesis: ENABLED (if API key is set)
🤖 Local LLM: ENABLED (mistral:7b-instruct)
```

## 🎤 Part 3: ElevenLabs Voice Setup

### Step 1: Get ElevenLabs API Key

1. Sign up at [ElevenLabs](https://elevenlabs.io/)
2. Go to [API Keys](https://elevenlabs.io/app/settings/api-keys)
3. Create or copy your API key
4. Add to your `.env` file:
   ```bash
   ELEVENLABS_API_KEY=sk_your_actual_api_key_here
   ```

### Step 2: Test Voice Synthesis

```bash
# Test voice synthesis endpoint
curl -X POST http://localhost:3000/api/voice/synthesize \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello, this is a test of the neural voice interface!"}' \
  --output test_voice.mp3

# Play the audio file to test
```

## 📱 Part 4: Enhanced Android App

The Android app now features a **futuristic cyber interface** with:

### 🎨 Sci-Fi Design Elements
- **Neon color scheme**: Cyan, purple, and green neon colors
- **Holographic cards**: Animated borders and glowing effects
- **Cyber typography**: Monospace fonts for a tech aesthetic
- **Animated backgrounds**: Moving grid patterns
- **Glowing buttons**: Neon button effects with hover animations
- **Status indicators**: Pulsing lights for connection status

### ✨ Features
- **System Status Panel**: Real-time monitoring of LLM and voice connections
- **Neural Chat Interface**: Enhanced messaging with cyber styling
- **Voice Synthesis Controls**: Dedicated voice testing interface
- **Holographic Effects**: Animated UI elements for immersion

### 🔧 Building the App

```bash
# In the project root
./gradlew assembleDebug

# Install on device/emulator
adb install -r android-simple/build/outputs/apk/debug/android-simple-debug.apk
```

## 🧪 Testing the Complete Setup

### 1. Test Health Endpoint
```bash
curl http://localhost:3000/health
```

Should show LLM and voice service status.

### 2. Test LLM Chat
```bash
curl -X POST http://localhost:3000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, tell me about AI!"}'
```

### 3. Test Voice + LLM
Open the Android app and:
1. Connect to WebSocket
2. Send a message
3. Use the voice button (🎤) for voice synthesis

## 🔧 Troubleshooting

### Phone LLM Issues

**Model not found:**
```bash
# Try different model variants
ollama pull mistral:7b
ollama pull mistral
ollama list  # Check what's available
```

**Network connection issues:**
```bash
# Check Ollama is listening on all interfaces
netstat -tlnp | grep 11434

# Test from phone browser
# Go to: http://localhost:11434/api/tags
```

**Memory issues:**
```bash
# Check available memory
free -h

# Close other apps, or try smaller model:
ollama pull tinyllama
```

### Server Issues

**Can't connect to phone LLM:**
- Verify phone and computer are on same WiFi
- Check firewall settings
- Ensure Ollama is running with `OLLAMA_HOST=0.0.0.0:11434`

**Voice synthesis not working:**
- Verify ElevenLabs API key is correct
- Check account credits/limits
- Test with a simple curl request

### Android App Issues

**Connection failed:**
- Ensure server is running on correct port
- For emulator, use `10.0.2.2:3000`
- For physical device, use computer's actual IP

## 🌟 Advanced Configuration

### Custom Voice Settings
Customize voice synthesis in the server:
```json
{
  "text": "Your message",
  "voiceSettings": {
    "stability": 0.5,
    "similarityBoost": 0.75,
    "style": 0.0,
    "useSpeakerBoost": true
  }
}
```

### Performance Optimization

**Phone Performance:**
- Keep phone plugged in during heavy use
- Close other apps to free RAM
- Consider phone cooling for extended sessions

**Server Performance:**
- Adjust `LLM_MAX_TOKENS` in config
- Monitor memory usage
- Use connection pooling for high traffic

## 📁 File Structure Summary

```
eylsia-scratch/
├── server/
│   ├── .env                    # Your configuration
│   ├── src/
│   │   ├── index.ts           # Main server
│   │   ├── llm.ts             # LLM integration
│   │   ├── elevenlabs.ts      # Voice synthesis
│   │   └── config.ts          # Configuration
│   └── package.json
├── android-simple/
│   ├── src/main/kotlin/com/example/elysiaapp/
│   │   ├── ui/
│   │   │   ├── App.kt         # Main cyber UI
│   │   │   ├── theme/         # Sci-fi theme
│   │   │   └── components/    # Custom components
│   │   └── data/
│   │       ├── ApiClient.kt   # Server communication
│   │       └── WsClient.kt    # WebSocket client
│   └── build.gradle.kts
└── ANDROID_LLM_SETUP.md       # This guide
```

## 🎯 Next Steps

1. **Customize the UI**: Modify colors, animations, and layouts in the theme files
2. **Add More Models**: Experiment with different LLM models in Ollama
3. **Enhance Voice**: Try different ElevenLabs voices and settings
4. **Deploy**: Consider deploying the server to a cloud provider
5. **Extend Features**: Add file uploads, image generation, or other AI capabilities

Your setup is now complete! You have a futuristic AI interface running with local LLM processing on your Android phone and professional voice synthesis. 🚀