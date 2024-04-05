package com.hisujung.web.config;

import com.hisujung.web.entity.Role;
import com.hisujung.web.jwt.JwtTokenFilter;
import com.hisujung.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    @Value("${jwt.secret}")
    private String secretKey;

    @Autowired
    public SecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }


    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //토큰 방식으로 인증을 하기 때문에 기존에 사용하던 폼로그인, 세션 비활성화
//        http.
//                cors()
//                .and()
//                .httpBasic().disable()
//                .csrf().disable()
//                .formLogin().disable()
//                .logout().disable()
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//
//        //요청에 대한 권한 설정
////    http.authorizeRequests()
////            .antMatchers("/main", "/token").permitAll()
////            .antMatchers("/admin/**").hasRole(Role.ADMIN.name())
////            .anyRequest().authenticated();
////
//        http
//                .authorizeHttpRequests((authz) -> authz
//                        .anyRequest().permitAll()
//                )
//                .httpBasic(withDefaults());
//
        http
                .formLogin().disable()
                .httpBasic().disable()
                .cors().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(new JwtTokenFilter(userService, secretKey), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests()
                .requestMatchers("/portfolio/**").permitAll()
                .requestMatchers("/portfolio/portfoliolist").permitAll()
                .requestMatchers("/portfolio/portfoliolist").authenticated()
                .requestMatchers("/hello").permitAll()
                .requestMatchers("/member/login").permitAll()
                .requestMatchers("/member/info").permitAll()
                .requestMatchers("/member/join").permitAll()
                .requestMatchers("/member/join/mailConfirm").permitAll()
                .requestMatchers("/member/join/verify/*").permitAll()
                .requestMatchers("/").permitAll()
                //.requestMatchers("member/portfoliolist").permitAll()
                .requestMatchers("/v*/api-docs/**").permitAll()
                .requestMatchers("/swagger*/**").permitAll()
                //.requestMatchers("/**/*").permitAll()
                .requestMatchers("/swagger-ui/index.html/* ").permitAll()
                .requestMatchers("/member").hasRole("USER")
                .requestMatchers("/member/info").authenticated()
                .requestMatchers("/admin/**").hasAuthority(Role.ADMIN.name())
                .anyRequest().permitAll();
                //.and()
                //.addFilterBefore(new JwtTokenFilter(userService, secretKey), UsernamePasswordAuthenticationFilter.class);
                //.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    private static final String[] AUTH_WHITELIST = {
            "/v2/api-docs",
            "/v3/api-docs/**",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/file/**",
            "/image/**",
            "/swagger/**",
            "/swagger-ui/**",
            "/h2/**"
    };
}