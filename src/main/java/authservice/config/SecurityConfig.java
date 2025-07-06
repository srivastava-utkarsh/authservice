package authservice.config;

import authservice.filter.JwtAuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;

	private final UserDetailsService userDetailsService;

	private final PasswordEncoder passwordEncoder;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService,PasswordEncoder passwordEncoder) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthFilter jwtAuthFilter) throws Exception {
		return http
				// Disable CSRF (not needed for stateless JWT)
				.csrf(csrf->csrf.disable())
				// Configure endpoint authorization
				.authorizeHttpRequests(auth->
						// Public endpoints
						auth.requestMatchers("/auth/v1/signup","/auth/v1/login","/auth/v1/refreshToken")
								.permitAll()
								// All other endpoints require authentication
								.anyRequest()
								.authenticated()
				)
				// Stateless session (required for JWT)
				.sessionManagement(sess->sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Add JWT filter before Spring Security's default filter
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				// Set custom authentication provider
				.authenticationProvider(authenticationProvider())
				.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider(){
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);
		return authenticationProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
