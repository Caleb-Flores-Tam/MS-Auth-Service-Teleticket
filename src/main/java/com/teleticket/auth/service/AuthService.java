package com.teleticket.auth.service;

import com.teleticket.auth.config.JwtProperties;
import com.teleticket.auth.dto.enumeration.UserStatus;
import com.teleticket.auth.dto.request.*;
import com.teleticket.auth.dto.response.AdminResetResponse;
import com.teleticket.auth.dto.response.TokenResponse;
import com.teleticket.auth.dto.response.UserResponse;
import com.teleticket.auth.dto.response.ValidateResponse;
import com.teleticket.auth.entity.PasswordResetToken;
import com.teleticket.auth.entity.RefreshToken;
import com.teleticket.auth.entity.User;
import com.teleticket.auth.exception.ApiValidateException;
import com.teleticket.auth.mapper.UserMapper;
import com.teleticket.auth.repository.*;
import com.teleticket.auth.util.ConstantUtil;
import com.teleticket.auth.util.HashUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserRoleRepository urRepo;
    private final RefreshTokenRepository rtRepo;
    private final PasswordResetTokenRepository prRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final JwtProperties props;
    private final UserMapper mapper;

    /* /auth/register */
    @Transactional
    public UserResponse register(RegisterRequest req) throws Exception {
        var user = new UserService(userRepo, roleRepo, urRepo, encoder, mapper, totpService, props).register(req.email(), req.password(), req.role());
        var roles = urRepo.findRoleNamesByUserId(user.getId());
        return mapper.toDto(user, roles);
    }

    /* /auth/login */
    @Transactional
    public TokenResponse login(LoginRequest req) {
        var user = userRepo.findByEmail(req.email()).orElseThrow(() -> new ApiValidateException("Credenciales inválidas"));
        if (!encoder.matches(req.password(), user.getPasswordHash()))
            throw new ApiValidateException("Credenciales inválidas");
        if (user.getStatus() != UserStatus.ACTIVE) throw new IllegalStateException("Usuario no activo");

        var roles = urRepo.findRoleNamesByUserId(user.getId());
        var access = jwtService.issueAccessToken(user.getId(), user.getEmail(), roles);
        var refresh = UUID.randomUUID().toString();

        saveRefresh(user, refresh, props.getRefreshTtl());
        return new TokenResponse("Bearer", access, props.getAccessTtl().toSeconds(), refresh);
    }

    /* /auth/refresh */
    @Transactional
    public TokenResponse refresh(RefreshRequest req) {
        var tokenHash = HashUtils.sha256(req.refreshToken());
        var rt = rtRepo.findByTokenHash(tokenHash).orElseThrow(() -> new ApiValidateException("Refresh inválido"));
        if (Boolean.TRUE.equals(rt.getRevoked()) || rt.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new ApiValidateException("Refresh expirado/revocado");

        var user = rt.getUser();
        var roles = urRepo.findRoleNamesByUserId(user.getId());
        var access = jwtService.issueAccessToken(user.getId(), user.getEmail(), roles);

        // Rotación de refresh tokens
        rt.setRevoked(true);
        var newRefresh = UUID.randomUUID().toString();
        saveRefresh(user, newRefresh, props.getRefreshTtl());

        return new TokenResponse("Bearer", access, props.getAccessTtl().toSeconds(), newRefresh);
    }

    /* /auth/validate */
    public ValidateResponse validate(String token) {
        try {
            var claims = jwtService.validate(token);
            return new ValidateResponse(true, claims);
        } catch (Exception e) {
            return new ValidateResponse(false, Map.of("error", e.getMessage()));
        }
    }

    /* /auth/logout */
    @Transactional
    public void logout(RefreshRequest req) {
        rtRepo.findByTokenHash(HashUtils.sha256(req.refreshToken())).ifPresent(rt -> rt.setRevoked(true));
    }

    /* /auth/logout-all */
    @Transactional
    public void logoutAll(Long userId) {
        rtRepo.findAll().stream().filter(rt -> rt.getUser().getId().equals(userId)).forEach(rt -> rt.setRevoked(true));
    }

    /* /auth/forgot-password */
    @Transactional
    public void forgotPassword(String email) {
        var user = userRepo.findByEmail(email).orElse(null);
        if (user == null) return;

        var reset = UUID.randomUUID().toString();
        prRepo.save(PasswordResetToken.builder()
                .user(user).tokenHash(HashUtils.sha256(reset))
                .expiryDate(LocalDateTime.now().plusHours(2))
                .used(false).build());
        // TODO: enviar correo con link: https://tu-ui/reset?token=<reset>
    }

    /* /auth/reset-password */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        var prt = prRepo.findByTokenHash(HashUtils.sha256(req.getResetToken()))
                .orElseThrow(() -> new ApiValidateException("Token inválido"));
        if (prt.getUsed() || prt.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new ApiValidateException("Token expirado/ya usado");

        var user = prt.getUser();
        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);

        prt.setUsed(true);
        prRepo.save(prt);

        logoutAll(user.getId());
    }

    /* /auth/change-password */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        var user = userRepo.findById(userId).orElseThrow();
        if (!encoder.matches(req.oldPassword(), user.getPasswordHash()))
            throw new ApiValidateException("Contraseña actual incorrecta");
        user.setPasswordHash(encoder.encode(req.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
        logoutAll(userId);
    }

    /* /auth/delete-account */
    @Transactional
    public void deleteAccount(Long userId, String currentPassword) {
        var user = userRepo.findById(userId).orElseThrow();
        if (!encoder.matches(currentPassword, user.getPasswordHash()))
            throw new ApiValidateException("Contraseña incorrecta");
        user.setStatus(UserStatus.DELETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
        logoutAll(userId);
    }

    public void sendCodeEmailVerification(String email){
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ApiValidateException("Usuario no encontrado"));




    }

    private String generateCode(String email){
        String code = String.format("%06d", (int)(Math.random() * 1000000));

        return code;
    }

    private void saveRefresh(User user, String raw, java.time.Duration ttl) {
        rtRepo.save(RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtils.sha256(raw))
                .expiryDate(LocalDateTime.now().plusSeconds(ttl.toSeconds()))
                .revoked(false)
                .build());
    }

    public boolean validateTotp(String username, int code) throws Exception {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new ApiValidateException("Usuario no encontrado"));

        byte[] secretBytes = new Base32().decode(user.getSecretBase32());

        Key key = new javax.crypto.spec.SecretKeySpec(secretBytes, "HmacSHA1");

        return totpService.validateCode(key, code);
    }

    public String getQrCodeBase64() {
        String sessionUser = getSessionInfo();
        User user = userRepo.findByEmail(sessionUser)
                .orElseThrow(() -> new ApiValidateException(ConstantUtil.USER_NOT_FOUND));
        return user.getQrCodeBase64();
    }

    @Transactional(readOnly = true)
    private String getSessionInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiValidateException(ConstantUtil.USER_NOT_FOUND);
        }
        return authentication.getName();
    }

    public AdminResetResponse resetCredentialsAsAdmin(@Valid AdminResetCredentialsRequest req) throws Exception {
        String adminEmail = getSessionInfo();
        User admin = userRepo.findByEmail(adminEmail)
                .orElseThrow(() -> new ApiValidateException("Administrador no encontrado"));

        var roles = urRepo.findRoleNamesByUserId(admin.getId());
        if (!roles.contains("ADMIN")) {
            throw new ApiValidateException("No tiene permisos para restablecer credenciales");
        }

        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new ApiValidateException("Usuario no encontrado"));

        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            user.setPasswordHash(encoder.encode(req.newPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
        logoutAll(user.getId()); // Invalida sesiones activas

        return new AdminResetResponse(
                user.getEmail(),
                req.newPassword() != null
        );
    }

}

