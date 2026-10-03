package rw.ac.auca.garagerepairshopmanagementsystem.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   Environment environment) throws Exception {
        boolean oauthEnabled = StringUtils.hasText(environment.getProperty("spring.security.oauth2.client.registration.github.client-id"))
                && StringUtils.hasText(environment.getProperty("spring.security.oauth2.client.registration.github.client-secret"));
        boolean jwtResourceServerEnabled = StringUtils.hasText(environment.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri"))
            || StringUtils.hasText(environment.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri"))
            || StringUtils.hasText(environment.getProperty("spring.security.oauth2.resourceserver.jwt.public-key-location"));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                    "/api/auth/**",
                    "/error"
                ).permitAll();
                if (oauthEnabled) {
                auth.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll();
                }
                auth.anyRequest().authenticated();
            })
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (oauthEnabled) {
            http.oauth2Login(Customizer.withDefaults());
        }
        if (jwtResourceServerEnabled) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt());
        }

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
