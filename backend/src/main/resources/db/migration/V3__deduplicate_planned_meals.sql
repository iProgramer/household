-- Fix: V2 migration inserted planned meals that duplicate manually created ones
-- (same household + date). Keep the entry with a recipe_id link when possible
-- (those were created via the UI), otherwise keep any one.

DELETE FROM planned_meals
WHERE id NOT IN (
    SELECT DISTINCT ON (household_id, date) id
    FROM planned_meals
    ORDER BY household_id, date, recipe_id NULLS LAST, id
);

-- Prevent this from happening again
ALTER TABLE planned_meals
    ADD CONSTRAINT uq_planned_meals_household_date UNIQUE (household_id, date);
