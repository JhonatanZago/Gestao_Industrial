INSERT INTO permissao (codigo, nome)
SELECT 'EMPRESA_VISUALIZAR', 'Visualizar empresas'
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'EMPRESA_VISUALIZAR');
INSERT INTO permissao (codigo, nome)
SELECT 'EMPRESA_GERENCIAR', 'Gerenciar empresas'
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'EMPRESA_GERENCIAR');
INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, x.id FROM perfil p CROSS JOIN permissao x
WHERE p.codigo = 'ADMINISTRADOR' AND x.codigo IN ('EMPRESA_VISUALIZAR', 'EMPRESA_GERENCIAR')
  AND NOT EXISTS (SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = x.id);
