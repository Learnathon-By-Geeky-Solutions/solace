package dev.solace.twiggle.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.solace.twiggle.config.RateLimiterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestController.class)
@Import({RateLimiterConfiguration.class})
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void test_ShouldReturnHelloWorld() throws Exception {
        mockMvc.perform(get("/api/v1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Test endpoint executed successfully"))
                .andExpect(jsonPath("$.data").value("Hello, World!"));
    }

    @Test
    void testError_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/test-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("This is a test error"))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void testServerError_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/test-server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("This is a test server error"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }
}
