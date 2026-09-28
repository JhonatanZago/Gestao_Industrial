INSERT INTO permissao (codigo, nome)
SELECT 'AUDITORIA_VISUALIZAR', 'Visualizar auditoria'
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'AUDITORIA_VISUALIZAR');
INSERT INTO permissao (codigo, nome)
SELECT 'AUDITORIA_FINANCEIRA_VISUALIZAR', 'Visualizar auditoria financeira'
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'AUDITORIA_FINANCEIRA_VISUALIZAR');
INSERT INTO permissao (codigo, nome)
SELECT 'AUDITORIA_USUARIOS_VISUALIZAR', 'Visualizar auditoria de usuarios e permissoes'
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'AUDITORIA_USUARIOS_VISUALIZAR');

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, x.id FROM perfil p CROSS JOIN permissao x
WHERE p.codigo = 'ADMINISTRADOR'
  AND x.codigo IN ('AUDITORIA_VISUALIZAR', 'AUDITORIA_FINANCEIRA_VISUALIZAR', 'AUDITORIA_USUARIOS_VISUALIZAR')
  AND NOT EXISTS (SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = x.id);

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, x.id FROM perfil p JOIN permissao x ON x.codigo = 'AUDITORIA_VISUALIZAR'
WHERE p.codigo = 'GESTOR'
  AND NOT EXISTS (SELECT 1 FROM perfil_permissao pp WHERE pp.perfil_id = p.id AND pp.permissao_id = x.id);
