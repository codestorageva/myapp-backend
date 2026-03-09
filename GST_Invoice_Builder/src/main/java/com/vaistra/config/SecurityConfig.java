package com.vaistra.config;

import com.vaistra.config.jwt.JwtAuthenticationEntryPoint;
import com.vaistra.config.jwt.JwtAuthenticationFilter;
import com.vaistra.config.jwt.JwtService;
import com.vaistra.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final HandlerExceptionResolver exceptionResolver;
    private final OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler;


    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtService jwtService, UserRepository userRepository, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver, OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler)
    {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.exceptionResolver = exceptionResolver;
        this.oAuthAuthenticationSuccessHandler = oAuthAuthenticationSuccessHandler;
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter()
    {
        return new JwtAuthenticationFilter(exceptionResolver, jwtService, userDetailsService, userRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/auth/login").permitAll()
//                        .requestMatchers("/auth/SocialLogin").permitAll()
                                .requestMatchers(HttpMethod.POST, "/auth/user/register").permitAll()
                                .requestMatchers(HttpMethod.POST, "/auth/changePassword").permitAll()
                                .requestMatchers(HttpMethod.POST, "/auth/emailExist").permitAll()
//                                .requestMatchers(HttpMethod.POST,"/product").permitAll()
                                .requestMatchers("/auth/forget-pwd").permitAll()
                                .requestMatchers("/auth/chk-forget-otp").permitAll()
                                .requestMatchers("/auth/reset-pwd").permitAll()
                                .requestMatchers("/auth/resend-otp").permitAll()
                                .requestMatchers("/auth/mobile-otp-send").permitAll()
                                .requestMatchers("/auth/mobile-otp-verify").permitAll()
                                .requestMatchers("/auth/email-otp-send").permitAll()
                                .requestMatchers("/auth/email-otp-verify").permitAll()
                                .requestMatchers("/auth/vidExist").permitAll()
                                .requestMatchers("/api/test").permitAll()
                                .requestMatchers("/auth/username-availability").permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth->{
                    oauth.successHandler(oAuthAuthenticationSuccessHandler);
                })
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 🔧 Important: Register authentication provider
        httpSecurity.authenticationProvider(getDaoAuthenticationProvider());

        httpSecurity.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
    @Bean
    public AuthenticationProvider getDaoAuthenticationProvider()
    {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(getPasswordEncoder());

        return daoAuthenticationProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder getPasswordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper()

    {
        return new ModelMapper();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000"); // Add your frontend origin(s) here
        configuration.addAllowedOrigin("http://192.168.1.111:3000");
        configuration.addAllowedOrigin("http://local-invoice.com");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

