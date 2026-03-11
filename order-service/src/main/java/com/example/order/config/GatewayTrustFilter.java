package com.example.order.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GatewayTrustFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Извлекаем user context из headers (добавленных Gateway)
        String userId = request.getHeader("X-User-Id");
        String roles = request.getHeader("X-User-Roles");
        String userEmail = request.getHeader("X-User-Email");


        if (userId != null) {
            // Создаем Authentication объект из заголовков
            List<GrantedAuthority> authorities = extractAuthorities(roles);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            // Добавляем email в details
            Map<String, Object> details = new HashMap<>();
            details.put("email", userEmail);
            auth.setDetails(details);
            // Устанавливаем в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private List<GrantedAuthority> extractAuthorities(String rolesHeader) {
        if (rolesHeader == null) return List.of();
        return Arrays.stream(rolesHeader.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
