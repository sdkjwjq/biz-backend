package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.TokenBlacklist;

@Mapper
public interface TokenBlacklistMapper {
    @Insert("INSERT INTO token_blacklist(token, expiry_time) VALUES(#{token}, #{expiryTime})")
    void addToBlacklist(TokenBlacklist tokenBlacklist);

    @Select("SELECT * FROM token_blacklist WHERE token = #{token}")
    TokenBlacklist findByToken(String token);
}

