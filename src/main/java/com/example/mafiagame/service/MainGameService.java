package com.example.mafiagame.service;

import com.example.mafiagame.entity.MainGame;
import com.example.mafiagame.repository.MainGameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;
import java.util.UUID;

@Service
public class MainGameService {

    private static final Logger logger = LoggerFactory.getLogger(MainGameService.class);

    @Autowired
    private MainGameRepository mainGameRepository;

    public void incrementPlayerWins(Long mainGameId, String playerNickname, String gameType) {
        // 요청별 고유 UUID 생성 후 MDC에 추가
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        try {
            logger.info("Incrementing player wins for game ID: {}, player: {}, requestId: {}", mainGameId, playerNickname, requestId);

            Optional<MainGame> optionalMainGame = mainGameRepository.findById(mainGameId);

            if (optionalMainGame.isEmpty()) {
                throw new MainGameNotFoundException(mainGameId);
            }

            MainGame mainGame = optionalMainGame.get();

            if (mainGame.getPlayer1().equals(playerNickname)) {
                mainGame.setPlayer1Wins(mainGame.getPlayer1Wins() + 1);
            } else if (mainGame.getPlayer2().equals(playerNickname)) {
                mainGame.setPlayer2Wins(mainGame.getPlayer2Wins() + 1);
            } else {
                throw new InvalidPlayerException(playerNickname);
            }

            mainGameRepository.save(mainGame);
            logger.info("Successfully incremented wins for player: {}, requestId: {}", playerNickname, requestId);

        } catch (MainGameNotFoundException | InvalidPlayerException e) {
            logger.error("Error occurred: {}, requestId: {}", e.getMessage(), requestId);
            throw e;
        } finally {
            // 요청 완료 후 MDC 초기화
            MDC.clear();
        }
    }

    public MainGame getMainGame(Long mainGameId) {
        return mainGameRepository.findById(mainGameId)
                .orElseThrow(() -> new MainGameNotFoundException(mainGameId));
    }
}

// 사용자 정의 예외
@ResponseStatus(HttpStatus.NOT_FOUND)
class MainGameNotFoundException extends RuntimeException {
    public MainGameNotFoundException(Long mainGameId) {
        super("Main game not found with ID: " + mainGameId);
    }
}

// 사용자 정의 예외
@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidPlayerException extends RuntimeException {
    public InvalidPlayerException(String playerNickname) {
        super("Player nickname not found: " + playerNickname);
    }
}

