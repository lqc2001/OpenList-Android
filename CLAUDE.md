# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Status

**Hilt Dependency Injection**: ✅ **COMPLETED**
- Full Hilt integration with dependency injection
- All ViewModels, Repositories, and Activities updated
- Build successful with debug APK generation
- Documentation updated with Hilt patterns and troubleshooting

**Login Crash Fix**: ✅ **COMPLETED**
- Fixed SimpleFileViewModel constructor injection issue
- Added @HiltViewModel annotation and @Inject constructor
- Resolved dependency binding conflicts in Hilt modules
- App no longer crashes on login attempt

**Flow Exception Transparency Fix**: ✅ **COMPLETED**
- Fixed nullable data handling in API response models
- Updated FileListResponse, SearchResponse, FileLinkResponse, UploadResponse
- Added proper null checks in SimpleFileViewModel
- Eliminated flow transparency violations in error handling

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

### MVVM + Clean Architecture with Hilt
The project follows MVVM architecture with Clean Architecture principles and Hilt dependency injection:

```
Presentation Layer (Compose UI + ViewModels)
    ↓ (Hilt Dependency Injection)
Domain Layer (Use Cases + Business Logic)
    ↓ (Hilt Dependency Injection)
Data Layer (Repositories + Data Sources)
```

### Key Architectural Patterns

1. **Single Source of Truth**: Each data source has one authoritative provider
2. **Dependency Inversion**: High-level modules don't depend on low-level modules
3. **Reactive Programming**: Kotlin Flow for async data streams
4. **State Management**: StateFlow for UI state management
5. **Dependency Injection**: Hilt for compile-time dependency management

### Hilt Dependency Injection

The project uses Hilt for modern, type-safe dependency injection:

#### Core Components
- **@HiltAndroidApp**: Application class annotation for Hilt setup
- **@AndroidEntryPoint**: Activity/Fragment annotation for dependency injection
- **@HiltViewModel**: ViewModel annotation for constructor injection
- **@Module**: Hilt modules for providing dependencies
- **@Inject**: Constructor injection annotation

#### Hilt Modules
- **AppModule**: Provides Application, PreferencesRepository, OkHttpClient, Retrofit, AListApiService, AppDatabase, PlayHistoryDao
- **RepositoryModule**: Provides AuthRepository, FileRepository, PlayHistoryRepository

#### Dependency Graph
```
AListApplication (@HiltAndroidApp)
    ↓
MainActivity (@AndroidEntryPoint)
    ↓
ViewModels (@HiltViewModel)
    ↓
Repositories (@Inject)
    ↓
Data Sources (API, Database, Preferences)
```

### Core Modules Structure

#### Dependency Injection (`di/`)
- **AppModule.kt**: Provides Application-level dependencies (Network, Database, Preferences)
- **RepositoryModule.kt**: Provides Repository instances for data operations
- **Hilt Components**: Singleton, ActivityRetained, Activity, Fragment scopes

#### Network Layer (`data/network/`)
- **NetworkModule.kt**: OkHttp/Retrofit configuration with optimized timeouts
- **NetworkConnectivityMonitor.kt**: Real-time network status monitoring
- **RetryInterceptor.kt**: Automatic request retry with exponential backoff
- **NetworkStateInterceptor.kt**: Network state validation and error handling
- **AuthInterceptor.kt**: Bearer token authentication

#### Repository Layer (`data/repository/`)
- **AuthRepository.kt**: Authentication operations with Hilt injection
- **FileRepository.kt**: File operations with Hilt injection
- **PlayHistoryRepository.kt**: Play history management with Hilt injection

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

### ViewModel Pattern with Hilt
All ViewModels use Hilt for dependency injection, eliminating the need for manual factories:
```kotlin
@HiltViewModel
class SimpleAuthViewModel @Inject constructor(
    val application: Application,
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel()
```

### Repository Pattern with Hilt
Repositories use constructor injection for their dependencies:
```kotlin
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: AListApiService,
    private val preferencesRepository: PreferencesRepository
) {
    // Repository implementation
}
```

### Activity/Fragment Pattern
Activities and Fragments use `@AndroidEntryPoint` for Hilt injection:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // ViewModels automatically injected via Hilt
    val viewModel: SimpleAuthViewModel by viewModels()
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

