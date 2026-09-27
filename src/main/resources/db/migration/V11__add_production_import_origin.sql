alter table registro_producao add column origem varchar(20) not null default 'MANUAL';
alter table registro_producao add column importacao_id bigint;
alter table registro_producao add column importacao_linha integer;
alter table registro_producao add constraint fk_producao_importacao foreign key (importacao_id) references importacao_producao(id);
create index idx_producao_importacao on registro_producao(importacao_id, importacao_linha);
