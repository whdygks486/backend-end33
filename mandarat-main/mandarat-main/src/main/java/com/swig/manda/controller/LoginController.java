package com.swig.manda.controller;

import com.swig.manda.config.auth.PrincipalDetails;
import com.swig.manda.dto.*;
import com.swig.manda.model.Member;
import com.swig.manda.repository.MemberRepository;
import com.swig.manda.service.MemberService;
import com.swig.manda.service.SendMailService;
import groovy.util.logging.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@CrossOrigin(origins ="*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/member")
public class LoginController {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    MemberService memberService;
    @Autowired
    SendMailService sendMailService;

    @Autowired
    private AuthenticationManager authenticationManager;


    // 비밀번호 찾기 요청
    @PostMapping("/check/findPw")
    public ResponseEntity<String> handleFindPasswordRequest(@RequestBody FindUserpasswordRequest findUserpasswordRequest) {
        String email = findUserpasswordRequest.getEmail();
        String username = findUserpasswordRequest.getUserId();

        try {
            sendMailService.sendResetPasswordEmail(email, username);
            return ResponseEntity.ok("비밀번호 재설정 이메일을 성공적으로 발송했습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("비밀번호 재설정 이메일 발송에 실패했습니다.");
        }
    }


    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return username;
        }


        String masked = username.substring(0, 1) + "*".repeat(2) + username.substring(3,username.length());
        return masked;
    }

    //아이디 찾기 요청
    @PostMapping("/check/find_username")
    public ResponseEntity<String> handleFindIdRequest(@RequestBody FindUsernameRequest findUsernameRequest){


        String email = findUsernameRequest.getEmail();
        String name = findUsernameRequest.getUsername();



        String username=memberService.findUsernameByEmailAndName(email,name);

        if (username != null) {

            return ResponseEntity.ok(maskUsername(username));
        } else {

            return ResponseEntity.notFound().build();
        }


    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUserId(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        String memberId = principalDetails.getUsername();

        Map<String, Object> response = new HashMap<>();
        response.put("memberId", memberId);
        return ResponseEntity.ok(response);
    }




    // 비밀번호 업데이트 요청!!
    @PostMapping("/pwUpdate")
    public ResponseEntity<String> updatePassword(@RequestBody PWupdateDto pwUpdateDto) {
        try {
            if (!pwUpdateDto.getNewPassword().equals(pwUpdateDto.getNewRepassword())) {
                return ResponseEntity.badRequest().body("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            }

            Optional<Member> optionalMember = memberRepository.findByUserId(pwUpdateDto.getUserId());
            if (!optionalMember.isPresent()) {
                return ResponseEntity.badRequest().body("사용자를 찾을 수 없습니다.");
            }

            Member member = optionalMember.get();
            String currentPassword = member.getPassword();

            if (!bCryptPasswordEncoder.matches(pwUpdateDto.getPassword(), currentPassword)) {
                return ResponseEntity.badRequest().body("현재 비밀번호가 정확하지 않습니다.");
            }

            memberService.updatePassword(pwUpdateDto.getUserId(), pwUpdateDto.getNewPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("비밀번호 업데이트 중 오류가 발생했습니다.");
        }
    }

}
