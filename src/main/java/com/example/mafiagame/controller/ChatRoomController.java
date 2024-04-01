package com.example.mafiagame.controller;

import com.example.mafiagame.dto.ChatRoom;
import com.example.mafiagame.dto.LoginInfo;
import com.example.mafiagame.repository.ChatRoomRepository;
import com.example.mafiagame.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
@Slf4j
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/room")
    public String rooms() {
        return "/chat/room";
    }

    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        return chatRoomRepository.findAllRoom();
    }

    @PostMapping("/room")
    @ResponseBody
    public ResponseEntity<?> createRoom(@RequestParam(required = true) String name) {
        try {
            log.info("채팅방 생성 요청 받음 - 이름: {}", name);
            ChatRoom chatRoom = chatRoomRepository.createChatRoom(name);
            return ResponseEntity.ok(chatRoom);
        } catch (IllegalArgumentException ex) {
            // 이름 파라미터가 누락된 경우
            return ResponseEntity.badRequest().body("이름 파라미터가 누락되었습니다.");
        } catch (Exception ex) {
            // 채팅방 생성 실패나 다른 예외 상황에 대한 처리
            log.error("채팅방 생성 실패.", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("채팅방 생성에 실패하였습니다.");
        }
    }
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        return chatRoomRepository.findRoomById(roomId);
    }

    @GetMapping("/user")
    @ResponseBody
    public LoginInfo getUserInfo() {
        log.info("getUserInfo start");
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                log.info("인증되었습니다.");
                String name = auth.getName();
                log.info("이름은"+ name);
                String token = jwtTokenProvider.generateToken(name);
                log.info("토큰 클리어");
                return LoginInfo.builder().name(name).token(token).build();
            } else {
                // 인증되지 않은 경우에 대한 처리
                log.warn("인증되지 않았습니다.");
                throw new RuntimeException("User is not authenticated");
            }
        } catch (RuntimeException e) {
            log.error("런타임 예외가 발생했습니다: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 예상치 못한 예외 발생 시 로깅하고 다시 던지기
            log.error("Error occurred while fetching user information: {}", e.getMessage());
            throw new RuntimeException("Internal Server Error");
        }
    }
}
