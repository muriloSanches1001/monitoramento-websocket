package br.com.monitoramento.weblumio.utils;

import br.com.monitoramento.weblumio.entities.user.User;
import br.com.monitoramento.weblumio.enums.AccountType;
import br.com.monitoramento.weblumio.repositories.UserRepository;
import br.com.monitoramento.weblumio.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ValidationUtils {

    private final UserRepository userRepository;

    @Autowired
    public ValidationUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isUserAdmin(User user) {
        return user != null && user.getAccountType().equals(AccountType.ADMIN);
    }

    public boolean isUserAdminByToken(String token, TokenService tokenService) {
        String username = tokenService.getUserUsernameByToken(token);
        User user = userRepository.findByUsername(username);
        return isUserAdmin(user);
    }

    public boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
