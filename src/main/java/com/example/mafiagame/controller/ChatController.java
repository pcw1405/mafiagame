package com.example.mafiagame.controller;

import com.example.mafiagame.dto.ChatMessage;
import com.example.mafiagame.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
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

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final ChannelTopic channelTopic;


    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("token") String token) {
        String nickname = jwtTokenProvider.getUserNameFromJwt(token);
        // 로그인 회원 정보로 대화명 설정
        message.setSender(nickname);
        // 채팅방 입장시에는 대화명과 메시지를 자동으로 세팅한다.
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            message.setSender("[알림]");
            message.setMessage(nickname + "님이 입장하셨습니다.");
        }
        // Websocket에 발행된 메시지를 redis로 발행(publish)
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
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

