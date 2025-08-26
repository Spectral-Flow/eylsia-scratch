# Elysia Full-Stack Demo with Voice Integration

A modern full-stack demo application showcasing the latest technologies:

- **Backend**: Elysia (latest) with ElevenLabs voice synthesis, WebSocket chat, and TypeScript
- **Frontend**: Android app with Kotlin, Jetpack Compose, Material 3, and voice controls
- **Infrastructure**: Docker, GitHub Actions CI/CD, automated dependency updates

## ✨ New Features

### 🎤 ElevenLabs Voice Integration
- **Real ElevenLabs API Integration**: Full integration with ElevenLabs text-to-speech API
- **Professional Voice Synthesis**: Convert text to natural speech with customizable settings
- **Real-time Voice Chat**: Send messages and receive voice responses via WebSocket
- **Voice Caching**: Intelligent caching system reduces API calls and improves performance
- **Multiple Voice Models**: Support for different ElevenLabs voices and settings
- **Android Voice Controls**: Voice synthesis buttons and status indicators
- **Graceful Fallback**: Automatic fallback to mock mode when API is unavailable

### 🚀 Enhanced Architecture & Performance
- **Modular Services**: Clean separation of concerns with dedicated service classes
- **Advanced Error Handling**: Custom error types, retry logic, and circuit breakers
- **Professional Logging**: Structured logging with request tracking and performance metrics
- **Rate Limiting**: Production-grade rate limiting with configurable thresholds
- **Configuration Management**: Environment-based config with comprehensive validation
- **Health Monitoring**: Comprehensive service status and dependency checking
- **Performance Optimizations**: Caching, cleanup routines, and memory management

## 🚀 Quick Start

### Prerequisites

