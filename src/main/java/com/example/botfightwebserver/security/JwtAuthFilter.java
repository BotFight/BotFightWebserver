package com.example.botfightwebserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${ADMINS}")
    private String admins;

    private Set<String> adminIds;

    @PostConstruct  // Load admins when bean is created
    public void loadAdmins() {
        adminIds=Arrays.stream(StringUtils.trimAllWhitespace(admins).split(",")).collect(Collectors.toSet());
    }


    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                String userId = claims.getSubject();
                List<String> roles = new ArrayList<>();
                roles.add("USER");
                if (adminIds.contains(userId)) {
                    roles.add("ADMIN");
                }
                List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Spring Security expects "ROLE_" prefix
                    .toList();
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, authorities));
                SecurityContextHolder.setContext(context);
            } catch (JwtException e) {
                System.out.println("blocked because try fail" + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "idk whats happening " + e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
