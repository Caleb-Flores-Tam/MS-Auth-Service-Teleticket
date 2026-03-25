package com.teleticket.auth.service;

import com.teleticket.auth.dto.request.EmailRequest;
import com.teleticket.auth.dto.request.VerificationCodeEmailRequest;
import com.teleticket.auth.entity.VerificationCodeEmail;
import com.teleticket.auth.exception.ApiValidateException;
import com.teleticket.auth.mapper.VerificationCodeMapper;
import com.teleticket.auth.repository.UserRepository;
import com.teleticket.auth.repository.VerificationCodeEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeEmailService{

    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final VerificationCodeEmailRepository verificationCodeEmailRepository;
    private final VerificationCodeMapper verificationCodeMapper;

    @Value("${email.service.url}")
    private String emailServiceUrl;

    @Value("${password.reset.token.expiration.minutes:30}")
    private int tokenExpirationMinutes;

    @Value("${app.url.from.image}")
    private String urlForLogo;

    @Transactional
    public void sendVerificationCodeEmail(String email){

        if (email == null || email.isEmpty()) {
            throw new ApiValidateException("Email must not be null or empty");
        }

        String code = this.generateVerificationCode();
        String messageHtml = this.buildMessageHtml(code);

        VerificationCodeEmailRequest verificationCodeEmailRequest =
                VerificationCodeEmailRequest.builder()
                        .code(code)
                        .used(false)
                        .email(email)
                        .expiresAt(LocalDateTime.now().plusMinutes(tokenExpirationMinutes))
                        .build();

        VerificationCodeEmail verificationCodeEmail = verificationCodeMapper.toEntity(verificationCodeEmailRequest);
        log.info("Guardando el código de verificación para el email: {}", verificationCodeEmail.toString());
        verificationCodeEmailRepository.save(verificationCodeEmail);

        log.info("Enviamos el correo de verificación a: {}", email);
        this.sendEmail(email, messageHtml);
    }

    @Transactional
    public boolean verifyCode(String email, String code){
        LocalDateTime validAfter = LocalDateTime.now().minusMinutes(5);

        return verificationCodeEmailRepository.existsByEmailAndCodeAndCreatedAtAfter(email, code, validAfter);
    }

    private String generateVerificationCode() {
        int codeLength = 6;
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            int digit = (int) (Math.random() * 10);
            code.append(digit);
        }
        return code.toString();
    }

    private String buildMessageHtml(String codigo) {

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="x-apple-disable-message-reformatting">
                <meta name="color-scheme" content="dark">
                <meta name="supported-color-schemes" content="dark">
                <title>Código de Verificación - MarketDollar</title>
                <style>
                * { box-sizing: border-box; }
                body, table, td { color: #ffffff !important; }
                a { text-decoration: none; color: #17CEB0; }
                @keyframes pulse {
                    0%%, 100%% { opacity: 1; }
                    50%% { opacity: 0.8; }
                }
                .code-glow {
                    animation: pulse 2s ease-in-out infinite;
                }
                </style>
            </head>
            <body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI','Helvetica Neue',Arial,sans-serif;background-color:#0a0e1a;">
                <div style="display:none;max-height:0;overflow:hidden;opacity:0;">
                    🔐 Tu código de MarketDollar es %s • Válido por 5 minutos
                </div>
            
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" 
                    style="background:linear-gradient(135deg, #0a0e1a 0%%, #1a1f3a 50%%, #0a0e1a 100%%);min-height:100vh;">
                <tr>
                    <td align="center" style="padding:40px 20px;">
                    
                    <!-- Container principal -->
                    <table role="presentation" width="600" cellspacing="0" cellpadding="0"
                        style="background:linear-gradient(145deg, #16213e 0%%, #0f1229 100%%);border-radius:16px;overflow:hidden;border:1px solid rgba(23, 206, 176, 0.15);box-shadow:0 20px 60px rgba(0,0,0,0.5);max-width:600px;">
            
                        <!-- Header con Logo -->
                        <tr>
                        <td style="padding:0;background:linear-gradient(135deg, #178FD6 0%%, #17CEB0 100%%);position:relative;">
                            <div style="padding:50px 32px 40px;text-align:center;">
                                <img src="%s"
                                    width="220" alt="MarketDollar"
                                    style="display:block;margin:0 auto;border:0;max-width:100%%;filter:drop-shadow(0 4px 12px rgba(0,0,0,0.3));">
                            </div>
                            <!-- Decoración inferior del header -->
                            <div style="height:30px;background:linear-gradient(to bottom, transparent 0%%, #16213e 100%%);"></div>
                        </td>
                        </tr>
            
                        <!-- Contenido principal -->
                        <tr>
                        <td style="padding:20px 32px 40px;">
                            <!-- Título con icono -->
                            <div style="text-align:center;margin-bottom:30px;">
                                <div style="display:inline-block;width:70px;height:70px;background:linear-gradient(135deg, rgba(23, 143, 214, 0.2) 0%%, rgba(23, 206, 176, 0.2) 100%%);border-radius:50%%;line-height:70px;font-size:36px;margin-bottom:20px;border:2px solid rgba(23, 206, 176, 0.3);box-shadow:0 0 30px rgba(23, 206, 176, 0.2);">
                                    🔐
                                </div>
                                <h1 style="margin:0;font:700 32px/1.2 'Segoe UI',Arial,sans-serif;color:#17CEB0;text-shadow:0 2px 10px rgba(23, 206, 176, 0.3);">
                                    Verifica tu Cuenta
                                </h1>
                            </div>
                            
                            <p style="margin:0 0 35px;font:400 16px/1.8 'Segoe UI',Arial,sans-serif;color:#b8bfd8;text-align:center;">
                                Hemos recibido una solicitud para verificar tu cuenta en <strong style="color:#17CEB0;font-weight:600;">MarketDollar</strong>. 
                                Utiliza el siguiente código para completar el proceso de forma segura.
                            </p>
            
                            <!-- Código de verificación con diseño mejorado -->
                            <div style="text-align:center;margin:40px 0;">
                                <div style="display:inline-block;position:relative;">
                                    <!-- Glow effect background -->
                                    <div style="position:absolute;top:-10px;left:-10px;right:-10px;bottom:-10px;background:radial-gradient(circle, rgba(23, 206, 176, 0.15) 0%%, transparent 70%%);border-radius:20px;filter:blur(20px);"></div>
                                    
                                    <!-- Código container -->
                                    <div style="position:relative;padding:35px 50px;border-radius:16px;background:linear-gradient(135deg, rgba(23, 143, 214, 0.12) 0%%, rgba(23, 206, 176, 0.12) 100%%);border:2px solid #178FD6;box-shadow:0 8px 32px rgba(23, 143, 214, 0.25),inset 0 1px 0 rgba(255,255,255,0.1);">
                                        <div style="font:600 11px/1 'Segoe UI',Arial,sans-serif;color:#17CEB0;letter-spacing:3px;text-transform:uppercase;margin-bottom:15px;opacity:0.9;">
                                            Código de Verificación
                                        </div>
                                        <div class="code-glow" style="font:800 56px/1 'Courier New',monospace;color:#17CEB0;letter-spacing:16px;text-shadow:0 0 25px rgba(23,206,176,0.6),0 0 50px rgba(23,206,176,0.3);margin:20px 0;">
                                            %s
                                        </div>
                                        <div style="display:inline-flex;align-items:center;gap:8px;background:linear-gradient(135deg, rgba(23, 143, 214, 0.25) 0%%, rgba(23, 206, 176, 0.25) 100%%);color:#17CEB0;padding:10px 24px;border-radius:25px;font-size:13px;font-weight:600;margin-top:15px;border:1px solid rgba(23, 206, 176, 0.4);">
                                            <span style="font-size:16px;">⏱</span>
                                            <span>Válido por 5 minutos</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
            
                            <!-- Separador decorativo -->
                            <div style="margin:40px 0;height:1px;background:linear-gradient(90deg, transparent 0%%, rgba(23, 206, 176, 0.4) 50%%, transparent 100%%);"></div>
            
                            <!-- Advertencia de expiración con diseño mejorado -->
                            <div style="margin:30px 0;padding:24px;background:linear-gradient(135deg, rgba(255, 193, 7, 0.08) 0%%, rgba(255, 152, 0, 0.08) 100%%);border-radius:12px;border-left:4px solid #ffc107;box-shadow:0 4px 15px rgba(255, 193, 7, 0.1);">
                                <div style="display:flex;align-items:flex-start;gap:12px;">
                                    <span style="font-size:24px;line-height:1;">⚠️</span>
                                    <div>
                                        <p style="margin:0 0 8px;font:600 15px/1.4 'Segoe UI',Arial,sans-serif;color:#ffeb3b;">
                                            Importante: Tiempo Limitado
                                        </p>
                                        <p style="margin:0;font:400 14px/1.6 'Segoe UI',Arial,sans-serif;color:#ffd54f;">
                                            Este código es válido únicamente por <strong style="color:#ffeb3b;">5 minutos</strong> desde el momento en que fue generado. 
                                            Si el código expira, deberás solicitar uno nuevo.
                                        </p>
                                    </div>
                                </div>
                            </div>
            
                            <!-- Info de seguridad con diseño mejorado -->
                            <div style="margin:30px 0;padding:24px;background:linear-gradient(135deg, rgba(23, 206, 176, 0.08) 0%%, rgba(23, 143, 214, 0.08) 100%%);border-radius:12px;border-left:4px solid #17CEB0;box-shadow:0 4px 15px rgba(23, 206, 176, 0.1);">
                                <div style="display:flex;align-items:flex-start;gap:12px;">
                                    <span style="font-size:24px;line-height:1;">🛡️</span>
                                    <div>
                                        <p style="margin:0 0 8px;font:600 15px/1.4 'Segoe UI',Arial,sans-serif;color:#17CEB0;">
                                            Seguridad Primero
                                        </p>
                                        <p style="margin:0;font:400 14px/1.6 'Segoe UI',Arial,sans-serif;color:#b8bfd8;">
                                            Nunca compartas este código con nadie, incluyendo empleados de MarketDollar. 
                                            Nuestro equipo <strong style="color:#17CEB0;">nunca te solicitará</strong> este código por teléfono, correo electrónico o mensaje de texto.
                                        </p>
                                    </div>
                                </div>
                            </div>
            
                            <p style="margin:30px 0 0;font:400 15px/1.7 'Segoe UI',Arial,sans-serif;color:#9ca3af;text-align:center;">
                                Si no solicitaste este código de verificación, puedes ignorar este correo de forma segura. 
                                Tu cuenta permanecerá protegida.
                            </p>
            
                            <!-- Sección de soporte con diseño mejorado -->
                            <div style="text-align:center;padding:32px 24px;background:linear-gradient(135deg, rgba(23, 143, 214, 0.05) 0%%, rgba(23, 206, 176, 0.05) 100%%);border-radius:12px;margin-top:35px;border:1px solid rgba(23, 206, 176, 0.1);">
                                <p style="margin:0 0 18px;font:400 15px/1.5 'Segoe UI',Arial,sans-serif;color:#b8bfd8;">
                                    ¿Necesitas ayuda? Nuestro equipo está disponible <strong style="color:#17CEB0;">24/7</strong>
                                </p>
                                <a href="#" style="display:inline-flex;align-items:center;gap:8px;color:#ffffff;background:linear-gradient(135deg, #178FD6 0%%, #17CEB0 100%%);text-decoration:none;font-weight:600;padding:14px 32px;border-radius:8px;font-size:15px;box-shadow:0 4px 15px rgba(23, 206, 176, 0.3);transition:transform 0.2s;">
                                    <span>💬</span>
                                    <span>Contactar Soporte</span>
                                </a>
                            </div>
                        </td>
                        </tr>
            
                        <!-- Footer mejorado -->
                        <tr>
                        <td style="padding:35px 32px;background:#0a0e1a;border-top:1px solid rgba(23, 206, 176, 0.15);">
                            <div style="text-align:center;">
                                <p style="margin:0 0 10px;font:600 14px/1.6 'Segoe UI',Arial,sans-serif;color:#17CEB0;">
                                    MarketDollar - Casa de Cambio
                                </p>
                                <p style="margin:0 0 20px;font:400 13px/1.6 'Segoe UI',Arial,sans-serif;color:#6b7280;">
                                    Transferencias rápidas, seguras y confiables
                                </p>
                                <p style="margin:0 0 25px;font:400 13px/1.6 'Segoe UI',Arial,sans-serif;color:#4b5563;">
                                    Este es un correo electrónico automático, por favor no respondas a este mensaje.
                                </p>
                                <div style="padding-top:20px;border-top:1px solid rgba(23, 206, 176, 0.1);margin-bottom:20px;">
                                    <a href="#" style="color:#178FD6;margin:0 12px;font-size:13px;text-decoration:none;">Centro de Ayuda</a>
                                    <span style="color:#4b5563;">•</span>
                                    <a href="#" style="color:#178FD6;margin:0 12px;font-size:13px;text-decoration:none;">Política de Privacidad</a>
                                    <span style="color:#4b5563;">•</span>
                                    <a href="#" style="color:#178FD6;margin:0 12px;font-size:13px;text-decoration:none;">Términos de Servicio</a>
                                </div>
                                <p style="margin:0;font:400 12px/1.5 'Segoe UI',Arial,sans-serif;color:#4b5563;">
                                    © 2025 MarketDollar. Todos los derechos reservados.
                                </p>
                            </div>
                        </td>
                        </tr>
                    </table>
                    </td>
                </tr>
                </table>
            </body>
            </html>
            """;
        return html.formatted(codigo, urlForLogo, codigo);
    }

    private void sendEmail(String email, String messageHtml){

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .subject("Código de Verificación - Market Dollar")
                .body(messageHtml)
                .isHtml(true)
                .build();

        try {
            webClientBuilder.build()
                    .post()
                    .uri(emailServiceUrl + "/api/email/send")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Se envio a este enlace del email, {}", emailServiceUrl + "/api/email/send");
            log.info("Se envio el correo de verificación a: {}", email);
        } catch (Exception e){
            log.error("Error al enviar email: ", e);
            throw new ApiValidateException("Error al enviar el correo de verificación");
        }

    }
}
