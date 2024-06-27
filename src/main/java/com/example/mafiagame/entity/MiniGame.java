package com.example.mafiagame.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Table(name="minigame")
@Setter
@ToString
@NoArgsConstructor
public class MiniGame implements Game{

    @Id
    @Column(name="game_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String player1;
    private String player2;

    private String winner;
    private String loser;
    private long draw;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    @Override
    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    @Override
    public String getPlayer1() {
        return player1;
    }

    @Override
    public String getPlayer2() {
        return player2;
    }

    @Override
    public void setWinner(String winner) {
        this.winner = winner;
    }

    @Override
    public void setLoser(String loser) {
        this.loser = loser;
    }
}
