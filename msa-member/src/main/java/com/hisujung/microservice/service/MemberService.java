package com.hisujung.microservice.service;


import com.hisujung.microservice.controller.MemberApiController;
import com.hisujung.microservice.dto.MemberSignupRequestDto;

public interface MemberService {
    //회원가입
    public Long join(MemberSignupRequestDto requestDto) throws Exception;

    void authEmail(MemberApiController.EmailRequest request);
}

