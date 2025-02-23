package pl.edu.weeia.eclinic.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import pl.edu.weeia.eclinic.model.entity.AppAuthority;
import pl.edu.weeia.eclinic.model.entity.Authority;
import pl.edu.weeia.eclinic.model.repository.AuthorityRepository;
import pl.edu.weeia.eclinic.model.repository.UserRepo;
import pl.edu.weeia.eclinic.security.jwt.AuthEntryPointJwt;
import pl.edu.weeia.eclinic.security.jwt.AuthTokenFilter;
import pl.edu.weeia.eclinic.security.jwt.JwtUtils;
import pl.edu.weeia.eclinic.security.oauth2.AuthMapper;
import pl.edu.weeia.eclinic.security.oauth2.OAuth2LoginSuccessHandler;
import pl.edu.weeia.eclinic.security.services.MyUserDetailsService;
import pl.edu.weeia.eclinic.model.entity.User;

import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;


@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler successHandler;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtUtils jwtUtils;
    private final MyUserDetailsService userDetailsService;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(JwtUtils jwtUtils, MyUserDetailsService userDetailsService) {
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    SecurityConfig(@Lazy OAuth2LoginSuccessHandler successHandler,
                      @Lazy MyUserDetailsService userDetailsService,
                      AuthEntryPointJwt unauthorizedHandler,
                      JwtUtils jwtUtils) {
        this.successHandler = successHandler;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtUtils = jwtUtils;

        this.userDetailsService = userDetailsService;
    }

    private static final String[] AUTH_WHITELIST = {
            "/swagger-resources/**",
            "/v2/api-docs",
            "/h2-console/**",
            "/webjars/**",
            "/static/**",
            "/images/**",          // <-- Add this line to whitelist images
            "/css/**",             // <-- Add this line to whitelist CSS files if needed
            "/js/**",              // <-- Add this line to whitelist JS files if needed
            "/", //landing page is allowed for all
            "/landing",
            "/signup",
            "/favicon.ico",
            "/dog/**",
            "/error/**",
            "/index",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/h2-console/**",
            "/ws/**",
            "/app/**",
            "/topic/**",
            "/api/auth/public/**",

    };

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http, AuthMapper authMapper) throws Exception {
        http.csrf(csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**",
                                "/ws/**",
                                "/app/**",
                                "/topic/**",
                                "/api/auth/public/**"
                        )
                        .ignoringRequestMatchers("/ws/**", "/app/**", "/topic/**")
                        .ignoringRequestMatchers("/api/auth/public/**"));
        http
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(ah -> ah
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority(AppAuthority.ROLE_ADMIN.name())
                        .requestMatchers("/api/csrf-token").permitAll()
                        .requestMatchers("/api/auth/public/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(authMapper))// Use custom mapper
                        .successHandler(successHandler)); // Your existing handler

        http.exceptionHandling(exception
                -> exception.authenticationEntryPoint(unauthorizedHandler));
        http.addFilterBefore(authenticationJwtTokenFilter(jwtUtils, userDetailsService),
                UsernamePasswordAuthenticationFilter.class);
        http.formLogin(withDefaults());
        http.httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public CommandLineRunner initUserData(AuthorityRepository roleRepository,
                                          UserRepo userRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            Authority userAuthority = roleRepository.findByRoleName(AppAuthority.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Authority(AppAuthority.ROLE_USER)));

            Authority adminAuthority = roleRepository.findByRoleName(AppAuthority.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Authority(AppAuthority.ROLE_ADMIN)));

            if (!userRepository.existsByEmail("user1@example.com")) {
                User user1 = User.builder()
//                        .username("user1")
                        .email("user1@example.com")
                        .password(passwordEncoder.encode("password1"))
                        .authorities(Set.of(userAuthority))
                        .build();
                userRepository.save(user1);
            }

            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = User.builder()
//                        .username("admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("adminPass"))
                        .authorities(Set.of(adminAuthority))
                        .build();
                userRepository.save(admin);
            }
        };
    }
}