# Deploy do backend

Este guia é agnóstico de provedor. A publicação remota não é executada automaticamente.

1. Gerar e revisar o artefato com .\gradlew.bat clean build.
2. Provisionar PostgreSQL e configurar DB_URL, DB_USERNAME, DB_PASSWORD.
3. Configurar JWT_SECRET, CORS, limites de upload, pool, observabilidade e IA conforme o ambiente.
4. Iniciar a aplicação e aguardar /actuator/health/readiness.
5. Confirmar métricas, logs JSON, correlation ID e smoke test.
6. Fazer backup conforme a política do ambiente antes de qualquer migration.
7. Para rollback, retirar a instância nova do tráfego, preservar logs/backup e retornar ao artefato anterior conforme a política de migrations; não apagar dados automaticamente.

O tag sugerido após uma decisão GO é backend-v1.0.0. A criação/publicação do tag exige revisão humana.
