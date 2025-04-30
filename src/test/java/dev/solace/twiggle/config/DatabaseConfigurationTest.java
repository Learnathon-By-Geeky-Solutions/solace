package dev.solace.twiggle.config;

import static org.junit.jupiter.api.Assertions.*;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigurationTest {

    @Autowired
    private DatabaseConfiguration databaseConfiguration;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name:${spring.datasource.driverClassName}}")
    private String driverClassName;

    @Test
    void testDataSourceBeanCreation() {
        // When
        DataSource dataSource = databaseConfiguration.dataSource();

        // Then
        assertNotNull(dataSource, "DataSource should not be null");
        assertInstanceOf(
                LazyConnectionDataSourceProxy.class,
                dataSource,
                "DataSource should be wrapped in LazyConnectionDataSourceProxy");
    }

    @Test
    void testDataSourceConnection() throws Exception {
        // Given
        DataSource dataSource = databaseConfiguration.dataSource();

        // When
        try (Connection connection = dataSource.getConnection()) {
            // Then
            assertTrue(connection.isValid(1), "Connection should be valid");
            assertEquals(url, connection.getMetaData().getURL(), "Connection URL should match configuration");
            assertEquals(
                    username.toLowerCase(),
                    connection.getMetaData().getUserName().toLowerCase(),
                    "Connection username should match configuration (case-insensitive)");
        }
    }

    @Test
    void testHikariConfigSettings() {
        // This test verifies the HikariConfig settings are properly configured

        // Given
        DataSource dataSource = databaseConfiguration.dataSource();
        LazyConnectionDataSourceProxy lazyDataSource = (LazyConnectionDataSourceProxy) dataSource;
        HikariDataSource hikariDataSource = (HikariDataSource) lazyDataSource.getTargetDataSource();

        // Get the actual pool settings from the HikariDataSource
        assert hikariDataSource != null;
        int actualMaxPoolSize = hikariDataSource.getMaximumPoolSize();
        int actualMinIdle = hikariDataSource.getMinimumIdle();
        long actualConnectionTimeout = hikariDataSource.getConnectionTimeout();
        long actualIdleTimeout = hikariDataSource.getIdleTimeout();
        long actualMaxLifetime = hikariDataSource.getMaxLifetime();
        long actualLeakDetectionThreshold = hikariDataSource.getLeakDetectionThreshold();

        // Then
        assertEquals(10, actualMaxPoolSize, "Maximum pool size should be 10");
        assertEquals(3, actualMinIdle, "Minimum idle connections should be 3");
        assertEquals(30000, actualConnectionTimeout, "Connection timeout should be 30000ms");
        assertEquals(600000, actualIdleTimeout, "Idle timeout should be 600000ms");
        assertEquals(1800000, actualMaxLifetime, "Max lifetime should be 1800000ms");
        assertEquals(60000, actualLeakDetectionThreshold, "Leak detection threshold should be 60000ms");

        // Test additional properties
        Properties props = hikariDataSource.getDataSourceProperties();
        assertTrue(Boolean.parseBoolean(props.getProperty("tcpKeepAlive")), "tcpKeepAlive should be true");
    }

    @Test
    void testCloseDataSource() throws Exception {
        // Create a fresh instance of DatabaseConfiguration for this test
        DatabaseConfiguration freshConfig = new DatabaseConfiguration();

        // Set the required properties via reflection
        java.lang.reflect.Field urlField = DatabaseConfiguration.class.getDeclaredField("url");
        urlField.setAccessible(true);
        urlField.set(freshConfig, url);

        java.lang.reflect.Field usernameField = DatabaseConfiguration.class.getDeclaredField("username");
        usernameField.setAccessible(true);
        usernameField.set(freshConfig, username);

        java.lang.reflect.Field passwordField = DatabaseConfiguration.class.getDeclaredField("password");
        passwordField.setAccessible(true);
        passwordField.set(freshConfig, password);

        java.lang.reflect.Field driverField = DatabaseConfiguration.class.getDeclaredField("driverClassName");
        driverField.setAccessible(true);
        driverField.set(freshConfig, driverClassName);

        // Get a fresh data source
        DataSource dataSource = freshConfig.dataSource();
        LazyConnectionDataSourceProxy lazyDataSource = (LazyConnectionDataSourceProxy) dataSource;
        HikariDataSource hikariDataSource = (HikariDataSource) lazyDataSource.getTargetDataSource();

        // Now verify it's not closed initially
        assertFalse(hikariDataSource.isClosed(), "HikariDataSource should not be closed initially");

        // Call the closeDataSource method
        freshConfig.closeDataSource();

        // Verify hikariDataSource is closed after calling closeDataSource
        assertTrue(hikariDataSource.isClosed(), "HikariDataSource should be closed after closeDataSource is called");
    }

    @Test
    void testDestroy() throws Exception {
        // Create a spy of DatabaseConfiguration to verify method calls
        DatabaseConfiguration spy = org.mockito.Mockito.spy(databaseConfiguration);

        // Call the destroy method
        spy.destroy();

        // Verify that closeDataSource was called
        org.mockito.Mockito.verify(spy).closeDataSource();
    }
}
