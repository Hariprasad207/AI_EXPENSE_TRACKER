package com.example.Expense_Tracker.Config;

import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.entity.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepo userRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        System.out.println(
                "Request: " + request.getMethod()
                        + " " + request.getRequestURI()
        );

        System.out.println(
                "Authorization header: " + authHeader
        );

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            System.out.println("No Bearer token found");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        System.out.println(
                "Token valid: " + jwtService.isTokenValid(token)
        );

        if (!jwtService.isTokenValid(token)) {

            System.out.println("JWT validation failed");

            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtService.extractUserId(token);

        System.out.println(
                "JWT userId: " + userId
        );

        Users user = userRepo
                .findById(userId)
                .orElse(null);

        if (user == null) {
            System.out.println(
                    "User not found: " + userId
            );

        } else {

            System.out.println(
                    "User found: " + user.getEmail()
            );

            System.out.println(
                    "User active: " + user.getIsActive()
            );
        }

        if (user != null && user.getIsActive()) {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            java.util.Collections.emptyList()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            System.out.println(
                    "Authentication SUCCESS: "
                            + SecurityContextHolder
                            .getContext()
                            .getAuthentication()
            );
        }

        filterChain.doFilter(request, response);
    }
}