- **Bun** (latest): [Install from bun.sh](https://bun.sh/)
- **Java 17+**: For Android development
- **Android Studio** or **Android SDK**: For building the Android app
- **Docker** (optional): For containerized deployment

### Running the Backend

```bash
cd server
bun install
bun run dev
```

The server will start at `http://localhost:3000` with:
- Health endpoint: `GET /health` (enhanced with service status)
- Hello API: `GET /api/hello?name=YourName` (shows voice capability)
- Voice synthesis: `POST /api/voice/synthesize` (ElevenLabs integration)
- Available voices: `GET /api/voice/voices` (list voice models)
- WebSocket chat: `ws://localhost:3000/ws` (with voice message support)
- Swagger docs: `http://localhost:3000/swagger` (interactive API docs)

### Setting Up Voice Features

1. Get an ElevenLabs API key from [elevenlabs.io](https://elevenlabs.io/)
2. Copy the environment template:
   ```bash
   cp .env.example .env
   ```
3. Add your API key to `.env`:
   ```bash
   ELEVENLABS_API_KEY=your_api_key_here
   ```

Voice features work without an API key (mock mode) for development.

### Running the Android App

```bash
cd android-simple
./gradlew assembleDebug
# Install on device/emulator:
adb install -r build/outputs/apk/debug/android-simple-debug.apk
```

**Note**: The Android app expects the server to be running on `10.0.2.2:3000` (Android emulator localhost mapping). The app includes voice synthesis controls that activate when the server has ElevenLabs integration enabled.

## 🏗️ Project Structure

```
├── server/                    # Elysia backend
│   ├── src/
│   │   └── index.ts          # Main server file
│   ├── package.json          # Dependencies & scripts
│   ├── biome.json           # Linting & formatting
│   ├── tsconfig.json        # TypeScript config
│   └── Dockerfile           # Container config
├── android/                  # Android Kotlin app
│   ├── app/
│   │   ├── src/main/java/com/example/elysiaapp/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ui/App.kt    # Main Compose UI
│   │   │   └── data/        # API & WebSocket clients
│   │   └── build.gradle.kts
│   ├── gradle/libs.versions.toml  # Version catalog
│   └── settings.gradle.kts
├── .github/workflows/        # CI/CD automation
│   ├── server-ci.yml        # Backend testing & Docker
│   ├── android-ci.yml       # Android build & test
│   └── updates.yml          # Dependency updates
└── renovate.json            # Automated dependency management
```

## 🔧 Development

### Backend Commands

```bash
cd server
bun run dev         # Start development server with watch
bun run build       # Build for production
bun run start       # Start production server
bun run lint        # Lint and format code
bun run typecheck   # TypeScript type checking
```

### Android Commands

```bash
cd android
./gradlew assembleDebug           # Build debug APK
./gradlew testDebugUnitTest       # Run unit tests
./gradlew connectedAndroidTest    # Run instrumented tests
./gradlew clean                   # Clean build artifacts
```

### Docker

Build and run the backend in Docker:

```bash
cd server
docker build -t elysia-demo .
docker run -p 3000:3000 elysia-demo
```

## 📱 Features

### Backend (Elysia/Bun)
- ✅ REST API with JSON schema validation
- ✅ WebSocket real-time chat with voice integration
- ✅ **ElevenLabs voice synthesis** with real API integration
- ✅ **Production-grade rate limiting** with configurable thresholds
- ✅ **Advanced error handling** with circuit breakers and retries
- ✅ **Performance monitoring** with logging metrics and health checks
- ✅ **Intelligent caching** for voice API responses
- ✅ CORS support for cross-origin requests
- ✅ Structured logging with request IDs and performance tracking
- ✅ Enhanced authentication middleware
- ✅ Swagger/OpenAPI documentation
- ✅ Comprehensive error handling middleware
- ✅ Enhanced health check endpoint with service monitoring
- ✅ Docker containerization with optimized builds

### Android App (Kotlin/Compose)
- ✅ Material 3 design system
- ✅ Jetpack Compose UI
- ✅ HTTP client (Ktor) for REST API calls
- ✅ WebSocket client for real-time chat
- ✅ Kotlinx Coroutines for async operations
- ✅ kotlinx.serialization for JSON handling
- ✅ Unit and instrumented tests
- ✅ Modern Gradle configuration with version catalogs

### DevOps & Automation
- ✅ GitHub Actions CI/CD
- ✅ Automated dependency updates (Renovate)
- ✅ Code quality checks (linting, type checking)
- ✅ Docker image building and testing
- ✅ APK artifact uploads

## 🔄 Latest Versions Used

### Backend
- **Bun**: 1.2.20+
- **Elysia**: 1.3.18+
- **TypeScript**: 5.9.2+
- **Biome**: 2.2.2+

### Android
- **Kotlin**: 2.1.0+
- **Android Gradle Plugin**: 8.8.2+
- **Compose BOM**: 2024.12.01+
- **Ktor**: 3.0.3+
- **Material 3**: 1.3.1+

### CI/CD
- **GitHub Actions**: Latest official actions
- **Renovate**: v40.3.9+ for dependency updates
- **Docker**: Multi-stage builds with latest base images

## 🧪 Testing

The project includes comprehensive testing:

### Backend
- Type checking with TypeScript
- Linting with Biome
- Integration tests via curl in CI
- Docker container testing

### Android
- Unit tests for core logic
- Instrumented tests for UI components
- Compose UI testing with test rules

Run all tests:

```bash
# Backend
cd server && bun run typecheck && bun run lint

# Android
cd android && ./gradlew test connectedAndroidTest
```

## 🔄 Continuous Updates

This project uses Renovate for automated dependency management:

- **Daily scans** for new versions
- **Auto-merge** for patch and minor updates
- **Grouped updates** by ecosystem (Kotlin, Android, Elysia, etc.)
- **Security vulnerability** alerts and fixes
- **Dependency dashboard** for visibility

## 📚 API Documentation

When the server is running, visit `http://localhost:3000/swagger` for interactive API documentation.

### Endpoints

- `GET /health` - Server health check
- `GET /api/hello?name=string` - Greeting API with optional name parameter
- `WS /ws` - WebSocket endpoint for real-time chat

### WebSocket Protocol

Send text messages to `/ws` and receive JSON responses:

```json
{
  "type": "message",
  "from": "client", 
  "text": "Your message",
  "timestamp": "2024-01-01T00:00:00.000Z"
}
```

## 🛠️ Troubleshooting

### Common Issues

1. **Android app can't connect to server**
   - Ensure server is running on `localhost:3000`
   - Android emulator maps `10.0.2.2` to host `localhost`
   - Check firewall settings

2. **Build failures**
   - Ensure latest JDK 17+ is installed
   - Run `./gradlew clean` for Android builds
   - Check network connectivity for dependency downloads

3. **WebSocket connection issues**
   - Verify server WebSocket endpoint is accessible
   - Check for proxy/firewall blocking WebSocket connections
   - Enable `usesCleartextTraffic` in AndroidManifest.xml for HTTP

### Development Tips

- Use Android Studio's Device Manager for consistent emulator setup
- Enable developer options and USB debugging for physical device testing
- Use `adb logcat` to debug Android app issues
- Check server logs for backend debugging

## 📄 License

This project is a demo/template and is provided as-is for educational and reference purposes.