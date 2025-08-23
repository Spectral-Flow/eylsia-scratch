# Elysia Scratch - Full-Stack Demo

A modern full-stack demo application showcasing the latest technologies:

- **Backend**: Elysia (TypeScript) on Bun runtime with REST API and WebSocket chat
- **Android**: Kotlin + Jetpack Compose with Material 3 design
- **Infrastructure**: Docker, GitHub Actions CI/CD, automated dependency updates

## 🚀 Quick Start

### Prerequisites

- **Bun** (latest): [Install Bun](https://bun.sh/docs/installation)
- **Android Studio** (latest): [Download](https://developer.android.com/studio)
- **JDK 21**: [Install Temurin JDK](https://adoptium.net/)
- **Docker** (optional): [Install Docker](https://docs.docker.com/get-docker/)

### Run Server

```bash
# Navigate to server directory
cd server

# Install dependencies
bun install

# Start development server with hot reload
bun run dev

# Or start production server
bun run start
```

The server will be available at:
- API: http://localhost:3000/api/hello
- Health: http://localhost:3000/api/health
- WebSocket: ws://localhost:3000/ws
- Documentation: http://localhost:3000/swagger

### Run Android App

```bash
# Navigate to android directory
cd android

# Build and install debug APK
./gradlew installDebug

# Or open in Android Studio and run
```

The app connects to the server at `10.0.2.2:3000` (Android emulator host mapping).

## 🏗️ Architecture

### Server (Elysia + Bun)

```
server/
├── src/
│   ├── index.ts      # Main server setup with middleware
│   ├── routes.ts     # REST API endpoints
│   └── ws.ts         # WebSocket chat handler
├── package.json      # Dependencies and scripts
├── tsconfig.json     # TypeScript configuration
├── biome.json        # Code formatting and linting
└── Dockerfile        # Container configuration
```

**Features:**
- REST endpoints: `/api/hello`, `/api/health`, `/api/protected`
- WebSocket chat broadcasting at `/ws`
- CORS enabled for Android emulator
- Request ID middleware for tracing
- Structured logging
- Bearer token authentication (demo)
- OpenAPI/Swagger documentation
- JSON schema validation

### Android (Kotlin + Compose)

```
android/
├── app/src/main/java/com/example/elysiaapp/
│   ├── MainActivity.kt           # Main activity with StrictMode
│   ├── ui/
│   │   ├── App.kt               # Main Compose UI
│   │   ├── AppViewModel.kt      # UI state management
│   │   └── theme/Theme.kt       # Material 3 theme
│   └── data/
│       ├── ApiClient.kt         # HTTP client (Ktor)
│       └── WsClient.kt          # WebSocket client
├── build.gradle.kts             # App-level Gradle config
└── gradle/libs.versions.toml    # Version catalog
```

**Features:**
- Material 3 design system
- Jetpack Compose UI with modern components
- MVVM architecture with ViewModel
- Kotlin Coroutines and Flows
- Ktor HTTP client with JSON serialization
- WebSocket real-time chat
- StrictMode enabled for debugging
- Unit and instrumented tests

## 🔧 Development

### Server Commands

```bash
cd server

# Development with hot reload
bun run dev

# Type checking
bun run typecheck

# Linting and formatting
bun run lint
bun run lint:fix

# Testing
bun test

# Build for production
bun run build

# Docker build
docker build -t elysia-server .
docker run -p 3000:3000 elysia-server
```

### Android Commands

```bash
cd android

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (requires emulator)
./gradlew connectedDebugAndroidTest

# Lint and format code
./gradlew detekt

# Clean build
./gradlew clean
```

## 📱 Features Demo

### Server Ping
- Tap "Ping Server" to call `/api/hello?name=Richie`
- Shows JSON response with greeting and timestamp
- Displays network errors if server is unavailable

### Real-time Chat
- WebSocket connection to `/ws` endpoint
- Send messages from Android app
- Messages broadcast to all connected clients
- Connection status indicator
- Auto-scroll to latest messages

## 🔒 Security & Performance

### Security Features
- CORS properly configured
- Input validation with JSON schemas
- Bearer token authentication pattern
- Helmet.js security headers
- ProGuard/R8 code obfuscation (Android)
- Network security config (Android)

### Performance Optimizations
- Gradle build cache and parallel execution
- Kotlin incremental compilation
- R8 full mode for Android
- Bun's fast runtime and bundler
- Docker multi-stage builds
- Connection pooling in HTTP clients

## 🚢 Deployment

### Docker

```bash
# Build and run server
cd server
docker build -t elysia-server .
docker run -p 3000:3000 elysia-server
```

### CI/CD

The project includes GitHub Actions workflows:

- **server-ci.yml**: Build, test, and containerize server
- **android-ci.yml**: Build APK, run tests, security scans
- **updates.yml**: Automated dependency updates

### Artifacts

- Android APK artifacts uploaded from CI
- Docker images tagged with `:latest` and `:git-sha`
- Test reports and coverage data

## 🔄 Dependencies

This project uses the latest stable versions:

- **Kotlin**: 2.1.0
- **Compose BOM**: 2024.12.01
- **Android Gradle Plugin**: 8.7.3
- **Elysia**: ^1.1.26
- **TypeScript**: ^5.7.2
- **Bun**: latest via Docker

### Automated Updates

- **Renovate** configured for aggressive latest version updates
- Daily dependency scans
- Automatic security vulnerability patches
- Grouped updates by framework (Compose, Kotlin, Ktor, etc.)

## 🧪 Testing

### Server Tests
```bash
cd server
bun test
```

### Android Tests
```bash
cd android

# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires emulator)
./gradlew connectedDebugAndroidTest
```

### Integration Testing
1. Start server: `cd server && bun run dev`
2. Run Android app in emulator
3. Test ping functionality and chat features

## 🛠️ Troubleshooting

### Common Issues

**Server won't start:**
- Check if port 3000 is available
- Verify Bun is installed: `bun --version`
- Check dependencies: `cd server && bun install`

**Android connection failed:**
- Ensure server is running on `localhost:3000`
- Use `10.0.2.2:3000` for Android emulator
- Check network permissions in AndroidManifest.xml

**Build failures:**
- Clear Gradle cache: `cd android && ./gradlew clean`
- Update Android SDK and tools
- Verify JDK 21 is installed and configured

### Development Tips

- Use `bun run dev` for server hot reload
- Enable StrictMode in Android debug builds
- Monitor WebSocket connections in browser DevTools
- Use Android Studio's Network Inspector for debugging

## 📄 License

This project is a demo and is provided as-is for educational purposes.