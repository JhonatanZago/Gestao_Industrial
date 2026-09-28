INSERT INTO perfil (codigo, nome, descricao)
SELECT 'ADMINISTRADOR', 'Administrador', 'Acesso administrativo dentro da própria empresa'
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE codigo = 'ADMINISTRADOR');
INSERT INTO perfil (codigo, nome, descricao)
SELECT 'GESTOR', 'Gestor', 'Gestão operacional dos setores associados'
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE codigo = 'GESTOR');
INSERT INTO perfil (codigo, nome, descricao)
SELECT 'OPERACIONAL', 'Operacional', 'Execução operacional dos setores associados'
WHERE NOT EXISTS (SELECT 1 FROM perfil WHERE codigo = 'OPERACIONAL');

INSERT INTO permissao (codigo, nome) SELECT 'USUARIO_VISUALIZAR', 'Visualizar usuários' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'USUARIO_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'USUARIO_GERENCIAR', 'Gerenciar usuários' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'USUARIO_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'SETOR_VISUALIZAR', 'Visualizar setores' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'SETOR_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'SETOR_GERENCIAR', 'Gerenciar setores' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'SETOR_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'FUNCIONARIO_VISUALIZAR', 'Visualizar funcionários' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'FUNCIONARIO_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'FUNCIONARIO_GERENCIAR', 'Gerenciar funcionários' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'FUNCIONARIO_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'PECA_VISUALIZAR', 'Visualizar peças' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'PECA_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'PECA_GERENCIAR', 'Gerenciar peças' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'PECA_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'MAQUINA_VISUALIZAR', 'Visualizar máquinas' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'MAQUINA_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'MAQUINA_GERENCIAR', 'Gerenciar máquinas' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'MAQUINA_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'TURNO_VISUALIZAR', 'Visualizar turnos' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'TURNO_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'TURNO_GERENCIAR', 'Gerenciar turnos' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'TURNO_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'PRODUCAO_VISUALIZAR', 'Visualizar produção' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'PRODUCAO_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'PRODUCAO_REGISTRAR', 'Registrar produção' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'PRODUCAO_REGISTRAR');
INSERT INTO permissao (codigo, nome) SELECT 'PRODUCAO_IMPORTAR', 'Importar produção' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'PRODUCAO_IMPORTAR');
INSERT INTO permissao (codigo, nome) SELECT 'PARADA_VISUALIZAR', 'Visualizar paradas' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'PARADA_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'PARADA_GERENCIAR', 'Gerenciar paradas' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'PARADA_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'META_VISUALIZAR', 'Visualizar metas' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'META_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'META_GERENCIAR', 'Gerenciar metas' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'META_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'TEMPO_PADRAO_VISUALIZAR', 'Visualizar tempos padrão' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'TEMPO_PADRAO_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'TEMPO_PADRAO_GERENCIAR', 'Gerenciar tempos padrão' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'TEMPO_PADRAO_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'ANALYTICS_VISUALIZAR', 'Visualizar analytics' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'ANALYTICS_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'DESEMPENHO_VISUALIZAR', 'Visualizar desempenho' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'DESEMPENHO_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'FINANCEIRO_INDUSTRIAL_VISUALIZAR', 'Visualizar financeiro industrial' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'FINANCEIRO_INDUSTRIAL_VISUALIZAR');
INSERT INTO permissao (codigo, nome) SELECT 'VALOR_PRODUCAO_GERENCIAR', 'Gerenciar valores de produção' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'VALOR_PRODUCAO_GERENCIAR');
INSERT INTO permissao (codigo, nome) SELECT 'RELATORIO_VISUALIZAR', 'Visualizar relatórios' WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'RELATORIO_VISUALIZAR');

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, x.id FROM perfil p CROSS JOIN permissao x
WHERE p.codigo = 'ADMINISTRADOR'
  AND NOT EXISTS (SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = x.id);

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, x.id FROM perfil p JOIN permissao x ON x.codigo IN (
    'SETOR_VISUALIZAR', 'FUNCIONARIO_VISUALIZAR', 'FUNCIONARIO_GERENCIAR',
    'PECA_VISUALIZAR', 'PECA_GERENCIAR', 'MAQUINA_VISUALIZAR', 'MAQUINA_GERENCIAR',
    'TURNO_VISUALIZAR', 'TURNO_GERENCIAR', 'PRODUCAO_VISUALIZAR', 'PRODUCAO_REGISTRAR',
    'PRODUCAO_IMPORTAR', 'PARADA_VISUALIZAR', 'PARADA_GERENCIAR', 'META_VISUALIZAR',
    'META_GERENCIAR', 'TEMPO_PADRAO_VISUALIZAR', 'TEMPO_PADRAO_GERENCIAR',
    'ANALYTICS_VISUALIZAR', 'DESEMPENHO_VISUALIZAR', 'RELATORIO_VISUALIZAR')
WHERE p.codigo = 'GESTOR'
  AND NOT EXISTS (SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = x.id);

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, x.id FROM perfil p JOIN permissao x ON x.codigo IN (
    'SETOR_VISUALIZAR', 'FUNCIONARIO_VISUALIZAR', 'PECA_VISUALIZAR',
    'MAQUINA_VISUALIZAR', 'TURNO_VISUALIZAR', 'PRODUCAO_VISUALIZAR',
    'PRODUCAO_REGISTRAR', 'PARADA_VISUALIZAR', 'META_VISUALIZAR',
    'TEMPO_PADRAO_VISUALIZAR', 'ANALYTICS_VISUALIZAR', 'DESEMPENHO_VISUALIZAR',
    'RELATORIO_VISUALIZAR')
WHERE p.codigo = 'OPERACIONAL'
  AND NOT EXISTS (SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = x.id);
