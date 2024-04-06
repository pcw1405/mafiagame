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
        ENTER,QUIT,TALK
    }

    @Builder
    public ChatMessage(MessageType type,String roomId,String sender,String message,long userCount){
        this.type=type;
        this.roomId=roomId;
        this.sender=sender;
        this.message=message;
        this.userCount=userCount;
    }

    private MessageType type;
    private String roomId; //방번호
    private String sender; // 메시지 보낸 사람
    private String message; // 메시지
    private long userCount;


}
