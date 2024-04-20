package com.example.mafiagame.repository;


import com.example.mafiagame.entity.MiniGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MiniGameRepository extends JpaRepository<MiniGame, Long> {
    MiniGame findByPlayer1OrPlayer2(String player1, String player2);

}
