package com.hisujung.microservice.jwt;

import com.hisujung.microservice.entity.Member;
import com.hisujung.microservice.jwt.JwtTokenUtil;
import com.hisujung.microservice.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final String secretKey;
    private final RedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        //Header의 Authorization의 값이 비어있으면 => Jwt Token을 전송하지 않음 => 로그인 하지 않음
        if(authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        //Header의 Authorization의 값이 'Bearer '로 시작하지 않으면 => 잘못된 토큰
        if(!authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //전송받은 값에서 'Bearer ' 뒷부분(Jwt token) 추출
        String token = authorizationHeader.split(" ")[1];

        //전송받은 Jwt Token이 만료되었으면 => 다음 필터 진행(인증 X)
        if(JwtTokenUtil.isExpired(token, secretKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        String isBlackList = (String)redisTemplate.opsForValue().get(token);

        // 블랙리스트에 해당 토큰이 존재하지 않을 경우
        if (ObjectUtils.isEmpty(isBlackList)) {

            // 토큰이 유효하면 토큰으로부터 유저 정보를 받아옴
            //Jwt Token에서 loginId 추출
            String loginId = JwtTokenUtil.getLoginId(token, secretKey);

            //추출된 loginId로 Member 찾아오기
            Member loginUser = userService.getLoginUserByLoginId(loginId);

            //loginUser 정보로 UsernamePasswordAuthenticationToken 발급
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginUser.getEmail(), null, List.of(new SimpleGrantedAuthority(loginUser.getRole().name())));
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //권한 부여
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);

    }
}