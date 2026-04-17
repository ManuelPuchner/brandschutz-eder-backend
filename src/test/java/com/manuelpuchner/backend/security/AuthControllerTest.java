package com.manuelpuchner.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({JwtUtil.class, JwtAuthFilter.class, SecurityConfig.class})
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-for-unit-tests-minimum-32chars!!",
        "app.jwt.expiration=3600000",
        "app.admin.username=testadmin",
        "app.admin.password=testpass"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // POST /auth/login
    // -------------------------------------------------------------------------

    @Test
    void login_givenValidCredentials_thenReturns200WithJwtToken() throws Exception {
        Map<String, String> body = Map.of("username", "testadmin", "password", "testpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token", not(emptyString())));
    }

    @Test
    void login_givenWrongPassword_thenReturns401WithErrorMessage() throws Exception {
        Map<String, String> body = Map.of("username", "testadmin", "password", "wrongpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("invalid credentials")));
    }

    @Test
    void login_givenWrongUsername_thenReturns401WithErrorMessage() throws Exception {
        Map<String, String> body = Map.of("username", "unknownuser", "password", "testpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("invalid credentials")));
    }

    @Test
    void login_givenBothCredentialsWrong_thenReturns401() throws Exception {
        Map<String, String> body = Map.of("username", "wronguser", "password", "wrongpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
