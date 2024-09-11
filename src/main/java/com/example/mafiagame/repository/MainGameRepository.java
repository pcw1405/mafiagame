package com.example.mafiagame.repository;

import com.example.mafiagame.entity.MainGame;
import com.example.mafiagame.entity.MiniGame;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MainGameRepository extends JpaRepository<MainGame, Long> {


//    @Query(value = "SELECT * FROM maingame WHERE winner = :winner AND created_at > :startDate", nativeQuery = true)
//    List<MainGame> findGamesByWinnerAndDate(@Param("winner") String winner, @Param("startDate") LocalDate startDate);

    //findGamesByWinnerAndDate("John", LocalDate.of(2023, 1, 1))와
    // 같이 호출하면 John이 승리한 2023년 1월 1일 이후의 모든 게임 기록을 조회하게 됩니다.

    @Modifying
    @Query(value = "DELETE FROM maingame WHERE winner = :winner", nativeQuery = true)
    void deleteGamesByWinner(@Param("winner") String winner);
    //JPA는 기본적으로 1차 캐시(엔티티 매니저의 영속성 컨텍스트)를 사용하기 때문에
    // 대용량 데이터를 처리할 때 메모리 사용량이 급격히 증가할 수 있습니다.

    // 네이티브 쿼리 예시
    @Query(value = "SELECT player1, player2, SUM(player1_wins) AS player1Wins, SUM(player2_wins) AS player2Wins, SUM(draw) AS draws " +
            "FROM maingame WHERE (player1 = :player1 AND player2 = :player2) OR (player1 = :player2 AND player2 = :player1) " +
            "GROUP BY player1, player2", nativeQuery = true)
    List<Object[]> findMatchStatistics(@Param("player1") String player1, @Param("player2") String player2);
    //JPA로 동일한 쿼리를 작성하려면 복잡한 JPQL을 사용하거나 여러 개의 엔티티를 맵핑해야 할 수 있습니다.
    //JPA에서는 이러한 집계 연산을 위해 일련의 코드 로직이 필요하며, 엔티티 매니저의 관리(1차 캐시, 영속성 컨텍스트)로 인해 성능 저하가 발생할 수 있습니다.
    //findMatchStatistics("Alice", "Bob")를 호출하면 Alice와 Bob이 서로 경기한 모든 결과를 집계하여 Alice의 승리 횟수, Bob의 승리 횟수, 그리고 무승부 횟수를 반환합니다.
}
