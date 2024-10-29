package com.example.mafiagame.service;

import com.example.mafiagame.entity.MainGame;
import com.example.mafiagame.repository.MainGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class MainGameServiceTest {

    @MockBean
    private MainGameRepository mainGameRepository;

    @Autowired
    private MainGameService mainGameService;

    private MainGame mainGame;

    @BeforeEach
    public void setup() {
        mainGame = new MainGame();
        mainGame.setId(1L);
        mainGame.setPlayer1("player1");
        mainGame.setPlayer2("player2");
        mainGame.setPlayer1Wins(0);
        mainGame.setPlayer2Wins(0);
    }

    @Test
    public void testIncrementPlayerWins_Player1_Success() {
        when(mainGameRepository.findById(1L)).thenReturn(Optional.of(mainGame));

        mainGameService.incrementPlayerWins(1L, "player1", "gameType");

        assertEquals(1, mainGame.getPlayer1Wins());
        verify(mainGameRepository, times(1)).save(mainGame);
    }

    @Test
    public void testIncrementPlayerWins_Player2_Success() {
        when(mainGameRepository.findById(1L)).thenReturn(Optional.of(mainGame));

        mainGameService.incrementPlayerWins(1L, "player2", "gameType");

        assertEquals(1, mainGame.getPlayer2Wins());
        verify(mainGameRepository, times(1)).save(mainGame);
    }

    @Test
    public void testIncrementPlayerWins_MainGameNotFound() {
        when(mainGameRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MainGameNotFoundException.class, () -> mainGameService.incrementPlayerWins(1L, "player1", "gameType"));
    }

    @Test
    public void testIncrementPlayerWins_InvalidPlayer() {
        when(mainGameRepository.findById(1L)).thenReturn(Optional.of(mainGame));

        assertThrows(InvalidPlayerException.class, () -> mainGameService.incrementPlayerWins(1L, "invalidPlayer", "gameType"));
    }

    @Test
    public void testGetMainGame_Success() {
        when(mainGameRepository.findById(1L)).thenReturn(Optional.of(mainGame));

        MainGame result = mainGameService.getMainGame(1L);

        assertEquals(mainGame, result);
    }

    @Test
    public void testGetMainGame_MainGameNotFound() {
        when(mainGameRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MainGameNotFoundException.class, () -> mainGameService.getMainGame(1L));
    }
}