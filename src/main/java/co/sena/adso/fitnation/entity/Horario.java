package co.sena.adso.fitnation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Réplica de app/models/horario.py (Flask).
 *
 * Notas de mapeo:
 * - Flask guarda hora_inicio/hora_fin como String(10) ("HH:MM"), no como TIME.
 * - El entrenador es la FK idUser -> users (relación trainer de Flask).
 * - usuariosInscritos es la tabla user_horarios (columnas user_id/horario_id,
 *   minúsculas como en Flask).
 * - idUser se mapea como columna básica (lo usan las plantillas) y trainer
 *   como ManyToOne de solo lectura sobre la misma columna.
 * - trainer/usuariosInscritos se dejan no nulos porque las plantillas los
 *   recorren aunque el horario venga vacío (placeholder de /horarios/edit).
 */
@Entity
@Table(name = "horarios", schema = "fitnation")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHorario")
    private Integer idHorario;

    @Column(name = "idUser", nullable = false)
    private Integer idUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", referencedColumnName = "idUser",
            insertable = false, updatable = false)
    private User trainer = new User();

    @Column(name = "dia_semana", nullable = false, length = 20)
    private String diaSemana;

    @Column(name = "hora_inicio", nullable = false, length = 10)
    private String horaInicio;

    @Column(name = "hora_fin", nullable = false, length = 10)
    private String horaFin;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_horarios", schema = "fitnation",
            joinColumns = @JoinColumn(name = "horario_id", referencedColumnName = "idHorario"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "idUser"))
    private List<User> usuariosInscritos = new ArrayList<>();

    public Horario() {
    }

    public Integer getIdHorario() { return idHorario; }
    public void setIdHorario(Integer idHorario) { this.idHorario = idHorario; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    public User getTrainer() { return trainer; }
    public void setTrainer(User trainer) { this.trainer = trainer; }

    public List<User> getUsuariosInscritos() { return usuariosInscritos; }
    public void setUsuariosInscritos(List<User> usuariosInscritos) {
        this.usuariosInscritos = usuariosInscritos;
    }
}