### Hilt Dependency Injection Issues
- **Issue**: "Dependency cycle detected" or "Duplicate bindings"
- **Solution**: Avoid providing Application instance manually in Hilt modules
- **Issue**: "Hilt components must be annotated with @HiltAndroidApp"
- **Solution**: Ensure Application class has proper @HiltAndroidApp annotation
- **Issue**: "ViewModel cannot be provided without @HiltViewModel"
- **Solution**: Add @HiltViewModel annotation and @Inject constructor to ViewModels
- **Issue**: "Activity must be annotated with @AndroidEntryPoint"
- **Solution**: Add @AndroidEntryPoint annotation to Activities/Fragments using Hilt

### Build Issues After Hilt Integration
- **Issue**: Kapt compilation failures with Hilt
- **Solution**: Ensure kapt dependencies are correctly configured and Kotlin version compatibility
- **Issue**: "Unresolved reference: map" in Repository classes
- **Solution**: Add proper imports for Kotlin Flow operators (`kotlinx.coroutines.flow.map`)
- **Issue**: Factory pattern conflicts after Hilt migration
- **Solution**: Remove manual Factory companion objects from ViewModels

### Runtime Issues
- **Issue**: App crashes on login with "NoSuchMethodException: SimpleFileViewModel.<init>[]"
- **Solution**: Add @HiltViewModel annotation and @Inject constructor to ViewModels; remove manual Factory patterns
- **Issue**: "Cannot create an instance of class com.openlist.android.ui.viewmodel.SimpleFileViewModel"
- **Solution**: Ensure all dependencies are properly provided in Hilt modules and ViewModel uses constructor injection
- **Issue**: "Duplicate bindings" errors during compilation
- **Solution**: Remove duplicate @Provides methods from different Hilt modules; keep only one provider per type

### Flow Exception Transparency Issues
- **Issue**: "Flow exception transparency is violated" errors in API calls
- **Solution**: Ensure API response data models use nullable types for server responses that may be null
- **Issue**: "NullPointerException: Attempt to invoke virtual method 'getContent()' on a null object reference"
- **Solution**: Add proper null checks before accessing nested data fields in API responses
- **Pattern**: Always check for null both at response level and data level:
```kotlin
if (response != null && response.data != null) {
    // Safe to access response.data.content
} else {
    // Handle null case appropriately
}
```

## Development Guidelines

### Adding New Features with Hilt
1. **Create ViewModels**: Use `@HiltViewModel` and constructor injection
2. **Create Repositories**: Use `@Inject` constructor and add to `RepositoryModule`
3. **Add Dependencies**: Add new dependencies to appropriate Hilt module
4. **Update UI**: Use `@AndroidEntryPoint` for Activities/Fragments
5. **Add Test Cases**: Leverage Hilt's testability for unit testing

### Adding New API Endpoints
1. Add interface method to `AListApiService`
2. Create corresponding data models in `data/model/`
3. Implement repository pattern with Hilt injection
4. Add error handling and network validation
5. Update ViewModel to use injected repository

### UI Development
- Use Jetpack Compose for all new UI components
- Follow Material3 design guidelines
- Implement proper accessibility support
- Handle different screen sizes with responsive layouts
- Use `@AndroidEntryPoint` for dependency injection

### Network Operations
- Always use the configured network module via Hilt injection
- Implement proper error handling
- Show network status to users
- Use appropriate timeouts and retry logic
- Leverage Hilt's singleton scoping for shared dependencies
- Handle nullable API responses safely to prevent crashes

### Data Model Best Practices
- Always make API response data fields nullable (`?`) to handle server inconsistencies
- Add null checks before accessing nested data fields in responses
- Use safe call operators (`?.`) when accessing potentially null data
- Provide meaningful error messages when data is unexpectedly null
- Follow the pattern: "check response null, then check data null, then access fields"

### Testing with Hilt
- Use Hilt's testing infrastructure for unit tests
- Mock dependencies using Hilt's `@UninstallModules`
- Test ViewModels with injected mock repositories
- Use `@HiltAndroidTest` for integration tests