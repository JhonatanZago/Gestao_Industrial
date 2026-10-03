# Banco de dados

O runtime usa PostgreSQL e o perfil de testes usa H2 com Flyway. O Hibernate valida o schema (ddl-auto=validate); migrations históricas não devem ser editadas.

## Migrations

As migrations V1–V22 criam empresas, setores, funcionários, peças, produção, paradas, máquinas, turnos, importação, tempos padrão, metas, valores industriais, autorização, auditoria, qualidade, RAG e operações.

~~~powershell
docker compose up -d postgres
docker compose exec -T postgres psql -U postgres -d sistema_gestao_industrial -c "select version();"
~~~

Para validar do zero, use um banco temporário e um usuário temporário, aplique a imagem atual da aplicação e remova ambos após o teste. Nunca use down -v em ambiente com dados que devam ser preservados.

Constraints e índices devem ser validados com os testes e planos PostgreSQL descritos em docs/PERFORMANCE_BACKEND.md.
