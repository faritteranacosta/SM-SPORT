# SM-SPORT

```mermaid
flowchart TB
 subgraph subGraph0["Capa de Presentación"]
        CTRL["Controllers<br>Auth, Cliente, Proveedor,<br>Servicio, Reserva, Admin"]
  end
 subgraph subGraph1["Capa de Seguridad"]
        SEC["Security<br>JWT Filter + Token Provider<br>UserDetailsService"]
  end
 subgraph subGraph2["Capa de Servicio"]
        SERV["Services<br>Lógica de Negocio<br>Usuario, Servicio, Reserva, Pago"]
  end
 subgraph subGraph3["Capa de Persistencia"]
        REPO["Repositories<br>Acceso a Datos<br>Spring Data JPA"]
  end
 subgraph subGraph4["Capa de Dominio"]
        ENT["Entities<br>Usuario, Cliente, Proveedor,<br>Servicio, Reserva, Pago"]
  end
 subgraph subGraph5["DTOs y Mappers"]
        DTO["DTOs<br>Request/Response"]
        MAP["Mappers<br>Entity ↔ DTO<br>MapStruct"]
  end
 subgraph subGraph6["Manejo de Errores"]
        EXC["Exception Handlers<br>Global + Custom Exceptions"]
  end
 subgraph subGraph7["Base de Datos"]
        DB[("Database<br>PostgreSQL")]
  end

    %% Flujo principal
    CTRL -- "1. Request" --> SEC
    SEC -- "2. Valida JWT" --> CTRL
    CTRL -- "3. Usa" --> DTO
    DTO -- "4. Convierte con" --> MAP
    MAP -- "5. Envía Entity" --> SERV
    SERV -- "6. Llama" --> REPO
    REPO -- "7. Opera" --> ENT
    ENT -- "8. Persiste" --> DB

    %% Flujo de respuesta
    DB --> ENT
    ENT --> MAP
    MAP --> DTO
    DTO --> CTRL

    %% Manejo de errores
    SERV -. Errores .-> EXC
    CTRL -. Errores .-> EXC

    %% Estilos
    style CTRL fill:#4A90E2,color:#fff
    style SEC fill:#E94B3C,color:#fff
    style SERV fill:#50C878,color:#fff
    style REPO fill:#F39C12,color:#fff
    style ENT fill:#9B59B6,color:#fff
    style DTO fill:#1ABC9C,color:#fff
    style MAP fill:#16A085,color:#fff
    style EXC fill:#E74C3C,color:#fff
    style DB fill:#34495E,color:#fff

```

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
└── mapper/
    ├── UsuarioMapper.java
    ├── ServicioMapper.java
    └── ... (mappers con MapStruct)

