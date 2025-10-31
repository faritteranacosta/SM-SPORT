package com.sm_sport.model.entity;

@Entity
@Table(name = "politicas_reembolso", indexes = {
        @Index(name = "idx_politica_activa", columnList = "activa")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoliticaReembolso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_politica", length = 50)
    private String idPolitica;

    @Column(name = "nombre_politica", nullable = false, length = 100)
    private String nombrePolitica;

    @Column(name = "dias_anticipacion", nullable = false)
    private Integer diasAnticipacion; // Días de anticipación para cancelar

    @Column(name = "porcentaje_reembolso", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeReembolso; // Porcentaje a reembolsar

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
