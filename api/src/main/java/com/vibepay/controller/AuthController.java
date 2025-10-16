package com.vibepay.controller;

import com.vibepay.dto.LoginRequest;
import com.vibepay.dto.MemberResponse;
import com.vibepay.dto.SignupRequest;
import com.vibepay.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {
        MemberResponse response = memberService.login(request, session);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        memberService.logout(session);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 로그인 상태 확인
     */
    @GetMapping("/check")
    public ResponseEntity<MemberResponse> checkAuth(HttpSession session) {
        MemberResponse response = memberService.getCurrentMember(session);
        return ResponseEntity.ok(response);
    }
}
