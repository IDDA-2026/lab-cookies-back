package com.ironhack.simple_auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import jakarta.servlet.http.Cookie;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testLoginSetsCookieAndLogoutClearsItAndMeEndpoint() throws Exception {
        // 1. Try to access /api/me without cookie -> should be 403 Forbidden
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isForbidden());

        // 2. Perform login with valid credentials -> should set token cookie
        String loginBody = """
                {
                  "email": "demo@ironhack.com",
                  "password": "password"
                }
                """;

        var result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("token"))
                .andExpect(cookie().httpOnly("token", true))
                .andExpect(jsonPath("$.email").value("demo@ironhack.com"))
                .andExpect(jsonPath("$.token").doesNotExist()) // token shouldn't be in JSON body
                .andReturn();

        Cookie tokenCookie = result.getResponse().getCookie("token");
        org.junit.jupiter.api.Assertions.assertNotNull(tokenCookie);

        // 3. Access /api/me with token cookie -> should be 200 OK and return user
        mockMvc.perform(get("/api/me")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("demo@ironhack.com"));

        // 4. Access /api/logout -> should clear token cookie
        mockMvc.perform(post("/api/logout")
                        .cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("token", 0));
    }
}
