-- ============================================================
-- SmartRecipe – Smart Recipe & Meal Planning Application
-- Normalized MySQL Database Schema & Sample Data
-- Database Name: smartrecipe_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS smartrecipe_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE smartrecipe_db;

-- Drop tables in reverse dependency order for clean re-import
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS shopping_list;
DROP TABLE IF EXISTS meal_plans;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS ingredients;
DROP TABLE IF EXISTS recipes;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- 1. USERS TABLE
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    profile_image VARCHAR(500) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. CATEGORIES TABLE
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT '',
    image VARCHAR(500) DEFAULT ''
) ENGINE=InnoDB;

-- 3. RECIPES TABLE
CREATE TABLE recipes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    name VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    image VARCHAR(500) DEFAULT '',
    preparation_time INT NOT NULL DEFAULT 10,
    cooking_time INT NOT NULL DEFAULT 20,
    servings INT NOT NULL DEFAULT 2,
    instructions TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_recipes_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 4. INGREDIENTS TABLE
CREATE TABLE ingredients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    ingredient_name VARCHAR(150) NOT NULL,
    quantity VARCHAR(50) NOT NULL,
    unit VARCHAR(50) NOT NULL DEFAULT 'pcs',
    CONSTRAINT fk_ingredients_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. FAVORITES TABLE
