package vtb.courses.spring_security.lesson2.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@EnableMethodSecurity
public class HttpController implements ErrorController {
	@Autowired
	UserDetailsManager userManager;

	BCryptPasswordEncoder passwordEncoder;

	@PreAuthorize("hasRole('PERMISSION_INDEX')")
	@GetMapping("/")
	public String index() {
		return "index";
	}

	@PreAuthorize("hasRole('PERMISSION_ROUTE1')")
	@GetMapping("/route1")
	public String route1(Model model) {
		// Вернём в страницу имя текущего пользователя
		model.addAttribute("name", SecurityContextHolder.getContext().getAuthentication().getName());
		return "route1";
	}
	@PreAuthorize("hasRole('PERMISSION_ROUTE2')")
	@GetMapping("/route2")
	public String route2(Model model) {
		// Вернём в страницу имя текущего пользователя
		model.addAttribute("name", SecurityContextHolder.getContext().getAuthentication().getName());
		return "route2";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/create_user")
	public String createUser(Model model) {
		if (userManager.userExists("Joe")) {
			// Если такой польватель уже имеется проинформируем об этом на странице
			model.addAttribute("user_status", " already exists!");
		} else {
			// Пользователя ещё нет, создаем через UserDetailsManager, устанавливаем временный пароль
			String tempPassword = String.valueOf((int) (Math.random() * 899 + 100));
			UserDetails user = User.builder()
					.username("Joe")
					.password("{bcrypt}" + passwordEncoder.encode(tempPassword))
					.roles("PERMISSION_ROUTE1", "PERMISSION_INDEX")
					.build();
			userManager.createUser(user);
			model.addAttribute("user_status", "created! Temporary password: " + tempPassword);
		}
		return "user_created";
	}

	public HttpController() {
		passwordEncoder = new BCryptPasswordEncoder();
	}
}
