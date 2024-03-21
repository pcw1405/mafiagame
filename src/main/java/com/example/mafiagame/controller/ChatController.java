package com.example.mafiagame.controller;

import com.example.mafiagame.dto.MemberFormDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Log4j2
public class ChatController {

    @GetMapping("/chat")
    public String chatGET(){
        log.info("@ChatController.chat GET()");
        return "chater";
    }

    @GetMapping("/chat/user")
    @ResponseBody
    public String getUser() {
        log.info("getUserInfo start");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = null;
        if (auth != null && auth.isAuthenticated()) {
            name = auth.getName();
        }
        return name;
    }
}

