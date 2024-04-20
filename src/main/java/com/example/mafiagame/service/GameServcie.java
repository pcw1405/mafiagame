package com.example.mafiagame.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameServcie {
    private static final String REDIS_KEY_PREFIX = "opponentChoice:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 상대방의 선택 데이터를 Redis에 저장하는 메서드
    public void saveOpponentChoice(String gameId, String opponentChoice) {
        String redisKey = REDIS_KEY_PREFIX + gameId;
        redisTemplate.opsForValue().set(redisKey, opponentChoice);
    }

    // Redis에서 상대방의 선택 데이터를 가져오는 메서드
    @Service
    public class GameService {
        private static final String REDIS_KEY_PREFIX = "opponentChoice:";

        @Autowired
        private RedisTemplate<Long, String> redisTemplate;

        // 상대방의 선택 데이터를 Redis에 저장하는 메서드
        public void saveOpponentChoice(Long gameId, String opponentChoice) {
            String redisKey = REDIS_KEY_PREFIX + gameId;
            redisTemplate.opsForValue().set(redisKey, opponentChoice);
        }

        // Redis에서 상대방의 선택 데이터를 가져오는 메서드
        public String getOpponentChoice(String gameId) {
            String redisKey = REDIS_KEY_PREFIX + gameId;
            return redisTemplate.opsForValue().get(redisKey);
        }
    }
}
