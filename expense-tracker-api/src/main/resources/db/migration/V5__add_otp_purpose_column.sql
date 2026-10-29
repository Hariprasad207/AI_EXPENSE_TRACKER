-- Restore the OTP purpose column for databases adopted at an older schema version.
-- Fresh installations already have this column from V1.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'password_reset_otp'
          AND column_name = 'purpose'
    ) THEN
        ALTER TABLE public.password_reset_otp
            ADD COLUMN purpose varchar(30)
            DEFAULT 'PASSWORD_RESET' NOT NULL;
    END IF;
END
$$;
