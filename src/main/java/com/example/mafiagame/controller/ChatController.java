package com.example.mafiagame.controller;

import com.example.mafiagame.config.SessionManager;
import com.example.mafiagame.dto.ChatMessage;
import com.example.mafiagame.entity.MiniGame;
import com.example.mafiagame.repository.ChatRoomRepository;
import com.example.mafiagame.repository.MiniGameRepository;
import com.example.mafiagame.service.ChatService;

import com.example.mafiagame.service.GameService;
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
    private final GameService gameService;


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
    public void miniGame(ChatMessage message, @Header("token") String token,@Header("request") String request,@Header(required = false, value = "receiver") String target ) {
        log.info("miniGame Controller start");
//        nickname= 현재 로그인한 사람
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
            message.setTarget(target);
            chatService.sendChatMessage(message,gameMaker);

        } else if (message.getType().equals(ChatMessage.MessageType.GAME_REQUEST)){
            gameMaker=request;
            message.setTarget(target);
            chatService.sendChatMessage(message,gameMaker);

        } else if (message.getType().equals(ChatMessage.MessageType.GAME_RESPONSE)){
            choice=request;
            log.info("( response part )  choice: {}", choice);

            log.info(" checkLine about session");
            long gameId = gameService.getCurrentGameId(nickname);
            log.info("(response part) gameId: {}", gameId);

            chatService.sendChatMessage(message);

            gameService.saveChoice(gameId,nickname ,choice);
//            chatService.sendChatMessage(message, choice);

            String testGetChoice=gameService.getChoice(gameId,nickname);
            log.info("redisTestGetChoice: {}", testGetChoice);

            String result =gameService.determineGameResult(gameId,nickname);
//            gameId를 매개체로 두 초이스가 있는지 확인을 한다
            if(result=="무승부"){
                message.setType(ChatMessage.MessageType.GAME_RESULT);
                message.setMessage("무승부입니다");
                gameService.clearGameData(gameId);


                chatService.sendChatMessage(message,result);

            }else if(result=="선택미완료"){
                    log.info("선택미완료");
            }else{
                message.setType(ChatMessage.MessageType.GAME_RESULT);
                message.setMessage("축하합니다"+result+"가 기였습니다");

                gameService.clearGameData(gameId);

                chatService.sendChatMessage(message,result);
            }
        } else {
            chatService.sendChatMessage(message);
        }

    }




}

