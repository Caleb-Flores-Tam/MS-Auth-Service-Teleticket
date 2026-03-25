# 🔐 Authentication & Authorization Service - Teleticket
<br>

<p >

Este microservicio es el encargado de centralizar la seguridad de la plataforma Teleticket. Provee una solución robusta para la gestión de identidades, autenticación multifactor (2FA) y autorización basada en roles (RBAC), utilizando estándares modernos de la industria.
</p>
<br><br>

# 🚀 Tecnologías y Seguridad
<br>

- Spring Boot 3 & Spring Security: Núcleo de la lógica de seguridad.
- JSON Web Tokens (JWT): Implementación de autenticación Stateless con soporte para Access Tokens y Refresh Tokens.
- Asymmetric Encryption (RSA/Keys): Uso de llaves públicas y privadas para la firma y validación de tokens.
- TOTP (Two-Factor Authentication): Soporte para autenticación de dos factores mediante Google Authenticator o similares.
- Hibernate / Spring Data JPA: Persistencia de usuarios, roles y tokens en base de datos.
- MapStruct: Para un mapeo limpio entre entidades y DTOs.
- Docker: Listo para despliegue en contenedores.
<br><br>

# 🛠️ Características Principales
<br>

<p >

- Gestión de Ciclo de Vida del Usuario: Registro, inicio de sesión, verificación de correo electrónico y recuperación de contraseñas.
- Seguridad Avanzada con 2FA: Implementación de `TotpController` y `QrCodeGenerator` para permitir a los usuarios vincular sus cuentas con aplicaciones de autenticación.
- Endpoint JWKS (`JwksController`): Exposición de llaves públicas para que otros microservicios (como el de Órdenes o Eventos) puedan validar los tokens de forma independiente sin consultar constantemente al servicio de Auth.
- Sistema de Roles (RBAC): Manejo de permisos basado en `UserRole` y `Role` para restringir el acceso a endpoints administrativos.
- Refresh Token Flow: Mecanismo para renovar la sesión del usuario sin necesidad de re-autenticarse, mejorando la experiencia de usuario y la seguridad.
</p>

<br><br>

# 🏛️ Estructura del Proyecto
<br>

<p >

- `config`: Configuración de seguridad, propiedades JWT y gestión de llaves criptográficas.
- `controller`: Endpoints para autenticación, gestión de TOTP y descubrimiento de llaves (OIDC style).
- `service`: Lógica de negocio segmentada (`JwtService`, `TotpService`, `PasswordResetService`).
- `dto`: Amplia gama de objetos de transferencia para peticiones (`LoginRequest`, `RegisterRequest`) y respuestas estandarizadas.
- `util`: Utilidades criptográficas, generación de QR y constantes.
</p>
<br><br>

# ⚙️ Configuración Rápida
<br>

<p >

- Requisitos: Java 17+, Maven, Base de Datos (MySQL/PostgreSQL).
- Generación de Llaves: El servicio utiliza KeyConfig.java para manejar las llaves RSA necesarias para la firma de tokens.
- Ejecución:

```http
    mvn clean install
    mvn spring-boot:run
```
</p>

<br><br>

# 🔐 Endpoints Clave
<br>
<p >

- `POST /auth/login`: Autenticación y generación de tokens.
- `POST /auth/register`: Creación de nuevas cuentas.
- `GET /auth/.well-known/jwks.json`: Expone las llaves públicas para validación externa.
- `POST /totp/enablev`: Inicia el proceso de activación de 2FA generando un código QR.
</p>