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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    private static final String GAME_STATE_CACHE = "GameState:";

    public enum Hand {
        SCISSORS, ROCK, PAPER
    }

    public MainGame createMainGame(String player1, String player2) {
        MainGame game = new MainGame();
        game.setPlayer1(player1);
        game.setPlayer2(player2);
        return mainGameRepository.save(game);
    }

    public MiniGame createMiniGame(String player1, String player2) {
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


    public String playingGame(Long gameId, String nickName, String gameType) {
        log.info("Starting game with gameId: {}, nickName: {}, gameType: {}", gameId, nickName, gameType);

        // 캐시에서 게임 상태 조회
        Game game = getGameState(gameId, gameType);
        if (game == null) {
            log.info("Game not found in cache, checking database...");

            // 캐시에 없으므로 데이터베이스에서 조회
            if ("mafia".equals(gameType)) {
                Optional<MainGame> optionalGame = mainGameRepository.findById(gameId);
                if (optionalGame.isPresent()) {
                    game = optionalGame.get();
                    log.info("Game found in database for mafia gameType with gameId: {}", gameId);
                    return determineGameResult(game, nickName, gameType);
                } else {
                    log.error("Game with gameId {} not found in MainGame database", gameId);
                }
            } else {
                Optional<MiniGame> optionalGame = miniGameRepository.findById(gameId);
                if (optionalGame.isPresent()) {
                    game = optionalGame.get();
                    log.info("Game found in database for mini gameType with gameId: {}", gameId);
                    return determineGameResult(game, nickName, gameType);
                } else {
                    log.error("Game with gameId {} not found in MiniGame database", gameId);
                }
            }
        } else {
            log.info("Game found in cache for gameId: {}", gameId);

            return determineGameResult(game, nickName, gameType);

        }

        return "gameId no";
    }
    // 이 코드는 게임결과를 가져오기 위한 코드

    // 나중에 스페셜게임을 추가할 수 도 있으니까 수정하기 쉽게 코드를 작성

    private String determineGameResult(Game game, String nickName, String gameType) {
        String player1Nickname = game.getPlayer1();
        String player2Nickname = game.getPlayer2();

        String player1Choice = getChoice(game.getId(), player1Nickname);
        String player2Choice = getChoice(game.getId(), player2Nickname);

        if (player1Choice == null || player2Choice == null) {
            log.info("아직 선택이 다 이루어지지 않았습니다");
            log.info("Both players have not made their selections yet");
            return "선택미완료";
        }else{
            log.info("player1 ({}) 선택: {}", player1Nickname, player1Choice);
            log.info("player2 ({}) 선택: {}", player2Nickname, player2Choice);
        }

        String winner;
        String loser;
        if (player1Choice.equals(player2Choice)) {
            winner = null;
            loser = null;
            game.setWinner(null);

            if ("mafia".equals(gameType)) {
                ((MainGame) game).setDraw(((MainGame) game).getDraw() + 1);
            } else {
                ((MiniGame) game).setDraw(((MiniGame) game).getDraw() + 1);
            }

        } else if ((player1Choice.equals("SCISSORS") && player2Choice.equals("PAPER")) ||
                (player1Choice.equals("ROCK") && player2Choice.equals("SCISSORS")) ||
                (player1Choice.equals("PAPER") && player2Choice.equals("ROCK"))) {
            winner = player1Nickname;
            loser = player2Nickname;

            if ("mafia".equals(gameType)) {
                ((MainGame) game).setPlayer1Wins(((MainGame) game).getPlayer1Wins() + 1);
            }else{
                game.setWinner(player1Nickname);
                game.setLoser(player2Nickname);
            }
        } else {
            winner = player2Nickname;
            loser = player1Nickname;

            if ("mafia".equals(gameType)) {
                ((MainGame) game).setPlayer2Wins(((MainGame) game).getPlayer2Wins() + 1);
            }else{
                game.setWinner(player2Nickname);
                game.setLoser(player1Nickname);
            }
        }
        String finalMessage = null;
        if ("mafia".equals(gameType)) {
            MainGame mainGame = (MainGame) game;
            int totalRounds = mainGame.getPlayer1Wins() + mainGame.getPlayer2Wins() + (int) mainGame.getDraw();
            log.info(totalRounds);
            if (totalRounds >= 5) {

                finalMessage = determineFinalWinner(mainGame, player1Nickname, player2Nickname);
            }
//                if (mainGame.getPlayer1Wins() > mainGame.getPlayer2Wins()) {
//                    mainGame.setTotalWinner(player1Nickname);
//                    mainGame.setTotalLoser(player2Nickname);
//
//                    if(winner==null && loser==null){
//                        return player1Nickname + "," + player2Nickname+","+"final"+","+mainGame.getTotalWinner();
//                    }
//                    return winner + "," + loser+","+"final"+","+mainGame.getTotalWinner();
//                }else if (mainGame.getPlayer1Wins() < mainGame.getPlayer2Wins()) {
//                    mainGame.setTotalWinner(player2Nickname);
//                    mainGame.setTotalLoser(player1Nickname);
//                    if(winner==null && loser==null){
//                        return player1Nickname + "," + player2Nickname+","+"final"+","+mainGame.getTotalWinner();
//                    }
//                    return winner + "," + loser+","+"final"+","+mainGame.getTotalWinner();
//                }else{
//                    if(winner==null && loser==null){
//                        return player1Nickname + "," + player2Nickname+","+"totalTie";
//                    }
//
//                }
//
//
//

            mainGameRepository.save((MainGame) game);
        } else {
            miniGameRepository.save((MiniGame) game);
            //updateGameState가 있으니까 굳이 필요없는 코드일수도 있다
        }

          updateGameState(game);

        if (winner != null) {
            evictGameState(game.getId());
            return winner + "," + loser+","+finalMessage;

        } else {
            evictGameState(game.getId());
            return player1Nickname+","+player2Nickname+","+finalMessage+","+"draw";
        }
    }
//    이 코드는 게임 상태의 관리와 게임 데이터 저장 그리고 게임 결과 결정까지 하므로
    //

//    데이터가 더 늘어날지도 모르니까 json으로?
    public ChatMessage handleGameResult(ChatMessage message, String result, long gameId) {
        if (result.contains("draw")) {
            message.setType(ChatMessage.MessageType.GAME_RESULT);
            message.setMessage("무승부입니다");
//            if (result.contains("totalDraw")) {
//                message.setMessage("무승부입니다 최종적으로도 무승부입니다");
//            }
            message.setTarget(result);
            clearGameData(gameId);
        } else if (result.equals("선택미완료")) {
            log.info("선택미완료");
        } else {
            String[] parts = result.split(",");
            if (parts.length >= 2) {
                String winner = parts[0];
                String loser = parts[1];
                log.info("Winner: " + winner);
                log.info("Loser: " + loser);

                message.setType(ChatMessage.MessageType.GAME_RESULT);
                message.setMessage("축하합니다 " + winner + "가 이겼습니다");
                message.setTarget(result);
                clearGameData(gameId);

            } else {
                log.error("Unexpected result format: " + result);
            }
        }

        if (result.contains("최종적으로")){
            String[] parts = result.split(",");

            message.setMessage(message.getMessage() + parts[2]);
        }


        return message;
    }

    private String determineFinalWinner(MainGame mainGame, String player1Nickname, String player2Nickname) {
        int totalRounds = mainGame.getPlayer1Wins() + mainGame.getPlayer2Wins() + (int) mainGame.getDraw();
        log.info(totalRounds);

        if (totalRounds == 5) {
            if (mainGame.getPlayer1Wins() > mainGame.getPlayer2Wins()) {
                mainGame.setTotalWinner(player1Nickname);
                mainGame.setTotalLoser(player2Nickname);
                evictGameState(mainGame.getId());
                return "최종적으로" + mainGame.getTotalWinner()+"이 이겼습니다";
            } else if (mainGame.getPlayer1Wins() < mainGame.getPlayer2Wins()) {
                mainGame.setTotalWinner(player2Nickname);
                mainGame.setTotalLoser(player1Nickname);
                evictGameState(mainGame.getId());
                return "최종적으로" + mainGame.getTotalWinner()+"이 이겼습니다.";
            } else {
                evictGameState(mainGame.getId());
                return "최종적으로 무승부입니다";
            }
        }
        return null;
    }

    //게임 상태 캐싱
    //게임 상태를 조회할 때마다 데이터베이스를 참조하는 대신,
    // 캐시에 저장된 게임 상태를 사용하여 성능을 향상시킬 수 있습니다.
    // 게임 상태를 캐싱하여 조회하는 메서드
    @Cacheable(value = "gameStateCache", key = "#gameId")     //@Cacheable 어노테이션은 메서드의 결과를 캐시합니다.
    //value = "gameStateCache"는 캐시의 이름을 지정합니다. 이 캐시의 이름은 "gameStateCache"입니다.
    //key = "#gameId"는 캐시의 키를 gameId로 지정합니다. 즉, gameId를 기준으로 캐시를 저장하고 조회합니다.
    public Game getGameState(Long gameId, String gameType) {
        if ("mafia".equals(gameType)) {
            return mainGameRepository.findById(gameId).orElse(null);
        } else {
            return miniGameRepository.findById(gameId).orElse(null);
        }
    }

    //이 코드도 인터페이스인 game이라는 객체가 있을 때 사용할 수 있는 메서드군



    // 게임 상태를 업데이트하면서 캐시도 함께 업데이트하는 메서드
    //이것은 게임객체가 주어졌을 때 캐시를 저장할 수 있는 코드
    @CachePut(value = "gameStateCache", key = "#game.id") //@CachePut 어노테이션은 메서드 실행 결과를 캐시에 강제로 저장합니다.
    //value = "gameStateCache"는 해당 캐시의 이름을 지정합니다.
    //key = "#game.id"는 캐시의 키를 game 객체의 id로 지정합니다. 이때 game.getId()를 통해 id를 가져옵니다.
    public Game updateGameState(Game game) {

        if (game instanceof MainGame) {
            return mainGameRepository.save((MainGame) game);
            //게임의 객체 자체를 저장한다
        } else {
            return miniGameRepository.save((MiniGame) game);
        }
    }

    @CacheEvict(value = "gameStateCache", allEntries = true)
    public void evictAllGameStates() {
        // 캐시의 모든 항목을 제거
        log.info("All game states have been evicted from the cache.");
    }

    @CacheEvict(value = "gameStateCache", key = "#gameId")
    public void evictGameState(Long gameId) {
        // 캐시에서 gameId에 해당하는 항목을 제거
        log.info("Game state with gameId {} has been evicted from the cache.", gameId);
    }

    //부하 감소: 데이터베이스나 외부 API 호출 빈도를 줄여 서버 부하를 낮추고 성능을 개선합니다.
    //일관성 관리: 자주 변하지 않는 데이터의 경우? 캐시를 사용해 일관된 데이터 제공이 가능합니다.
    //빠른 접근: 캐시는 메모리에 저장되어 있어 데이터를 빠르게 읽을 수 있으며, 이로 인해 시스템 응답 속도가 빨라집니다.
    // 이제 이것을 어떻게 활용할 것인가
}
