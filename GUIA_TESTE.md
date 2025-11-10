# Guia de Teste - CP3 OAuth 2.0

## 📋 Sobre este Guia

Este guia demonstra a implementação completa do CP3, que consiste em:
- **Authorization Server**: Emite Access Tokens e ID Tokens em formato JWT
- **Resource Server**: Valida tokens JWT usando chaves assimétricas (RSA)
- **Fluxo OAuth 2.0**: Authorization Code Grant Flow para clientes confidenciais
- **Documentação**: Swagger/OpenAPI para todos os endpoints

## Pré-requisitos
- Java 21 instalado
- Maven instalado (ou usar Maven Wrapper)
- Postman ou similar (opcional, mas recomendado)

## Passo 1: Iniciar Authorization Server

```bash
cd authorization-server
mvn spring-boot:run
```

O servidor iniciará na porta **9000**

## Passo 2: Iniciar Resource Server (em outro terminal)

```bash
cd resource-server
mvn spring-boot:run
```

O servidor iniciará na porta **8080**

## Passo 3: Obter Tokens JWT (Access Token e ID Token)

**⚠️ IMPORTANTE:** O CP3 exige que o Authorization Server emita tanto **Access Tokens** quanto **ID Tokens** em formato JWT. Ambos serão retornados na resposta do token exchange.

### Opção 1: Via Browser (Fluxo Authorization Code Grant)

1. Acesse no navegador:
```
http://localhost:9000/oauth2/authorize?response_type=code&client_id=client-id&redirect_uri=http://localhost:8080/callback&scope=read:data write:info openid profile
```

2. Faça login com:
   - Usuário: `admin` / Senha: `admin123` (tem role ADMIN)
   - OU Usuário: `manager` / Senha: `manager123` (tem role MANAGER)
   - OU Usuário: `user` / Senha: `user123` (tem role USER)

3. Autorize o acesso

4. Você será redirecionado para `http://localhost:8080/callback?code=...`

5. Copie o código de autorização da URL

6. Troque o código por token:
```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "client-id:client-secret" \
  -d "grant_type=authorization_code&code=SEU_CODIGO_AQUI&redirect_uri=http://localhost:8080/callback"
```

**Resposta esperada:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_token": "...",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "scope": "read:data write:info openid profile"
}
```

**✅ Validação CP3:** A resposta contém tanto `access_token` quanto `id_token`, ambos em formato JWT.

### Opção 2: Via Postman/Insomnia

**POST** `http://localhost:9000/oauth2/token`
- **Headers:**
  - Content-Type: `application/x-www-form-urlencoded`
  - Authorization: `Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=` (Base64 de client-id:client-secret)

- **Body (form-urlencoded):**
  - grant_type: `authorization_code`
  - code: `[code obtido do passo anterior]`
  - redirect_uri: `http://localhost:8080/callback`

**✅ Validação CP3:** A resposta deve conter `access_token` e `id_token` (ambos JWT).

## Passo 4: Validar Chaves Assimétricas (RSA)

**⚠️ IMPORTANTE:** O CP3 exige que a validação do token seja feita usando **chaves assimétricas (RSA)**.

### Verificar JWK Set (Chaves Públicas)

O Authorization Server expõe as chaves públicas em:
```
http://localhost:9000/oauth2/jwks
```

Teste no navegador ou via curl:
```bash
curl http://localhost:9000/oauth2/jwks
```

**Resposta esperada:** JSON com chaves públicas RSA (JWK Set)

**✅ Validação CP3:** 
- Authorization Server possui chave **privada** (assina tokens)
- Authorization Server expõe chave **pública** em `/oauth2/jwks`
- Resource Server valida tokens usando apenas a chave **pública** (não precisa da privada)

## Passo 5: Testar Endpoints do Resource Server

### 1. Endpoint Público (sem token) - ✅ Requisito CP3
```bash
curl http://localhost:8080/public
```

**Resposta esperada:** `Este é um endpoint público. Não requer autenticação!`

**✅ Validação CP3:** Endpoint `/public` acessível sem autenticação.

### 2. Endpoint Protegido por Role (com token) - ✅ Requisito CP3

```bash
curl http://localhost:8080/roles \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_AQUI"
```

**Resposta esperada:** `Acesso permitido! Você possui a role ADMIN. Usuário: admin`

**Nota:** Apenas usuários com role ADMIN podem acessar.

**✅ Validação CP3:** 
- Endpoint `/roles` requer role específica (ADMIN)
- Resource Server extrai role do claim `roles` do JWT
- Validação baseada em ROLE funcionando

**Teste de Negação (403 Forbidden):**
Tente acessar com usuário que não tem role ADMIN:
1. Faça login com `user` / `user123` (só tem role USER)
2. Obtenha novo token
3. Tente acessar `/roles` → Deve retornar **403 Forbidden**

### 3. Endpoint Protegido por Scope (com token) - ✅ Requisito CP3

```bash
curl http://localhost:8080/scopes \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_AQUI"
```

**Resposta esperada:** `Acesso permitido! Seu token possui o scope 'read:data'. Usuário: admin`

**Nota:** O token deve conter o scope `read:data`.

**✅ Validação CP3:** 
- Endpoint `/scopes` requer scope específico (`read:data`)
- Resource Server extrai scope do claim `scopes` do JWT
- Validação baseada em SCOPE funcionando

