-- Add final_price column if it doesn't exist
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='doors' AND column_name='final_price') THEN
        ALTER TABLE doors ADD COLUMN final_price double precision NOT NULL DEFAULT 0;
    END IF;
END $$;

-- Update existing records
UPDATE doors SET final_price = COALESCE(price, 0) WHERE final_price IS NULL; 