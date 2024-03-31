package com.hisujung.microservice.controller;


import com.hisujung.web.dto.LoginRequestDto;
import com.hisujung.web.dto.MemberSignupRequestDto;
import com.hisujung.web.entity.Member;
import com.hisujung.web.jwt.JwtTokenUtil;
import com.hisujung.web.mail.MailSender;
import com.hisujung.web.service.MemberService;
import com.hisujung.web.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberApiController {
    @Autowired
    private Environment env;

    private final MemberService memberService;
    private final UserService userService;
//    private final MemberServicelmpl memberServicelmpl;
    //private final EmailService emailService;
    private final MailSender mailSender;
    @Value("${jwt.secret}")
    private String secretKey;

    @PostMapping("/join")
    public Long join(@RequestBody MemberSignupRequestDto requestDto) throws Exception {
        return memberService.join(requestDto);
        //return 1L;
    }

    @PostMapping("/join/mailConfirm")
    @ResponseBody
    public String mailConfirm(@RequestParam String email) throws Exception {
        log.info(email);
        String code = mailSender.send(email);
        log.info("인증코드 : " + code);
        return code;
    }

    @GetMapping("/join/verify/{key}")
    public String getVerify(@PathVariable String key) {
        String message;
        try {
            mailSender.verifyEmail(key);
            message = "인증에 성공하였습니다.";
        } catch (Exception e) {
            message = "인증에 실패하였습니다.";
        }
        return message;
    }
//
//    @PostMapping("/emails/verification-requests")
//    public ResponseEntity sendMessage(@RequestParam("email") @Valid @CustomEmail String email) {
//        memberServicelmpl.sendCodeToEmail(email);
//
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    @GetMapping("/emails/verifications")
//    public ResponseEntity verificationEmail(@RequestParam("email") @Valid @CustomEmail String email,
//                                            @RequestParam("code") String authCode) {
//        EmailVerificationResult response = memberServicelmpl.verifiedCode(email, authCode);
//
//        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
//    }

//    @PostMapping("/login")
//    public String login(@RequestBody Map<String, String> member) {
//        return memberService.login(member);
//    }

//    @PostMapping("/login")
//    public String login(@RequestBody LoginRequestDto loginRequestDto) {
//
//        Member user = userService.login(loginRequestDto);
//
//        //로그인 아이디나 비밀번호가 틀린 경우 global error return
//        if(user == null) {
//            return "로그인 아이디 또는 비밀번호가 틀렸습니다.";
//        }
//
//        //로그인 성공 => Jwt Token 발급
//
//        long expireTimeMs = 1000 * 60 * 60 * 8; //Token 유효 시간 = 8시간
//
//        String jwtToken = JwtTokenUtil.createToken(loginRequestDto.getEmail(), secretKey, expireTimeMs);
//
//        return jwtToken;
//    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDto loginRequestDto) {
        Member user = userService.login(loginRequestDto);

        if (user == null) {
            return ResponseEntity.badRequest().body("로그인 아이디 또는 비밀번호가 틀렸습니다.");
        }

        long expireTimeMs = 1000 * 60 * 60 * 8; // Token 유효 시간 = 8시간
        String jwtToken = JwtTokenUtil.createToken(loginRequestDto.getEmail(), secretKey, expireTimeMs);

        // 사용자 정보를 JSON 형태로 리턴
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("token", jwtToken);
        response.put("email", user.getEmail());
        response.put("username", user.getUsername());

        log.info("hihihibye");
        return ResponseEntity.ok(response);
    }

    @GetMapping("info")
    public String userInfo(Authentication auth) {

        //로그인id는 사용자의 이메일
        Member loginUser = userService.getLoginUserByLoginId(auth.getName());

        return String.format("loginId : %s\nusername : %s\nrole: %s",
                loginUser.getEmail(), loginUser.getUsername(), loginUser.getRole());

    }
}
