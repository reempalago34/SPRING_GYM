package co.sena.adso.fitnation.config;

import co.sena.adso.fitnation.security.FitnationUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Seguridad por sesión de formulario — el equivalente a Flask-Login.
 *
 * Flask:
 *   @bp.route('/', methods=['GET', 'POST'])      -> login
 *   @bp.route('/dashboard')                      -> panel
 *   @bp.route('/logout')                         -> cierre de sesión
 *   login_manager.login_view = 'auth.login'      -> /login por defecto
 *
 * Aquí: /login -> procesamiento -> /dashboard, sesión en servidor (cookie JSESSIONID),
 * igual que la sesión firmada de Flask.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FitnationUserDetailsService userDetailsService;

    public SecurityConfig(FitnationUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Contraseñas.
     *
     * OJO con los datos migrados desde Flask: Werkzeug usa scrypt con su
     * propio formato ("scrypt:32768:8:1$salt$hash"), que NO es el formato
     * "$s0$..." de SCryptPasswordEncoder de Spring. Los usuarios ya
     * existentes en producción necesitan un PasswordEncoder dedicado que
     * detecte el prefijo "scrypt:"; los nuevos se guardan en bcrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Obligatorio para que el control de sesiones concurrentes
     * (maximumSessions) detecte el cierre de sesión: sin este publisher
     * Spring no se entera de que la sesión murió.
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Público: login, estáticos y health (Coolify)
                .requestMatchers(
                    // En Flask GET / es la pantalla de login
                    "/",
                    "/login",
                    "/forgot-password", "/reset-password/**",
                    // En Flask /User/add (Crear Cuenta) no lleva @login_required:
                    // el botón "Crear Cuenta" está en la pantalla de login.
                    "/User/add",
                    "/css/**", "/js/**", "/img/**", "/bootstrap/**",
                    "/favicon.ico", "/health", "/error",
                    // Documentación OpenAPI/Swagger de la API REST
                    "/docs", "/docs/**", "/api-docs", "/api-docs/**",
                    "/swagger-ui.html", "/swagger-ui/**", "/webjars/**"
                ).permitAll()
                // Backend REST: los GET de /api/** son públicos (solo lectura).
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("nameUser")       // mismo nombre de campo que en Flask
                .passwordParameter("passwordUser")   // mismo nombre de campo que en Flask
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                // Flask cierra sesión con un simple <a href="/logout"> (GET);
                // se conserva ese comportamiento para replicar 1:1 el menú.
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Sin sesiones múltiples: una sola sesión por usuario, como Flask
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?logout")
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect(request.getContextPath() + "/login"))
            );

        return http.build();
    }
}
