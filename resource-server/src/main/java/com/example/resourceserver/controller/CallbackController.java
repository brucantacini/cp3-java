package com.example.resourceserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callback")
public class CallbackController {

    @GetMapping
    public ResponseEntity<String> callback(@RequestParam(value = "code", required = false) String code,
                                           @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            return ResponseEntity.badRequest()
                .body("Erro na autorização: " + error);
        }
        
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("Código de autorização não encontrado na URL");
        }
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Código de Autorização</title>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background-color: #f5f5f5;
                    }
                    .container {
                        background: white;
                        padding: 2rem;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        max-width: 600px;
                        width: 90%;
                    }
                    h1 {
                        color: #333;
                        margin-top: 0;
                    }
                    .code-box {
                        background: #f8f9fa;
                        border: 2px solid #007bff;
                        border-radius: 4px;
                        padding: 1rem;
                        margin: 1rem 0;
                        word-break: break-all;
                        font-family: monospace;
                        font-size: 0.9rem;
                    }
                    .instructions {
                        background: #e7f3ff;
                        padding: 1rem;
                        border-radius: 4px;
                        margin-top: 1rem;
                        font-size: 0.9rem;
                    }
                    .success {
                        color: #28a745;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>Código de Autorização Obtido!</h1>
                    <p class="success">✓ Autorização realizada com sucesso</p>
                    <p><strong>Seu código de autorização:</strong></p>
                    <div class="code-box">%s</div>
                    <div class="instructions">
                        <p><strong>Próximos passos:</strong></p>
                        <ol>
                            <li>Copie o código acima</li>
                            <li>No Postman, faça uma requisição POST para:</li>
                            <li><code>http://localhost:9000/oauth2/token</code></li>
                            <li>Use Basic Auth: <code>client-id</code> / <code>client-secret</code></li>
                            <li>Body (form-urlencoded):</li>
                            <ul>
                                <li><code>grant_type</code>: <code>authorization_code</code></li>
                                <li><code>code</code>: [cole o código aqui]</li>
                                <li><code>redirect_uri</code>: <code>http://localhost:8080/callback</code></li>
                            </ul>
                        </ol>
                    </div>
                </div>
            </body>
            </html>
            """.replace("%s", code != null ? code : "");
        
        return ResponseEntity.ok()
            .header("Content-Type", "text/html; charset=UTF-8")
            .body(html);
    }
}


