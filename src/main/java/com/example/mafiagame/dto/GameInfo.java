package com.example.mafiagame.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GameInfo {

    private String gameId;
    private String roomId;
    private String player1;
    private String player2;
}
