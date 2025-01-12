package dev.solace.twiggle.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestController.class)
public class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Tests the `/api/test` endpoint of the TestController to verify its expected behavior.
     *
     * This test method performs a GET request to the `/api/test` endpoint and validates:
     * - The HTTP response status is 200 (OK)
     * - The JSON response contains a status field with value 200
     * - The JSON response contains a message field with the text "Test endpoint executed successfully"
     * - The JSON response contains a data field with the value "Hello, World!"
     *
     * @throws Exception if any error occurs during the mock MVC request performance
     */
    @Test
    public void test_ShouldReturnHelloWorld() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Test endpoint executed successfully"))
                .andExpect(jsonPath("$.data").value("Hello, World!"));
    }
}
