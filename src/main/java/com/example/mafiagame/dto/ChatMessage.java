package com.example.mafiagame.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMessage {

    public enum MessageType{
        ENTER,QUIT,TALK,GAME_REQUEST_ACCEPT,GAME_REQUEST,GAME_REQUEST_REJECT,GAME_RESPONSE
        ,GAME_RESULT
    }

    @Builder
    public ChatMessage(MessageType type,String roomId,String sender,String target,String message,long userCount,String gameType){
        this.type=type;
        this.roomId=roomId;
        this.sender=sender;
        this.message=message;
        this.target = target;
        this.userCount=userCount;
        this.gameType=gameType;
    }

    private MessageType type;
    private String roomId; //방번호
    private String sender; // 메시지 보낸 사람
    private String target;
    private String gameType;
    private String message; // 메시지
    private long userCount;


}
