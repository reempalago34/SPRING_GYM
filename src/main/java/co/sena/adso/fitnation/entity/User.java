package co.sena.adso.fitnation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Réplica de app/models/users.py (Flask).
 *
 * En Flask la columna del hash se declara así:
 *     password_hash = db.Column('passwordUser', db.String(255), nullable=False)
 * por eso aquí el atributo es {@code passwordHash} pero la columna real
 * sigue siendo "passwordUser".
 */
@Entity
@Table(name = "users", schema = "fitnation")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUser")
    private Integer idUser;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "passwordUser", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String rol = "usuario";

    @Column(length = 20)
    private String telefono;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public User() {
    }

    public User(String nombre, String email, String passwordHash, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    /** Equivale a UserMixin.get_id() de Flask-Login. */
    public String getIdUserAsString() {
        return String.valueOf(idUser);
    }

    /** Réplica de User.tiene_inscripcion_activa() de Flask. */
    public boolean tieneInscripcionActiva() {
        return false;
    }

    /**
     * Réplica del default de Flask: {@code creado_en = db.Column(db.DateTime, default=get_colombia_time)}.
     * Sin esto Hibernate envía NULL y la columna es NOT NULL.
     */
    @PrePersist
    void onCreate() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now(ZoneId.of("America/Bogota"));
        }
    }

    /**
     * Propiedades de solo lectura que Flask expone en el modelo User
     * ({@code nameUser}, {@code emailUser}, {@code creado_enUser}) y que las
     * plantillas usan tal cual.
     */
    public String getNameUser() {
        return nombre;
    }

    public String getEmailUser() {
        return email;
    }

    public LocalDateTime getCreadoEnUser() {
        return creadoEn;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
