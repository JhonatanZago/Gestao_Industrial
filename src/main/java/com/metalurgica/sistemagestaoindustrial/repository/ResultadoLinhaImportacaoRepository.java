package com.metalurgica.sistemagestaoindustrial.repository;
import com.metalurgica.sistemagestaoindustrial.domain.ResultadoLinhaImportacao; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ResultadoLinhaImportacaoRepository extends JpaRepository<ResultadoLinhaImportacao,Long> { Page<ResultadoLinhaImportacao> findByImportacaoId(Long importacaoId,Pageable p); }
