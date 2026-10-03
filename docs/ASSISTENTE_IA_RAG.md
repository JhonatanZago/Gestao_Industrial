# Assistente IA/RAG

O backend expõe o assistente sob /api/v1/empresas/{empresaId}/assistente e documentos RAG sob /assistente/documentos. Os gateways fake e disabled permitem testes sem API externa; um provedor pago não é requisito da suíte.

As respostas devem ser grounded nos dados autorizados do backend, respeitar empresa/setor e retornar estado explícito para ausência ou indisponibilidade. Financeiro e auditoria continuam protegidos por permissão.

Valide perguntas de produção, comparação e paradas com provider fake; SEM_DADOS; empresa/setor não autorizado; permissões financeiras/auditoria; prompt/tool injection; e fonte/isolamento de documentos RAG quando habilitado.

Não trate uma resposta de IA como fonte de verdade independente dos endpoints analíticos.
