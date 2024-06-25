package com.example.mafiagame.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Table(name="game")
@Setter
@ToString
@NoArgsConstructor
public class MainGame implements Game{

    @Id
    @Column(name="game_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String player1;
    private String player2;

    private String winner;
    private String loser;
    private long draw;
}
