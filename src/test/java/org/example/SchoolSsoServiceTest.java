package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.example.entity.vo.SysLoginVO;
import org.example.entity.SysUser;
import org.example.mapper.SysMapper;
import org.example.service.ReviewContextService;
import org.example.service.SchoolSsoService;
import org.example.service.SysService;
import org.example.utils.JWTUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchoolSsoServiceTest {
    private HttpServer provider;

    @AfterEach void stop() { if (provider != null) provider.stop(0); }

    @Test void exchangesOnlyMatchingStateAndExactStaffIdentity() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        provider = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        provider.createContext("/cas/oauth2.0/accessToken", exchange -> {
            requests.incrementAndGet();
            respond(exchange, "{\"access_token\":\"test-access-token\",\"expires_in\":7200}");
        });
        provider.createContext("/cas/oauth2.0/profile", exchange -> {
            requests.incrementAndGet();
            respond(exchange, "{\"id\":\"110228\",\"attributes\":[{\"CODE\":\"110228\"}]}");
        });
        provider.start();
        SysService users = mock(SysService.class);
        when(users.loginFromSso(110228L)).thenReturn(new SysLoginVO("测试用户", "platform-token", false));
        SchoolSsoService service = service(users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/api");
        var start = service.start(request);
        String url = start.getBody().get("url");
        assertTrue(url.contains("redirect_uri=http%3A%2F%2F127.0.0.1%2F"));
        String state = param(url, "state");
        assertEquals(43, state.length());
        assertTrue(start.getHeaders().getFirst("Set-Cookie").contains("HttpOnly"));
        assertTrue(start.getHeaders().getFirst("Set-Cookie").contains("SameSite=Lax"));
        request.setCookies(new jakarta.servlet.http.Cookie("sg_sso_state", state));
        assertThrows(IllegalArgumentException.class, () -> service.exchange("code", "wrong", request));
        assertEquals(0, requests.get());
        var login = service.exchange("code", state, request);
        assertEquals("platform-token", login.getBody().getToken());
        assertEquals(2, requests.get());
        verify(users).loginFromSso(110228L);
        assertTrue(login.getHeaders().getFirst("Set-Cookie").contains("Max-Age=0"));
    }

    @Test void rejectsProviderIdentityMismatch() throws Exception {
        provider = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        provider.createContext("/cas/oauth2.0/accessToken", e -> respond(e, "{\"access_token\":\"test\"}"));
        provider.createContext("/cas/oauth2.0/profile", e -> respond(e,
                "{\"id\":\"110228\",\"attributes\":[{\"CODE\":\"999999\"}]}"));
        provider.start();
        SysService users = mock(SysService.class);
        SchoolSsoService service = service(users);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/api");
        String state = param(service.start(request).getBody().get("url"), "state");
        request.setCookies(new jakarta.servlet.http.Cookie("sg_sso_state", state));
        assertThrows(SecurityException.class, () -> service.exchange("code", state, request));
        verifyNoInteractions(users);
    }

    @Test void ssoUsesCurrentLocalAccountAndLeavesPasswordFlagUntouched() {
        SysMapper mapper = mock(SysMapper.class);
        ReviewContextService review = mock(ReviewContextService.class);
        SysService users = new SysService();
        ReflectionTestUtils.setField(users, "sysMapper", mapper);
        ReflectionTestUtils.setField(users, "reviewContexts", review);
        SysUser active = new SysUser();
        active.setUserId(123456L);
        active.setUserName("123456");
        active.setNickName("测试用户");
        active.setRole("1");
        active.setStatus("1");
        active.setIsDelete(0);
        active.setForcePasswordChange(1);
        when(mapper.getUserById(123456L)).thenReturn(active);
        when(review.loginToken(eq(123456L), anyString())).thenAnswer(invocation -> invocation.getArgument(1));

        SysLoginVO login = users.loginFromSso(123456L);
        assertFalse(login.isRequiresPasswordChange());
        assertEquals("SSO", JWTUtil.verifyJwtToken(login.getToken()).getClaim("loginSource").asString());
        assertEquals(1, active.getForcePasswordChange());

        active.setStatus("0");
        assertThrows(SecurityException.class, () -> users.loginFromSso(123456L));
        verify(review, times(1)).loginToken(eq(123456L), anyString());
    }

    private SchoolSsoService service(SysService users) {
        return new SchoolSsoService(true, "test-client", "test-secret",
                "http://127.0.0.1:" + provider.getAddress().getPort() + "/cas",
                "http://127.0.0.1/", users, new ObjectMapper());
    }

    private static String param(String url, String name) {
        for (String pair : URI.create(url).getRawQuery().split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts[0].equals(name)) return java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        }
        throw new AssertionError("Missing parameter " + name);
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, String body) throws java.io.IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, data.length);
        try (var out = exchange.getResponseBody()) { out.write(data); }
    }
}
