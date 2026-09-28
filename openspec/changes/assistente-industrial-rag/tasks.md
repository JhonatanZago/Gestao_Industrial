# Tasks

## 1. Fundamentos e contratos

- [x] 1.1 Criar configurações `AI_ENABLED` e limites operacionais com padrão seguro desabilitado, códigos de erro/status e validação de configuração; verificar que a aplicação inicia sem credenciais e que o contexto de teste expõe os defaults esperados.
- [x] 1.2 Criar os DTOs imutáveis de pergunta, resposta, período, aviso, fonte, qualidade e erro; verificar por testes de validação o contrato de campos obrigatórios, limites e ausência de campos de autorização no request.
- [x] 1.3 Criar o contexto autenticado imutável com usuário, empresa, perfil, permissões e setores; verificar que valores enviados pelo corpo não substituem o contexto de segurança.
- [x] 1.4 Criar as portas `ChatModelGateway`, `EmbeddingGateway` e `VectorStorePort`, erros classificados e fakes determinísticos; verificar que testes unitários executam sem dependência de rede, segredo ou provedor pago.

## 2. Catálogo de tools e integração com o ERP

- [ ] 2.1 Implementar o contrato versionado de tool com schema de entrada/saída, permissões, escopo, timeout e identificador de fonte; verificar rejeição de tool desconhecida e argumentos fora do schema.
- [ ] 2.2 Implementar o catálogo inicial de consultas read-only para produção, comparação, série temporal, setor, funcionário, peça, máquina, paradas, metas e desempenho; verificar cada tool com fake das Services existentes e resultado rastreável.
- [ ] 2.3 Integrar tools de valor industrial, qualidade e auditoria às respectivas Services/facades, mantendo as regras de cálculo existentes; verificar permissão financeira/auditoria e propagação de `qualityData`/avisos.
- [ ] 2.4 Aplicar validação de empresa, setor, período, filtros, quantidade de resultados e número de chamadas antes de cada tool; verificar por testes que escopos inválidos não alcançam Services ou repositórios.
- [ ] 2.5 Documentar o catálogo interno e seus schemas junto aos testes de contrato; verificar que cada tool registrada possui descrição, permissão, fonte e cenário read-only correspondente.

## 3. Endpoint e orquestração do assistente

- [ ] 3.1 Implementar `POST /api/v1/empresas/{empresaId}/assistente/perguntar` com autenticação, validação, mapeamento de erros e resposta estruturada; verificar cenários de pergunta válida, inválida, empresa fora do escopo e ausência de autorização.
- [ ] 3.2 Implementar resolução backend de datas relativas, filtros e clarificação de intenção; verificar datas de ontem/semana e perguntas ambíguas com relógio injetável e timezone configurado.
- [ ] 3.3 Implementar o orquestrador que seleciona tools/RAG, aplica limites e monta contexto separado para o provider; verificar limite de chamadas, timeout e ausência de SQL/acesso direto ao banco.
- [ ] 3.4 Implementar chamada estruturada ao `ChatModelGateway`, retry somente transitório e mapeamento de indisponibilidade, timeout e rate limit; verificar comportamento determinístico com fake de provider.
- [ ] 3.5 Implementar os estados `RESPONDIDO`, `SEM_DADOS`, `SEM_PERMISSAO`, `PRECISA_CLARIFICACAO` e `INDISPONIVEL`, incluindo códigos `AI_*`; verificar que ausência de dados não gera estimativas e provider ausente não impede o startup.
- [ ] 3.6 Implementar suporte opcional a `conversationId` sem ampliar empresa, usuário ou escopo; verificar rejeição segura de conversa inexistente, expirada ou pertencente a outro escopo.

## 4. Grounding e segurança da resposta

