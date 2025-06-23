# WeatherTrack

A simple Android weather tracking application that helps users monitor daily weather statistics in their city with automatic background syncing and comprehensive weekly summaries.

## 📱 Features

### Core Functionality
- **Real-time Weather Data**: Fetches current weather information including temperature, humidity, wind speed, and weather conditions
- **Automatic Background Sync**: Intelligently syncs weather data every 6 hours using WorkManager
- **Weekly Weather History**: View temperature trends and weather patterns over the past 7 days
- **Location-Based Weather**: Automatically detects user location or falls back to saved/default locations
- **Offline-First Architecture**: Stores all weather data locally for offline access

### User Interface
- **Modern Material Design**: Clean, intuitive interface with dark theme optimized for readability
- **Interactive Weather Cards**: Detailed weather information presented in easy-to-read cards
- **Swipe-to-Refresh**: Manual refresh capability with pull-to-refresh gesture
- **Historical Data Visualization**: Weekly weather history with clickable daily entries
- **Detailed Weather Views**: Tap any historical day to view comprehensive weather details

## 🏗️ Architecture & Technology Stack

### Architecture Pattern
- **MVVM (Model-View-ViewModel)**: Clear separation between UI logic and business logic
- **Clean Architecture**: Organized into presentation, domain, and data layers
- **Repository Pattern**: Centralized data access with single source of truth

### 🎯 Clean Architecture Implementation

This application follows **Clean Architecture principles** with clear separation of concerns across three main layers:

#### **📱 Presentation Layer** (UI & ViewModels)
- **Responsibility**: Handles user interactions and UI state management
- **Components**:
  - `MainActivity`, `WeatherDetailActivity` - UI controllers
  - `MainActivityViewModel` - State management and UI logic
  - `WeatherForecastAdapter` - RecyclerView data binding
  - View Binding classes for type-safe UI references
- **Dependencies**: Only depends on Domain layer (Repository interfaces)
- **Data Flow**: Observes LiveData from ViewModels, displays data to users

#### **🔧 Domain Layer** (Business Logic & Interfaces)
- **Responsibility**: Contains business rules and defines contracts
- **Components**:
  - `WeatherRepository` interface - Defines data access contracts
  - Business logic for weather data processing
  - Data transformation rules
- **Independence**: No dependencies on external frameworks
- **Purpose**: Acts as a bridge between Presentation and Data layers

#### **💾 Data Layer** (Data Sources & Implementation)
- **Responsibility**: Manages data from multiple sources (API, Database, Cache)
- **Components**:
  - `WeatherRepositoryImpl` - Repository pattern implementation
  - `ApiService` - Remote data source (OpenWeatherMap API)
  - `WeatherDao`, `WeatherDatabase` - Local data source (Room)
  - `WeatherEntity` - Local data models
  - Data mapping between network and local models
- **Data Sources**: 
  - **Remote**: REST API using Retrofit
  - **Local**: Room database for offline storage
  - **Cache**: SharedPreferences for location data

#### **🔄 Clean Architecture Benefits in WeatherTrack**:
1. **Testability**: Each layer can be tested independently
2. **Maintainability**: Changes in one layer don't affect others
3. **Scalability**: Easy to add new features or data sources
4. **Separation of Concerns**: Each layer has a single responsibility
5. **Dependency Inversion**: Higher layers don't depend on lower layers

#### **📊 Data Flow in Clean Architecture**:
```
User Action → Activity → ViewModel → Repository Interface → Repository Implementation → API/Database
                ↓
         LiveData ← ViewModel ← Repository Interface ← Repository Implementation ← Response/Data
                ↓
            UI Update
```

#### **🏛️ Layer Dependencies**:
```
Presentation Layer (Activities, ViewModels)
        ↓ (depends on)
Domain Layer (Repository Interfaces, Use Cases)
        ↓ (implemented by)
Data Layer (Repository Implementation, API, Database)
```

**Key Principle**: Inner layers (Domain) don't know about outer layers (Presentation), maintaining clean separation and enabling easy testing and modification.

### 🛠️ Technologies Used

#### **1. Dependency Injection**
- **Dagger Hilt**: Used throughout the application for dependency injection
  - `@HiltAndroidApp` in `MyApplication` class
  - `@AndroidEntryPoint` in Activities (MainActivity, WeatherDetailActivity)
  - `@HiltViewModel` in ViewModels
  - Module classes: `DataModule`, `NetworkModule`, `RepositoryModule`
  - **Purpose**: Manages object creation and dependencies automatically

#### **2. Database & Local Storage**
- **Room Persistence Library**: Local database management
  - `WeatherDatabase`: Main database class with TypeConverters
  - `WeatherDao`: Database access object with complex queries
  - `WeatherEntity`: Entity class with JSON serialization for complex objects
  - **Usage**: Stores weather history, enables offline access, manages data persistence

