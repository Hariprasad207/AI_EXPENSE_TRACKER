-- Add missing user relationships.
ALTER TABLE public.categories
    ADD CONSTRAINT fk_categories_user_id
    FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.expenses
    ADD CONSTRAINT fk_expenses_user_id
    FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.income
    ADD CONSTRAINT fk_income_user_id
    FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.budgets
    ADD CONSTRAINT fk_budgets_user_id
    FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.ai_chat_history
    ADD CONSTRAINT fk_ai_chat_history_user_id
    FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.ai_insight
    ADD CONSTRAINT fk_ai_insight_user_id
    FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.notifications
    ADD CONSTRAINT fk_notifications_user_id
    FOREIGN KEY (user_id) REFERENCES public.users(id);

-- Add missing category relationships.
ALTER TABLE public.income
    ADD CONSTRAINT fk_income_category_id
    FOREIGN KEY (category_id) REFERENCES public.categories(id);

ALTER TABLE public.budgets
    ADD CONSTRAINT fk_budgets_category_id
    FOREIGN KEY (category_id) REFERENCES public.categories(id);

ALTER TABLE public.notifications
    ADD CONSTRAINT fk_notifications_category_id
    FOREIGN KEY (category_id) REFERENCES public.categories(id);

-- Allow only one overall budget per user per month and year.
CREATE UNIQUE INDEX ux_budgets_overall_user_month_year
    ON public.budgets (user_id, month, year)
    WHERE category_id IS NULL;
