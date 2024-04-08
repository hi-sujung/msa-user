package com.hisujung.microservice.jwt;


import com.hisujung.microservice.annotation.LoginUser;
import com.hisujung.microservice.entity.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver  {

    private final HttpSession httpSession;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        //@LoginUser 있으면
        boolean isLoginUserAnnotation = methodParameter.getParameterAnnotation(LoginUser.class) != null;
        //SessionUser 클래스 타입의 파라미터에 @LoginUser이 사용되었는가?
        boolean isUserClass = SessionUser.class.equals(methodParameter.getParameterType());

        return isLoginUserAnnotation && isUserClass;

    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return httpSession.getAttribute("user");
    }
}
