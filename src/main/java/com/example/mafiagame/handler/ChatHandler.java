package com.example.mafiagame.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class ChatHandler extends TextWebSocketHandler {
    private static List<WebSocketSession> list=new ArrayList<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
        String payload = message.getPayload();
        log.info("payload:" + payload);
        //페이로드란 전송되는 데이터를 의미한다
//예를 들어 택배 배송을 보내고 받을 때 택배 물건이 페이로드고 송장이나 박스 등은 부가적은 것이기 때문에 페이로드가 아니다.

        for(WebSocketSession sess: list){
            sess.sendMessage(message);
        }
    }

    // Client가 접속 시 호출 되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        list.add(session);
        log.info(session + "클라이언트 접속");
    }

    // Client가 접속 해제 시 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception{
        log.info(session + "클라이언트 접속해제");
        list.remove(session);
    }
}
