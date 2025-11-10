# README - CP3 OAuth 2.0 Implementation

## Estrutura do Projeto

```
cp3/
├── authorization-server/    # Servidor de Autorização (porta 9000)
├── resource-server/         # Servidor de Recursos (porta 8080)
├── cp3.md                  # Instruções do checkpoint
```

## Como Executar

### 1. Authorization Server

```bash
cd authorization-server
mvn spring-boot:run
```

Ou usando Maven Wrapper (se disponível):
```bash
./mvnw spring-boot:run
```

### 2. Resource Server

Em outro terminal:

```bash
cd resource-server
mvn spring-boot:run
```

Ou usando Maven Wrapper:
```bash
./mvnw spring-boot:run
```

## Funcionalidades Implementadas

### Authorization Server
- ✅ Emissão de tokens JWT (Access Token e ID Token)
- ✅ Fluxo Authorization Code Grant
- ✅ Claims customizados (roles, scopes)
- ✅ Chaves assimétricas RSA
- ✅ Usuários com diferentes roles (ADMIN, MANAGER, USER)
- ✅ Endpoint JWK Set para validação de tokens

### Resource Server
- ✅ Validação de tokens JWT
- ✅ Endpoint público (/public)
- ✅ Endpoint protegido por role (/roles)
- ✅ Endpoint protegido por scope (/scopes)
- ✅ Documentação Swagger/OpenAPI

## Teste Rápido

Consulte o arquivo `GUIA_TESTE.md` para instruções detalhadas de teste.

## Credenciais

**Usuários:**
- admin / admin123 (Role: ADMIN)
- manager / manager123 (Role: MANAGER)
- user / user123 (Role: USER)

**Cliente OAuth2:**
- Client ID: client-id
- Client Secret: client-secret

## Endpoints

### Authorization Server (porta 9000)
- `/oauth2/authorize` - Endpoint de autorização
- `/oauth2/token` - Endpoint de token
- `/login` - Página de login
- `/.well-known/jwks.json` - JWK Set

### Resource Server (porta 8080)
- `/public` - Endpoint público
- `/roles` - Endpoint protegido por role ADMIN
- `/scopes` - Endpoint protegido por scope read:data
- `/swagger-ui.html` - Interface Swagger

