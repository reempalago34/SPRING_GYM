package co.sena.adso.fitnation.security;

import co.sena.adso.fitnation.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * UserDetails de Spring Security construido sobre el User de Flask.
 *
 * Flask usa directamente el rol crudo ('admin', 'usuario'...) con
 * UserMixin; aquí lo exponemos como ROLE_* para poder usar
 * hasRole() en la configuración de seguridad.
 */
public class FitnationUserDetails implements UserDetails {

    private final User user;

    public FitnationUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Integer getIdUser() {
        return user.getIdUser();
    }

    /** Réplica de <code>current_user.nombre</code> de la plantilla de Flask. */
    public String getNombre() {
        return user.getNombre();
    }

    /** Réplica de <code>current_user.rol</code> de la plantilla de Flask. */
    public String getRol() {
        return user.getRol();
    }

    public boolean tieneInscripcionActiva() {
        return user.tieneInscripcionActiva();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().toUpperCase()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        // Flask hace login por nombre de usuario (campo nameUser -> nombre)
        return user.getNombre();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