#### **3. Networking**
- **Retrofit**: HTTP client for API communication
  - `ApiService`: Interface defining weather API endpoints
  - **Base URL**: `https://api.openweathermap.org/data/2.5/`
  - **Converter**: Gson for JSON serialization/deserialization
- **OkHttp**: HTTP client with logging interceptor
  - Connection timeouts and retry mechanisms
  - Request/response logging for debugging

#### **4. Reactive Programming & Data Binding**
- **LiveData**: Observable data holder for reactive UI updates
  - **In ViewModels**: `MainActivityViewModel` exposes weather data as LiveData
  - **In Repository**: Returns API responses wrapped in LiveData
  - **In Activities**: Observes data changes and updates UI automatically
  - **Usage Examples**:
    ```java
    // ViewModel
    public LiveData<Resource<WeatherResponse>> getCurrentWeatherData()
    
    // Activity
    viewModel.getCurrentWeatherData().observe(this, resource -> {
        // Update UI based on resource state
    });
    ```

#### **5. Background Processing**
- **WorkManager**: Reliable background task scheduling
  - `WeatherSyncWorker`: Periodic weather data fetching (every 6 hours)
  - `WeatherWorkScheduler`: Manages work scheduling and constraints
  - `WeatherSyncWorkerFactory`: Custom worker factory with Hilt integration
  - **Constraints**: Network connectivity, battery not low
  - **Persistence**: Survives app kills and device reboots

#### **6. Location Services**
- **Google Play Services Location API**: GPS and location management
  - `LocationHelper`: Wrapper class for location operations
  - `FusedLocationProviderClient`: Efficient location updates
  - **Features**: Current location detection, permission handling
- **LocationPreferences**: SharedPreferences wrapper for location caching

#### **7. UI & View Binding**
- **View Binding**: Type-safe view references
  - Enabled in all activities and fragments
  - Eliminates findViewById calls
  - **Usage**: `ActivityMainBinding`, `ActivityWeatherDetailBinding`
- **Material Design Components**: Modern UI elements
  - SwipeRefreshLayout, FloatingActionButton, MaterialButton
  - CardView with custom backgrounds and styling

#### **8. Data Models & Serialization**
- **Gson**: JSON parsing and object serialization
  - Weather API response models: `WeatherResponse`, `Main`, `Weather`, `Wind`, etc.
  - Room TypeConverters for complex object storage
- **Custom Resource Wrapper**: Handles API response states
  - `Resource<T>` class with SUCCESS, ERROR, LOADING states
  - Unified error handling across the app

#### **9. Error Handling & Utilities**
- **Custom Error Management**: User-friendly error messages
  - Network error detection and handling
  - API error code interpretation (401, 404, 500, etc.)
  - Database operation error handling
- **Utility Classes**:
  - `NetworkUtils`: Internet connectivity checking
  - `LocationPreferences`: Location data persistence
  - `Constants`: App-wide configuration constants

### 📦 Project Structure
```
app/src/main/java/com/example/myapplication/
├── 📁 common/
│   ├── Constants.java (API keys, default coordinates)
│   ├── MyApplication.java (@HiltAndroidApp)
│   ├── Resource.java (Response wrapper)
│   ├── 📁 utils/
│   │   ├── LocationHelper.java (GPS operations)
│   │   ├── LocationPreferences.java (Location caching)
│   │   └── NetworkUtils.java (Connectivity checks)
│   └── 📁 worker/
│       ├── WeatherSyncWorker.java (@HiltWorker)
│       ├── WeatherSyncWorkerFactory.java
│       └── WeatherWorkScheduler.java
├── 📁 data/
│   ├── 📁 local/
│   │   ├── WeatherDatabase.java (Room database)
│   │   ├── WeatherDao.java (Database queries)
│   │   ├── WeatherEntity.java (Room entity)
│   │   └── 📁 converter/
│   │       └── WeatherTypeConverter.java (JSON converters)
│   ├── 📁 model/
│   │   ├── WeatherResponse.java (API response model)
│   │   ├── Main.java, Weather.java, Wind.java, etc.
│   ├── 📁 network/
│   │   └── ApiService.java (Retrofit interface)
│   └── 📁 repository/
│       └── WeatherRepositoryImpl.java (Repository implementation)
├── 📁 di/
│   ├── DataModule.java (Database dependencies)
│   ├── NetworkModule.java (Retrofit, OkHttp)
│   └── RepositoryModule.java (Repository binding)
├── 📁 domain/
│   └── 📁 repository/
│       └── WeatherRepository.java (Repository interface)
└── 📁 presentation/
    ├── 📁 mainActivity/
    │   ├── MainActivity.java (@AndroidEntryPoint)
    │   ├── MainActivityViewModel.java (@HiltViewModel)
    │   └── WeatherForecastAdapter.java (RecyclerView)
    └── 📁 weatherDetailActivity/
        └── WeatherDetailActivity.java (@AndroidEntryPoint)
```

