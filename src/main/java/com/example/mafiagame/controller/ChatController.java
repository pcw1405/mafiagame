package com.example.mafiagame.controller;

import com.example.mafiagame.config.SessionManager;
import com.example.mafiagame.dto.ChatMessage;
import com.example.mafiagame.entity.MiniGame;
import com.example.mafiagame.repository.ChatRoomRepository;
import com.example.mafiagame.repository.MiniGameRepository;
import com.example.mafiagame.service.ChatService;
import com.example.mafiagame.service.GameServcie;
import com.example.mafiagame.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
@Slf4j
public class ChatController {

//    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
//    private final ChannelTopic channelTopic;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;
    private final MiniGameRepository miniGameRepository;
    private final GameServcie gameServcie;
    private final SessionManager sessionManager;

    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("token") String token) {
        String nickname = jwtTokenProvider.getUserNameFromJwt(token);
        // 로그인 회원 정보로 대화명 설정
        message.setSender(nickname);
        // 채팅방 인원수 세팅
        message.setUserCount(chatRoomRepository.getUserCount(message.getRoomId()));

        // 채팅방 입장시에는 대화명과 메시지를 자동으로 세팅한다.

        // Websocket에 발행된 메시지를 redis로 발행(publish)
//        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
        chatService.sendChatMessage(message);
    }

    @MessageMapping("/chat/miniGame")
    public void miniGame(ChatMessage message, @Header("token") String token,@Header("request") String request) {
        String nickname = jwtTokenProvider.getUserNameFromJwt(token);
        // 로그인 회원 정보로 대화명 설정
        message.setSender(nickname);
        // 채팅방 인원수 세팅
        message.setUserCount(chatRoomRepository.getUserCount(message.getRoomId()));

        String gameMaker;
        String choice;


        log.info("request: {}", request);
        log.info("nickname: {}", nickname);
        // 채팅방 입장시에는 대화명과 메시지를 자동으로 세팅한다.
        // Websocket에 발행된 메시지를 redis로 발행(publish)
//        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
        if (message.getType().equals(ChatMessage.MessageType.GAME_REQUEST_ACCEPT)){
            gameMaker=request;
            chatService.sendChatMessage(message,gameMaker);

        } else if (message.getType().equals(ChatMessage.MessageType.GAME_REQUEST)){
            gameMaker=request;
            chatService.sendChatMessage(message,gameMaker);

        } else if (message.getType().equals(ChatMessage.MessageType.GAME_RESPONSE)){
            choice=request;

            chatService.sendChatMessage(message,choice);
            long gameId = sessionManager.getCurrentGameId(nickname);

            gameServcie.saveOpponentChoice(choice,gameId);

        } else {
            chatService.sendChatMessage(message);
        }

    }


}

