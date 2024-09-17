package br.com.monitoramento.weblumio.controllers;

import br.com.monitoramento.weblumio.dtos.ApiResponseDTO;
import br.com.monitoramento.weblumio.entities.user.UserDTO;
import br.com.monitoramento.weblumio.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/")
public class UserController {

    public final UserService userService;

    @Autowired
    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping(value = "user/find")
    public ResponseEntity<ApiResponseDTO> findUserByPageable(
            @RequestHeader("Authorization") @Validated String token,
            @RequestParam(value = "page", defaultValue = "0") @Validated int page,
            @RequestParam(value = "size", defaultValue = "10") @Validated int size
    ) {

        ApiResponseDTO response = userService.findUserByPageable(token, page, size);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PostMapping(value = "auth/login")
    public ResponseEntity<ApiResponseDTO> login(
            @RequestBody @Validated UserDTO user
    ) {

        ApiResponseDTO response = userService.loginUser(user);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PostMapping(value = "user/create")
    public ResponseEntity<ApiResponseDTO> create(
            @RequestHeader("Authorization") @Validated String token,
            @RequestBody @Validated UserDTO userDTO
            ) {

        ApiResponseDTO response = userService.registerUser(userDTO, token);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @PutMapping(value = "user/update/{id}")
    public ResponseEntity<ApiResponseDTO> update(
            @RequestHeader("Authorization") @Validated String token,
            @RequestBody @Validated UserDTO userDTO,
            @PathVariable @Validated Long id
    ) {

        ApiResponseDTO response = userService.updateUser(token, userDTO, id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

    @DeleteMapping(value = "user/delete/{id}")
    public ResponseEntity<ApiResponseDTO> delete(
            @RequestHeader("Authorization") @Validated String token,
            @PathVariable @Validated Long id
    ) {

        ApiResponseDTO response = userService.deleteUser(token, id);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

}
