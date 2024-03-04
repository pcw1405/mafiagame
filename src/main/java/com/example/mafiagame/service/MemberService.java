package com.example.mafiagame.service;

import com.example.mafiagame.entity.Member;
import com.example.mafiagame.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;

    public Member saveMember(Member member){
        validateDupulicateMember(member);
        return memberRepository.save(member);
    }

    private void validateDupulicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }
    //스프링 시큐리티에서 UserDetailsService 를 구현하고 있는 클래스를 통해 로그인구현
    // UserDetailsService 인터페이스는 데이터 베이스에서 회원정보를 가지고 오는 역할 담당
    //loadUserByUsername() 메소드가 존재하며 , 회원 정보를 조회하여 사용자의 정보와 권한
    //을 갖는 UserDetails 인터페이스 반환
    //UserDetails: 스프링 시큐리티에서 회원 정보를 담기 위해서 사용하는 인터페이스
    //인터페이스를 직접 구현하거나 스피링 시큐리티에서 제공하는 User 클래스
    // (UserDetails 인터페이스를 구현하고 있는 클래스)를 사용
    @Override
    public UserDetails loadUserByUsername(String email) throws
            UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if(member == null){
            throw new UsernameNotFoundException(email);
        } //스프링 시큐리티에서 사용하는 예외처리
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                //배열로 반환되어 문자열로 변환해서 넣어줘야함
                .build();
    }




}