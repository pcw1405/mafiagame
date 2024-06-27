package com.example.mafiagame.service;


import com.example.mafiagame.entity.MainGame;
import com.example.mafiagame.repository.MainGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MainGameService {

    @Autowired
    private MainGameRepository mainGameRepository;

    public void incrementPlayerWins(Long mainGameId, String playerNickname, String gameType) {
        Optional<MainGame> optionalMainGame = mainGameRepository.findById(mainGameId);

        if (optionalMainGame.isPresent()) {
            MainGame mainGame = optionalMainGame.get();

            if (mainGame.getPlayer1().equals(playerNickname)) {
                mainGame.setPlayer1Wins(mainGame.getPlayer1Wins() + 1);
            } else if (mainGame.getPlayer2().equals(playerNickname)) {
                mainGame.setPlayer2Wins(mainGame.getPlayer2Wins() + 1);
            }

            mainGameRepository.save(mainGame);
        }
    }

    public MainGame getMainGame(Long mainGameId) {
        return mainGameRepository.findById(mainGameId).orElseThrow(() -> new IllegalArgumentException("Invalid main game ID"));
    }
}
