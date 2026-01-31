package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.example.entity.SysUser;
import org.example.entity.TokenBlacklist;
import org.example.mapper.TokenBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * JWT工具类：生成、验证和解析Token
 */
public class JWTUtil {
    /** 密钥，与生成 Token 时使用的密钥一致 */
    private static final String SECRET_KEY = "secretKey";

    /**
     * 生成jwt token
     * @param user 用户对象
     * @return Token字符串
     */
    public static String generateJwtToken(SysUser user) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + 3600000; // 令牌有效期为 1 小时
        Date exp = new Date(expMillis);

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return JWT.create()
                .withClaim("id",user.getUserId())
                .withClaim("username", user.getUserName())
                .withClaim("role", user.getRole())
                .withIssuer("auth0")
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    /**
     * 验证jwt token
     * @param token Token字符串
     * @return 解码后的JWT对象
     */
    public static DecodedJWT verifyJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception) {
            // Invalid signature/claims
            throw new RuntimeException("Invalid token: " + exception.getMessage());
        }
    }

    /**
     * 从Token获取用户ID
     * @param token Token字符串
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("id").asLong();
    }

    /**
     * 从Token获取角色
     * @param token Token字符串
     * @return 角色字符串
     */
    public static String getRoleFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("role").asString();
    }

    /**
     * 检查Token是否过期
     * @param token Token字符串
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        Date expiresAt = decodedJWT.getExpiresAt();
        return expiresAt.before(new Date());
    }

    /**
     * 检查Token是否在黑名单中
     * @param token Token字符串
     * @return 是否在黑名单中
     */
    public static boolean isTokenInBlacklist(String token) {
        // TODO: 添加黑名单检查逻辑
        return false;
    }
}