- [ ] 4.1 Implementar modelo de evidência que vincula claims a resultados de tools ou trechos RAG, com período, fonte e avisos; verificar que toda resposta factual retorna `dataUsed` e referências internas seguras.
- [ ] 4.2 Implementar validação de números, percentuais, datas, entidades e comparações contra as fontes retornadas; verificar remoção/rejeição de total inventado e cálculo somente sobre bases autorizadas.
- [ ] 4.3 Implementar tratamento de qualidade parcial e ausência de dados no prompt e na resposta; verificar que dados suspeitos carregam avisos e que documento/manual não substitui medição do ERP.
- [ ] 4.4 Isolar instruções de sistema, pergunta, resultado de tool e trecho documental no contexto do modelo; verificar resistência a prompt injection e tool injection sem alteração de permissões ou catálogo.
- [ ] 4.5 Sanitizar chain-of-thought, prompts privados, tokens internos e segredos da resposta; verificar que o contrato preserva resposta/avisos/fontes permitidos e remove conteúdo interno.

## 5. RAG industrial e ciclo de vida documental

- [ ] 5.1 Criar modelo/persistência versionada de documento e chunk com `empresaId`, `documentId`, tipo, título, versão/hash, setor e permissões; verificar migration nova sem alteração das migrations existentes.
- [ ] 5.2 Implementar ingestão administrativa de TXT/MD com validação de tamanho, nome, MIME, conteúdo extraível, macros e scripts; verificar rejeição segura e autorização antes da persistência.
- [ ] 5.3 Implementar normalização e chunking determinístico com hash de conteúdo e idempotência; verificar que o mesmo documento não cria chunks ativos duplicados.
- [ ] 5.4 Implementar indexação via `EmbeddingGateway`/`VectorStorePort`, filtros pré-retrieval por empresa/setor/permissão/status e limite de contexto; verificar isolamento com documentos semanticamente similares de outros escopos.
- [ ] 5.5 Implementar atualização, reindexação, invalidação e remoção atômica/idempotente, preservando versão anterior em falha de embedding; verificar que índice parcial não fica ativo e que a operação é auditável.
- [ ] 5.6 Implementar endpoint administrativo de documento quando a ingestão HTTP for habilitada e health/status sem segredo; verificar permissão, escopo, erro de PDF sem extractor seguro e não exposição de conteúdo entre empresas.

## 6. Limites, privacidade e operação

- [ ] 6.1 Implementar rate limit e limites por usuário/empresa para pergunta, tools, tokens, documentos, upload e tempo total; verificar `AI_RATE_LIMIT` e encerramento seguro sem cache cruzado.
- [ ] 6.2 Implementar telemetria mínima de provider/modelo, latência, tokens, contagem/sucesso de tools e eventos administrativos sem prompt/resposta completa por padrão; verificar ausência de secrets nos logs.
- [ ] 6.3 Implementar health/status administrativo de IA e auditoria de ingestão/reindexação/remoção; verificar que o status informa disponibilidade classificada sem URL secreta, token ou conteúdo de usuário.
- [ ] 6.4 Cobrir permissões financeiras, auditoria, setor, empresa e operações mutáveis com testes de segurança; verificar que nenhuma reformulação, conversa, RAG ou chamada do provider contorna o backend.

## 7. Integração e regressão

- [ ] 7.1 Executar a matriz determinística completa de produção, comparação, períodos relativos, setor, funcionário, peça, máquina, paradas, metas, desempenho, financeiro permitido/negado, qualidade, auditoria, ausência e ambiguidade; verificar todos os estados e fontes esperados.
- [ ] 7.2 Executar a matriz de prompt/tool injection, limites, timeout, provider indisponível, IA desabilitada, segredo ausente, RAG por empresa/setor e reindexação; verificar que não há vazamento nem escrita no ERP.
- [ ] 7.3 Executar `clean test`, `build` e validação OpenSpec em ambiente sem credenciais/serviços de IA; verificar que as APIs existentes de analytics, relatórios, qualidade e auditoria permanecem em regressão verde.
- [ ] 7.4 Registrar no change os contratos operacionais finais, configurações necessárias e procedimento de rollback/desligamento; verificar que a documentação permite habilitar/desabilitar a feature sem editar código ou versionar segredo.
