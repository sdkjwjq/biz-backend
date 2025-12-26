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

public class JWTUtil {
    private static final String SECRET_KEY = "secretKey"; // 与生成 Token 时使用的密钥一致
    //生成jwt
    public static String generateJwtToken(SysUser user) {
        long nowMillis = System.currentTimeMillis();
//        Date now = new Date(nowMillis);
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
    //验证jwt
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

    public static Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        return decodedJWT.getClaim("id").asLong();
    }

//    isTokenExpired
    public static boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = verifyJwtToken(token);
        Date expiresAt = decodedJWT.getExpiresAt();
        return expiresAt.before(new Date());
    }

//    isTokenInBlacklist

    public static boolean isTokenInBlacklist(String token) {
        // TODO: 添加黑名单检查逻辑
        return false;
    }

}
