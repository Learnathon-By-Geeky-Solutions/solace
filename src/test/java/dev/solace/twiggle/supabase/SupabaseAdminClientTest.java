package dev.solace.twiggle.supabase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.config.SupabaseConfig;
import dev.solace.twiggle.dto.supabase.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SupabaseAdminClientTest {

    @Mock
    private SupabaseConfig supabaseConfig;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Captor
    private ArgumentCaptor<UserRequest> userRequestCaptor;

    private SupabaseAdminClient supabaseAdminClient;

    @BeforeEach
    void setUp() {
        // Mock SupabaseConfig
        when(supabaseConfig.getProjectId()).thenReturn("test-project-id");
        when(supabaseConfig.getServiceRoleKey()).thenReturn("test-service-role-key");

        // Create the client with a constructor that uses WebClient.Builder
        try (MockedStatic<WebClient> webClientBuilderMockedStatic = mockStatic(WebClient.class)) {
            webClientBuilderMockedStatic.when(WebClient::builder).thenReturn(webClientBuilder);
            when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
            when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
            when(webClientBuilder.build()).thenReturn(webClient);
            supabaseAdminClient = new SupabaseAdminClient(supabaseConfig);
        }
    }

    @Test
    void createUser_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "testPassword123";
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("success"));

        // Act
        supabaseAdminClient.createUser(email, password);

        // Assert
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/users");
        verify(requestBodyUriSpec).bodyValue(userRequestCaptor.capture());

        UserRequest capturedRequest = userRequestCaptor.getValue();
        assertEquals(email, capturedRequest.getEmail());
        assertEquals(password, capturedRequest.getPassword());
    }

    @Test
    void createUser_Error() {
        // Arrange
        String email = "test@example.com";
        String password = "testPassword123";
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Test error")));

        // Act & Assert
        supabaseAdminClient.createUser(email, password);

        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/users");
        verify(requestBodyUriSpec).bodyValue(any(UserRequest.class));
    }

    @Test
    void updateUser_Success() {
        // Arrange
        String userId = "test-user-id";
        String newEmail = "new@example.com";
        String newPassword = "newPassword123";
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("success"));

        // Act
        supabaseAdminClient.updateUser(userId, newEmail, newPassword);

        // Assert
        verify(webClient).put();
        verify(requestBodyUriSpec).uri("/users/" + userId);
        verify(requestBodyUriSpec).bodyValue(userRequestCaptor.capture());

        UserRequest capturedRequest = userRequestCaptor.getValue();
        assertEquals(newEmail, capturedRequest.getEmail());
        assertEquals(newPassword, capturedRequest.getPassword());
    }

    @Test
    void updateUser_Error() {
        // Arrange
        String userId = "test-user-id";
        String newEmail = "new@example.com";
        String newPassword = "newPassword123";
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Test error")));

        // Act & Assert
        supabaseAdminClient.updateUser(userId, newEmail, newPassword);

        verify(webClient).put();
        verify(requestBodyUriSpec).uri("/users/" + userId);
        verify(requestBodyUriSpec).bodyValue(any(UserRequest.class));
    }

    @Test
    void constructor_InitializesWebClientCorrectly() {
        // Verify the WebClient.Builder was configured correctly
        verify(webClientBuilder).baseUrl("https://test-project-id.supabase.co/auth/v1/admin");
        verify(webClientBuilder, times(2)).defaultHeader(anyString(), anyString());
        verify(webClientBuilder).build();
    }
}