CREATE TABLE favorites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    recipe_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_recipe_fav (user_id, recipe_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. MEAL PLANS TABLE
CREATE TABLE meal_plans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    recipe_id INT NOT NULL,
    meal_date DATE NOT NULL,
    meal_type ENUM('Breakfast', 'Lunch', 'Dinner', 'Snacks') NOT NULL,
    CONSTRAINT fk_meal_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_plans_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. SHOPPING LIST TABLE
CREATE TABLE shopping_list (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity VARCHAR(50) NOT NULL DEFAULT '1',
    unit VARCHAR(50) NOT NULL DEFAULT 'pcs',
    status ENUM('pending', 'purchased') NOT NULL DEFAULT 'pending',
    CONSTRAINT fk_shopping_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. REVIEWS TABLE
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    recipe_id INT NOT NULL,
    rating DECIMAL(2,1) NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Helpful indexes for fast search & filtering
CREATE INDEX idx_recipes_category ON recipes(category_id);
CREATE INDEX idx_recipes_name ON recipes(name);
CREATE INDEX idx_ingredients_name ON ingredients(ingredient_name);
CREATE INDEX idx_meal_plans_user_date ON meal_plans(user_id, meal_date);

-- ============================================================
-- SAMPLE SEED DATA
-- Default passwords (bcrypt hashed):
--   Admin (admin@smartrecipe.com): Admin@123
--   User  (aarav@smartrecipe.com): User@123
--   User  (priya@smartrecipe.com): User@123
-- ============================================================

INSERT INTO users (id, name, email, phone, password, role, profile_image) VALUES
(1, 'SmartRecipe Admin', 'admin@smartrecipe.com', '9999999999', '$2b$10$wK8x9p1zQ2vJ5Z3vX8J2meH9z7s1uY6rQ4vN8cM1xP3tW5yB7dK2G', 'admin', ''),
(2, 'Aarav Sharma', 'aarav@smartrecipe.com', '9876543210', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user', ''),
(3, 'Priya Patel', 'priya@smartrecipe.com', '9876501234', '$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user', '');

INSERT INTO categories (id, name, description, image) VALUES
(1, 'Breakfast', 'Energizing morning meals to start your day', '/uploads/samples/cat_1.jpg'),
(2, 'Lunch', 'Wholesome midday meals and bowls', '/uploads/samples/cat_2.jpg'),
(3, 'Dinner', 'Comforting evening dinners for family', '/uploads/samples/cat_3.jpg'),
(4, 'Snacks', 'Quick bites and evening tea-time snacks', '/uploads/samples/cat_4.jpg'),
(5, 'Desserts', 'Sweet treats, cakes, and puddings', '/uploads/samples/cat_5.jpg'),
(6, 'Vegetarian', 'Delicious 100% plant and dairy dishes', '/uploads/samples/cat_6.jpg'),
(7, 'Non-Vegetarian', 'Protein-rich chicken, egg, and meat recipes', '/uploads/samples/cat_7.jpg'),
(8, 'Healthy', 'Nutritious, light, and balanced recipes', '/uploads/samples/cat_8.jpg'),
(9, 'Quick Recipes', 'Ready in 20 minutes or less', '/uploads/samples/cat_9.jpg');

-- 16 Complete Sample Recipes
INSERT INTO recipes (id, user_id, category_id, name, description, image, preparation_time, cooking_time, servings, instructions) VALUES
(1, 2, 4, 'Vegetable Sandwich', 'Crispy toasted sandwich stuffed with fresh veggies, green chutney, and melted cheese.', '/uploads/samples/recipe_1.jpg', 10, 5, 2, '1. Slice tomato, cucumber, onion, and boiled potato thinly.\n2. Spread butter on bread slices.\n3. Layer vegetables, sprinkle chaat masala, and add cheese slice.\n4. Grill or toast with butter until golden brown and crispy.\n5. Cut diagonally and serve with ketchup.'),
(2, 2, 9, 'Creamy Tomato Pasta', 'Classic Italian penne pasta tossed in a rich garlic tomato herb sauce and cheese.', '/uploads/samples/recipe_2.jpg', 10, 15, 3, '1. Boil pasta in salted water until al dente and drain.\n2. Heat olive oil, saute chopped garlic and onions.\n3. Add pureed tomatoes, oregano, chili flakes, and salt.\n4. Simmer for 6 minutes, add cream/cheese and boiled pasta.\n5. Toss well and garnish with basil.'),
(3, 3, 4, 'Margherita Cheese Pizza', 'Homemade crispy crust pizza topped with tangy tomato sauce and mozzarella cheese.', '/uploads/samples/recipe_3.jpg', 20, 15, 4, '1. Roll pizza dough into a 10-inch round base.\n2. Spread pizza tomato sauce evenly.\n3. Top with grated mozzarella cheese, sliced tomatoes, and oregano.\n4. Bake at 220C for 12-15 minutes until cheese bubbles.\n5. Slice and serve hot.'),
(4, 2, 2, 'Vegetable Fried Rice', 'Flavorful Indo-Chinese stir-fried rice with crunchy vegetables and soy sauce.', '/uploads/samples/recipe_4.jpg', 10, 12, 2, '1. Cook and cool rice completely.\n2. Heat oil in a wok, saute garlic, onion, carrot, beans, and capsicum on high heat.\n3. Add cooked rice, soy sauce, vinegar, black pepper, and salt.\n4. Toss on high flame for 3 minutes.\n5. Garnish with spring onions.'),
(5, 3, 3, 'Paneer Butter Masala', 'Rich and creamy restaurant-style cottage cheese curry in spiced tomato butter gravy.', '/uploads/samples/recipe_5.jpg', 15, 25, 4, '1. Saute onions, tomatoes, ginger, garlic, and cashews with butter; blend to a smooth puree.\n2. Heat butter in a pan, add Kashmiri red chili powder and puree.\n3. Cook until oil separates; add garam masala and salt.\n4. Add paneer cubes and fresh cream; simmer 5 minutes.\n5. Garnish with kasuri methi and serve with naan.'),
(6, 2, 3, 'Dal Tadka', 'Comforting yellow lentils tempered with ghee, cumin, garlic, and red chilies.', '/uploads/samples/recipe_6.jpg', 10, 20, 4, '1. Pressure cook toor dal with turmeric and salt for 3 whistles.\n2. Mash lightly and adjust consistency with hot water.\n3. Heat ghee in a small pan; add cumin seeds, chopped garlic, onion, tomato, and red chili.\n4. Pour sizzling tadka over the dal.\n5. Garnish with fresh coriander.'),
(7, 3, 1, 'Aloo Paratha', 'Crispy whole-wheat flatbread stuffed with spiced mashed potatoes and served with butter.', '/uploads/samples/recipe_7.jpg', 15, 15, 3, '1. Boil and mash potatoes; mix with green chili, ginger, coriander, cumin, and salt.\n2. Knead soft whole wheat dough.\n3. Stuff potato filling inside a dough ball and roll gently.\n4. Cook on hot tawa with ghee/butter until golden brown spots appear on both sides.\n5. Serve hot with curd and pickle.'),
(8, 2, 2, 'Veg Biryani', 'Aromatic basmati rice layered with spiced mixed vegetables, saffron, and mint.', '/uploads/samples/recipe_8.jpg', 20, 30, 4, '1. Soak basmati rice for 30 minutes and parboil with whole spices.\n2. Cook onion, tomato, potato, carrot, peas, curd, and biryani masala.\n3. Layer rice over vegetable gravy, top with mint and saffron milk.\n4. Cover tightly and dum-cook on low heat for 15 minutes.\n5. Serve with raita.'),
(9, 3, 1, 'Masala Dosa', 'Crispy fermented South Indian crepe filled with spiced potato onion masala.', '/uploads/samples/recipe_9.jpg', 15, 15, 3, '1. Prepare potato filling by tempering mustard seeds, curry leaves, onion, turmeric, and boiled potatoes.\n2. Spread dosa batter thinly on a hot greased tawa.\n3. Drizzle oil around edges until crisp and golden.\n4. Place potato masala in the center and fold.\n5. Serve with coconut chutney and sambar.'),
(10, 2, 1, 'Kanda Poha', 'Light and healthy flattened rice cooked with onions, mustard seeds, peanuts, and lemon.', '/uploads/samples/recipe_10.jpg', 5, 10, 2, '1. Rinse poha in a strainer and keep aside.\n2. Heat oil, roast peanuts, then add mustard seeds, curry leaves, green chili, and chopped onions.\n3. Add turmeric, salt, and rinsed poha; mix gently.\n4. Steam covered for 2 minutes.\n5. Finish with lemon juice and fresh coriander.'),
(11, 3, 1, 'Soft Steamed Idli', 'Fluffy steamed rice and lentil cakes served with chutney and hot sambar.', '/uploads/samples/recipe_11.jpg', 10, 12, 4, '1. Grease idli moulds lightly with oil.\n2. Stir fermented rice-urad batter gently with salt.\n3. Pour batter into moulds.\n4. Steam for 10-12 minutes until a toothpick comes out clean.\n5. Cool 2 minutes and unmould.'),
(12, 2, 8, 'Garden Fresh Salad', 'Crisp cucumber, tomato, lettuce, and paneer tossed in zesty lemon herb dressing.', '/uploads/samples/recipe_12.jpg', 10, 0, 2, '1. Wash and chop cucumber, tomato, onion, capsicum, and lettuce.\n2. Whisk olive oil, lemon juice, black pepper, oregano, and salt.\n3. Toss vegetables with dressing just before serving.\n4. Top with toasted seeds or paneer cubes.'),
(13, 3, 5, 'Rich Chocolate Cake', 'Moist, fudgy homemade chocolate sponge cake topped with silky cocoa ganache.', '/uploads/samples/recipe_13.jpg', 15, 35, 6, '1. Sift flour, cocoa powder, baking powder, baking soda, and sugar.\n2. Whisk milk, oil, and vanilla extract; fold into dry ingredients.\n3. Pour into a greased cake tin and bake at 180C for 30-35 minutes.\n4. Cool and spread chocolate ganache.'),
(14, 2, 5, 'Fluffy Classic Pancakes', 'Golden breakfast pancakes drizzled with honey, butter, and fresh berries.', '/uploads/samples/recipe_14.jpg', 5, 12, 3, '1. Whisk flour, sugar, baking powder, and salt.\n2. Stir in milk, egg (or yogurt), and melted butter until smooth.\n3. Pour ladlefuls onto a hot non-stick pan.\n4. Flip when bubbles form and cook until golden.\n5. Serve warm with honey and butter.'),
(15, 3, 8, 'Creamy Tomato Soup', 'Velvety roasted tomato and garlic soup served with crunchy bread croutons.', '/uploads/samples/recipe_15.jpg', 10, 15, 3, '1. Heat butter in a pot; saute garlic, onion, and chopped tomatoes.\n2. Add water/stock, bay leaf, and salt; simmer 10 minutes.\n3. Blend smooth and strain.\n4. Reheat with black pepper and cream.\n5. Serve hot with toasted bread croutons.'),
(16, 2, 7, 'Classic Butter Chicken', 'Tender marinated chicken simmered in spiced creamy tomato butter gravy.', '/uploads/samples/recipe_16.jpg', 20, 25, 4, '1. Marinate chicken with yogurt, ginger-garlic paste, chili powder, and lemon; pan-sear.\n2. Saute tomatoes, butter, cashews, and spices; blend smooth.\n3. Simmer sauce with seared chicken for 12 minutes.\n4. Finish with fresh cream and kasuri methi.');

-- INGREDIENTS FOR ALL 16 RECIPES
INSERT INTO ingredients (recipe_id, ingredient_name, quantity, unit) VALUES
-- 1. Vegetable Sandwich
(1, 'Bread', '4', 'slices'),
(1, 'Tomato', '1', 'pcs'),
(1, 'Potato', '1', 'pcs'),
(1, 'Onion', '1', 'pcs'),
(1, 'Cheese', '2', 'slices'),
(1, 'Butter', '2', 'tbsp'),
-- 2. Creamy Tomato Pasta
(2, 'Pasta', '250', 'g'),
(2, 'Tomato', '4', 'pcs'),
(2, 'Onion', '1', 'pcs'),
(2, 'Garlic', '4', 'cloves'),
(2, 'Cheese', '50', 'g'),
-- 3. Margherita Cheese Pizza
(3, 'Flour', '2', 'cup'),
(3, 'Tomato', '3', 'pcs'),
(3, 'Cheese', '150', 'g'),
(3, 'Garlic', '2', 'cloves'),
-- 4. Vegetable Fried Rice
(4, 'Rice', '2', 'cup'),
(4, 'Onion', '1', 'pcs'),
(4, 'Capsicum', '1', 'pcs'),
(4, 'Garlic', '3', 'cloves'),
-- 5. Paneer Butter Masala
(5, 'Paneer', '250', 'g'),
(5, 'Tomato', '4', 'pcs'),
(5, 'Onion', '2', 'pcs'),
(5, 'Butter', '3', 'tbsp'),
(5, 'Garlic', '4', 'cloves'),
(5, 'Ginger', '1', 'inch'),
-- 6. Dal Tadka
(6, 'Toor Dal', '1', 'cup'),
(6, 'Tomato', '2', 'pcs'),
(6, 'Onion', '1', 'pcs'),
(6, 'Garlic', '4', 'cloves'),
(6, 'Butter', '2', 'tbsp'),
-- 7. Aloo Paratha
(7, 'Flour', '2', 'cup'),
(7, 'Potato', '3', 'pcs'),
(7, 'Onion', '1', 'pcs'),
(7, 'Butter', '2', 'tbsp'),
-- 8. Veg Biryani
(8, 'Rice', '2', 'cup'),
(8, 'Potato', '1', 'pcs'),
(8, 'Tomato', '2', 'pcs'),
(8, 'Onion', '2', 'pcs'),
(8, 'Ginger', '1', 'tbsp'),
-- 9. Masala Dosa
(9, 'Rice', '2', 'cup'),
(9, 'Potato', '3', 'pcs'),
(9, 'Onion', '1', 'pcs'),
-- 10. Kanda Poha
(10, 'Poha', '2', 'cup'),
(10, 'Onion', '1', 'pcs'),
(10, 'Potato', '1', 'pcs'),
-- 11. Soft Steamed Idli
(11, 'Rice', '2', 'cup'),
(11, 'Urad Dal', '1', 'cup'),
-- 12. Garden Fresh Salad
(12, 'Tomato', '2', 'pcs'),
(12, 'Onion', '1', 'pcs'),
(12, 'Capsicum', '1', 'pcs'),
(12, 'Paneer', '100', 'g'),
-- 13. Rich Chocolate Cake
(13, 'Flour', '1.5', 'cup'),
(13, 'Milk', '1', 'cup'),
(13, 'Butter', '100', 'g'),
(13, 'Cocoa Powder', '4', 'tbsp'),
-- 14. Fluffy Classic Pancakes
(14, 'Flour', '1.5', 'cup'),
(14, 'Milk', '1', 'cup'),
(14, 'Eggs', '1', 'pcs'),
(14, 'Butter', '2', 'tbsp'),
-- 15. Creamy Tomato Soup
(15, 'Tomato', '5', 'pcs'),
(15, 'Onion', '1', 'pcs'),
(15, 'Garlic', '3', 'cloves'),
(15, 'Butter', '1', 'tbsp'),
(15, 'Bread', '2', 'slices'),
-- 16. Classic Butter Chicken
(16, 'Chicken', '500', 'g'),
(16, 'Tomato', '4', 'pcs'),
(16, 'Butter', '3', 'tbsp'),
(16, 'Garlic', '5', 'cloves'),
(16, 'Ginger', '1', 'tbsp');

-- SAMPLE REVIEWS
INSERT INTO reviews (user_id, recipe_id, rating, review_text) VALUES
(2, 5, 5.0, 'Super creamy and tastes just like a restaurant!'),
(3, 1, 4.5, 'Quick, crispy, and great for evening snacks.'),
(2, 7, 5.0, 'My family loved these stuffed aloo parathas.'),
(3, 2, 4.0, 'Easy weeknight pasta recipe with simple pantry ingredients.'),
(2, 15, 4.5, 'Warm, comforting tomato soup in 25 minutes.');

-- SAMPLE FAVORITES
INSERT INTO favorites (user_id, recipe_id) VALUES
(2, 1),
(2, 5),
(3, 7);

-- SAMPLE MEAL PLANS
INSERT INTO meal_plans (user_id, recipe_id, meal_date, meal_type) VALUES
(2, 10, CURDATE(), 'Breakfast'),
(2, 8, CURDATE(), 'Lunch'),
(2, 5, CURDATE(), 'Dinner');

-- SAMPLE SHOPPING LIST
INSERT INTO shopping_list (user_id, item_name, quantity, unit, status) VALUES
(2, 'Tomato', '500', 'g', 'pending'),
(2, 'Paneer', '250', 'g', 'pending'),
(2, 'Butter', '1', 'packet', 'purchased');
