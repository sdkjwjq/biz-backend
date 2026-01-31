package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.TokenBlacklist;

/**
 * Token黑名单数据访问接口
 */
@Mapper
public interface TokenBlacklistMapper {
    /**
     * 添加Token到黑名单
     * @param tokenBlacklist Token黑名单实体
     */
    @Insert("INSERT INTO token_blacklist(token, expiry_time) VALUES(#{token}, #{expiryTime})")
    void addToBlacklist(TokenBlacklist tokenBlacklist);

    /**
     * 根据Token查询黑名单
     * @param token Token字符串
     * @return Token黑名单实体
     */
    @Select("SELECT * FROM token_blacklist WHERE token = #{token}")
    TokenBlacklist findByToken(String token);

    /**
     * 清理过期的Token
     */
    @Select("DELETE FROM token_blacklist WHERE expiry_time < NOW()")
    void cleanupExpiredTokens();
}