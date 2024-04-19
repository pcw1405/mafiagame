package com.example.mafiagame.controller;

import com.example.mafiagame.dto.ChatMessage;
import com.example.mafiagame.repository.ChatRoomRepository;
import com.example.mafiagame.service.ChatService;
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
    public void miniGame(ChatMessage message, @Header("token") String token,@Header("requester") String requester) {
        String nickname = jwtTokenProvider.getUserNameFromJwt(token);
        // 로그인 회원 정보로 대화명 설정
        message.setSender(nickname);
        // 채팅방 인원수 세팅
        message.setUserCount(chatRoomRepository.getUserCount(message.getRoomId()));
        String gameMaker=requester;
        log.info("게임요청자는",requester);
        log.info("게임수락한 사람은",nickname);
        // 채팅방 입장시에는 대화명과 메시지를 자동으로 세팅한다.

        // Websocket에 발행된 메시지를 redis로 발행(publish)
//        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
        if (message.getType().equals(ChatMessage.MessageType.GAME_REQUEST_ACCEPT)){
            chatService.sendChatMessage(message,gameMaker);

        }else{
            chatService.sendChatMessage(message);
        }

    }

//    @GetMapping("/templates/chat/user")
//    @ResponseBody
//    public String getUser() {
//        log.info("getUserInfo start");
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String name = null;
//        if (auth != null && auth.isAuthenticated()) {
//            name = auth.getName();
//        }
//        return name;
//    }
}

