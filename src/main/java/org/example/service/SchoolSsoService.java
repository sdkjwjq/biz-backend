package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.vo.SysLoginVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/** The CAS credential and access token are used only on the server. */
@Service
public class SchoolSsoService {
    private static final String STATE_COOKIE = "sg_sso_state";
    private final boolean enabled;
    private final String clientId, clientSecret, casBaseUrl, redirectUri;
    private final SysService users;
    private final ObjectMapper json;
    private final HttpClient http;
    private final SecureRandom random = new SecureRandom();

    public SchoolSsoService(@Value("${sso.enabled:false}") boolean enabled,
                            @Value("${sso.client-id:}") String clientId,
                            @Value("${sso.client-secret:}") String clientSecret,
                            @Value("${sso.cas-base-url:http://ca.xzcit.cn/cas}") String casBaseUrl,
                            @Value("${sso.redirect-uri:}") String redirectUri,
                            SysService users, ObjectMapper json) {
        this.enabled = enabled;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.casBaseUrl = casBaseUrl.replaceAll("/+$", "");
        this.redirectUri = redirectUri;
        this.users = users;
        this.json = json;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    public boolean available() {
        return enabled && !clientId.isBlank() && !clientSecret.isBlank() && !redirectUri.isBlank();
    }

    public String logoutUrl() {
        return available() ? casBaseUrl + "/logout?service=" + encode(redirectUri) : "";
    }

    public ResponseEntity<Map<String, String>> start(HttpServletRequest request) {
        requireAvailable();
        byte[] nonce = new byte[32];
        random.nextBytes(nonce);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(nonce);
        String url = casBaseUrl + "/oauth2.0/authorize?response_type=code&client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri) + "&state=" + encode(state);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, stateCookie(request, state, 300).toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store").body(Map.of("url", url));
    }

    public ResponseEntity<SysLoginVO> exchange(String code, String state, HttpServletRequest request) {
        requireAvailable();
        String saved = null;
        if (request.getCookies() != null) for (Cookie cookie : request.getCookies()) {
            if (STATE_COOKIE.equals(cookie.getName())) saved = cookie.getValue();
        }
        if (code == null || code.isBlank() || code.length() > 2048 || state == null || state.length() > 128
                || saved == null || !MessageDigest.isEqual(state.getBytes(StandardCharsets.UTF_8), saved.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("统一认证登录状态已失效，请重新点击统一认证登录");
        }
        try {
            JsonNode token = fetch(casBaseUrl + "/oauth2.0/accessToken?client_id=" + encode(clientId)
                    + "&client_secret=" + encode(clientSecret) + "&code=" + encode(code)
                    + "&redirect_uri=" + encode(redirectUri));
            String accessToken = token.path("access_token").asText("");
            if (accessToken.isBlank()) throw new IllegalArgumentException("学校统一认证未返回有效凭证，请重试");
            JsonNode profile = fetch(casBaseUrl + "/oauth2.0/profile?access_token=" + encode(accessToken));
            String identity = profile.path("id").asText("").trim();
            String codeAttribute = findCode(profile.path("attributes"));
            if (!codeAttribute.isBlank() && !identity.isBlank() && !identity.equals(codeAttribute)) {
                throw new SecurityException("统一认证返回的账号标识不一致，请联系管理员");
            }
            if (identity.isBlank()) identity = codeAttribute;
            if (!identity.matches("[0-9]{1,18}")) throw new SecurityException("统一认证账号不是平台工号，请联系管理员关联账号");
            SysLoginVO login = users.loginFromSso(Long.parseLong(identity));
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, stateCookie(request, "", 0).toString())
                    .header(HttpHeaders.CACHE_CONTROL, "no-store").body(login);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("统一认证连接中断，请重试");
        } catch (java.io.IOException e) {
            // Never include the outbound URL: the token endpoint requires a secret in its query.
            throw new IllegalStateException("暂时无法连接学校统一认证，请稍后重试");
        }
    }

    private JsonNode fetch(String url) throws java.io.IOException, InterruptedException {
        URI endpoint;
        try { endpoint = URI.create(url); }
        catch (IllegalArgumentException error) { throw new IllegalStateException("统一认证地址配置不正确"); }
        HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(4))
                .header("Accept", "application/json").GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new IllegalStateException("学校统一认证暂时无法完成登录，请重试");
        return json.readTree(response.body());
    }

    private static String findCode(JsonNode attributes) {
        if (attributes == null || attributes.isMissingNode() || attributes.isNull()) return "";
        if (attributes.has("CODE")) {
            JsonNode code = attributes.path("CODE");
            if (code.isTextual() || code.isNumber()) return code.asText("").trim();
            if (code.isArray() && !code.isEmpty()) return code.get(0).asText("").trim();
        }
        if (attributes.isArray()) for (JsonNode node : attributes) {
            String found = findCode(node);
            if (!found.isBlank()) return found;
        }
        if (attributes.isObject()) for (JsonNode node : attributes) {
            String found = findCode(node);
            if (!found.isBlank()) return found;
        }
        return "";
    }

    private ResponseCookie stateCookie(HttpServletRequest request, String value, int seconds) {
        return ResponseCookie.from(STATE_COOKIE, value).httpOnly(true).secure(request.isSecure())
                .sameSite("Lax").path(request.getContextPath() + "/system/sso")
                .maxAge(Duration.ofSeconds(seconds)).build();
    }

    public String clearStateCookie(HttpServletRequest request) {
        return stateCookie(request, "", 0).toString();
    }

    private void requireAvailable() {
        if (!available()) throw new IllegalStateException("学校统一认证尚未启用");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
