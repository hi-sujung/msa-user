package com.hisujung.microservice.service;


import com.hisujung.web.dto.MemberSignupRequestDto;
import com.hisujung.web.entity.Member;
import com.hisujung.web.exception.BusinessLogicException;
import com.hisujung.web.exception.ExceptionCode;
import com.hisujung.web.jpa.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PasswordEncoder passwordEncoder;;

    @Transactional
    @Override
    public Long signUp(MemberSignupRequestDto requestDto) throws Exception {
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

//    @Override
//    public String login(Map<String, String> members) {
//
//        Member member = memberRepository.findByEmail(members.get("email"))
//                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 Email 입니다."));
//
//        String password = members.get("password");
//        if (!member.checkPassword(passwordEncoder, password)) {
//            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
//        }
//
//        List<String> roles = new ArrayList<>();
//        roles.add(member.getRole().name());
//
//        return jwtTokenProvider.createToken(member.getUsername(), roles);
//    }

   // private final MailService mailService;
    private final RedisService redisService;

//    @Value("${spring.mail.auth-code-expiration-millis}")
//    private long authCodeExpirationMillis;
    //e86dd9602bdb425584bb3fb1af3da13a


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

//    public void sendCodeToEmail(String toEmail) {
//        this.checkDuplicatedEmail(toEmail);
//        String title = "Travel with me 이메일 인증 번호";
//        String authCode = this.createCode();
//        mailService.sendEmail(toEmail, title, authCode);
//        // 이메일 인증 요청 시 인증 번호 Redis에 저장 ( key = "AuthCode " + Email / value = AuthCode )
//        redisService.setValues(AUTH_CODE_PREFIX + toEmail,
//                authCode, Duration.ofMillis(this.authCodeExpirationMillis));
//    }

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

//    public EmailVerificationResult verifiedCode(String email, String authCode) {
//        this.checkDuplicatedEmail(email);
//        String redisAuthCode = redisService.getValues(AUTH_CODE_PREFIX + email);
//        boolean authResult = redisService.checkExistsValue(redisAuthCode) && redisAuthCode.equals(authCode);
//
//        return EmailVerificationResult.of(authResult);
//    }



}
