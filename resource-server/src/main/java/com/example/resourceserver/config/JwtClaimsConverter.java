package com.example.resourceserver.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converter de claims JWT para authorities Spring Security
 * Extrai roles e scopes do token JWT e converte em authorities
 */
public class JwtClaimsConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extrai roles do claim "roles"
        if (jwt.hasClaim("roles")) {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList()));
            }
        }

        // Extrai scopes do claim "scopes"
        if (jwt.hasClaim("scopes")) {
            List<String> scopes = jwt.getClaimAsStringList("scopes");
            if (scopes != null) {
                authorities.addAll(scopes.stream()
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(Collectors.toList()));
            }
        }

        // Também extrai scopes do claim padrão "scope" (se existir)
        if (jwt.hasClaim("scope")) {
            String scope = jwt.getClaimAsString("scope");
            if (scope != null && !scope.isEmpty()) {
                String[] scopes = scope.split(" ");
                for (String s : scopes) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + s));
                }
            }
        }

        return authorities;
    }
}

