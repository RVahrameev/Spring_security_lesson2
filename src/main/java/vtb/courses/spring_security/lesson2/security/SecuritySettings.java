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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;

@Component
public class SecuritySettings {
    @Autowired
    private DataSource usersDataSource;

    private JdbcUserDetailsManager userManager;

    private static void  handleException(HttpServletRequest request, HttpServletResponse response, RequestRejectedException requestRejectedException) throws IOException, ServletException {
        System.out.println("Exception: " + requestRejectedException);
    }

    @Bean
    public WebSecurityCustomizer initSecurity() {

        return web -> web
                .requestRejectedHandler(SecuritySettings::handleException)
                ;
    }

//    @Bean @Order(1)
//    SecurityFilterChain logoutAccess(HttpSecurity http) throws Exception{
//        return http
//                .formLogin(c -> c.defaultSuccessUrl("/", true))
//                .authorizeHttpRequests((authorize) -> authorize
//                        .requestMatchers("/logout").permitAll())
//                .build()
//                ;
//    }

    @Bean @Order(1)
    SecurityFilterChain filterChainAuthenticatedAccessOnly(HttpSecurity http) throws Exception{

        return http
                .logout(c -> c.permitAll())
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
                .build();
    }

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

    @Bean
    UserDetailsManager users(DataSource dataSource) {
        userManager = new JdbcUserDetailsManager(usersDataSource);
        userManager.setEnableGroups(true);
        return userManager;
    }

}
