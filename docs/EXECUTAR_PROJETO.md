# Executar o projeto

Pré-requisitos: Windows, JDK compatível com o toolchain Java 17, Docker Desktop para PostgreSQL e PowerShell.

~~~powershell
git clone <repositorio>
cd SistemaGestaoIndustrial
.\gradlew.bat clean test
.\gradlew.bat build
docker compose up --build
~~~

A API fica em http://localhost:8080 por padrão. Configure DB_URL, DB_USERNAME, DB_PASSWORD e JWT_SECRET no ambiente; em produção, JWT_SECRET deve ter pelo menos 32 bytes e não deve ser versionado.

Verificações rápidas:

~~~powershell
Invoke-WebRequest http://localhost:8080/actuator/health/readiness
.\scripts\homologacao\smoke-test.ps1 -BaseUrl http://localhost:8080 -Email <email> -Senha <senha> -EmpresaId <id>
~~~

Use docker compose down para parar os serviços. Preserve o volume PostgreSQL salvo quando a remoção dos dados for deliberada.
