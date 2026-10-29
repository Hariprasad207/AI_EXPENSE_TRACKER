-- Seed shared default categories for fresh installations.
-- Shared categories have no user_id and cannot be owned by an individual user.

INSERT INTO public.categories (name, type, icon, user_id, is_custom)
VALUES
    ('Food',          'EXPENSE', 'restaurant',       NULL, false),
    ('Transport',     'EXPENSE', 'directions_car',   NULL, false),
    ('Housing',       'EXPENSE', 'home',             NULL, false),
    ('Shopping',      'EXPENSE', 'shopping_bag',     NULL, false),
    ('Healthcare',    'EXPENSE', 'medical_services', NULL, false),
    ('Bills',         'EXPENSE', 'receipt_long',     NULL, false),
    ('Entertainment', 'EXPENSE', 'movie',            NULL, false),
    ('Education',     'EXPENSE', 'school',           NULL, false),
    ('Salary',        'INCOME',  'payments',         NULL, false),
    ('Freelance',     'INCOME',  'work',             NULL, false),
    ('Investment',    'INCOME',  'trending_up',      NULL, false),
    ('Gift',          'INCOME',  'card_giftcard',    NULL, false);
