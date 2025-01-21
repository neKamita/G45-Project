-- First, create a temporary column that allows NULL
ALTER TABLE furniture_door_images 
ADD COLUMN furniture_door_id_temp bigint;

-- Copy any existing relationships if they exist
UPDATE furniture_door_images 
SET furniture_door_id_temp = door_id 
WHERE door_id IS NOT NULL;

-- Drop the old column if it exists
ALTER TABLE furniture_door_images 
DROP COLUMN IF EXISTS door_id;

-- Add the new NOT NULL column
ALTER TABLE furniture_door_images 
ADD COLUMN furniture_door_id bigint NOT NULL DEFAULT 1;  -- Using 1 as a temporary default

-- Copy data from temp column
UPDATE furniture_door_images 
SET furniture_door_id = COALESCE(furniture_door_id_temp, 1);

-- Drop temporary column
ALTER TABLE furniture_door_images 
DROP COLUMN furniture_door_id_temp;

-- Add foreign key constraint
ALTER TABLE furniture_door_images 
ADD CONSTRAINT fk_furniture_door 
FOREIGN KEY (furniture_door_id) 
REFERENCES furniture_door(id);

-- Remove the default value
ALTER TABLE furniture_door_images 
ALTER COLUMN furniture_door_id DROP DEFAULT;
