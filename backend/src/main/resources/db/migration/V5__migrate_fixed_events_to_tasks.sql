-- Einmalige Events: Originaldatum beibehalten
INSERT INTO tasks (id, household_id, title, date, assigned_to, status, recurrence_type, recurrence_weekday, project_id)
SELECT id, household_id, title, date, NULL, 'OPEN', NULL, NULL, NULL
FROM fixed_events WHERE recurrence_type IS NULL;

-- Wiederkehrende Events: Datum auf heute setzen (nächste Instanz wird beim Abhaken erzeugt)
INSERT INTO tasks (id, household_id, title, date, assigned_to, status, recurrence_type, recurrence_weekday, project_id)
SELECT id, household_id, title, CURRENT_DATE, NULL, 'OPEN', recurrence_type, recurrence_weekday, NULL
FROM fixed_events WHERE recurrence_type IS NOT NULL;

DROP TABLE fixed_events;
