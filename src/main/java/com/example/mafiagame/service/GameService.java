package com.example.mafiagame.service;

import com.example.mafiagame.entity.MiniGame;
import com.example.mafiagame.repository.MiniGameRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class GameService {
    private static final String REDIS_KEY_PREFIX = "Choice:";

    @Autowired
    private MiniGameRepository miniGame;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 상대방의 선택 데이터를 Redis에 저장하는 메서드
    public void saveChoice(Long gameId, String nickname, String choice) {
        String redisKey = REDIS_KEY_PREFIX + gameId + ":" + nickname;
        redisTemplate.opsForValue().set(redisKey, choice);
    }

    // Redis에서 상대방의 선택 데이터를 가져오는 메서드
    public String getChoice(Long gameId, String nickname) {
        String redisKey = REDIS_KEY_PREFIX + gameId + ":" + nickname;
        return redisTemplate.opsForValue().get(redisKey);
    }

    public void setCurrentGameId(String userName, long gameId) {
        String redisKey = "CurrentGameId:" + userName;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(gameId));
    }

    public Long getCurrentGameId(String userName) {
        String redisKey = "CurrentGameId:" + userName;
        String gameId = redisTemplate.opsForValue().get(redisKey);
        if (gameId != null) {
            return Long.parseLong(gameId);
        }
        return null;
    }

    public String determineGameResult(Long gameId,String nickName) {

        MiniGame miniGamePlay=miniGame.findByGameId(gameId);
        String player1Nickname=miniGamePlay.getPlayer1();
        String player2Nickname=miniGamePlay.getPlayer2();

        String player1Choice = getChoice(gameId, player1Nickname);
        String player2Choice = getChoice(gameId, player2Nickname);

        if (player1Choice == null || player2Choice == null) {
            // 선택이 모두 이루어지지 않은 경우
            log.info("아직 선택이 다 이루어지지 않았습니다");
            return "둘 중 한 명의 선택이 아직 이루어지지 않았습니다.";
        }

        if (player1Choice.equals(player2Choice)) {
            // 비긴 경우
            return "무승부입니다.";
        }

        if ((player1Choice.equals("가위") && player2Choice.equals("보")) ||
                (player1Choice.equals("바위") && player2Choice.equals("가위")) ||
                (player1Choice.equals("보") && player2Choice.equals("바위"))) {
            // player1이 이기는
            String Winner =player1Nickname;
            miniGamePlay.setWinner(player2Nickname);

            return player1Nickname + "님이 이겼습니다.";
        } else {
            // player2가 이기는 경우
            String Winner =player2Nickname;
            miniGamePlay.setWinner(player1Nickname);
            return player2Nickname + "님이 이겼습니다.";
        }
    }
}
