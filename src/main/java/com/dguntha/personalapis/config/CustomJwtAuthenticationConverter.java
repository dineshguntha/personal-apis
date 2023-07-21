package com.dguntha.personalapis.config;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Extract custom claims from the JWT token (if any)
        Map<String, Object> claims = jwt.getClaims();
        System.out.println("Inside the custom JWT Authentication converter ...********");
        // Perform additional validation checks or custom logic here
        // For example, check if the user has a specific role in the token claims

        // Get the authorities (roles) from the token and convert them to SimpleGrantedAuthority objects
        Collection<GrantedAuthority> authorities = extractAuthoritiesFromToken(claims);

        // Create a custom AuthenticationToken with the extracted authorities
        return new CustomAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthoritiesFromToken(Map<String, Object> claims) {
        // For demonstration purposes, let's assume the authorities are in a claim called "roles"
        Object roles = claims.get("roles");
        if (roles instanceof Collection<?>) {
            return ((Collection<?>) roles).stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
