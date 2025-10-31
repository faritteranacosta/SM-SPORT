# SM-SPORT

```text
src/main/java/com/sm_sport/
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── SwaggerConfig.java
├── model/
│   ├── entity/
│   │   ├── Usuario.java
│   │   ├── Cliente.java
│   │   ├── Proveedor.java
│   │   ├── Administrador.java
│   │   ├── Servicio.java
│   │   ├── Reserva.java
│   │   ├── Pago.java
│   │   ├── Resena.java
│   │   └── ... (todas las entidades)
│   ├── enums/
│   │   ├── EstadoUsuario.java
│   │   ├── EstadoReserva.java
│   │   ├── MetodoPago.java
│   │   └── ... (todos los enums)
├── repository/
│   ├── UsuarioRepository.java
│   ├── ClienteRepository.java
│   ├── ServicioRepository.java
│   ├── ReservaRepository.java
│   └── ... (todos los repositorios)
├── service/
│   ├── UsuarioService.java
│   ├── ServicioService.java
│   ├── ReservaService.java
│   ├── PagoService.java
│   └── ... (todos los servicios)
├── service/impl/
│   ├── UsuarioServiceImpl.java
│   ├── ServicioServiceImpl.java
│   └── ... (implementaciones)
├── dto/
│   ├── request/
│   │   ├── RegistroUsuarioRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CrearServicioRequest.java
│   │   └── ReservaRequest.java
│   └── response/
│       ├── UsuarioResponse.java
│       ├── ServicioResponse.java
│       └── ReservaResponse.java
├── controller/
│   ├── AuthController.java
│   ├── ClienteController.java
│   ├── ProveedorController.java
│   ├── ServicioController.java
│   ├── ReservaController.java
│   └── AdminController.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   └── UnauthorizedException.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
├── mapper/
│   ├── UsuarioMapper.java
│   ├── ServicioMapper.java
│   └── ... (mappers con MapStruct)
└── util/
    ├── DateUtil.java
    └── ValidationUtil.java
