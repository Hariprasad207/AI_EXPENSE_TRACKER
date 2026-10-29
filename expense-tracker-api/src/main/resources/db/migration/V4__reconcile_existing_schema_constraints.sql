-- Reconcile integrity rules for databases with existing Flyway history.
-- Fresh installations already create these rules in V2.

DO $$
DECLARE
    notifications_table_oid regclass := 'public.notifications'::regclass;
    categories_table_oid regclass := 'public.categories'::regclass;
    category_attnum smallint;
    id_attnum smallint;
BEGIN
    SELECT attnum INTO category_attnum
    FROM pg_attribute
    WHERE attrelid = notifications_table_oid
      AND attname = 'category_id'
      AND NOT attisdropped;

    SELECT attnum INTO id_attnum
    FROM pg_attribute
    WHERE attrelid = categories_table_oid
      AND attname = 'id'
      AND NOT attisdropped;

    IF category_attnum IS NULL OR id_attnum IS NULL THEN
        RAISE EXCEPTION
            'Cannot reconcile notifications.category_id foreign key: required columns are missing';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        WHERE c.conrelid = notifications_table_oid
          AND c.contype = 'f'
          AND c.confrelid = categories_table_oid
          AND c.conkey = ARRAY[category_attnum]::smallint[]
          AND c.confkey = ARRAY[id_attnum]::smallint[]
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conrelid = notifications_table_oid
              AND conname = 'fk_notifications_category_id'
        ) THEN
            RAISE EXCEPTION
                'Constraint fk_notifications_category_id exists but does not match the expected foreign key';
        END IF;

        ALTER TABLE public.notifications
            ADD CONSTRAINT fk_notifications_category_id
            FOREIGN KEY (category_id)
            REFERENCES public.categories(id);
    END IF;
END
$$;

DO $$
DECLARE
    budgets_table_oid regclass := 'public.budgets'::regclass;
    index_oid regclass;
    index_definition text;
    index_predicate text;
    index_is_unique boolean;
    index_is_valid boolean;
BEGIN
    SELECT c.oid INTO index_oid
    FROM pg_class c
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'public'
      AND c.relname = 'ux_budgets_overall_user_month_year'
      AND c.relkind = 'i';

    IF index_oid IS NOT NULL THEN
        SELECT
            i.indisunique,
            i.indisvalid,
            pg_get_indexdef(i.indexrelid),
            pg_get_expr(i.indpred, i.indrelid)
        INTO
            index_is_unique,
            index_is_valid,
            index_definition,
            index_predicate
        FROM pg_index i
        WHERE i.indexrelid = index_oid;

        IF index_is_unique IS DISTINCT FROM TRUE
           OR index_is_valid IS DISTINCT FROM TRUE
           OR index_definition NOT LIKE
               '%USING btree (user_id, month, year)%'
           OR regexp_replace(
               COALESCE(index_predicate, ''),
               '[()[:space:]]',
               '',
               'g'
           ) <> 'category_idISNULL'
        THEN
            RAISE EXCEPTION
                'Index ux_budgets_overall_user_month_year exists but does not match the expected definition: %',
                index_definition;
        END IF;
    ELSE
        CREATE UNIQUE INDEX ux_budgets_overall_user_month_year
            ON public.budgets (user_id, month, year)
            WHERE category_id IS NULL;
    END IF;
END
$$;
