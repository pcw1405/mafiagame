package com.example.mafiagame.repository;

import com.example.mafiagame.dto.ChatRoom;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomRepository {
    // Redis
    private static final String CHAT_ROOMS = "CHAT_ROOM";

    public static final String USER_COUNT = "USER_COUNT"; //
    public static final String ENTER_INFO ="ENTER_INFO"; // 채팅룸 입장


//    private final RedisTemplate<String, Object> redisTemplate;

    @Resource(name="redisTemplate")
    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;

    @Resource(name="redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;

    @Resource(name="redisTemplate")
    private ValueOperations<String, String> valueOps;
//    @PostConstruct
//    private void init() {
//        opsHashChatRoom = redisTemplate.opsForHash();
//    }

    // 모든 채팅방 조회
    public List<ChatRoom> findAllRoom() {

        return hashOpsChatRoom.values(CHAT_ROOMS);
    }

    // 특정 채팅방 조회
    public ChatRoom findRoomById(String id) {

        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    // 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    //유저가 입장한 채팅방 ID와 유저 세션 ID매핑 정보 저장
    public void setUserEnterInfo(String sessionId,String roomId){
        hashOpsEnterInfo.put(ENTER_INFO,sessionId,roomId);
    }

    //유저 세션으로 입장해 있는 채팅방 ID 조회
    public String getUserEnterRoomId(String sessionId){
        return hashOpsEnterInfo.get(ENTER_INFO,sessionId);
    }

    //유저 세션 정보와 맵핑된 채팅방ID 삭제
    public void removeUserEnterInfo(String sessionId){
        hashOpsEnterInfo.delete(ENTER_INFO,sessionId);
    }

    //채팅방 유저수 조회
    public long getUserCount(String roomId){
        return Long.valueOf(Optional.ofNullable(valueOps.get(USER_COUNT+"_"+roomId)).orElse("0"));
    }

    // 채팅방에 입장한 유저수 +1
    public long plusUserCount(String roomId){
        return Optional.ofNullable(valueOps.increment(USER_COUNT+"_"+roomId)).orElse(0L);
    }

    public long minusUserCount(String roomId){
        return Optional.ofNullable(valueOps.decrement(USER_COUNT+"_"+roomId)).filter(count -> count > 0).orElse(0L);
    }
}
