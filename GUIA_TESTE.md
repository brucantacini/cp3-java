# Guia de Teste - CP3 OAuth 2.0

## 📋 Requisitos do CP3

Este guia demonstra a implementação completa conforme os requisitos:

1. ✅ **Authorization Server**: Emite Access Tokens e ID Tokens JWT
2. ✅ **Authorization Code Grant Flow**: Fluxo completo para clientes confidenciais
3. ✅ **Claims JWT**: Tokens contêm ROLE e SCOPES
4. ✅ **Resource Server**: Valida tokens JWT usando chaves assimétricas (RSA)
5. ✅ **Endpoints**: `/public`, `/roles` (protegido por ROLE), `/scopes` (protegido por SCOPE)
6. ✅ **Swagger/OpenAPI**: Documentação completa de todos os endpoints

## Pré-requisitos
- Java 21 instalado
- Maven instalado (ou usar Maven Wrapper `mvnw.cmd`)
- Postman ou similar (opcional, mas recomendado)

## Passo 1: Iniciar Authorization Server

```bash
cd authorization-server
.\mvnw.cmd spring-boot:run
```

**OU usar o script:**
```bash
cd authorization-server
.\start.bat
```

O servidor iniciará na porta **9000**

**Verificar se iniciou corretamente:**
- Acesse: http://localhost:9000/oauth2/jwks
- Deve retornar um JSON com as chaves públicas RSA (JWK Set)

## Passo 2: Iniciar Resource Server (em outro terminal)

```bash
cd resource-server
.\mvnw.cmd spring-boot:run
```

**OU usar o script:**
```bash
cd resource-server
.\start.bat
```

O servidor iniciará na porta **8080**

## Passo 3: Obter Tokens JWT (Access Token e ID Token)

### 3.1. Via Browser - Fluxo Authorization Code Grant

1. **Acesse no navegador:**
```
http://localhost:9000/oauth2/authorize?response_type=code&client_id=client-id&redirect_uri=http://localhost:8080/callback&scope=read:data write:info openid profile
```

2. **Faça login com:**
   - Usuário: `admin` / Senha: `admin123` (tem role ADMIN)
   - OU Usuário: `manager` / Senha: `manager123` (tem role MANAGER)
   - OU Usuário: `user` / Senha: `user123` (tem role USER)

3. **Autorize o acesso** (clique no botão "Autorizar")

4. **Você será redirecionado para:** `http://localhost:8080/callback?code=ABC123...`

5. **Copie o código de autorização** da URL (o código após `code=`)

### 3.2. Trocar Código por Tokens

**IMPORTANTE:** O código de autorização é de uso único e expira rapidamente. Use-o imediatamente após gerar.

#### Opção A: Via cURL

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "client-id:client-secret" \
  -d "grant_type=authorization_code&code=SEU_CODIGO_AQUI&redirect_uri=http://localhost:8080/callback"
```

#### Opção B: Via Postman/Insomnia (RECOMENDADO)

**POST** `http://localhost:9000/oauth2/token`

- **Headers:**
  - Content-Type: `application/x-www-form-urlencoded`
  - Authorization: `Basic Auth`
    - Username: `client-id`
    - Password: `client-secret`

- **Body (x-www-form-urlencoded):**
  - grant_type: `authorization_code`
  - code: `[código copiado do passo anterior]`
  - redirect_uri: `http://localhost:8080/callback`

