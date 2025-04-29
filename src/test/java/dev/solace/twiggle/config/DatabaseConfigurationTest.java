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
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:testdb",
            "spring.datasource.username=sa",
            "spring.datasource.password=password",
            "spring.datasource.driver-class-name=org.h2.Driver"
        })
class DatabaseConfigurationTest {

    @Autowired
    private DatabaseConfiguration databaseConfiguration;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
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
}
