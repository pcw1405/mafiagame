package com.example.mafiagame.repository;

import com.example.mafiagame.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
    //회원가입시 중복된 회원이 있는지 검사 하기 위해
    //이메일로 회원을 검사할 수 있도록 쿼리 메소드 작성
}

