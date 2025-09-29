# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Core Build Commands
```bash
# Build the project
./gradlew build

# Generate debug APK
./gradlew assembleDebug

# Generate release APK
./gradlew assembleRelease

# Install to connected device
./gradlew installDebug

# Run tests
./gradlew test

# Run lint checks
./gradlew lint

# Clean build
./gradlew clean
```

### Development Environment
- **Android Studio**: Arctic Fox | 2020.3.1+
- **Android Gradle Plugin**: 8.6+
- **Kotlin**: 2.0+
- **Compile SDK**: 35 (Android 14)
- **Min SDK**: 23 (Android 6.0)
- **Java**: JDK 11

## Project Architecture

### MVVM + Clean Architecture
The project follows MVVM architecture with Clean Architecture principles:

```
Presentation Layer (Compose UI + ViewModels)
    ↓
Domain Layer (Use Cases + Business Logic)
    ↓
Data Layer (Repositories + Data Sources)
```

### Key Architectural Patterns

1. **Single Source of Truth**: Each data source has one authoritative provider
2. **Dependency Inversion**: High-level modules don't depend on low-level modules
3. **Reactive Programming**: Kotlin Flow for async data streams
4. **State Management**: StateFlow for UI state management

### Core Modules Structure

#### Network Layer (`data/network/`)
- **NetworkModule.kt**: OkHttp/Retrofit configuration with optimized timeouts
- **NetworkConnectivityMonitor.kt**: Real-time network status monitoring
- **RetryInterceptor.kt**: Automatic request retry with exponential backoff
- **NetworkStateInterceptor.kt**: Network state validation and error handling
- **AuthInterceptor.kt**: Bearer token authentication

#### Security Layer (`data/security/`)
- **SecurityHelper.kt**: Android Keystore integration for data encryption
- Encrypted credential storage using AndroidX Security Crypto

#### Data Persistence
- **Room Database**: Play history and file cache storage
- **DataStore**: Preferences and settings management
- **Android Keystore**: Sensitive data encryption

#### Error Handling (`data/error/`)
- **GlobalErrorHandler.kt**: Centralized error handling
- Custom exceptions: `NetworkException`, `AuthenticationException`
- Network-specific error messages and user feedback

### URL Handling System
The project includes a sophisticated URL handling system (`data/utils/UrlUtils.kt`):
- Auto-formatting URLs (adds http/https protocol)
- Validation and error checking
- Smart suggestions for incomplete URLs
- Network diagnostics and connectivity testing

### Network Diagnostics
Advanced network diagnostic capabilities (`data/utils/NetworkDiagnostics.kt`):
- Server connectivity testing
- Network quality assessment
- Detailed error reporting with user-friendly messages
- Optimization suggestions based on network conditions

## Key Features and Implementation Details

### Authentication Flow
1. **Login Screen**: Server URL input with smart formatting
2. **Network Validation**: Real-time network status checking
3. **URL Processing**: Automatic protocol addition and validation
4. **Token Management**: Bearer token storage and refresh
5. **Session Persistence**: Encrypted credential storage

### Network Optimization
- **Timeouts**: 20s connection, 60s read/write, 90s call timeout
- **Retry Logic**: 3 retries with exponential backoff
- **Connection Pool**: Optimized for mobile networks
- **Heartbeat**: 30s ping interval for connection health
- **Caching**: Response caching for API calls

### Media Player Integration
- **ExoPlayer (Media3)**: For audio/video playback
- **Picture-in-Picture**: Android 8.0+ support
- **Background Playback**: Foreground service implementation
- **Play History**: Room database for tracking

### UI/UX Features
- **Material3**: Modern design system
- **Network Status Banner**: Real-time network quality indicator
- **Smart URL Input**: Auto-suggestions and formatting
- **Error Cards**: Actionable error messages with diagnostics
- **Accessibility**: Full TalkBack support and adaptive layouts

## Important Implementation Notes

### Network Configuration
All HTTP clients are configured through `NetworkModule.provideOkHttpClient()` with:
- Multiple interceptors for auth, retry, and network state
- Optimized timeouts and connection pooling
- Comprehensive error handling

### URL Format Handling
Always use `UrlUtils.formatUrl()` when processing user input:
```kotlin
val formattedUrl = UrlUtils.formatUrl(userInput)
```

### Error Handling Pattern
Use `ErrorHandlingUtils.safeApiCall()` for all network operations:
```kotlin
ErrorHandlingUtils.safeApiCall(application) {
    apiService.someCall()
}.collect { result ->
    result.fold(
        onSuccess = { /* handle success */ },
        onFailure = { /* handle error */ }
    )
}
```

### ViewModel Pattern
All ViewModels follow the same pattern with factory pattern:
```kotlin
companion object {
    val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
            return SomeViewModel(application) as T
        }
    }
}
```

### Network State Monitoring
Always check network state before making requests:
```kotlin
if (!networkMonitor.isConnected.value) {
    // Show network error
    return
}
```

## Testing and Quality

### Current Test Coverage
- Unit tests for core utilities
- Network connectivity testing
- URL validation and formatting tests

### Build Quality Checks
```bash
./gradlew test      # Run unit tests
./gradlew lint       # Code quality checks
./gradlew assembleDebug # Verify debug build
```

## Common Issues and Solutions

### URL Format Errors
- **Issue**: "应为URL方案'http'或'https'，但找不到j.yzfy的方案"
- **Solution**: Use `UrlUtils.formatUrl()` to auto-add protocol prefixes

### Network Timeouts
- **Issue**: Connection timeouts on slow networks
- **Solution**: Configure timeouts in `NetworkModule` and use retry interceptor

### Authentication Failures
- **Issue**: Token expiration or invalid credentials
- **Solution**: Implement token refresh logic in `AuthInterceptor`

## Development Guidelines

### Adding New API Endpoints
1. Add interface method to `AListApiService`
2. Create corresponding data models in `data/model/`
3. Implement repository pattern in relevant ViewModel
4. Add error handling and network validation

### UI Development
- Use Jetpack Compose for all new UI components
- Follow Material3 design guidelines
- Implement proper accessibility support
- Handle different screen sizes with responsive layouts

### Network Operations
- Always use the configured network module
- Implement proper error handling
- Show network status to users
- Use appropriate timeouts and retry logic