# SmartRecipe – Complete Architecture, File Guide & Viva Preparation

## 1. End-to-End Architecture

```text
Android (Java + XML)
   ↓  Retrofit 2 + Gson + OkHttp (JWT Header)
Node.js + Express.js REST API (http://localhost:5000 / http://10.0.2.2:5000)
   ↓  mysql2/promise (Parameterized SQL Queries)
MySQL Database (smartrecipe_db @ localhost:3306)
```

---

## 2. Android Application (`SmartRecipe/`)

### A. Build & Manifest
- **`SmartRecipe/app/build.gradle`**: Configures Java 17, `compileSdk 37`, `minSdk 24`, `targetSdk 36`, and dependencies (`appcompat`, `material`, `recyclerview`, `cardview`, `swiperefreshlayout`, `retrofit2`, `converter-gson`, `okhttp3 logging-interceptor`, `glide`).
- **`SmartRecipe/app/src/main/AndroidManifest.xml`**: Declares `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_MEDIA_IMAGES`, `usesCleartextTraffic="true"` (for local HTTP Express dev server), launcher `SplashActivity`, and all 17 activities.

### B. Networking & Utilities (`java/com/example/smartrecipe/`)
- **`api/RetrofitClient.java`**: Singleton Retrofit client with OkHttp logging and an interceptor that reads the JWT token from `SessionManager` and attaches `Authorization: Bearer <token>`.
- **`api/ApiService.java`**: Declares every REST endpoint (`POST`, `GET`, `PUT`, `DELETE`, `@Multipart` image upload).
- **`api/ApiResponse.java`**: Generic response wrapper `{ success, message, data }`.
- **`utils/SessionManager.java`**: Stores logged-in user metadata (`id`, `name`, `email`, `phone`, `role`, `profile_image`, `jwt_token`) in `SharedPreferences` — never stores plain passwords.
- **`utils/Constants.java`**: Holds `BASE_URL` (`http://10.0.2.2:5000/`), intent keys, meal types, shopping units, and quick ingredient chips.
- **`utils/ValidationUtils.java` & `utils/NetworkUtils.java` & `utils/ImageUtils.java` & `utils/RecipeActionHelper.java`**: Input validation, connectivity & JSON error parsing, Glide image loading, and shared favorite-toggle helper.

### C. Models (`models/`)
`User.java`, `Category.java`, `Recipe.java`, `Ingredient.java`, `Favorite.java`, `MealPlan.java`, `ShoppingItem.java`, `Review.java`, `AdminStats.java`, `AuthResponse.java`.

### D. Activities (`activities/`) & Fragments (`fragments/`)
- **Onboarding & Auth:** `SplashActivity` (logo scale/fade animation + session check) → `LoginActivity` / `RegisterActivity` / `AdminLoginActivity`.
- **Main Hub:** `MainActivity` with `BottomNavigationView` (`HomeFragment`, `SearchFragment`, `FavoritesFragment`, `MealPlannerFragment`, `ProfileFragment`) and toolbar shortcuts (`ShoppingListActivity`, `MyRecipesActivity`).
- **Unique Feature – What Can I Cook?:** `WhatCanICookActivity` sends selected/typed ingredients to `POST /api/recipes/by-ingredients` and renders `match_percentage` + `missing_ingredients` via `WhatCanICookAdapter`.
- **Recipe Details & CRUD:** `RecipeDetailsActivity` (ingredients, instructions, reviews CRUD, meal planner modal, bulk add ingredients to shopping list), `MyRecipesActivity`, `AddEditRecipeActivity` (create/edit recipe + dynamic ingredients + image picker upload).
- **Planner, Shopping & Profile:** `MealPlannerFragment` (date filter + Breakfast/Lunch/Dinner/Snacks CRUD), `ShoppingListActivity` (items CRUD + purchased checkbox), `EditProfileActivity`, `ChangePasswordActivity`, `MyReviewsActivity`.
- **Admin Module:** `AdminDashboardActivity` (total users, recipes, categories, reviews, favorites), `AdminManageUsersActivity`, `AdminManageRecipesActivity`, `AdminManageCategoriesActivity`, `AdminManageReviewsActivity`.

---

## 3. MySQL Database (`database/smartrecipe_db.sql`)

Normalized 8-table schema (`smartrecipe_db`):
1. `users` (`id`, `name`, `email` UNIQUE, `phone`, `password` bcrypt hash, `role` ENUM, `profile_image`, `created_at`)
2. `categories` (`id`, `name` UNIQUE, `description`, `image`)
3. `recipes` (`id`, `user_id` FK, `category_id` FK, `name`, `description`, `image`, `preparation_time`, `cooking_time`, `servings`, `instructions`, `created_at`)
4. `ingredients` (`id`, `recipe_id` FK, `ingredient_name`, `quantity`, `unit`)
5. `favorites` (`id`, `user_id` FK, `recipe_id` FK, UNIQUE `(user_id, recipe_id)`)
6. `meal_plans` (`id`, `user_id` FK, `recipe_id` FK, `meal_date`, `meal_type` ENUM)
7. `shopping_list` (`id`, `user_id` FK, `item_name`, `quantity`, `unit`, `status` ENUM)
8. `reviews` (`id`, `user_id` FK, `recipe_id` FK, `rating` 1–5, `review_text`, `created_at`)

---

## 4. Node.js + Express Backend (`backend/`)

- **`server.js`**: Configures CORS, JSON body parsing, static `/uploads` serving, mounts all 10 route modules, checks MySQL health, and runs `ensureSeedUserPasswords()`.
- **`config/db.js`**: `mysql2/promise` connection pool reading credentials from `.env`.
- **`middleware/authMiddleware.js`**: `authenticateToken`, `optionalAuth`, and `requireAdmin` (restricts admin endpoints to `role === 'admin'`).
- **`middleware/uploadMiddleware.js`**: Multer disk storage for recipe images.
- **`controllers/`**: Business logic with parameterized SQL (`?`), transaction support (`beginTransaction`/`commit`/`rollback`) for recipe + ingredients writes, and ownership checks before update/delete.

---

## 5. Common Viva Questions & Answers

1. **Why use Retrofit instead of `HttpURLConnection`?**
   Retrofit maps REST endpoints to clean Java interfaces, handles background threading (`enqueue`), serializes/deserializes JSON automatically with Gson, and supports OkHttp interceptors for JWT headers.
2. **How does "What Can I Cook?" calculate match percentage?**
   `POST /api/recipes/by-ingredients` compares the user's available ingredient list against each recipe's rows in `ingredients`, computes `Math.round((matched_count / total_ingredients) * 100)`, collects `missing_ingredients`, and sorts descending by `match_percentage`.
3. **Why `http://10.0.2.2:5000/` in Android?**
   Inside the Android Emulator, `127.0.0.1` refers to the emulator itself; `10.0.2.2` is the special alias routed to the host PC's `localhost`.
4. **How is security handled?**
   Passwords are hashed on the backend with `bcryptjs`, plain passwords are never stored in Android `SharedPreferences`, SQL queries use `?` placeholders to prevent SQL injection, DB/JWT secrets live in `.env`, and admin routes enforce JWT + `role === 'admin'`.
