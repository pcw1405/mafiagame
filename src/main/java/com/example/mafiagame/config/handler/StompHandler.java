package com.example.mafiagame.config.handler;

import com.example.mafiagame.dto.ChatMessage;
import com.example.mafiagame.repository.ChatRoomRepository;
import com.example.mafiagame.service.ChatService;
import com.example.mafiagame.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        // websocket 연결시 헤더의 jwt token 검증
        if (StompCommand.CONNECT == accessor.getCommand()) {
            String jwtToken=accessor.getFirstNativeHeader("token");
//            위 코드는 WebSocket 통신을 통해 받은 "token"이라는 이름의 헤더 값을 가져와서 jwtToken에 저장합니다.

            log.info("CONNECT {}",jwtToken);
            jwtTokenProvider.validateToken(accessor.getFirstNativeHeader("token"));
        //
        } else if (StompCommand.SUBSCRIBE==accessor.getCommand()) {

            String roomId=chatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            //simpDestination 헤더는 사용자가 구독하거나 메시지를 전송할 때 대상 경로 (채팅방 ID를 추출할 수 있다)
            // 세션 아이디를 불러온다
            //왜 굳이 세션아이디를 만드는 것인지 파악해본다
            //세션 ID는 사용자가 웹소켓 연결을 맺을 때 Spring WebSocket 프레임워크에 의해 자동으로 생성된다.

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName(); // 사용자 이름 가져오기
            log.info("현재 사용자: {}", email);

            chatRoomRepository.setUserEnterInfo(sessionId,roomId);
            chatRoomRepository.setUserEnterInfo(sessionId,roomId,email);

            chatRoomRepository.plusUserCount(roomId);

            String name=Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).roomId(roomId).sender(name).build());
            log.info("SUBSCRIBED {}, {}",sessionId,roomId);

        } else if (StompCommand.DISCONNECT==accessor.getCommand()) {

            String sessionId=(String) message.getHeaders().get("simpSessionId");
            String roomId= chatRoomRepository.getUserEnterRoomId(sessionId);
            //채팅방의 인원수를 -1한다.
            chatRoomRepository.minusUserCount(roomId);
//            클라이언트 퇴장 메시지를 채팅방에 발송한다
            String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.QUIT).roomId(roomId).sender(name).build());
            //퇴장한 클라이언트의 roomId 맵핑 정보를 삭제한다.
            chatRoomRepository.removeUserEnterInfo(sessionId);
            log.info("DISCONNECTED {}, {}",sessionId,roomId);
        }
        return message;
    }

}
