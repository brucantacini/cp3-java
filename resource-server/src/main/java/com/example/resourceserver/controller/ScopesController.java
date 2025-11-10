package com.example.resourceserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scopes")
@PreAuthorize("hasAuthority('SCOPE_read:data')")
@Tag(name = "Scopes", description = "Endpoints protegidos por scopes - requer scope read:data")
@SecurityRequirement(name = "bearer-jwt")
public class ScopesController {

    @GetMapping
    @Operation(
        summary = "Endpoint protegido por scope", 
        description = "Este endpoint requer autenticação e o scope 'read:data'. O token JWT deve conter este scope."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Acesso permitido - token possui scope read:data"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - token não possui scope read:data"),
        @ApiResponse(responseCode = "401", description = "Não autenticado - token inválido ou ausente")
    })
    public ResponseEntity<String> scopesEndpoint(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Desconhecido";
        return ResponseEntity.ok(
            String.format("Acesso permitido! Seu token possui o scope 'read:data'. Usuário: %s", username)
        );
    }
}

