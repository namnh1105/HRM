package com.hainam.worksphere;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base test class for unit tests with common configurations
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseUnitTest {

    // Common test utilities and constants
    protected static final String TEST_EMAIL = "test@example.com";
    protected static final String TEST_PASSWORD = "testPassword123";
    protected static final String TEST_IP_ADDRESS = "192.168.1.1";

    @BeforeEach
    void baseSetUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }
}

