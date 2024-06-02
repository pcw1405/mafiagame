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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    //이메일 활용
    public void setUserEnterInfo(String sessionId,String roomId, String email){
        // 이메일을 키로 사용하여 roomId를 저장
        hashOpsEnterInfo.put(ENTER_INFO, email + "_roomId", roomId);
        // 이메일을 키로 사용하여 sessionId를 저장
        hashOpsEnterInfo.put(ENTER_INFO, email + "_sessionId", sessionId);
    }
    //룸아이디에 해당하는 이메일을 찾는 로직을 만들기 위해서 굳이 이렇게 _과 문자열로 합쳐준다

    public String getRoomIdByEmail(String email) {
        return (String) hashOpsEnterInfo.get(ENTER_INFO, email + "_roomId");
    }

    public String getSessionIdByEmail(String email) {
        return (String) hashOpsEnterInfo.get(ENTER_INFO, email + "_sessionId");
    }

    public List<String> getEmailsByRoomId(String roomId) {
        List<String> emails = new ArrayList<>();

        // 모든 키를 가져오기
        Set<String> keys = hashOpsEnterInfo.keys(ENTER_INFO);

        for (String key : keys) {
            // "_roomId"로 끝나는 키를 필터링
            if (key.endsWith("_roomId")) {
                // 키에서 이메일 부분 추출
                String email = key.substring(0, key.length() - "_roomId".length());
                // 해당 키의 값이 주어진 roomId와 일치하는지 확인
                String storedRoomId = (String) hashOpsEnterInfo.get(ENTER_INFO, key);
                if (roomId.equals(storedRoomId)) {
                    emails.add(email);
                }
            }
        }

        return emails;
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
