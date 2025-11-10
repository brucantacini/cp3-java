package com.example.resourceserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@Tag(name = "Public", description = "Endpoints públicos - não requerem autenticação")
public class PublicController {

    @GetMapping
    @Operation(summary = "Endpoint público", description = "Endpoint que não requer autenticação. Qualquer pessoa pode acessar.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Acesso permitido")
    })
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Este é um endpoint público. Não requer autenticação!");
    }
}

