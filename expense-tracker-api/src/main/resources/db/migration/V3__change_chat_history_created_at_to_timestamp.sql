-- Preserve existing chat dates at midnight UTC.
-- Original time-of-day values cannot be recovered because the
-- previous column stored dates only.

ALTER TABLE public.ai_chat_history
    ALTER COLUMN created_at
    TYPE timestamp(6) with time zone
    USING (
        created_at::timestamp without time zone
        AT TIME ZONE 'UTC'
    );