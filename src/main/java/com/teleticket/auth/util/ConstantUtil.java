package com.luisalt20.auth.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantUtil {
  public static final String OK_CODE = "000";
  public static final String OK_MESSAGE = "Operación realizada con éxito.";
  public static final String ERROR_MESSAGE = "Ocurrió un error al procesar la operación.";
  public static final String ERROR_CODE = "999";
  public static final String PASSWORD_CHANGED = "Contraseña cambiada con éxito";
  public static final String USER_REGISTERED = "Usuario registrado con éxito";
  public static final String LOGIN_SUCCESS = "Inicio de sesión exitoso";
  public static final String TOKEN_REFRESHED = "Token renovado con éxito";
  public static final String TOKEN_VALIDATED = "Token validado correctamente";
  public static final String LOGGED_OUT = "Sesión cerrada correctamente";
  public static final String LOGGED_OUT_ALL_DEVICES = "Sesión cerrada en todos los dispositivos";
  public static final String PASSWORD_RESET_REQUESTED = "Solicitud de cambio de contraseña enviada con éxito";
  public static final String PASSWORD_RESET = "Contraseña restablecida con éxito";
  public static final String ACCOUNT_DELETED = "Cuenta eliminada con éxito";
  public static final String USER_FOUND = "Usuario encontrado con éxito";
  public static final String USER_NOT_FOUND = "No hay sesión activa.";
  public static final String ROL_NOT_FOUND = "Rol no encontrado.";
  public static final String MISSING_TOKEN = "Token faltante";
  public static final String BAD_REQUEST = "Solicitud incorrecta";
  public static final String TOTP_OK = "Acceso temporal otorgado.";
  public static final String TOTP_ERROR = "Código inválido.";
  public static final String TOTP_INVALID = "Código inválido.";
  public static final String MUST_VALIDATE = "Debe validar su TOTP antes de acceder a este recurso.";
  public static final String PASSWORD_RESET_EMAIL_SENT = "Correo de restablecimiento de contraseña enviado.";
  public static final String PASSWORD_RESET_MESSAGE = "Contraseña actualizada exitosamente";
  public static final String TOKEN_VALIDATION_MESSAGE = "El token es válido.";
  public static final String PASSWORD_RESET_EMAIL_RESENT = "Correo de verificacion enviado.";
  public static final String EMAIL_VERIFICATION_SENT = "Correo verficado con exito.";
}
