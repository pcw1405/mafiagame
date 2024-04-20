package com.example.mafiagame.repository;


import com.example.mafiagame.entity.MiniGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MiniGameRepository extends JpaRepository<MiniGame, Long> {
//    Optional<MiniGame> findById(Long gameId);

}
