package com.example.bitirmeprojemfinal.config;


import com.example.bitirmeprojemfinal.entity.UserDtls;
import com.example.bitirmeprojemfinal.repository.UserRepository;
import com.example.bitirmeprojemfinal.service.UserService;
import com.example.bitirmeprojemfinal.util.AppConstant;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String email = request.getParameter("username");
		String errorMessage;

		UserDtls userDtls = userRepository.findByEmail(email);

		if (userDtls != null) {
			if (userDtls.getIsEnable()) {
				if (userDtls.getAccountNonLocked()) {
					if (userDtls.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
						userService.increaseFailedAttempt(userDtls);
						errorMessage = "E-posta veya şifre hatalı!";
					} else {
						userService.userAccountLock(userDtls);
						errorMessage = "Hesabınız kilitlendi! Çok fazla başarısız giriş denemesi yaptınız.";
					}
				} else {
					if (userService.unlockAccountTimeExpired(userDtls)) {
						errorMessage = "Hesap kilidi kaldırıldı. Lütfen tekrar giriş yapmayı deneyin.";
					} else {
						errorMessage = "Hesabınız kilitli! Lütfen 24 saat sonra tekrar deneyin.";
					}
				}
			} else {
				errorMessage = "Hesabınız aktif değil. Lütfen e-posta adresinizi kontrol edin.";
			}
		} else {
			errorMessage = "E-posta veya şifre hatalı!";
		}

		request.getSession().setAttribute("error", errorMessage);
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, new LockedException(errorMessage));
	}

}
