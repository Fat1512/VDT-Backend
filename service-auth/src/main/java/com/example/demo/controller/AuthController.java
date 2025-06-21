package com.example.demo.controller;

import com.example.demo.dto.APIResponse;
import com.example.demo.dto.APIResponseMessage;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.TokenDTO;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<APIResponse> login(@RequestBody LoginRequest loginRequest) {
        TokenDTO tokenDTO = authService.login(loginRequest);
        APIResponse apiResponse = APIResponse.builder()
                .status(HttpStatus.OK)
                .message(APIResponseMessage.SUCCESSFULLY_LOGIN.name())
                .data(tokenDTO)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
