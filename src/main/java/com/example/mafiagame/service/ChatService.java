package com.example.mafiagame.service;

import com.example.mafiagame.dto.ChatMessage;
import com.example.mafiagame.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;
    private final ChatRoomRepository chatRoomRepository;

//    destination 정보에서 roomId정보 추출
    public String getRoomId(String destination){
        int lastIndex =destination.lastIndexOf('/');
        if(lastIndex != -1 )
            return destination.substring(lastIndex+1);
        else
            return "";
    }

//    채팅방에 메시지 발송
    public void sendChatMessage(ChatMessage chatMessage){
        chatMessage.setUserCount(chatRoomRepository.getUserCount(chatMessage.getRoomId()));
        if(ChatMessage.MessageType.ENTER.equals(chatMessage.getType())){
            chatMessage.setMessage(chatMessage.getSender()+ "님이 방에 입장하셨습니다. ");
            chatMessage.setSender("[알림]");
        }else if(ChatMessage.MessageType.QUIT.equals(chatMessage.getType())){
            chatMessage.setMessage(chatMessage.getSender()+"님이 방에서 나갔습니다.");
            chatMessage.setSender("[알림]");
        }else  if (chatMessage.getType().equals(ChatMessage.MessageType.GAME_REQUEST)) {
            chatMessage.setMessage(chatMessage.getSender()+"님의 게임요청");
            chatMessage.setSender("[시스템]");
        }else if (chatMessage.getType().equals(ChatMessage.MessageType.GAME_REQUEST_ACCEPT)){
            chatMessage.setMessage(chatMessage.getSender()+"님이 게임요청을 수락했습니다");
            chatMessage.setSender("[시스템]");
        }else if (chatMessage.getType().equals(ChatMessage.MessageType.GAME_REQUEST_REJECT)){
            chatMessage.setMessage(chatMessage.getSender()+"님이 게임요청을 거절했습니다");
            chatMessage.setSender("[시스템]");
        }else if (chatMessage.getType().equals(ChatMessage.MessageType.GAME_RESPONSE)){
//            chatMessage.setMessage(chatMessage.getSender()+"님이 게임요청을 거절했습니다");
            chatMessage.setSender("[시스템]");
        }
            //GAME_REQUEST_ACCEPT
        //GAME_REQUEST_REJECT
        redisTemplate.convertAndSend(channelTopic.getTopic(),chatMessage);
    }

}
