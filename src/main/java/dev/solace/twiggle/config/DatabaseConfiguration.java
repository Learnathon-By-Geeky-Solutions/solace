package dev.solace.twiggle.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database configuration to manage connection pooling and prevent connection leaks.
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfiguration implements DisposableBean {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    private HikariDataSource hikariDataSource;

    /**
     * Create a HikariCP data source with optimized settings to prevent connection leaks.
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // Connection pool settings
        config.setMaximumPoolSize(10); // Reduced from 20 to 10
        config.setMinimumIdle(3);

        // Connection leak prevention settings
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes

        // Enable leak detection
        config.setLeakDetectionThreshold(60000); // 60 seconds

        // Register for JVM shutdown to ensure the pool is closed
        config.setRegisterMbeans(true);
        config.setAllowPoolSuspension(false);

        // Add additional connection properties
        Properties props = new Properties();
        props.setProperty("tcpKeepAlive", "true");
        config.setDataSourceProperties(props);

        // Store the HikariDataSource reference for proper cleanup
        this.hikariDataSource = new HikariDataSource(config);

        // Create and wrap the HikariDataSource with LazyConnectionDataSourceProxy
        // to delay getting a connection until it's actually needed
        return new LazyConnectionDataSourceProxy(hikariDataSource);
    }

    /**
     * Ensure the connection pool is properly closed on application shutdown
     */
    @PreDestroy
    public void closeDataSource() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
        }
    }

    @Override
    public void destroy() throws Exception {
        closeDataSource();
    }
}
