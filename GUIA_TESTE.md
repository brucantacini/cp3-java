# Guia de Teste - CP3 OAuth 2.0

## Pré-requisitos
- Java 21 instalado
- Maven instalado (ou usar Maven Wrapper)

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

## Passo 3: Obter Token JWT

### Opção 1: Via Browser (Fluxo Authorization Code)

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

### Opção 2: Via Postman/Insomnia

**POST** `http://localhost:9000/oauth2/token`
- **Headers:**
  - Content-Type: `application/x-www-form-urlencoded`
  - Authorization: `Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=` (Base64 de client-id:client-secret)

- **Body (form-urlencoded):**
  - grant_type: `authorization_code`
  - code: `[code obtido do passo anterior]`
  - redirect_uri: `http://localhost:8080/callback`

## Passo 4: Testar Endpoints do Resource Server

### 1. Endpoint Público (sem token)
```bash
curl http://localhost:8080/public
```

**Resposta esperada:** `Este é um endpoint público. Não requer autenticação!`

### 2. Endpoint Protegido por Role (com token)

```bash
curl http://localhost:8080/roles \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_AQUI"
```

**Resposta esperada:** `Acesso permitido! Você possui a role ADMIN. Usuário: admin`

**Nota:** Apenas usuários com role ADMIN podem acessar.

### 3. Endpoint Protegido por Scope (com token)

```bash
curl http://localhost:8080/scopes \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN_AQUI"
```

**Resposta esperada:** `Acesso permitido! Seu token possui o scope 'read:data'. Usuário: admin`

**Nota:** O token deve conter o scope `read:data`.

## Passo 5: Acessar Swagger UI

Acesse no navegador:
```
http://localhost:8080/swagger-ui.html
```

No Swagger UI você pode:
1. Clicar em "Authorize"
2. Colar o token JWT no campo
3. Testar os endpoints diretamente pela interface

## Verificar Claims do Token JWT

Para ver os claims do token, você pode usar:
- https://jwt.io (cole o token para decodificar)
- Ou decodificar via código

O token deve conter:
- `roles`: Array com as roles do usuário (ex: ["ADMIN", "USER"])
- `scopes`: Array com os scopes (ex: ["read:data", "write:info"])
- `username`: Nome do usuário

## Credenciais de Teste

**Usuários:**
- `admin` / `admin123` - Roles: ADMIN, USER
- `manager` / `manager123` - Roles: MANAGER, USER  
- `user` / `user123` - Roles: USER

**Cliente OAuth2:**
- Client ID: `client-id`
- Client Secret: `client-secret`

## URLs Importantes

- Authorization Server: http://localhost:9000
- Resource Server: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:9000/h2-console
- JWK Set: http://localhost:9000/.well-known/jwks.json

