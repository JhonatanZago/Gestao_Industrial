# Spec Delta

## Purpose

Permitir receber planilhas XLSX operacionais com layouts variáveis e convertê-las em linhas intermediárias normalizadas, sem depender do nome da aba ou da posição fixa das colunas.

## ADDED Requirements

### Requirement: Import accepts controlled XLSX workbooks
O sistema MUST aceitar apenas arquivos XLSX válidos, não vazios e dentro do limite configurável, sem executar macros; arquivos corrompidos, formatos não suportados ou planilhas sem aba válida MUST ser rejeitados com erro seguro.

#### Scenario: Accept valid workbook
- **WHEN** o upload contém um XLSX legível com uma aba contendo cabeçalhos suportados
- **THEN** o sistema seleciona a aba configurada ou a primeira aba válida e inicia a interpretação

#### Scenario: Reject unsafe or invalid file
- **WHEN** o upload é vazio, excede o limite, não é XLSX válido ou exige macro para interpretar campos críticos
- **THEN** o sistema rejeita o arquivo com código técnico apropriado e não cria produção

### Requirement: Import maps headers through explicit aliases
O sistema MUST identificar campos por cabeçalhos normalizados e aliases configuráveis, incluindo códigos de funcionário/peça, quantidade, data, início, fim e campos opcionais de setor, máquina e turno; a ausência de campo obrigatório MUST rejeitar o arquivo antes das linhas.

#### Scenario: Map reordered aliased headers
- **WHEN** a planilha usa ordem diferente e cabeçalhos como `Cod. Funcionário` e `Cód. Peça`
- **THEN** os campos são mapeados para o modelo intermediário correto

#### Scenario: Reject missing required header
- **WHEN** não existe cabeçalho obrigatório de funcionário, peça, quantidade, data, início ou fim
- **THEN** a importação é rejeitada antes do processamento de registros e informa `CABECALHO_AUSENTE`

### Requirement: Normalize spreadsheet values without losing business codes
Códigos de funcionário e peça MUST ser lidos como texto apresentado, preservando zeros à esquerda, letras e separadores; quantidade, data, horário e data/hora MUST aceitar somente formatos explicitamente suportados.

#### Scenario: Preserve leading zeros
- **WHEN** uma célula exibe o código de funcionário `00147`
- **THEN** a linha intermediária mantém exatamente `00147`

#### Scenario: Parse overnight date and time
- **WHEN** a linha informa 26/09/2026, início 22:00 e fim 02:00 conforme o perfil de importação
- **THEN** o fim é construído no dia seguinte segundo a estratégia documentada

#### Scenario: Ignore empty rows but validate partial rows
- **WHEN** uma linha está completamente vazia ou parcialmente preenchida
- **THEN** a linha vazia é ignorada e a linha parcial é reportada com seus erros de validação
