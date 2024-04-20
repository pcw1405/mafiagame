package com.example.mafiagame.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private static final String REDIS_KEY_PREFIX = "Choice:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 상대방의 선택 데이터를 Redis에 저장하는 메서드
    public void saveOpponentChoice(Long gameId, String nickname, String choice) {
        String redisKey = REDIS_KEY_PREFIX + gameId + ":" + nickname;
        redisTemplate.opsForValue().set(redisKey, choice);
    }

    // Redis에서 상대방의 선택 데이터를 가져오는 메서드
    public String getOpponentChoice(Long gameId, String nickname) {
        String redisKey = REDIS_KEY_PREFIX + gameId + ":" + nickname;
        return redisTemplate.opsForValue().get(redisKey);
    }
}
