# 🌦️ FossyWeather

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![API Keys](https://img.shields.io/badge/API%20Keys-Zero-brightgreen.svg)]()
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/your-username/FossyWeather/pulls)

A privacy-first, completely free, and open-source (FOSS) weather application built with **Jetpack Compose**. FossyWeather strictly adheres to **Material Design 3** guidelines, features dynamic weather effects, marine/tide modeling, and requires **zero API keys** by leveraging open data sources.

---

## ✨ Features Breakdown

### 🌤️ Core Weather Engine
* **Zero API Key Setup:** Powered by the [Open-Meteo API](https://open-meteo.com/) for forecast, marine, and geocoding data out of the box.
* **Comprehensive Metrics:** Tracks real-time temperature, "feels like" (apparent) temperature, humidity, atmospheric pressure (MSL), wind speed/direction/gusts, UV index, visibility, and cloud cover.
* **Marine & Coastal Data:** Specialized modeling for wave height, wave period, sea surface temperature, and a **72-hour modeled tide height estimate** for coastal locations.
* **Offline Astronomy Engine:** Calculates moon phases (name, age, illumination percentage, next full/new moon) and precise local sunrise/sunset times directly on-device without network calls.

---

### 🎨 Advanced UI & UX (Material Design 3)
* **Material You Dynamic Theming:** Automatically adapts UI colors based on the user's Android 12+ wallpaper, with native support for Light, Dark, and System theme modes.
* **Dynamic Weather FX (Canvas-based):**
  * 🌧️ **Rain:** Falling droplets with dynamic wind-reactive skew.
  * ❄️ **Snow:** Drifting flakes with randomized sizes and lateral drift speeds.
  * ☁️ **Cloudy:** Parallax scrolling cloud layers.
  * ☀️ **Clear:** Rotating sun with pulsing animated light rays.
* **Fluid Motion & Transitions:** Staggered entry animations for home dashboard elements and full slide-and-fade global page transitions.

---

### 📍 Location Management & Navigation
* **Real-time GPS Tracking:** Integrates `FusedLocationProviderClient` for quick and precise "My Location" updates.
* **Worldwide Multi-city Storage:** Persistent city bookmarking backed by Jetpack DataStore.
* **Interactive Navigation Drawer:** Material 3 `ModalNavigationDrawer` featuring fast switching between saved locations, location adding, and swipe-to-delete with double-confirmation prompts.

---

### 📊 Interactive Data Visualization
* **Custom Line Charts:** High-performance line charts rendered via `drawWithCache` to plot 24-hour pressure trends and 72-hour tide cycles.
* **Temperature Range Bars:** Visual temperature spread bars embedded inside daily forecasts to highlight weekly highs and lows relative to the current temperature.
* **Deep-Dive Breakdown Screens:** Every main card (Hourly, Daily, Moon, Tide, UV) is interactive—tap any module to open a dedicated full-screen analytical view.

---

### 📱 Home Screen Widgets (Jetpack Glance)
* **Glance Engine:** Battery-efficient, reliable native Android widgets.
* **Multiple Layout Modes:** Current condition cards and multi-day preview widgets.
* **Dynamic Customization:** Deep integration with app state—customizable data density (toggle visibility for UV, Humidity, Pressure, etc. from app settings).

---

### 🛠️ Unique Utilities
* **Direct APK Sharing:** Built-in "Support & Share" tool allowing users to share the `FossyWeather.apk` file directly to nearby devices via Android `FileProvider`.
* **One-Tap Weather Summary:** Generate and send friendly text-based weather summaries instantly to friends and family.
* **Notification Engine:** Built-in infrastructure for scheduled morning weather forecasts and emergency high-priority weather alerts.

---

## 🏗️ Technical Architecture

FossyWeather follows modern Android architecture best practices (**MVVM + Clean Architecture**), separating data sources, domain business logic, and Compose UI views.

| Layer / Aspect | Technology Used |
| :--- | :--- |
| **Language** | 100% Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Asynchronous Core** | Kotlin Coroutines & `StateFlow` / `SharedFlow` |
| **Networking** | Retrofit 2 + OkHttp + KotlinX Serialization |
| **Persistence** | Jetpack DataStore (Preferences) |
| **Widgets** | Jetpack Glance |
| **Maps** | `osmdroid` (fully open-source map tiles) |
| **Location** | Google Play Services - Location (`FusedLocationProviderClient`) |

---

## 📥 How to Compile

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/FossyWeather.git
   cd FossyWeather
   ```

2. **Open in Android Studio:**
   Open the root directory in Android Studio (Ladybug / Hedgehog or newer recommended).

3. **Build & Run:**
   No API keys required! Simply sync Gradle and click **Run** (`Shift + F10`) on your device or emulator running Android 12 (API 31) or higher.

   Or Download Precompiled From [Releases](https://github.com/qoij69/FossyWeather/releases) 

---

## 🤝 Contributing

Contributions are warmly welcomed! Whether you want to fix a bug, add a new weather effect, improve accessibility, or translate the app:

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📜 License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. 

Permissions of this strong copyleft license are conditioned on making available complete source code of licensed works and modifications, which include larger works using a licensed work under the same license. See the [LICENSE](LICENSE) file for details.

---

<p align="center">
Made with ❤️, Kotlin & Jetpack Compose
</p>
