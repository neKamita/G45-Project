-- Delete any orphaned image records (those without a valid furniture_door_id)
DELETE FROM furniture_door_images 
WHERE furniture_door_id NOT IN (SELECT id FROM furniture_door);

-- Ensure all remaining records have valid foreign keys
UPDATE furniture_door_images fdi
SET furniture_door_id = fd.id
FROM furniture_door fd
WHERE fdi.furniture_door_id IS NULL
AND fd.id = (SELECT MIN(id) FROM furniture_door);
