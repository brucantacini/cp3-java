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
@RequestMapping("/roles")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Roles", description = "Endpoints protegidos por roles - requer role ADMIN")
@SecurityRequirement(name = "bearer-jwt")
public class RolesController {

    @GetMapping
    @Operation(
        summary = "Endpoint protegido por role", 
        description = "Este endpoint requer autenticação e a role ADMIN. Apenas usuários com role ADMIN podem acessar."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Acesso permitido - usuário possui role ADMIN"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - usuário não possui role ADMIN"),
        @ApiResponse(responseCode = "401", description = "Não autenticado - token inválido ou ausente")
    })
    public ResponseEntity<String> rolesEndpoint(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Desconhecido";
        return ResponseEntity.ok(
            String.format("Acesso permitido! Você possui a role ADMIN. Usuário: %s", username)
        );
    }
}

