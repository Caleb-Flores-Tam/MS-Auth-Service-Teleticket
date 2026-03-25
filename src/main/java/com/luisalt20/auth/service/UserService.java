package com.luisalt20.auth.service;

import com.luisalt20.auth.config.JwtProperties;
import com.luisalt20.auth.dto.enumeration.UserStatus;
import com.luisalt20.auth.dto.request.RegisterRequest;
import com.luisalt20.auth.entity.User;
import com.luisalt20.auth.entity.UserRole;
import com.luisalt20.auth.exception.ApiValidateException;
import com.luisalt20.auth.mapper.UserMapper;
import com.luisalt20.auth.repository.RoleRepository;
import com.luisalt20.auth.repository.UserRepository;
import com.luisalt20.auth.repository.UserRoleRepository;
import com.luisalt20.auth.util.ConstantUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserRoleRepository urRepo;
    private final PasswordEncoder encoder;
    private final UserMapper mapper;
    private final TotpService totpService;
    private final JwtProperties jwtProperties;

    @Transactional
    public User register(String email, String rawPassword, String role) throws Exception {
        if (userRepo.existsByEmail(email)) throw new ApiValidateException("Email ya registrado.");

        var user = mapper.toEntity(new RegisterRequest(email, rawPassword, role));
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Key key = totpService.generateSecretKey();
        Base32 base32 = new Base32();
        var secretBase32 = base32.encodeToString(key.getEncoded());
        user.setSecretBase32(secretBase32);
        var otpAuthURL = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                jwtProperties.getNameTotp(),
                user.getEmail(),
                secretBase32.replace("=", ""),
                jwtProperties.getNameTotp()
        );
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        user.setQrCodeBase64(Base64.getEncoder().encodeToString(baos.toByteArray()));
        user = userRepo.save(user);

        urRepo.save(UserRole.builder().user(user).role(roleRepo.findByName(role)
                .orElseThrow(() -> new ApiValidateException(ConstantUtil.ROL_NOT_FOUND))).build());
        return user;
    }

    public List<String> rolesOf(Long userId) {
        return urRepo.findRoleNamesByUserId(userId);
    }
}

