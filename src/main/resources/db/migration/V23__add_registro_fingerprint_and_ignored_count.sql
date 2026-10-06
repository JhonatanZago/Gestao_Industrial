alter table registro_producao add column fingerprint_registro varchar(64);
create unique index uq_registro_producao_fingerprint on registro_producao(fingerprint_registro);
alter table importacao_producao add column linhas_ignoradas integer not null default 0;
