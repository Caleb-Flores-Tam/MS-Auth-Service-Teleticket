package com.luisalt20.auth.controller;

import com.luisalt20.auth.dto.request.*;
import com.luisalt20.auth.dto.response.*;
import com.luisalt20.auth.mapper.UserMapper;
import com.luisalt20.auth.repository.UserRepository;
import com.luisalt20.auth.repository.UserRoleRepository;
import com.luisalt20.auth.service.AuthService;
import com.luisalt20.auth.service.PasswordResetService;
import com.luisalt20.auth.service.VerificationCodeEmailService;
import com.luisalt20.auth.util.ConstantUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@CrossOrigin("*")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepo;
    private final UserRoleRepository urRepo;
    private final UserMapper mapper;
    private final PasswordResetService passwordResetService;
    private final VerificationCodeEmailService verificationCodeEmailService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest req,
                                                              UriComponentsBuilder uri) throws Exception {
        var dto = authService.register(req);
        var location = uri.path("/auth/users/{id}").buildAndExpand(dto.id()).toUri();
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.USER_REGISTERED)
                .data(dto)
                .build();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        ApiResponse<TokenResponse> response = ApiResponse.<TokenResponse>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.LOGIN_SUCCESS)
                .data(authService.login(req))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest req) {
        ApiResponse<TokenResponse> response = ApiResponse.<TokenResponse>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.TOKEN_REFRESHED)
                .data(authService.refresh(req))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ValidateResponse>> validate(@RequestBody(required = false) ValidateRequest body,
                                                                  @RequestHeader(value = "Authorization", required = false) String auth) {
        String token = (body != null && body.token() != null) ? body.token()
                : (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
        if (token == null) {
            ApiResponse<ValidateResponse> response = ApiResponse.<ValidateResponse>builder()
                    .code(ConstantUtil.ERROR_CODE)
                    .message(ConstantUtil.MISSING_TOKEN)
                    .data(new ValidateResponse(false, java.util.Map.of("error", "Falta token")))
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
        ApiResponse<ValidateResponse> response = ApiResponse.<ValidateResponse>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.TOKEN_VALIDATED)
                .data(authService.validate(token))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.LOGGED_OUT)
                .data(null)
                .build();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        var uid = ((Number) jwt.getClaim("uid")).longValue();
        authService.logoutAll(uid);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.LOGGED_OUT_ALL_DEVICES)
                .data(null)
                .build();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req.getEmail());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.PASSWORD_RESET_REQUESTED)
                .data(null)
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.PASSWORD_RESET)
                .build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt,
                                                            @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(((Number) jwt.getClaim("uid")).longValue(), req);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.PASSWORD_CHANGED)
                .build());
    }

    @PostMapping("/delete-account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt,
                                                           @Valid @RequestBody DeleteAccountRequest req) {
        var uid = ((Number) jwt.getClaim("uid")).longValue();
        authService.deleteAccount(uid, req.currentPassword());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.ACCOUNT_DELETED)
                .build());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable Long id) {
        var user = userRepo.findById(id).orElseThrow();
        var roles = urRepo.findRoleNamesByUserId(id);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.USER_FOUND)
                .data(mapper.toDto(user, roles))
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/reset-credentials")
    public ResponseEntity<ApiResponse<AdminResetResponse>> resetCredentialsAsAdmin(
            @Valid @RequestBody AdminResetCredentialsRequest req) throws Exception {

        var data = authService.resetCredentialsAsAdmin(req);
        ApiResponse<AdminResetResponse> response = ApiResponse.<AdminResetResponse>builder()
                .code(ConstantUtil.OK_CODE)
                .message("Credenciales restablecidas correctamente")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<Void>> forgot(@Valid @RequestBody ForgotPasswordRequest req) {
        passwordResetService.forgotPassword(req);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.PASSWORD_RESET_EMAIL_SENT)
                .data(null)
                .build());
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> restePassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        passwordResetService.resetPassword(resetPasswordRequest);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.PASSWORD_RESET_MESSAGE)
                .data(null)
                .build());
    }

    @GetMapping("/validate-token/{token}")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(@PathVariable String token) {

        return ResponseEntity.ok(ApiResponse.<TokenValidationResponse>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.TOKEN_VALIDATION_MESSAGE)
                .data(passwordResetService.validateTokenWithDetails(token))
                .build());
    }

    @PostMapping("/resend-reset-email")
    public ResponseEntity<ApiResponse<Void>> sendVerificationEmail(@RequestParam String email) {

        log.info("Llego este correo, {}", email);
        verificationCodeEmailService.sendVerificationCodeEmail(email);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.PASSWORD_RESET_EMAIL_RESENT)
                .data(null)
                .build());
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyCodeForEmailVerification(
            @RequestParam String email,
            @RequestParam String code) {

        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.EMAIL_VERIFICATION_SENT)
                .data(verificationCodeEmailService.verifyCode(email, code))
                .build());
    }
}


