package com.vaistra.config.jwt;


import com.vaistra.entity.User;
import com.vaistra.exception.LoggedOutException;
import com.vaistra.exception.UserUnauthorizedException;
import com.vaistra.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

public class  JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthenticationFilter(HandlerExceptionResolver exceptionResolver, JwtService jwtService,
                                   UserDetailsService userDetailsService, UserRepository userRepository) {
        this.exceptionResolver = exceptionResolver;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String path = request.getRequestURI();
//        if (path.startsWith("/auth")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            String authHeader = request.getHeader("Authorization");
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Full authentication is required\",\"status\":401}");
//                return;
//            }
//
//            String token = authHeader.substring(7);
//            String username = jwtService.getUsernameFromToken(token);
//            if (username == null) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid JWT token\",\"status\":401}");
//                return;
//            }
//
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//            if (!jwtService.validateToken(token, userDetails)) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid JWT token\",\"status\":401}");
//                return;
//            }
//
//            // Set Spring Security authentication
//            UsernamePasswordAuthenticationToken auth =
//                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            filterChain.doFilter(request, response);
//
//        } catch (Exception e) {
//            exceptionResolver.resolveException(request, response, null, e);
//        }
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtService.getUsernameFromToken(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(token, userDetails)) {

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

        } catch (Exception e) {
            exceptionResolver.resolveException(request, response, null, e);
        }

        filterChain.doFilter(request, response);
    }
}

