-- Drop existing constraints
ALTER TABLE IF EXISTS door_images 
    DROP CONSTRAINT IF EXISTS fk33nbb6c7curx5h7rp8479q7ea,
    DROP CONSTRAINT IF EXISTS fkg7n1en2dxlecn4q2gr7ote66j;

-- Create new constraint with correct table name
ALTER TABLE door_images
    ADD CONSTRAINT fk_door_images_doors
    FOREIGN KEY (door_id)
    REFERENCES doors(id)
    ON DELETE CASCADE; 