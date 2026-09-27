alter table registro_producao add column maquina_id bigint;
alter table registro_producao add column turno_id bigint;
alter table parada_producao add column maquina_id bigint;
alter table registro_producao add constraint fk_producao_maquina foreign key (maquina_id) references maquina(id);
alter table registro_producao add constraint fk_producao_turno foreign key (turno_id) references turno(id);
alter table parada_producao add constraint fk_parada_maquina foreign key (maquina_id) references maquina(id);
create index idx_producao_maquina_inicio on registro_producao(maquina_id, inicio);
create index idx_producao_turno_inicio on registro_producao(turno_id, inicio);
create index idx_parada_maquina_inicio on parada_producao(maquina_id, inicio);
