# Design

## Context

O projeto atual é um esqueleto Spring Boot com Java 17, Gradle Groovy e package-base `com.metalurgica.sistemagestaoindustrial`. A implementação deverá criar a primeira fatia funcional sem assumir módulos anteriores. Os contratos comportamentais estão em `specs/empresa-setores/spec.md`; a motivação está em `proposal.md`.

## Goals / Non-Goals

**Goals:**

- Criar uma separação clara entre domínio, persistência, serviço, mapeamento, API e tratamento de erros.
- Garantir que Empresa seja a raiz do agregado e que toda operação de Setor respeite seu escopo.
- Manter setores como dados persistidos, permitindo expansão sem recompilar regras específicas.
- Entregar uma base testável com migrations e configurações distintas para runtime e testes.

**Non-Goals:**

- Não criar autenticação/autorização, frontend, importação Excel, funcionários, peças, produção, indicadores ou IA.
- Não introduzir MapStruct ou outra dependência adicional apenas para reduzir código de mapeamento.
- Não implementar exclusão física ou filtros avançados.

## Decisions

### Modelo e ciclo de vida

Usar entidades JPA `Empresa` e `Setor`, com `Setor` contendo uma referência obrigatória à Empresa. O status ativo/inativo será persistido e controlado por serviços; as operações normais não terão DELETE físico. Timestamps serão preenchidos automaticamente por callbacks ou auditoria simples, mantendo a solução pequena para a primeira change.

Alternativa rejeitada: enums ou classes por setor, pois impediriam cadastrar novos setores sem alteração de código. Exclusão física também foi rejeitada por quebrar referências históricas futuras.

### Normalização e unicidade

Normalizar CNPJ removendo caracteres não numéricos, e código/nome removendo espaços externos; código será convertido para uma forma canônica em maiúsculas. A regra será aplicada no service antes da consulta e persistência, com índice/constraint no banco para CNPJ e para `(empresa_id, codigo)`. A unicidade lógica do nome será verificada pelo service na mesma Empresa, com uma estratégia consistente de comparação normalizada.

Alternativa rejeitada: confiar somente em validação de controller ou somente em constraint. A validação antecipada melhora a mensagem; a constraint evita corrida entre requisições.

### API e camadas

Resources/Controllers receberão requests validados e delegarão ao service. Services resolverão existência, pertencimento, normalização, duplicidade e status. Repositories fornecerão consultas por CNPJ, empresa/código, empresa/nome e listagens filtradas. Responses serão DTOs manuais para evitar serialização circular e acoplamento da API ao JPA.

### Erros e paginação

Um `@RestControllerAdvice` mapeará exceções específicas de recurso ausente, conflito e regra de negócio, além de erros de validação, para um contrato JSON estável sem stack trace. Listagens usarão `Pageable` e respostas paginadas; os filtros ficarão restritos a ativo, nome e código.

### Banco e testes

Flyway será a fonte versionada do schema, com migrations para Empresa e Setor e constraints explícitas. PostgreSQL será a configuração principal; H2 ficará isolado no perfil de teste. Testes unitários cobrirão regras de service com Mockito e testes de integração/API validarão migrations, isolamento e o cenário de códigos iguais em empresas diferentes.

Alternativa rejeitada: `spring.jpa.hibernate.ddl-auto=create` como estratégia definitiva, pois não oferece histórico controlado de schema.

## Risks / Trade-offs

- [Concorrência em nomes de setor] → manter a validação no service e, se suportado pelo schema escolhido, adicionar constraint/índice para a mesma forma normalizada; cobrir conflito no teste de integração.
- [Diferenças de dialeto entre PostgreSQL e H2] → manter SQL de migration compatível com o escopo usado nos testes e executar a suíte contra H2, deixando validação de PostgreSQL documentada para o ambiente runtime.
- [Paginação e filtros crescerem além do escopo] → limitar consultas iniciais aos filtros definidos e deixar extensões para uma change futura.
- [Evolução do agregado Empresa] → evitar cascatas destrutivas e manter o vínculo Setor obrigatório, preservando registros inativos para futuras referências históricas.

## Migration Plan

1. Adicionar dependências/configuração e migrations, executar a suíte em banco H2.
2. Publicar entidades, repositories, services, DTOs, mappers, resources e advice.
3. Validar `clean test` e `build`, depois aplicar migrations em um banco PostgreSQL vazio ou controlado.
4. Em rollback, interromper a release e reverter a versão da aplicação; não apagar dados de Empresa/Setor automaticamente. Migrations destrutivas não fazem parte desta change.
