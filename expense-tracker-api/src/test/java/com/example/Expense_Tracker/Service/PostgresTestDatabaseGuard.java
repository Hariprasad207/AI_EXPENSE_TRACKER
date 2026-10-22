package com.example.Expense_Tracker.Service;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class PostgresTestDatabaseGuard
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        String url = context.getEnvironment().getProperty("spring.datasource.url");

        String expected =
                "jdbc:postgresql://localhost:15432/expense_tracker_concurrency_test";

        if (!expected.equals(url)) {
            throw new IllegalStateException(
                    "PostgreSQL concurrency tests blocked: database URL must exactly match "
                            + expected);
        }
    }
}
