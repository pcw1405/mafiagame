package com.example.mafiagame.service;

import com.example.mafiagame.dto.ChatMessage;
import com.example.mafiagame.entity.Game;
import com.example.mafiagame.entity.MainGame;
import com.example.mafiagame.entity.MiniGame;
import com.example.mafiagame.repository.MainGameRepository;
import com.example.mafiagame.repository.MiniGameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@Log4j2
@RequiredArgsConstructor
public class GameService {
    private static final String REDIS_KEY_PREFIX = "Choice:";

    private final MiniGameRepository miniGameRepository;
    private final MainGameRepository mainGameRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public enum Hand {
        SCISSORS, ROCK, PAPER
    }

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
        return miniGameRepository.save(game);
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


    public String settingGame(Long gameId, String nickName, String gameType) {
        if ("mafia".equals(gameType)) {
            Optional<MainGame> optionalGame = mainGameRepository.findById(gameId);
            if (optionalGame.isPresent()) {
                MainGame game = optionalGame.get();
                return determineGameResult(game, nickName, gameType);
            }
        } else {
            Optional<MiniGame> optionalGame = miniGameRepository.findById(gameId);
            if (optionalGame.isPresent()) {
                MiniGame game = optionalGame.get();
                return determineGameResult(game, nickName, gameType);
            }
        }
        return "gameId no";
    }

    // 나중에 스페셜게임을 추가할 수 도 있으니까 수정하기 쉽게 코드를 작성

    private String determineGameResult(Game game, String nickName, String gameType) {
        String player1Nickname = game.getPlayer1();
        String player2Nickname = game.getPlayer2();

        String player1Choice = getChoice(game.getId(), player1Nickname);
        String player2Choice = getChoice(game.getId(), player2Nickname);

        if (player1Choice == null || player2Choice == null) {
            log.info("아직 선택이 다 이루어지지 않았습니다");
            return "선택미완료";
        }

        String winner;
        String loser;
        if (player1Choice.equals(player2Choice)) {
            winner = null;
            loser = null;
            game.setWinner(null);
        } else if ((player1Choice.equals("SCISSORS") && player2Choice.equals("PAPER")) ||
                (player1Choice.equals("ROCK") && player2Choice.equals("SCISSORS")) ||
                (player1Choice.equals("PAPER") && player2Choice.equals("ROCK"))) {
            winner = player1Nickname;
            loser = player2Nickname;
            game.setWinner(player1Nickname);
            game.setLoser(player2Nickname);
            if ("mafia".equals(gameType)) {
                ((MainGame) game).setPlayer1Wins(((MainGame) game).getPlayer1Wins() + 1);
            }
        } else {
            winner = player2Nickname;
            loser = player1Nickname;
            game.setWinner(player2Nickname);
            game.setLoser(player1Nickname);
            if ("mafia".equals(gameType)) {
                ((MainGame) game).setPlayer2Wins(((MainGame) game).getPlayer2Wins() + 1);
            }
        }

        if ("mafia".equals(gameType)) {
            mainGameRepository.save((MainGame) game);
        } else {
            miniGameRepository.save((MiniGame) game);
        }

        if (winner != null) {
            return winner + "," + loser;
        } else {
            return "draw";
        }
    }

    public ChatMessage handleGameResult(ChatMessage message, String result, long gameId) {
        if (result.equals("무승부")) {
            message.setType(ChatMessage.MessageType.GAME_RESULT);
            message.setMessage("무승부입니다");
//        chatService.sendChatMessage(message, result);
            clearGameData(gameId);
        } else if (result.equals("선택미완료")) {
            log.info("선택미완료");
        } else {
            String[] parts = result.split(",");
            if (parts.length == 2) {
                String winner = parts[0];
                String loser = parts[1];
                log.info("Winner: " + winner);
                log.info("Loser: " + loser);

                message.setType(ChatMessage.MessageType.GAME_RESULT);
                message.setMessage("축하합니다 " + winner + "가 이겼습니다");
                message.setTarget(result);
//              chatService.sendChatMessage(message, result);
                clearGameData(gameId);
            } else {
                log.error("Unexpected result format: " + result);
            }
        }
        return message;
    }
}
