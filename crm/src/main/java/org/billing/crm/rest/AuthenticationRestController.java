package org.billing.crm.rest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.billing.crm.dto.AuthenticationRequestDto;
import org.billing.crm.dto.RefreshTokenRequestDto;
import org.billing.crm.security.JwtTokenProvider;
import org.billing.crm.services.UserService;
import org.billing.data.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth/")
public class AuthenticationRestController {


    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;

    private final AuthenticationManager authenticationManager;


    public AuthenticationRestController(UserService userService, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("login")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequestDto request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword()));
        User user = userService.getUser(request.getUsername().trim());
        String accessToken = jwtTokenProvider.createAccessToken(request.getUsername().trim(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(request.getUsername().trim(), user.getRole().name());

        Map<Object, Object> response = new HashMap<>();
        response.put("username", request.getUsername());
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("role", user.getRole());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequestDto request) {
        String accessToken = jwtTokenProvider.createAccessTokenByRefreshToken(request.getRefreshToken());

        Map<Object, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", request.getRefreshToken());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("logout")
    public void authenticate(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        securityContextLogoutHandler.logout(request, response, null);
    }

//    @PostMapping("register")
//    public ResponseEntity<User> register(@RequestBody RegistrationRequestDto requestDto) {
//        User user = userService.register(requestDto.getUsername(), requestDto.getPassword());
//        return new ResponseEntity<>(user, HttpStatus.OK);
//    }
}
