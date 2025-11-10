package com.example.authorizationserver.config;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.stream.Collectors;

/**
 * Customizador de claims JWT
 * Adiciona roles e scopes customizados ao token JWT
 */
public class JwtClaimsCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        // Adiciona scopes ao token
        if (context.getAuthorizationGrantType() != null) {
            context.getClaims().claim("scopes", 
                context.getAuthorizedScopes().stream()
                    .collect(Collectors.toList()));
        }

        // Adiciona roles do usuário autenticado
        if (context.getPrincipal() != null) {
            var authorities = context.getPrincipal().getAuthorities();
            var roles = authorities.stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
            
            context.getClaims().claim("roles", roles);
            context.getClaims().claim("authorities", 
                authorities.stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toList()));
        }

        // Adiciona username
        if (context.getPrincipal().getName() != null) {
            context.getClaims().claim("username", context.getPrincipal().getName());
        }
    }
}

