# FossyWeather

A full Material You (Material 3) weather app for **Android 12+ (API 31+)**, built with Kotlin and
Jetpack Compose. It uses only free, open, no-API-key data sources — no Google Play Services
dependency for location, no paid weather API.

## Features

- **Material You dynamic color** — the whole UI recolors itself from the user's wallpaper
  (native to Android 12+, so it's always available on the app's minimum supported OS version).
- **Location-based weather** — uses Android's built-in `LocationManager` (GPS + network), not
  Google Play Services, so it runs on de-Googled devices too. Falls back to manual city search.
- **Current conditions**: temperature, feels-like, humidity, pressure (+ trend chart & rising/
  falling/steady read), wind speed/gusts/direction, UV index, visibility, cloud cover.
- **24-hour and 8-day forecast** with precipitation probability.
- **Moon phase card** — phase name, % illumination, next full/new moon — computed **entirely
  on-device** with a standard synodic-month algorithm (no network call, always available).
- **Sunrise / sunset** times.
- **Tide & sea card** — wave height, sea-surface temperature, and a modeled sea-level height
  (tide) chart for coastal locations, pulled from Open-Meteo's Marine API. Automatically hides
  itself for inland locations.
- **Map screen** — OpenStreetMap via `osmdroid` (fully FOSS, no API key), with a togglable live
  precipitation radar overlay from RainViewer's free public API.
- Unit toggle (°C/°F), persisted with DataStore.

## Data sources (all free / keyless)

| Data | Source |
|---|---|
| Current + hourly + daily forecast, pressure, UV, wind | [Open-Meteo Forecast API](https://open-meteo.com) |
| City search / geocoding | Open-Meteo Geocoding API |
| Wave height, sea temp, modeled tide height | Open-Meteo Marine API |
| Moon phase | Local astronomical calculation (no API) |
| Base map tiles | OpenStreetMap (via osmdroid) |
| Precipitation radar overlay | [RainViewer](https://www.rainviewer.com/api.html) public API |

**Note on tides**: Open-Meteo's Marine API derives `sea_level_height_msl` from a global tidal
model. It's a good approximation for open coastlines but isn't a substitute for an official local
tide table (e.g. for harbor navigation) — the app shows this disclaimer in the UI too.

## Project structure

```
app/src/main/java/com/fossyfriend/fossyweather/
├── data/
│   ├── remote/         Retrofit API interfaces + DTOs (OpenMeteoApi, GeocodingApi)
│   ├── repository/     WeatherRepository — combines forecast + marine calls
│   ├── location/       LocationProvider — plain Android LocationManager wrapper
│   └── prefs/          DataStore-backed UserPreferences (units, last location)
├── domain/              Pure Kotlin: MoonPhaseCalculator, WeatherCode mapping, models
├── viewmodel/           WeatherViewModel (UiState: Loading/NeedsPermission/Error/Success)
├── ui/
│   ├── theme/           Material You dynamic color theme
│   ├── screens/         HomeScreen, MapScreen, SettingsScreen, LocationSearchSheet
│   ├── components/      CurrentWeatherCard, HourlyForecastRow, DailyForecastList,
│   │                    DetailsGrid, MoonPhaseCard, PressureTrendCard, TideCard
│   └── nav/              Navigation graph
└── util/                Formatting helpers
```

## Building it

This was generated as source only (no Android SDK/emulator available in this environment), so
you'll build it locally:

1. Open the `FossWeather/` folder in **Android Studio (Koala+ recommended)**.
2. Android Studio will notice there's no Gradle wrapper jar (it couldn't be downloaded from this
   sandbox) and offer to generate one automatically on first sync — accept that prompt, or run
   `gradle wrapper` yourself if you have Gradle installed locally.
3. Let Gradle sync — it will pull dependencies from Google/Maven Central automatically.
4. Run on a device or emulator running **Android 12 (API 31) or higher**.
5. Grant location permission when prompted (or use the search icon to pick a city manually).

No API keys, `local.properties` secrets, or paid accounts are required anywhere in this project.

## Possible next steps

- Add a home-screen widget (Glance API) showing current conditions.
- Cache the last successful `WeatherBundle` in Room for full offline viewing.
- Swap in a dedicated tide-table API (e.g. a national hydrographic service) for harbor-accurate
  tide predictions where available.
- Add weather alerts/notifications via WorkManager background refresh.
