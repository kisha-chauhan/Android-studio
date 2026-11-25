
# SmartRecipe – Smart Recipe & Meal Planning Application

A complete native **Android (Java + XML)** application powered by a **Node.js + Express.js REST API** and **MySQL (`smartrecipe_db`)** database.

## Architecture

```text
Android Java + XML  →  Retrofit 2 + Gson  →  Node.js + Express.js REST API  →  MySQL (smartrecipe_db)
```

## Project Structure

- `SmartRecipe/` — Native Android Studio project (`com.example.smartrecipe`)
- `backend/` — Node.js + Express.js REST API server (`config/`, `controllers/`, `routes/`, `models/`, `middleware/`, `uploads/`)
- `database/smartrecipe_db.sql` — Normalized MySQL schema + 9 categories + 16 sample recipes
- `postman/SmartRecipe_Postman_Collection.json` — Ready-to-import Postman API test suite

## Setup & Run Instructions

### 1. MySQL Database (`smartrecipe_db`)
1. Start **MySQL** in XAMPP Control Panel (installed at `C:\xampp`).
2. Open phpMyAdmin (`http://localhost/phpmyadmin`) or run MySQL CLI:
   ```bat
   C:\xampp\mysql\bin\mysql.exe -u root < c:\Android-studio\database\smartrecipe_db.sql
   ```

### 2. Node.js + Express.js Backend
1. Install Node.js LTS (v18+) if not already installed (`https://nodejs.org/`).
2. Configure `c:\Android-studio\backend\.env` (default uses XAMPP MySQL `root` with empty password on port `3306`, server port `5000`).
3. Install dependencies and start the API server:
   ```bat
   cd c:\Android-studio\backend
   npm install
   npm start
   ```
4. Verify API in browser or Postman: `http://localhost:5000/`

### 3. Android Studio Project (`SmartRecipe/`)
1. Open `c:\Android-studio\SmartRecipe` in **Android Studio**.
2. Base URL in `Constants.java` is preconfigured as `http://10.0.2.2:5000/` for Android Emulator (for a physical device over Wi-Fi, update `Constants.BASE_URL` to your PC's local IP).
3. Run the app on an Android Emulator or build the APK (`./gradlew assembleDebug`).

## Default Sample Accounts

| Role | Email | Password |
| :--- | :--- | :--- |
| **Admin** | `admin@smartrecipe.com` | `Admin@123` |
| **User** | `aarav@smartrecipe.com` | `User@123` |
| **User** | `priya@smartrecipe.com` | `User@123` |
