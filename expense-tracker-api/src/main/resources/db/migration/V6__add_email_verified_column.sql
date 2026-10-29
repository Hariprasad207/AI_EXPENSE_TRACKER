-- Add email verification status for databases adopted at an older schema version.
-- Fresh installations already have this column from V1.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'email_verified'
    ) THEN
        ALTER TABLE public.users
            ADD COLUMN email_verified boolean DEFAULT true NOT NULL;
    END IF;
END
$$;
