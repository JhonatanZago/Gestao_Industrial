# Arquitetura do Backend

O sistema é uma aplicação Spring Boot MVC com camadas de resources HTTP, DTOs/mappers, serviços de domínio, repositories JPA, PostgreSQL/Flyway e módulos transversais de segurança, auditoria, qualidade, observabilidade e IA/RAG.

~~~mermaid
flowchart TD
    Client[Cliente HTTP] --> Security[JWT / autorização / escopo]
    Security --> Resources[Resources /api/v1]
    Resources --> Services[Services e regras de domínio]
    Services --> Repositories[Repositories JPA]
    Repositories --> Database[(PostgreSQL + Flyway)]
    Import[Excel XLSX] --> ImportService[Importação all-or-nothing]
    ImportService --> Services
    Services --> Analytics[Analytics / desempenho / financeiro]
    Analytics --> Reports[Relatórios e exportações]
    Services --> Audit[Auditoria append-only]
    Documents[Documentos] --> Rag[RAG com escopo]
    Rag --> Assistant[Assistente fake/disabled]
    Services --> Assistant
    Observability[Health / metrics / logs / correlation] -.-> Resources
~~~

Não há frontend neste repositório. Não há provedor remoto de IA obrigatório; os gateways fake/disabled permitem testes sem internet.
