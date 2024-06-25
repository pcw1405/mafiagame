package com.example.mafiagame.repository;

import com.example.mafiagame.entity.MainGame;
import com.example.mafiagame.entity.MiniGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainGameRepository extends JpaRepository<MainGame, Long> {
}
