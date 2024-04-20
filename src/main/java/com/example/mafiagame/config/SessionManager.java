package com.example.mafiagame.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private final Map<String, Long> userGameIdMap = new ConcurrentHashMap<>();

    public void setCurrentGameId(String userName, long gameId) {
        userGameIdMap.put(userName, gameId);
    }

    public Long getCurrentGameId(String userName) {
        return userGameIdMap.get(userName);
    }

    public void removeCurrentGameId(String userName) {
        userGameIdMap.remove(userName);
    }
}
