package com.example.mafiagame.service;

import com.example.mafiagame.entity.MainGame;
import com.example.mafiagame.entity.MiniGame;
import com.example.mafiagame.repository.MainGameRepository;
import com.example.mafiagame.repository.MiniGameRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@Log4j2
public class GameService {
    private static final String REDIS_KEY_PREFIX = "Choice:";

    @Autowired
    private MiniGameRepository miniGame;
    @Autowired
    private MainGameRepository mainGameRepository;

    @Autowired
    private MainGame mainGame;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public MainGame createMainGame(String player1, String player2) {
        MainGame game = new MainGame();
        game.setPlayer1(player1);
        game.setPlayer2(player2);
        return mainGameRepository.save(game);
    }

    private MiniGame createMiniGame(String player1, String player2) {
        MiniGame game = new MiniGame();
        game.setPlayer1(player1);
        game.setPlayer2(player2);
        return miniGame.save(game);
    }

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

    public void clearChoice(Long gameId, String nickname) {
        String redisKey = REDIS_KEY_PREFIX + gameId + ":" + nickname;
        redisTemplate.delete(redisKey);
    }

    public void clearCurrentGameId(String userName) {
        String redisKey = "CurrentGameId:" + userName;
        redisTemplate.delete(redisKey);
    }

    public void clearGameData(Long gameId) {
        Set<String> keys = redisTemplate.keys(REDIS_KEY_PREFIX + gameId + ":*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }


    public String determineGameResult(Long gameId, String nickName) {
        Optional<MiniGame> optionalMiniGamePlay = miniGame.findById(gameId);

        if (optionalMiniGamePlay.isPresent()) {
            MiniGame miniGamePlay = optionalMiniGamePlay.get();

            String player1Nickname = miniGamePlay.getPlayer1();
            String player2Nickname = miniGamePlay.getPlayer2();

            String player1Choice = getChoice(gameId, player1Nickname);
            String player2Choice = getChoice(gameId, player2Nickname);

            if (player1Choice == null || player2Choice == null) {
                // 선택이 모두 이루어지지 않은 경우
                log.info("아직 선택이 다 이루어지지 않았습니다");
                return "선택미완료";
            }

            String winner;
            String loser;
            if (player1Choice.equals(player2Choice)) {
                // 비긴 경우
                winner = null;
                loser = null;
                miniGamePlay.setWinner(null);
            } else if ((player1Choice.equals("SCISSORS") && player2Choice.equals("PAPER")) ||
                    (player1Choice.equals("ROCK") && player2Choice.equals("SCISSORS")) ||
                    (player1Choice.equals("PAPER") && player2Choice.equals("ROCK"))) {
                // player1이 이기는 경우
                winner = player1Nickname;
                System.out.println("이긴사람은"+winner);
                loser = player2Nickname;
                System.out.println("진 사람은"+loser);
                miniGamePlay.setWinner(player1Nickname);
                miniGamePlay.setLoser(player2Nickname);
            } else {
                // player2가 이기는 경우
                winner = player2Nickname;
                System.out.println("이긴사람은"+winner);

                loser = player1Nickname;
                System.out.println("진 사람은"+loser);
                miniGamePlay.setWinner(player2Nickname);
                miniGamePlay.setLoser(player1Nickname);
            }

            // 게임의 승자를 데이터베이스에 업데이트

            miniGame.save(miniGamePlay);
            // 메시지를
            if (winner != null) {
                return winner+","+loser;
            } else {
                return "draw";
            }
        } else {
            // 해당 gameId에 대한 게임이 존재하지 않는 경우
            return "gameId no";
        }
    }
}
