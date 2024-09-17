package br.com.monitoramento.weblumio.services;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.company.Company;
import br.com.monitoramento.weblumio.entities.user.User;
import br.com.monitoramento.weblumio.entities.user.UserDTO;
import br.com.monitoramento.weblumio.entities.user.UserResponseDTO;
import br.com.monitoramento.weblumio.enums.AccountType;
import br.com.monitoramento.weblumio.enums.ErrorCode;
import br.com.monitoramento.weblumio.enums.SucessCode;
import br.com.monitoramento.weblumio.repositories.UserRepository;
import br.com.monitoramento.weblumio.utils.ValidationUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final ValidationUtils validationUtils;

    @Autowired
    public UserService(
            UserRepository userRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder,
            ValidationUtils validationUtils) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.validationUtils = validationUtils;
    }

    public ApiResponseDTO findUserByPageable(String token, int page, int size) {
        try {
            User adminUser = this.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
            Page<User> users = userRepository.findAll(pageable);

            List<UserResponseDTO> userResponseDTOS = new ArrayList<>();
            for (User user : users) {
                if (user.getAccountType() != AccountType.ADMIN) {
                    userResponseDTOS.add(this.userToUserResponse(user));
                }
            }

            log.info("Users fetched");
            return new ApiResponseDTO(SucessCode.FINDED, "Users fetched", HttpStatus.OK, userResponseDTOS);
        } catch (Exception e) {
            log.error("Error while getting all users, error: {}", e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDTO registerUser(UserDTO userDTO, String token) {
        if (!isValidUsername(userDTO.username())) {
            log.error("Invalid username");
            return new ApiResponseDTO(ErrorCode.INVALID_USERNAME, "Invalid Username", HttpStatus.CONFLICT);
        }

        if (!isValidPassword(userDTO.password())) {
            log.error("Invalid password");
            return new ApiResponseDTO(ErrorCode.INVALID_PASSWORD, "Invalid Password", HttpStatus.CONFLICT);
        }

        try {
            User adminUser = this.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Company company = adminUser.getCompany();
            if (company == null) {
                log.error("Admin user is not associated with any company");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "Admin user is not associated with any company", HttpStatus.NOT_FOUND);
            }

            String usernameWithDomain = userDTO.username() + '@' + company.getCompanyCode();

            if (userRepository.findByUsername(usernameWithDomain) != null) {
                log.error("Username already exists");
                return new ApiResponseDTO(ErrorCode.INVALID_USERNAME, "Username already exists", HttpStatus.CONFLICT);
            }

            User newUser = new User(
                    usernameWithDomain,
                    passwordEncoder.encode(userDTO.password()),
                    company
            );
            newUser.setAccountType(AccountType.USER);
            userRepository.save(newUser);
            log.info("User with username '{}' created", newUser.getUsername());
            return new ApiResponseDTO(SucessCode.USER_REGISTERED, "User with username '" + newUser.getUsername() + "' created", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error in creating user with username '{}', error: {}", userDTO.username(), e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseDTO loginUser(UserDTO userDTO) {
        try {
            User user = userRepository.findByUsername(userDTO.username());
            if (user == null) {
                log.error("User not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "User not found", HttpStatus.NOT_FOUND);
            }
            if (!passwordEncoder.matches(userDTO.password(), user.getPassword())) {
                log.error("Invalid password");
                return new ApiResponseDTO(ErrorCode.INVALID_PASSWORD, "Invalid Password", HttpStatus.CONFLICT);
            }
            String token = tokenService.generateAccessApiToken(user);
            return new ApiResponseDTO(SucessCode.LOGIN_SUCCESS, "User logged", HttpStatus.OK, token);
        } catch (Exception e) {
            log.error("Error in login user with username '{}', error: {}", userDTO.username(), e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDTO updateUser(String token, UserDTO userDTO, Long userId) {
        if (!isValidUsername(userDTO.username())) {
            log.error("Invalid username");
            return new ApiResponseDTO(ErrorCode.INVALID_USERNAME, "Invalid Username", HttpStatus.CONFLICT);
        }

        if (!isValidPassword(userDTO.password())) {
            log.error("Invalid password");
            return new ApiResponseDTO(ErrorCode.INVALID_PASSWORD, "Invalid Password", HttpStatus.CONFLICT);
        }

        try {
            User adminUser = this.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<User> optionalUserToUpdate = userRepository.findById(userId);
            if (optionalUserToUpdate.isEmpty()) {
                log.error("User not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "User not found", HttpStatus.NOT_FOUND);
            }
            User userToUpdate = optionalUserToUpdate.get();

            String usernameWithDomain = userDTO.username() + '@' + userToUpdate.getCompany().getCompanyCode();
            userToUpdate.setPassword(passwordEncoder.encode(userDTO.password()));

            if (!userToUpdate.getUsername().equals(usernameWithDomain)) {
                // Se for diferente, verifique se o novo nome já existe
                if (userRepository.findByUsername(usernameWithDomain) != null) {
                    log.error("Username already exists");
                    return new ApiResponseDTO(ErrorCode.ALREADY_EXISTS, "Username already exists", HttpStatus.CONFLICT);
                }
                userToUpdate.setUsername(usernameWithDomain); // Atualiza apenas se for diferente
            }

            userRepository.save(userToUpdate);
            log.info("User with username '{}' updated", userToUpdate.getUsername());
            return new ApiResponseDTO(SucessCode.UPDATED, "User with username '" + userToUpdate.getUsername() + "' updated", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error in updating user with username '{}', error: {}", userDTO.username(), e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ApiResponseDTO deleteUser(String token, Long userId) {
        try {
            User adminUser = this.getUserByToken(token);
            if (!validationUtils.isUserAdmin(adminUser)) {
                log.error("Unauthorized user or invalid admin token");
                return new ApiResponseDTO(ErrorCode.UNAUTHORIZED, "Unauthorized or invalid admin token", HttpStatus.UNAUTHORIZED);
            }

            Optional<User> userToDelete = userRepository.findById(userId);
            if (userToDelete.isEmpty()) {
                log.error("User not found");
                return new ApiResponseDTO(ErrorCode.NOT_FOUND, "User not found", HttpStatus.NOT_FOUND);
            }

            userRepository.delete(userToDelete.get());
            log.info("User with username '{}' deleted", userToDelete.get().getUsername());
            return new ApiResponseDTO(SucessCode.DELETED, "User deleted", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error in deleting user with id '{}', error: {}", userId, e.getMessage());
            return new ApiResponseDTO(ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isValidUsername(String username) {
        // Verifica se o nome de usuário está no formato correto

        String specialCharPattern = "[^a-z0-9]"; // Qualquer coisa que não seja letras minúsculas ou números
        return username.length() >= 3 &&
                !Pattern.compile(specialCharPattern).matcher(username).find() &&
                username.equals(username.toLowerCase());
    }

    private boolean isValidPassword(String password) {
        // Verifica se tem ao menos 8 caracteres, uma letra maiúscula, uma letra minúscula, um número e um caractere especial
        return password.length() >= 8 &&
                Pattern.compile("[A-Z]").matcher(password).find() &&
                Pattern.compile("[a-z]").matcher(password).find() &&
                Pattern.compile("[0-9]").matcher(password).find() &&
                Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();
    }

    public User getUserByToken(String token) {
        String username = tokenService.getUserUsernameByToken(token);
        return userRepository.findByUsername(username);
    }

    private UserResponseDTO userToUserResponse(User user) {

        return new UserResponseDTO(user.getId(),
                user.getUsername());
    }

}
