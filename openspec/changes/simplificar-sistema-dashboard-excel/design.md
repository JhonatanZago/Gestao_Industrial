# Design

## Context

O backend já possui `ExcelImportReader`, `ImportacaoValidator`, `ImportacaoProducaoService`, `RegistroProducao`, `Setor`, endpoints de importação e endpoints de analytics. Ele também já calcula duração registrada, aplica escopo de empresa/setor e registra hash da importação. A análise encontrou três pontos que precisam ser alinhados ao contrato novo: o leitor ainda reconhece colunas opcionais (`Setor`, `Máquina`, `Turno`), a validação atual aceita quantidade decimal e trata intervalo noturno como possível caso válido, e os analytics/relatórios expõem indicadores de paradas, metas, desempenho e financeiro que não pertencem ao caminho mínimo.

## Goals / Non-Goals

**Goals:**

- Tornar o fluxo existente a implementação oficial do contrato Excel → produção → dashboard.
- Preservar APIs e entidades compatíveis sempre que já atenderem ao contrato, adicionando somente ajustes necessários.
- Garantir rastreabilidade, isolamento por empresa/setor, reimportação determinística e números reconciliáveis.
- Entregar uma fixture determinística e uma sequência de testes que prove o caminho completo.

**Non-Goals:**

- Reescrever o frontend ou criar uma nova aplicação web nesta mudança.
- Remover máquinas, turnos, metas, financeiro, qualidade, auditoria ou IA/RAG.
- Criar métricas de eficiência, OEE, paradas inferidas, receita ou lucro a partir de dados que a planilha não fornece.
- Alterar migrations históricas ou fazer limpeza massiva do banco.

## Decisions

### 1. Reusar os endpoints de importação e analytics

Manter o contexto atual `/api/v1/empresas/{empresaId}/setores/{setorId}/importacoes/producao` e os recursos de analytics existentes, ajustando contratos e respostas somente onde necessário. Isso evita duplicação e preserva integrações já documentadas. Criar uma API paralela seria mais simples no curto prazo, mas produziria duas regras de importação e permitiria divergência entre dashboard e relatório.

### 2. Setor como contexto obrigatório, não como coluna

O setor será validado no caminho da requisição e aplicado a todas as linhas. O leitor poderá tolerar apenas o formato oficial; qualquer compatibilidade temporária com colunas antigas deverá ser explicitamente neutra e não poderá sobrescrever o setor do contexto. Inferência por filename ou conteúdo será proibida porque é ambígua e quebra isolamento.

### 3. Normalização e validação em camadas

Separar falhas de arquivo/cabeçalho, falhas de parsing por linha e falhas de referência/domínio. O resultado de cada linha terá linha, campo, valor seguro e código de erro. A persistência ocorrerá apenas depois da validação completa; o caminho de rejeição manterá o histórico da importação, mas não registros de produção.

### 4. Duração é dado registrado, não eficiência

Persistir e agregar `fim - início` por registro. Gaps entre registros não serão convertidos em parada. Recursos de paradas existentes continuarão disponíveis para dados explicitamente registrados, mas não serão chamados para completar o fluxo standard.

### 5. Analytics mínimo como fonte única

Usar os registros de produção filtrados por empresa, setor e período como fonte para visão geral, setor, funcionário, peça e relatório. Métricas secundárias continuarão em módulos separados. Se um relatório executivo existente depender de componentes opcionais, será criado ou ajustado um resumo simplificado que não falhe quando esses componentes não tiverem dados.

### 6. Reimportação por hash mais contexto de negócio

Preservar o hash SHA-256 já existente, sempre combinado com empresa, setor e status da importação. Validar também duplicidade de linhas e sobreposição conforme as regras de domínio existentes. O nome do arquivo será somente informação de auditoria e nunca chave de idempotência.

### 7. Migração incremental e índices compatíveis

Antes de criar migration, verificar se colunas, constraints e índices existentes já suportam o contrato. Qualquer ajuste será aditivo e reversível. O padrão de consulta usará os índices existentes de empresa/início, setor/início, funcionário/período e peça/início, complementando-os apenas se testes de consulta demonstrarem necessidade.

### 8. Testes de aceitação com fixture fixa

Usar quatro linhas determinísticas de `Pintura a pó` da especificação: total 350, funcionário `00147` igual a 200, funcionário `00152` igual a 150, `PEC-001` igual a 270, `PEC-002` igual a 80 e duração total de 185 minutos. A cadeia de testes será leitor → validação → importação → endpoints de dashboard → drill-down → relatório, com casos negativos entre cada camada.

## Risks / Trade-offs

- [Risco] Arquivos e código existentes estão com aliases e mensagens legadas; restringir de imediato pode quebrar clientes antigos. → [Mitigação] mapear consumidores, manter compatibilidade somente quando não contradizer o formato oficial e registrar comportamento de transição nos testes.
- [Risco] Funcionários/peças podem ainda não existir com códigos preservando zeros. → [Mitigação] validar lookup textual, criar fixture de homologação e rejeitar referência inexistente sem converter o código para número.
- [Risco] O relatório executivo atual agrega metas, qualidade, paradas e financeiro. → [Mitigação] não removê-lo; introduzir/ajustar um resumo core independente e testar reconciliação com os dashboards.
- [Risco] Agregar em memória pode degradar períodos grandes. → [Mitigação] medir com dataset de limite, revisar Specifications/repositórios e manter agregações no backend/banco conforme o padrão atual.
- [Risco] Dados históricos podem ter semântica diferente para intervalo ou quantidade. → [Mitigação] aplicar as regras novas à entrada nova, preservar histórico e documentar diferenças observáveis sem migration destrutiva.

## Migration Plan

1. Executar baseline de testes/build e registrar endpoints, entidades, índices, migrations e consumidores atuais.
2. Implementar primeiro o contrato do leitor/validador e a fixture, mantendo o histórico de importação.
3. Ajustar persistência, idempotência e agregações; adicionar migrations apenas se a análise comprovar necessidade.
4. Validar a sequência completa com autenticação e autorização reais, incluindo rejeições.
5. Publicar documentação/template e executar build final.
6. Rollback: desativar o novo fluxo/feature flag se houver, preservar dados e migrations aditivas; não apagar registros nem reverter migrations históricas.

## Open Questions

Nenhuma questão bloqueadora. A seleção das rotas existentes versus pequenos ajustes de resposta deve ser decidida pela inspeção dos testes e consumidores durante a implementação, sem alterar o contrato funcional desta spec.
