package com.example.mafiagame.service;

import com.example.mafiagame.config.SessionManager;
import com.example.mafiagame.dto.ChatMessage;
import com.example.mafiagame.entity.Game;
import com.example.mafiagame.entity.MainGame;
import com.example.mafiagame.entity.MiniGame;
import com.example.mafiagame.repository.ChatRoomRepository;
import com.example.mafiagame.repository.MiniGameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final MiniGameRepository miniGameRepository;
    private final GameService gameService;

//    destination 정보에서 roomId정보 추출
    public String getRoomId(String destination){
        int lastIndex =destination.lastIndexOf('/');
        if(lastIndex != -1 )
            return destination.substring(lastIndex+1);
        else
            return "";
    }

//    오버라이딩
    public void sendChatMessage(ChatMessage chatMessage, String request) {
        log.info("sendChatMessage2 start");
        // 두 번째 인자를 활용한 새로운 메서드 구현 내용...
        chatMessage.setUserCount(chatRoomRepository.getUserCount(chatMessage.getRoomId()));
        if (chatMessage.getType().equals(ChatMessage.MessageType.GAME_REQUEST_ACCEPT)){
            chatMessage.setMessage(chatMessage.getSender()+"님이 게임요청을 수락했습니다");
            String gameMaker=request;

            Game game;

            if(chatMessage.getGameType().equals("mafia")) {
                game = gameService.createMainGame(gameMaker, chatMessage.getSender());
            }else{
                game = gameService.createMiniGame(gameMaker, chatMessage.getSender());
            }

//            game.setPlayer2(chatMessage.getSender());
//            game.setPlayer1(gameMaker);

//            miniGameRepository.save(game);
            long gameId=game.getId();
            gameService.clearGameData(gameId);

//            log.info("chatService session line check");
//            player1이라기 보단 엔티티 안에서 gamemaker라는 변수가 더 직관적이라 좋지 않을까?
            gameService.setCurrentGameId(game.getPlayer1(),gameId);
            gameService.setCurrentGameId(game.getPlayer2(),gameId);
            long gameIdCheck = gameService.getCurrentGameId(chatMessage.getSender());
//            sessionManager.setCurrentGameId(game.getPlayer1(), gameId);
//            sessionManager.setCurrentGameId(game.getPlayer2(), gameId);
//            long gameIdCheck = sessionManager.getCurrentGameId(gameMaker);
            log.info("gamId: {}", gameIdCheck);

            chatMessage.setSender("[시스템]");

        }
//        else if(chatMessage.getType().equals(ChatMessage.MessageType.GAME_REQUEST)){
//            chatMessage.setMessage(chatMessage.getSender()+"님의 게임요청");
//            chatMessage.setSender("[시스템]");
//
//        } else if(chatMessage.getType().equals(ChatMessage.MessageType.GAME_RESULT)){
//            chatMessage.setSender("[시스템]");
//
//        }
        //GAME_REQUEST_ACCEPT
        //GAME_REQUEST_REJECT
        redisTemplate.convertAndSend(channelTopic.getTopic(),chatMessage);
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
        }else if (chatMessage.getType().equals(ChatMessage.MessageType.GAME_REQUEST_REJECT)){
            chatMessage.setMessage(chatMessage.getSender()+"님이 게임요청을 거절했습니다");
            chatMessage.setSender("[시스템]");

        }else if (chatMessage.getType().equals(ChatMessage.MessageType.GAME_RESPONSE)){
            chatMessage.setMessage(chatMessage.getSender()+"님이 선택을 했습니다");
            chatMessage.setSender("[시스템]");

        }else if(chatMessage.getType().equals(ChatMessage.MessageType.GAME_REQUEST)){
            chatMessage.setMessage(chatMessage.getSender()+"님의 게임요청");
            chatMessage.setSender("[시스템]");

        } else if(chatMessage.getType().equals(ChatMessage.MessageType.GAME_RESULT)){
            chatMessage.setSender("[시스템]");

        }
            //GAME_REQUEST_ACCEPT
        //GAME_REQUEST_REJECT
        redisTemplate.convertAndSend(channelTopic.getTopic(),chatMessage);
    }

}
