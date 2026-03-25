package com.teleticket.auth.service;

import com.teleticket.auth.dto.request.EmailRequest;
import com.teleticket.auth.dto.request.ForgotPasswordRequest;
import com.teleticket.auth.dto.request.ResetPasswordRequest;
import com.teleticket.auth.dto.response.TokenValidationResponse;
import com.teleticket.auth.entity.PasswordResetToken;
import com.teleticket.auth.entity.User;
import com.teleticket.auth.exception.ApiValidateException;
import com.teleticket.auth.repository.PasswordResetTokenRepository;
import com.teleticket.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebClient.Builder webClientBuilder;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.url.from.image}")
    private String urlFromImage;

    @Value("${password.reset.token.expiration.minutes:5}")
    private int tokenExpirationMinutes;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + forgotPasswordRequest.getEmail()));

        tokenRepository.deleteByUser_Email(user.getEmail());

        //Generamos token unico
        String token = UUID.randomUUID().toString();
        log.info("Token generado para {}: {}", user.getEmail(), token);

        // Guardamos el token en la base de datos
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(token)
                .used(false)
                .expiryDate(LocalDateTime.now().plusMinutes(tokenExpirationMinutes))
                .user(user)
                .build();

        tokenRepository.save(resetToken);

        sendResetPasswordEmail(user.getEmail(), token, user.getEmail());
    }

    private void sendResetPasswordEmail(String email, String token, String userName) {
        String resetLink = frontendUrl + "/authentication/reset-password?token=" + token;

        String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                </html>
                """, tokenExpirationMinutes, urlFromImage, userName, resetLink, tokenExpirationMinutes);

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo(email);
        emailRequest.setSubject("Recuperación de Contraseña");
        emailRequest.setBody(htmlBody);
        emailRequest.setHtml(true);

        try {
            webClientBuilder.build()
                    .post()
                    .uri(emailServiceUrl + "/api/email/send")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Email de recuperación enviado a: {}", email);
        } catch (Exception e) {
            log.error("Error al enviar email: ", e);
            throw new ApiValidateException("Error al enviar el correo de recuperación");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Buscar el token
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(request.getResetToken())
                .orElseThrow(() -> new ApiValidateException("Token inválido"));

        // Validar que no esté usado
        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new ApiValidateException("Este token ya fue utilizado");
        }

        // Validar que no esté expirado
        boolean isExpired = resetToken.getExpiryDate().isBefore(LocalDateTime.now());
        if (isExpired) {
            throw new ApiValidateException("El token ha expirado");
        }

        // Buscar usuario
        User user = userRepository.findById(resetToken.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar contraseña
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Contraseña actualizada exitosamente para: {}", user.getEmail());
    }

    @Transactional
    public TokenValidationResponse validateTokenWithDetails(String token) {
        TokenValidationResponse response = new TokenValidationResponse();

        try {
            PasswordResetToken resetToken = tokenRepository.findByTokenHash(token)
                    .orElse(null);

            if (resetToken == null) {
                response.setValid(false);
                response.setMessage("Token no encontrado");
                return response;
            }

            if (Boolean.TRUE.equals(resetToken.getUsed())) {
                response.setValid(false);
                response.setMessage("Este token ya fue utilizado");
                return response;
            }

            boolean expired = resetToken.getExpiryDate().isBefore(LocalDateTime.now());

            if (expired) {
                response.setValid(false);
                response.setMessage("El token ha expirado");
                return response;
            }

            response.setValid(true);
            response.setMessage("Token válido");
            response.setEmail(resetToken.getUser().getEmail());

            return response;

        } catch (Exception e) {
            log.error("Error al validar token: ", e);
            response.setValid(false);
            response.setMessage("Error al validar el token");
            return response;
        }
    }
}
