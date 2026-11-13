package com.portfoliopro.api.auth;

import java.security.SecureRandom;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {
        @DynamicPropertySource
        static void configureJwtSecret(DynamicPropertyRegistry registry) {
                byte[] key = new byte[32];
                new SecureRandom().nextBytes(key);
                registry.add("app.jwt.secret", () -> Base64.getEncoder().encodeToString(key));
        }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void userCanRegisterLoginAndAccessProtectedProfile() throws Exception {
        String registration = """
                {"name":"Jordan Lee","email":"jordan@example.com","password":"secure-pass-123"}
                """;
        MvcResult registrationResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("jordan@example.com"))
                .andExpect(jsonPath("$.user.currency").value("INR"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn();
        String registrationCookie = cookieValue(registrationResult);

        mockMvc.perform(get("/api/users/me").cookie(new Cookie(JwtAuthenticationFilter.COOKIE_NAME, registrationCookie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jordan Lee"));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"JORDAN@example.com","password":"secure-pass-123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        mockMvc.perform(get("/api/users/me").cookie(new Cookie(
                        JwtAuthenticationFilter.COOKIE_NAME, cookieValue(loginResult))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void protectedProfileRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationValidatesInputAndRejectsDuplicateEmail() throws Exception {
        String invalidRequest = """
                {"name":"J","email":"not-an-email","password":"short"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isMap());

        String registration = """
                {"name":"Casey Doe","email":"casey@example.com","password":"secure-pass-123"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isConflict());
    }

    private String cookieValue(MvcResult result) {
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        if (setCookie == null || !setCookie.contains("HttpOnly")) {
            throw new AssertionError("Expected an HttpOnly authentication cookie");
        }
        return setCookie.substring(setCookie.indexOf('=') + 1, setCookie.indexOf(';'));
    }
}