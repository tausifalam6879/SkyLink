package com.skylink.filter;

import com.skylink.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil
    ) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header =
                request.getHeader(
                        "Authorization"
                );

        if (
                header != null
                        &&
                        header.startsWith(
                                "Bearer "
                        )
        ) {

            String token =
                    header.substring(7);

            try {

                Claims claims =
                        jwtUtil.extractAllClaims(
                                token
                        );

                String email =
                        claims.getSubject();

                UsernamePasswordAuthenticationToken
                        authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_USER"
                                        )
                                )
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );

                System.out.println(
                        "JWT AUTHENTICATION SUCCESS: "
                                + email
                );

            } catch (Exception exception) {

                System.out.println(
                        "JWT AUTHENTICATION FAILED: "
                                + exception
                                .getClass()
                                .getSimpleName()
                                + " - "
                                + exception
                                .getMessage()
                );

                SecurityContextHolder
                        .clearContext();
            }
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}