**Teste de Negação (403 Forbidden):**
1. Obtenha token sem o scope `read:data` (não inclua no parâmetro `scope` da URL de autorização)
2. Tente acessar `/scopes` → Deve retornar **403 Forbidden**

## Passo 6: Acessar Swagger UI - ✅ Requisito CP3

Acesse no navegador:
```
http://localhost:8080/swagger-ui.html
```

No Swagger UI você pode:
1. Clicar em "Authorize"
2. Colar o token JWT no campo
3. Testar os endpoints diretamente pela interface

**✅ Validação CP3:** 
- Todos os endpoints do Resource Server estão documentados no Swagger
- Endpoints públicos e protegidos estão documentados
- É possível testar autenticação diretamente pela interface Swagger

## Passo 7: Verificar Claims do Token JWT

Para ver os claims do token, você pode usar:
- https://jwt.io (cole o token para decodificar)
- Ou decodificar via código

O token deve conter os seguintes claims customizados (✅ Requisito CP3):
- `roles`: Array com as roles do usuário (ex: `["ADMIN", "USER"]`)
- `scopes`: Array com os scopes (ex: `["read:data", "write:info", "openid", "profile"]`)
- `username`: Nome do usuário (ex: `"admin"`)
- `authorities`: Authorities completas do Spring Security

**Claims padrão JWT também presentes:**
- `sub`: Subject (ID do usuário)
- `iss`: Issuer (http://localhost:9000)
- `exp`: Expiration time
- `iat`: Issued at
- `aud`: Audience

**✅ Validação CP3:** 
- Access Token contém claims de ROLE e SCOPE
- Resource Server consegue validar roles e scopes a partir dos claims
- Claims permitem validação de função (ROLE) e escopos (SCOPES)

## Credenciais de Teste

**Usuários:**
- `admin` / `admin123` - Roles: ADMIN, USER
- `manager` / `manager123` - Roles: MANAGER, USER  
- `user` / `user123` - Roles: USER

**Cliente OAuth2:**
- Client ID: `client-id`
- Client Secret: `client-secret`

## Passo 8: Validar Fluxo Completo OAuth 2.0

### Checklist de Validação CP3

**Authorization Server:**
- [x] Emite Access Tokens em formato JWT
- [x] Emite ID Tokens em formato JWT
- [x] Suporta Authorization Code Grant Flow
- [x] Tokens contêm claims de ROLE e SCOPE
- [x] Expõe chaves públicas em `/oauth2/jwks`

**Resource Server:**
- [x] Valida tokens JWT usando chaves assimétricas (RSA)
- [x] Endpoint `/public` acessível sem autenticação
- [x] Endpoint `/roles` protegido por ROLE (ADMIN)
- [x] Endpoint `/scopes` protegido por SCOPE (read:data)
- [x] Extrai roles e scopes dos claims JWT

**Documentação:**
- [x] Todos os endpoints documentados no Swagger/OpenAPI
- [x] Endpoints públicos e protegidos documentados
- [x] Possibilidade de testar autenticação no Swagger

**Fluxo de Segurança:**
- [x] Resource Server valida assinatura do token (chave pública)
- [x] Resource Server verifica expiração do token
- [x] Resource Server extrai authorities (roles e scopes)
- [x] Regras de autorização baseadas em ROLE funcionando
- [x] Regras de autorização baseadas em SCOPE funcionando

## URLs Importantes

- **Authorization Server:** http://localhost:9000
- **Resource Server:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **H2 Console:** http://localhost:9000/h2-console
- **JWK Set (Chaves Públicas):** http://localhost:9000/oauth2/jwks ⚠️ **CORRIGIDO**

## 🔒 Explicação do Fluxo de Segurança (Para o Vídeo)

### Como o Resource Server Valida o Token:

1. **Cliente envia requisição** com `Authorization: Bearer <token>`
2. **Resource Server recebe o token JWT**
3. **Resource Server busca chave pública** do Authorization Server em `/oauth2/jwks`
4. **Resource Server valida assinatura** usando a chave pública (RSA)
   - Se a assinatura for válida → token foi emitido pelo Authorization Server
   - Se a assinatura for inválida → token foi alterado ou não é do Authorization Server
5. **Resource Server verifica expiração** (`exp` claim)
6. **Resource Server extrai claims** (`roles`, `scopes`, `username`)
7. **Resource Server converte em authorities** Spring Security:
   - `roles: ["ADMIN"]` → `ROLE_ADMIN`
   - `scopes: ["read:data"]` → `SCOPE_read:data`
8. **Resource Server aplica regras de autorização:**
   - `/roles` → verifica se tem `ROLE_ADMIN`
   - `/scopes` → verifica se tem `SCOPE_read:data`
9. **Permite ou nega acesso** baseado nas authorities

### Por que Chaves Assimétricas?

- **Authorization Server:** Tem chave **privada** (segredo) → assina tokens
- **Resource Server:** Tem apenas chave **pública** → valida tokens
- **Vantagem:** Se o Resource Server for comprometido, a chave privada não é exposta
- **Escalabilidade:** Múltiplos Resource Servers podem validar tokens do mesmo Authorization Server

