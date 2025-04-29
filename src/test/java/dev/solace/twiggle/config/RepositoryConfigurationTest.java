package dev.solace.twiggle.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

@SpringBootTest
class RepositoryConfigurationTest {

    @Autowired
    private TransactionManager transactionManager;

    @Test
    void testTransactionManagerConfiguration() {
        // Verify that the transaction manager is properly configured
        assertNotNull(transactionManager, "Transaction manager should be configured");
        assertInstanceOf(
                PlatformTransactionManager.class,
                transactionManager,
                "Transaction manager should be an instance of PlatformTransactionManager");
    }
}