### 🔄 Data Flow Architecture

1. **User Action** → Activity → ViewModel
2. **ViewModel** → Repository (via LiveData)
3. **Repository** → API Service (Retrofit) + Local Database (Room)
4. **Response** → Repository → ViewModel (LiveData emission)
5. **ViewModel** → Activity (LiveData observation)
6. **Activity** → UI Update (View Binding)

### 🎯 Key Implementation Details

#### **LiveData Usage Patterns**
```java
// Repository returns LiveData
@Override
public LiveData<Resource<WeatherResponse>> getCurrentWeatherByCoordinates(double lat, double lon) {
    weatherLiveData.setValue(Resource.loading());
    // API call and database operations
    return weatherLiveData;
}

// ViewModel exposes LiveData
public LiveData<Resource<WeatherResponse>> getCurrentWeatherData() {
    return weatherRepository.getCurrentWeatherByCoordinates(lat, lon);
}

// Activity observes changes
viewModel.getCurrentWeatherData().observe(this, resource -> {
    switch (resource.getStatus()) {
        case LOADING: showLoading(true); break;
        case SUCCESS: updateWeatherUI(resource.getData()); break;
        case ERROR: showError(resource.getMessage()); break;
    }
});
```

#### **Hilt Dependency Injection Setup**
```java
// Application class
@HiltAndroidApp
public class MyApplication extends Application { }

// Activity
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity { }

// ViewModel
@HiltViewModel
public class MainActivityViewModel extends ViewModel {
    @Inject
    public MainActivityViewModel(WeatherRepository repository) { }
}

// Modules
@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    @Provides
    @Singleton
    public static Retrofit provideRetrofit(OkHttpClient client) { }
}
```

#### **Room Database with TypeConverters**
```java
// Entity with complex objects
@Entity(tableName = "weather_cache")
@TypeConverters(WeatherTypeConverter.class)
public class WeatherEntity {
    public List<Weather> weather; // Stored as JSON
    public Main main;             // Stored as JSON
    public Wind wind;             // Stored as JSON
}

// TypeConverter for JSON serialization
public class WeatherTypeConverter {
    @TypeConverter
    public static String fromWeatherList(List<Weather> weatherList) {
        return gson.toJson(weatherList);
    }
}
```

#### **WorkManager Background Sync**
```java
// Worker with Hilt injection
@HiltWorker
public class WeatherSyncWorker extends Worker {
    @AssistedInject
    public WeatherSyncWorker(@Assisted Context context, 
                           @Assisted WorkerParameters params,
                           ApiService apiService, 
                           WeatherDao weatherDao) { }
}

// Scheduling periodic work
PeriodicWorkRequest weatherSyncRequest = new PeriodicWorkRequest.Builder(
    WeatherSyncWorker.class, 6, TimeUnit.HOURS)
    .setConstraints(constraints)
    .build();
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or newer
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 35 (Android 15)
- Java 8+ compatibility

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator

 ## 📱 How to Use

### First Launch
1. **Grant Permissions**: Allow location access for accurate weather data
2. **Automatic Setup**: App fetches current weather for your location
3. **Background Sync**: Automatic weather updates begin immediately

### Daily Usage
1. **View Current Weather**: Main screen displays comprehensive current conditions
2. **Manual Refresh**: Swipe down or tap the refresh FAB to update immediately
3. **Historical Data**: Scroll down to view the past 7 days of weather history
4. **Detailed Views**: Tap any historical day to see complete weather information

## 🛠️ Error Handling

### Network Errors
- **No Internet**: "No internet connection available" with retry option
- **API Failures**: Specific error messages based on response codes (401, 404, 500, etc.)
- **Timeout Issues**: "Connection timed out. Please try again"

### Location Errors
- **Permission Denied**: Falls back to last known or default location (Bhopal, India)
- **GPS Unavailable**: Uses cached location data when possible
- **Invalid Coordinates**: Validates coordinate ranges and provides error feedback

### Database Errors
- **Storage Issues**: Graceful handling of database write failures
- **Data Corruption**: Automatic cleanup and recovery mechanisms

## 🔮 Future Enhancements

- **Weather Alerts**: Push notifications for severe weather conditions
- **Multiple Locations**: Support for tracking weather in multiple cities
- **Extended Forecasts**: 14-day weather predictions
- **Weather Maps**: Interactive radar and satellite imagery

## 📄 License

This project is licensed under the MIT License.

---

**WeatherTrack** - Stay informed about your local weather with intelligent tracking and beautiful visualizations. 🌤️