**Resposta esperada:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_token": "...",
  "id_token": "eyJhbGciOiJSUzI1NiIs...",
  "scope": "read:data write:info openid profile"
}
```

**✅ IMPORTANTE:** A resposta contém:
- **access_token**: Token JWT para acessar recursos protegidos
- **id_token**: Token JWT com informações do usuário (OpenID Connect)
- Ambos são tokens JWT assinados com chave privada RSA

## Passo 4: Verificar Chaves Assimétricas (RSA)

**✅ REQUISITO CP3:** Resource Server deve validar tokens usando chaves assimétricas.

### 4.1. Verificar JWK Set (chaves públicas)

Acesse no navegador ou via cURL:
```
http://localhost:9000/oauth2/jwks
```

**Resposta esperada:** JSON com chaves públicas RSA (formato JWK Set)
```json
{
  "keys": [
    {
      "kty": "RSA",
      "n": "...",
      "e": "AQAB",
      ...
    }
  ]
}
```

**Explicação:**
- Authorization Server expõe chaves **públicas** em `/oauth2/jwks`
- Resource Server busca essas chaves para validar tokens
- Chave **privada** fica apenas no Authorization Server (para assinar tokens)
- Isso garante segurança: Resource Server não precisa conhecer a chave privada

### 4.2. Verificar que Resource Server usa chaves assimétricas

1. Acesse: `resource-server/src/main/resources/application.properties`
2. Verifique: `spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9000/oauth2/jwks`
3. Isso confirma que o Resource Server busca chaves públicas do Authorization Server

## Passo 5: Testar Endpoints do Resource Server

### 5.1. Endpoint Público (sem token) - ✅ REQUISITO CP3

```bash
curl http://localhost:8080/public
```

**Resposta esperada:** 
```json
{
  "message": "Este é um endpoint público. Não requer autenticação!",
  "timestamp": "2025-11-09T..."
}
```

**✅ Validação:** Endpoint acessível sem autenticação.

### 5.2. Endpoint Protegido por ROLE - ✅ REQUISITO CP3

**Requisito:** Acesso restrito a usuários com ROLE específica (ex: ADMIN)

```bash
curl http://localhost:8080/roles \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_AQUI"
```

**Resposta esperada (com role ADMIN):**
```json
{
  "message": "Acesso permitido! Você possui a role ADMIN.",
  "usuario": "admin",
  "roles": ["ADMIN", "USER"]
}
```

**Teste de negação (sem role ADMIN):**
- Use token de usuário `user` (só tem role USER)
- Deve retornar: `403 Forbidden`

**✅ Validação:** 
- Resource Server extrai `roles` do claim JWT
- Valida se contém `ROLE_ADMIN`
- Permite ou nega acesso baseado na role

### 5.3. Endpoint Protegido por SCOPE - ✅ REQUISITO CP3

**Requisito:** Acesso restrito a tokens com SCOPE específico (ex: read:data)

```bash
curl http://localhost:8080/scopes \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_AQUI"
```

**Resposta esperada (com scope read:data):**
```json
{
  "message": "Acesso permitido! Seu token possui o scope 'read:data'.",
  "usuario": "admin",
  "scopes": ["read:data", "write:info", "openid", "profile"]
}
```

**Teste de negação (sem scope read:data):**
- Gere token sem o scope `read:data`
- Deve retornar: `403 Forbidden`

**✅ Validação:**
- Resource Server extrai `scopes` do claim JWT
- Valida se contém `SCOPE_read:data`
- Permite ou nega acesso baseado no scope

## Passo 6: Verificar Claims do Token JWT - ✅ REQUISITO CP3

**Requisito:** Tokens devem conter claims de ROLE e SCOPES.

### 6.1. Decodificar Token JWT

Acesse: https://jwt.io

1. Cole o `access_token` recebido
2. O site decodifica automaticamente o token

### 6.2. Verificar Claims Obrigatórios

**✅ O token deve conter:**

**Claims padrão JWT:**
- `sub`: Subject (ID do usuário)
- `iss`: Issuer (http://localhost:9000)
- `aud`: Audience
- `exp`: Expiration time
- `iat`: Issued at
- `jti`: JWT ID

**✅ Claims customizados (REQUISITO CP3):**
- `roles`: Array com as roles do usuário
  ```json
  "roles": ["ADMIN", "USER"]
  ```
- `scopes`: Array com os scopes autorizados
  ```json
  "scopes": ["read:data", "write:info", "openid", "profile"]
  ```
- `username`: Nome do usuário
  ```json
  "username": "admin"
  ```
- `authorities`: Authorities completas (roles + scopes)
  ```json
  "authorities": ["ROLE_ADMIN", "ROLE_USER", "SCOPE_read:data", ...]
  ```

### 6.3. Verificar ID Token

**✅ REQUISITO CP3:** Authorization Server deve emitir ID Tokens.

1. Cole o `id_token` recebido em https://jwt.io
2. Verifique claims OpenID Connect:
   - `sub`: Subject
   - `iss`: Issuer
   - `aud`: Audience
   - `exp`, `iat`: Timestamps
   - `nonce`: (se aplicável)

**Diferença:**
- **Access Token**: Usado para acessar recursos protegidos
- **ID Token**: Contém informações de identidade do usuário (OpenID Connect)

## Passo 7: Acessar Swagger UI - ✅ REQUISITO CP3

**Requisito:** Todos os endpoints devem estar documentados com Swagger/OpenAPI.

Acesse no navegador:
```
http://localhost:8080/swagger-ui.html
```

### 7.1. Verificar Documentação

No Swagger UI você deve ver:

1. **Endpoint `/public`:**
   - Descrição: "Endpoint público"
   - Método: GET
   - Sem autenticação necessária

2. **Endpoint `/roles`:**
   - Descrição: "Endpoint protegido por role ADMIN"
   - Método: GET
   - Requer autenticação: Bearer JWT
   - Respostas: 200 (sucesso), 401 (não autenticado), 403 (sem permissão)

3. **Endpoint `/scopes`:**
   - Descrição: "Endpoint protegido por scope read:data"
   - Método: GET
   - Requer autenticação: Bearer JWT
   - Respostas: 200 (sucesso), 401 (não autenticado), 403 (sem permissão)

### 7.2. Testar via Swagger UI

1. Clique em **"Authorize"** (ícone de cadeado)
2. Cole o `access_token` no campo (sem a palavra "Bearer")
3. Clique em **"Authorize"**
4. Teste os endpoints clicando em **"Try it out"** e depois **"Execute"**

**✅ Validação:** Todos os endpoints estão documentados e testáveis via Swagger.

## Passo 8: Checklist de Validação do CP3

Use este checklist para garantir que todos os requisitos foram atendidos:

### ✅ Authorization Server (4 pontos)
- [ ] Emite Access Tokens JWT
- [ ] Emite ID Tokens JWT
- [ ] Suporta Authorization Code Grant Flow
- [ ] Tokens contêm claim `roles` (array de roles do usuário)
- [ ] Tokens contêm claim `scopes` (array de scopes autorizados)
- [ ] Tokens contêm claim `username`
- [ ] Tokens são assinados com chave privada RSA

### ✅ Resource Server (4 pontos)
- [ ] Protege recursos da API
- [ ] Valida tokens JWT usando chaves assimétricas (RSA)
- [ ] Busca chaves públicas do Authorization Server em `/oauth2/jwks`
- [ ] Endpoint `/public` acessível sem autenticação
- [ ] Endpoint `/roles` protegido por ROLE (requer ROLE_ADMIN)
- [ ] Endpoint `/scopes` protegido por SCOPE (requer SCOPE_read:data)
- [ ] Extrai roles e scopes do token JWT
- [ ] Aplica regras de autorização baseadas em roles e scopes

### ✅ Documentação Swagger/OpenAPI (2 pontos)
- [ ] Endpoint `/public` documentado
- [ ] Endpoint `/roles` documentado
- [ ] Endpoint `/scopes` documentado
- [ ] Todos os endpoints têm descrição
- [ ] Todos os endpoints têm exemplos de resposta
- [ ] Swagger UI acessível em `/swagger-ui.html`
- [ ] Possibilidade de testar endpoints diretamente no Swagger

## Credenciais de Teste

### Usuários (Authorization Server)
- `admin` / `admin123` 
  - Roles: ADMIN, USER
  - Pode acessar `/roles` (tem ROLE_ADMIN)
  
- `manager` / `manager123` 
  - Roles: MANAGER, USER
  - NÃO pode acessar `/roles` (não tem ROLE_ADMIN)
  
- `user` / `user123` 
  - Roles: USER
  - NÃO pode acessar `/roles` (não tem ROLE_ADMIN)

### Cliente OAuth2
- **Client ID:** `client-id`
- **Client Secret:** `client-secret`
- **Redirect URI:** `http://localhost:8080/callback`
- **Scopes suportados:** `openid`, `profile`, `read:data`, `write:info`
- **Grant Types:** `authorization_code`

