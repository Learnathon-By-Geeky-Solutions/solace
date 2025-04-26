package dev.solace.twiggle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for repositories to ensure proper transaction management
 * and prevent connection leaks with database operations.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "dev.solace.twiggle.repository",
        transactionManagerRef = "transactionManager",
        enableDefaultTransactions = true)
public class RepositoryConfiguration {
    // This configuration enables transaction management for repositories
    // to ensure proper connection handling and prevent connection leaks
}
