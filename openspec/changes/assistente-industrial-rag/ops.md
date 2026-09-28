# Operação do assistente industrial

## Configuração mínima

A feature permanece desligada por padrão:

```properties
AI_ENABLED=false
AI_MAX_QUESTION_LENGTH=2000
AI_MAX_TOOL_CALLS=8
AI_MAX_TOOL_RESULTS=1000
AI_TIMEOUT=PT30S
AI_MAX_OUTPUT_TOKENS=1000
AI_MAX_RETRIEVED_DOCUMENTS=8
AI_MAX_DOCUMENT_BYTES=5242880
AI_MAX_REQUESTS_PER_MINUTE=30
AI_RETRY_ATTEMPTS=1
AI_TIMEZONE=America/Sao_Paulo
AI_PDF_ENABLED=false
AI_CHUNK_SIZE=1200
AI_CHUNK_OVERLAP=100
```

Credenciais de provider, embedding e vector store devem vir de ambiente,
secret manager ou configuração externa. Nenhum segredo deve ser versionado.

## Habilitação

1. Configurar adapters de chat/embedding/vector store compatíveis com as portas
   do domínio.
2. Configurar credenciais fora do repositório.
3. Definir `AI_ENABLED=true` e manter os limites explícitos.
4. Consultar o status administrativo e executar a matriz de autorização antes
   de liberar o endpoint para usuários.

Sem provider, a aplicação ainda inicia e retorna `INDISPONIVEL` com código
classificado. Sem `AI_ENABLED=true`, retorna `AI_DISABLED` sem chamar serviços
externos.

## Desligamento e rollback

Definir `AI_ENABLED=false` interrompe novas consultas do assistente sem alterar
as APIs de produção, analytics, relatórios, qualidade ou auditoria. A ingestão
de documentos também deve ser retirada da exposição de rota quando o adapter
vetorial estiver indisponível. Dados documentais podem ser preservados para
investigação ou removidos pela operação administrativa autorizada; nenhuma
migration existente deve ser editada.
