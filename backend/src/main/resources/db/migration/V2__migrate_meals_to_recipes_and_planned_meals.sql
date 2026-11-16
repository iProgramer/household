-- Migrate data from the old `meals` table into the new Recipe/PlannedMeal model.
--
-- Old model: Meal had status IDEA (no date, = recipe idea) or PLANNED (has date).
-- New model: Recipe (catalog) + PlannedMeal (date assignment, optional recipe link).
--
-- Strategy:
--   1. IDEA meals → recipes (keep same id for traceability)
--   2. PLANNED meals → planned_meals (no recipe link, since the old model had no catalog concept)
--   3. Drop the meals table

-- Step 1: Move IDEAs into recipes (if not already migrated)
INSERT INTO recipes (id, household_id, title)
SELECT id, household_id, title
FROM meals
WHERE status = 'IDEA'
  AND id NOT IN (SELECT id FROM recipes);

-- Step 2: Move PLANNEDs into planned_meals
INSERT INTO planned_meals (id, household_id, date, title, recipe_id)
SELECT id, household_id, date, title, NULL
FROM meals
WHERE status = 'PLANNED'
  AND id NOT IN (SELECT id FROM planned_meals);

-- Step 3: Drop the old table
DROP TABLE IF EXISTS meals;
