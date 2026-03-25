package com.luisalt20.auth.controller;

import com.luisalt20.auth.dto.response.ApiResponse;
import com.luisalt20.auth.exception.ApiValidateException;
import com.luisalt20.auth.service.AuthService;
import com.luisalt20.auth.service.TotpSessionService;
import com.luisalt20.auth.util.ConstantUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/totp")
@RequiredArgsConstructor
@Validated
public class TotpController {
    private final AuthService authService;
    private final TotpSessionService totpSessionService;

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateTotp(@RequestParam String username, @RequestParam int code) throws Exception {
        var valid = authService.validateTotp(username, code);
        if (!valid) {
            throw new ApiValidateException(ConstantUtil.TOTP_INVALID);
        }
        totpSessionService.authorizeUser(username);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.TOTP_OK)
                .data(true)
                .build());
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateTotp(@RequestParam String username) {
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(ConstantUtil.OK_CODE)
                .message(ConstantUtil.TOTP_OK)
                .data(totpSessionService.isUserAuthorized(username))
                .build());
    }

    @GetMapping(value = "/view-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> viewQr() {
        byte[] imageBytes = Base64.getDecoder().decode(authService.getQrCodeBase64());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }
}


