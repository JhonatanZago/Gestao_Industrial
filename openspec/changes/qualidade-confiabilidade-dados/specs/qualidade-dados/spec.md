# Spec Delta

## Purpose

Definir uma camada determinística para medir a completude, consistência e confiabilidade dos dados industriais sem transformar alertas em correções automáticas ou em erros de domínio.

## ADDED Requirements

### Requirement: Classificar problemas de qualidade com códigos estáveis

Cada diagnóstico MUST possuir código estável, categoria, severidade (`INFO`, `ALERTA` ou `CRITICO`), mensagem objetiva, empresa, recurso opcional, setor opcional, data de referência opcional e detalhes estruturados. As categorias MUST incluir, quando aplicável, `CADASTRO`, `PRODUCAO`, `PARADA`, `TEMPO_PADRAO`, `VALOR_PRODUCAO`, `IMPORTACAO`, `COBERTURA`, `DUPLICIDADE` e `CONSISTENCIA_TEMPORAL`.

#### Scenario: Diagnóstico objetivo de baixa cobertura

- **WHEN** somente 80 de 100 produções do período possuem tempo padrão aplicável
- **THEN** o sistema MUST calcular cobertura de 80%, emitir uma mensagem objetiva e classificar o diagnóstico com código estável e severidade configurada, sem afirmar a causa da ausência

#### Scenario: Inconsistência crítica

- **WHEN** um dado legado apresenta tempo parado maior que a duração bruta ou tempo produtivo negativo
- **THEN** o sistema MUST produzir um diagnóstico `CRITICO` vinculado ao recurso, sem alterar o registro original

### Requirement: Calcular cobertura temporal e financeira sem confundir ausência com zero

O sistema MUST identificar, por empresa, período e setor autorizado, produções com e sem tempo padrão aplicável e com e sem valor industrial aplicável. O resumo MUST retornar registros totais, registros cobertos, registros descobertos, percentual de cobertura e, quando aplicável, quantidade produzida coberta e descoberta. Ausência de valor MUST permanecer distinta de valor monetário zero.

#### Scenario: Cobertura parcial de padrão e valor

- **WHEN** um período contém 100 registros, 80 padrões aplicáveis e 90 valores industriais aplicáveis
- **THEN** o resumo MUST retornar `coberturaPadrao` de 80% e `coberturaValor` de 90%, preservando os totais e sem transformar registros descobertos em zeros

#### Scenario: Lacuna histórica

- **WHEN** uma produção de 15/05/2026 possui padrão cuja vigência começa em 01/07/2026
- **THEN** o sistema MUST classificar o registro como sem padrão aplicável na data da produção e MUST NOT usar o padrão atual retroativamente

### Requirement: Detectar inconsistências temporais e valores suspeitos sem bloquear dados válidos

Os diagnósticos MUST avaliar registros persistidos ou importados para detectar fim anterior ao início, duração zero quando não permitida, datas futuras além do limite configurado, duração longa, combinação de duração curta com quantidade alta, quantidade não positiva em legado e taxa de produção fora do limite configurado. As regras MUST ser determinísticas e configuráveis; uma produção incomum, mas válida e dentro dos limites, MUST permanecer sem alerta indevido.

#### Scenario: Produção excepcionalmente longa

- **WHEN** a duração de um registro excede o limite configurado de duração máxima
- **THEN** o sistema MUST retornar um alerta vinculado ao registro, contendo duração e limite aplicados, sem desativar nem corrigir a produção

#### Scenario: Caso incomum permitido

- **WHEN** um registro curto com quantidade alta permanece dentro do limite de taxa configurado
- **THEN** o sistema MUST preservar o registro e MUST NOT classificá-lo como suspeito apenas por ser incomum

### Requirement: Diagnosticar paradas, cadastros e vigências problemáticas

O sistema MUST detectar, quando compatível com o modelo real, parada fora do intervalo da produção, fim de parada anterior ao início, sobreposição indevida, parada maior que a duração bruta, peça sem descrição obrigatória, máquina sem setor, contexto operacional incompleto, sobreposição de vigências de tempo padrão/valor/meta e lacunas relevantes de configuração. APIs novas já devem manter suas validações bloqueantes; o diagnóstico atende dados legados e importados.

#### Scenario: Parada impossível em legado

- **WHEN** uma produção possui 60 minutos brutos e paradas que totalizam 80 minutos
- **THEN** o sistema MUST retornar diagnóstico `CRITICO` de tempo produtivo impossível com os valores observados

#### Scenario: Vigências conflitantes

- **WHEN** dois padrões ativos do mesmo contexto possuem períodos sobrepostos
- **THEN** o sistema MUST retornar alerta de vigência conflitante identificando os recursos relacionados e MUST NOT excluir ou encerrar automaticamente qualquer configuração

### Requirement: Expor resumo de qualidade por empresa, período e setor

O sistema MUST oferecer `GET /api/v1/empresas/{empresaId}/qualidade-dados/resumo` com `inicio` e `fim` obrigatórios e `setorId` opcional. A resposta MUST conter, no mínimo, cobertura de tempo padrão, cobertura de valor, quantidade de alertas, quantidade de alertas críticos e status técnico somente se seus limites forem explícitos e configuráveis. Períodos inválidos ou ausentes MUST ser rejeitados sem consulta ilimitada.

#### Scenario: Resumo por setor

- **WHEN** a empresa possui Pintura com cobertura de padrão de 100% e Laser com cobertura de 50%, e o cliente solicita cada setor separadamente
- **THEN** cada resposta MUST retornar somente as métricas do setor solicitado e o resumo geral MUST corresponder à base autorizada agregada

#### Scenario: Período inválido

- **WHEN** `fim` é anterior a `inicio` ou um parâmetro obrigatório está ausente
- **THEN** a API MUST retornar erro de validação e MUST NOT executar varredura histórica ilimitada
