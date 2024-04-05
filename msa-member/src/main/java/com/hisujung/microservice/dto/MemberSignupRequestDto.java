package com.hisujung.microservice.dto;

import com.hisujung.web.entity.Member;
import com.hisujung.web.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberSignupRequestDto {

    @NotBlank(message = "학교 g메일 아이디를 입력해주세요.")
    @Pattern(regexp = "@sungshin.ac.kr$")
    private String email;

    @NotBlank(message = "이름을 입력해주세요.")
    private String userName;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,30}$",
            message = "비밀번호는 8~30 자리이면서 1개 이상의 알파벳, 숫자, 특수문자를 포함해야합니다.")
    private String password;

    private String checkedPassword;

    private String department1;

    private String department2;

    private Role role;


    @Builder
    public Member toEntity() {
        return Member.builder()
                .email(email)
                .userName(userName)
                .password(password)
                .department1(department1)
                .department2(department2)
                .role(Role.USER)
                .build();
    }
}