## URLs Importantes

### Authorization Server (porta 9000)
- **Base URL:** http://localhost:9000
- **Authorization Endpoint:** http://localhost:9000/oauth2/authorize
- **Token Endpoint:** http://localhost:9000/oauth2/token
- **JWK Set (chaves públicas):** http://localhost:9000/oauth2/jwks
- **H2 Console:** http://localhost:9000/h2-console

### Resource Server (porta 8080)
- **Base URL:** http://localhost:8080
- **Endpoint Público:** http://localhost:8080/public
- **Endpoint Roles:** http://localhost:8080/roles
- **Endpoint Scopes:** http://localhost:8080/scopes
- **Callback:** http://localhost:8080/callback
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

## Dicas de Teste

1. **Código de autorização expira rapidamente:**
   - Use o código imediatamente após gerar
   - Cada código só pode ser usado uma vez

2. **Token JWT expira em 5 minutos:**
   - Se receber `401 Unauthorized`, gere um novo token

3. **Para testar negação de acesso:**
   - Use usuário `user` para testar `/roles` (deve retornar 403)
   - Gere token sem scope `read:data` para testar `/scopes` (deve retornar 403)

4. **Verificar logs:**
   - Ambos servidores têm logging em DEBUG
   - Verifique os logs para entender o fluxo de autenticação/autorização

5. **Testar chaves assimétricas:**
   - Acesse `/oauth2/jwks` e verifique que retorna chaves públicas RSA
   - Resource Server deve validar tokens sem conhecer a chave privada

