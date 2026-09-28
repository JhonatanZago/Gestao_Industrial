INSERT INTO permissao (codigo, nome)
SELECT 'QUALIDADE_DADOS_VISUALIZAR', 'Visualizar qualidade dos dados'
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'QUALIDADE_DADOS_VISUALIZAR');

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, x.id FROM perfil p CROSS JOIN permissao x
WHERE p.codigo IN ('ADMINISTRADOR', 'GESTOR')
  AND x.codigo = 'QUALIDADE_DADOS_VISUALIZAR'
  AND NOT EXISTS (SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = x.id);
