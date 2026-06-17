package com.ironhack.simple_auth;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SimpleAuthApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void loginSetsHttpOnlyTokenCookieWithoutTokenInBody() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "demo@ironhack.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("token"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")))
                .andExpect(content().string(not(containsString("\"token\""))))
                .andExpect(jsonPath("$.user.email").value("demo@ironhack.com"));
    }

    @Test
    void protectedRouteReadsTokenFromCookie() throws Exception {
        Cookie tokenCookie = loginAndReturnTokenCookie();

        mockMvc.perform(get("/api/me").cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("demo@ironhack.com"));
    }

    @Test
    void protectedRouteDoesNotAcceptAuthorizationHeaderToken() throws Exception {
        Cookie tokenCookie = loginAndReturnTokenCookie();

        mockMvc.perform(get("/api/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCookie.getValue()))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutClearsTokenCookie() throws Exception {
        Cookie tokenCookie = loginAndReturnTokenCookie();

        mockMvc.perform(post("/api/logout").cookie(tokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("token", 0))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));
    }

    private Cookie loginAndReturnTokenCookie() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "demo@ironhack.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("token");
    }
}
