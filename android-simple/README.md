# Android Project Setup

This directory contains the Android project files. To set up the Android project:

## Quick Setup

1. **Create a new Android project in Android Studio:**
   - Choose "Empty Activity"
   - Package name: `com.example.elysiaapp`
   - Language: Kotlin
   - Minimum SDK: API 24

2. **Update your `build.gradle.kts` (Module: app):**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.elysiaapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.elysiaapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.8")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.8")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.1")
    
    // Ktor client for HTTP and WebSocket
    implementation("io.ktor:ktor-client-core:3.0.3")
    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("io.ktor:ktor-client-websockets:3.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

3. **Copy the source files from the repository:**
   - Copy `ApiClient.kt` to `app/src/main/java/com/example/elysiaapp/data/`
   - Copy `WsClient.kt` to `app/src/main/java/com/example/elysiaapp/data/`
   - Copy `App.kt` to `app/src/main/java/com/example/elysiaapp/ui/`
   - Update `MainActivity.kt` to use the `App` composable

4. **Update AndroidManifest.xml:**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Add to application tag: -->
android:usesCleartextTraffic="true"
```

5. **Run the project:**
   - Start the Elysia server: `cd server && bun run dev`
   - Build and run the Android app
   - The app will connect to `10.0.2.2:3000` (emulator localhost)

## Features

The Android app includes:
- Material 3 design with modern Compose UI
- REST API calls to test server connectivity
- Real-time WebSocket chat functionality  
- Proper error handling and loading states
- Unit and instrumented tests

## Testing

- **Unit tests**: Test business logic and data models
- **Instrumented tests**: Test UI components and interactions
- **Manual testing**: Verify server connectivity and chat functionality