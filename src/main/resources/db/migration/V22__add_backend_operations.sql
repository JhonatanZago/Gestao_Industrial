ALTER TABLE importacao_producao ADD COLUMN correlation_id VARCHAR(100);
ALTER TABLE importacao_producao ADD COLUMN erro_codigo VARCHAR(80);
ALTER TABLE importacao_producao ADD COLUMN erro_mensagem VARCHAR(500);
CREATE INDEX idx_importacao_correlation ON importacao_producao(correlation_id);

INSERT INTO permissao (codigo, nome, descricao)
SELECT 'OPERACAO_MONITORAR', 'Monitorar operação do backend', 'Acessar métricas e detalhes operacionais protegidos'
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'OPERACAO_MONITORAR');

INSERT INTO perfil_permissao (perfil_id, permissao_id)
SELECT p.id, x.id FROM perfil p CROSS JOIN permissao x
WHERE p.codigo = 'ADMINISTRADOR'
  AND x.codigo = 'OPERACAO_MONITORAR'
  AND NOT EXISTS (
      SELECT 1 FROM perfil_permissao pp
      WHERE pp.perfil_id = p.id AND pp.permissao_id = x.id
  );

