package com.hisujung.microservice.service;


import com.hisujung.microservice.controller.MemberApiController;
import com.hisujung.microservice.dto.MemberSignupRequestDto;
import com.hisujung.microservice.entity.Member;
import com.hisujung.microservice.exception.BusinessLogicException;
import com.hisujung.microservice.exception.ExceptionCode;
import com.hisujung.microservice.repository.MemberRepository;
import com.hisujung.microservice.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
@Slf4j
public class MemberServicelmpl implements MemberService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;

    //이메일 전송
    @Override
    @Transactional
    public void authEmail(MemberApiController.EmailRequest request) {

        // 임의의 authKey 생성
        Random random = new Random();
        String authKey = String.valueOf(random.nextInt(888888) + 111111);      // 범위 : 111111 ~ 999999

        // 이메일 발송
        sendAuthEmail(request.getEmail(), authKey);
    }

    private void sendAuthEmail(String email, String authKey) {

        String subject = "제목";
        String text = "회원 가입을 위한 인증번호는 " + authKey + "입니다. <br/>";

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true);  //포함된 텍스트가 HTML이라는 의미로 true.
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        // 유효 시간(5분)동안 {email, authKey} 저장
        redisUtil.setDataExpire(authKey, email, 60 * 5L);
    }


    @Transactional
    @Override
    public Long join(MemberSignupRequestDto requestDto) throws Exception {
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        if (!requestDto.getPassword().equals(requestDto.getCheckedPassword())) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        Member member = memberRepository.save(requestDto.toEntity());
        member.encodePassword(passwordEncoder);

        member.addUserAuthority();

        return member.getId();
    }


    private void checkDuplicatedEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent()) {
            log.debug("MemberServiceImpl.checkDuplicatedEmail exception occur email: {}", email);
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
        }
    }

    private String createCode() {
        int lenth = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lenth; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("MemberService.createCode() exception occur");
            throw new BusinessLogicException(ExceptionCode.NO_SUCH_ALGORITHM);
        }
    }
}
