package vtb.courses.spring_security.lesson2.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * SecuritySettings - полная конфигурация системы доступа
 */
@Component
public class SecuritySettings {
    @Autowired
    private DataSource usersDataSource;

    private static void  handleException(HttpServletRequest request, HttpServletResponse response, RequestRejectedException requestRejectedException) throws IOException, ServletException {
        System.out.println("Exception: " + requestRejectedException);
    }

    @Bean
    public WebSecurityCustomizer initSecurity() {

        return web -> web
                .requestRejectedHandler(SecuritySettings::handleException)
                ;
    }

    /**
     * Инициализирует поведение Spring Security
     *      Всем пользователям - разрешает завершать сессию
     *      Устанавливает начальную страницу, при успешном логине
     *      Требует авторизации для доступа к любой странице
     *      Разрешается сохранять сессию пользователя на его устройстве доступа
     *      Устанавливаем свою страницу для ошибки доступа 403
     */
    @Bean @Order(1)
    SecurityFilterChain filterChainAuthenticatedAccessOnly(HttpSecurity http) throws Exception{

        return http
                .logout(LogoutConfigurer::permitAll)
                .formLogin(c -> c.defaultSuccessUrl("/", true))
                .authorizeHttpRequests(c -> c
                        .anyRequest().authenticated()
                )
                .rememberMe(httpSecurityRememberMeConfigurer -> {
                            var repository = new JdbcTokenRepositoryImpl();
                            repository.setDataSource(usersDataSource);
                            httpSecurityRememberMeConfigurer.tokenRepository(repository);
                            httpSecurityRememberMeConfigurer.key("RememberMeTestKey");
                        }
                )
                .exceptionHandling(c -> c.accessDeniedPage("/AccessDenied.html"))
                .build();
    }

    /**
     * roleHierarchy - задаёт права пользователей
     */
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("""
                ROLE_ADMIN > ROLE_PERMISSION_INDEX
                ROLE_ADMIN > ROLE_PERMISSION_ROUTE1
                ROLE_ADMIN > ROLE_PERMISSION_ROUTE2
                ROLE_USER > ROLE_PERMISSION_INDEX
                ROLE_USER > ROLE_PERMISSION_ROUTE2
                """);
        return roleHierarchy;
    }

    /**
     * users - Создаём менеджер пользователей связанный с нашим JDBC источником данных
     */
    @Bean
    UserDetailsManager users(DataSource dataSource) {
        JdbcUserDetailsManager userManager = new JdbcUserDetailsManager(usersDataSource);
        userManager.setEnableGroups(true);
        return userManager;
    }

}
