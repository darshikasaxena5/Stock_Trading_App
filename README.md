# 📈 Stock Trading App

A modern stock trading application built using **Jetpack Compose**, integrating the **AlphaVantage API** for real-time stock data, featuring a sleek UI, offline support, and efficient data handling with **Room**, **MVVM architecture**, and **repository pattern**. Dagger Hilt powers the dependency injection for cleaner, testable code.

---

## 📑 Table of Contents

- [Features](#-features)
- [Installation](#-installation)
- [Usage](#-usage)
- [Architecture](#-architecture)
- [API and Data](#-api-and-data)
- [Configuration](#-configuration)
- [Troubleshooting](#-troubleshooting)
- [Contributors](#-contributors)
- [License](#-license)

---

## ✨ Features

- 🔍 **Explore Screen**: Search and browse stocks with pagination.
- 📋 **Watchlist Screen**: Manage your favorite stocks (add/remove).
- 🌑 **Theme Toggle**: Switch between Light and dark modes.
- 🚀 **Real-Time Data**: Integrated with [AlphaVantage API](https://www.alphavantage.co/) for live stock quotes.
- 🔐 **Header Spoofing**: Added header customization for API requests.
- 💾 **Room Database**: Local caching for offline access.
- ⚙️ **Dagger Hilt**: For dependency injection.
- ♻️ **MVVM with Repository Pattern**: Scalable and testable architecture.
- 📱 **Jetpack Compose UI**: Fully modern declarative UI approach.


---

## 🛠 Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/stock-trading-app.git
   cd stock-trading-app

2.Open the project in Android Studio.

3.Add your AlphaVantage API key in the local.properties file:
ALPHA_VANTAGE_API_KEY=your_api_key_here

4.	Sync the project and run it on an emulator or physical device.
> This version keeps proper spacing and syntax highlighting so GitHub renders it cleanly. Let me know if you'd like to replace `yourusername` with your GitHub handle!

▶️ Usage
	•	Use the Explore tab to search for stock symbols.
	•	Add any stock to your Watchlist by tapping the add button.
	•	Remove stocks from Watchlist with the remove option.
	•	Toggle between light/dark theme via the switch in the top app bar.

🏗 Architecture
	•	MVVM (Model-View-ViewModel)
	•	Repository Pattern
	•	Dagger Hilt for Dependency Injection
	•	Room for local database
	•	Jetpack Compose for UI
	•	Paging 3 for stock listing

📡 API and Data
	•	AlphaVantage API
	•	Endpoints used: TIME_SERIES_INTRADAY, SYMBOL_SEARCH, etc.
	•	API Docs
	•	Header Spoofing: Custom headers added for improved compatibility.

⚙️ Configuration

You may configure your API key and other build constants in a secure file such as:
object Config {
    const val API_KEY = BuildConfig.ALPHA_VANTAGE_API_KEY
}
Avoid hardcoding sensitive data directly into source files.

🐞 Troubleshooting
	•	API Limit Errors: AlphaVantage has rate limits (5 calls/minute). Use caching and delay logic.
	•	Empty Watchlist: Ensure Room DB is not being cleared unexpectedly.
	•	App Crash on Launch: Check if API key is missing or malformed.

👨‍💻 Contributors
	•	Darshika Saxena – Developer & Architect


