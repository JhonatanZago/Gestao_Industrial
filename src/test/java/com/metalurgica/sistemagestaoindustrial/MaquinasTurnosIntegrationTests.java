package com.metalurgica.sistemagestaoindustrial;

import com.metalurgica.sistemagestaoindustrial.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class MaquinasTurnosIntegrationTests {
    @Autowired MockMvc mvc; @Autowired EmpresaRepository empresaRepository; @Autowired SetorRepository setorRepository; @Autowired FuncionarioRepository funcionarioRepository; @Autowired PecaRepository pecaRepository; @Autowired RegistroProducaoRepository producaoRepository; @Autowired ParadaProducaoRepository paradaRepository; @Autowired MotivoParadaRepository motivoRepository; @Autowired MaquinaRepository maquinaRepository; @Autowired TurnoRepository turnoRepository;
    @BeforeEach void clean() { paradaRepository.deleteAll(); motivoRepository.deleteAll(); producaoRepository.deleteAll(); maquinaRepository.deleteAll(); turnoRepository.deleteAll(); pecaRepository.deleteAll(); funcionarioRepository.deleteAll(); setorRepository.deleteAll(); empresaRepository.deleteAll(); }

    @Test void managesMachinesAndShiftsIncludingOvernightDuration() throws Exception {
        var f = fixture("A", "47111111111111");
        createMachine(f, " PINT-CAB-01 ").andExpect(jsonPath("$.codigo").value("PINT-CAB-01"));
        var machine = maquinaRepository.findByEmpresaIdAndCodigo(f.empresa(), "PINT-CAB-01").orElseThrow().getId();
        createShift(f.empresa(), "T1", "06:00", "14:00").andExpect(jsonPath("$.duracaoNominalMinutos").value(480));
        createShift(f.empresa(), "T3", "22:00", "06:00").andExpect(jsonPath("$.duracaoNominalMinutos").value(480));
        mvc.perform(post("/api/v1/empresas/" + f.empresa() + "/maquinas").contentType(MediaType.APPLICATION_JSON).content(machineJson(f, "PINT-CAB-01"))).andExpect(status().isConflict());
        mvc.perform(patch("/api/v1/empresas/" + f.empresa() + "/maquinas/" + machine + "/desativar")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(get("/api/v1/empresas/" + f.empresa() + "/maquinas?setorId=" + f.setor())).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].codigo").value("PINT-CAB-01"));
    }

    @Test void associatesOptionalMachineAndShiftAndPreservesOldPayload() throws Exception {
        var f = fixture("B", "47222222222222"); createMachine(f, "PINT-CAB-01"); var machine = maquinaRepository.findByEmpresaIdAndCodigo(f.empresa(), "PINT-CAB-01").orElseThrow().getId(); createShift(f.empresa(), "T3", "22:00", "06:00"); var shift = turnoRepository.findByEmpresaIdAndCodigo(f.empresa(), "T3").orElseThrow().getId();
        mvc.perform(post(prodPath(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T23:30:00", "2026-09-27T01:00:00", machine, shift))).andExpect(status().isCreated()).andExpect(jsonPath("$.maquinaCodigo").value("PINT-CAB-01")).andExpect(jsonPath("$.turnoCodigo").value("T3"));
        var old = fixture("C", "47333333333333"); mvc.perform(post(prodPath(old.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(old, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00", null, null))).andExpect(status().isCreated()).andExpect(jsonPath("$.maquinaId").doesNotExist()).andExpect(jsonPath("$.turnoId").doesNotExist());
    }

    @Test void rejectsInvalidAllocationsAndKeepsStopRules() throws Exception {
        var a = fixture("D", "47444444444444"); var b = fixture("E", "47555555555555"); createMachine(a, "PINT-01"); createMachine(b, "PINT-01"); var machineA = maquinaRepository.findByEmpresaIdAndCodigo(a.empresa(), "PINT-01").orElseThrow().getId(); var machineB = maquinaRepository.findByEmpresaIdAndCodigo(b.empresa(), "PINT-01").orElseThrow().getId(); var prod = createProduction(a, null, null);
        mvc.perform(put(prodPath(a.empresa(), prod)).contentType(MediaType.APPLICATION_JSON).content(productionJson(a, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00", machineB, null))).andExpect(status().isNotFound());
        mvc.perform(put(prodPath(a.empresa(), prod)).contentType(MediaType.APPLICATION_JSON).content(productionJson(a, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00", machineA, 99999L))).andExpect(status().isNotFound());
        var motivo = createMotivo(a.empresa()); var paradaPath = prodPath(a.empresa(), prod) + "/paradas";
        mvc.perform(post(paradaPath).contentType(MediaType.APPLICATION_JSON).content(stopJson(motivo, null))).andExpect(status().isCreated());
        mvc.perform(post(paradaPath).contentType(MediaType.APPLICATION_JSON).content(stopJson(motivo, machineB))).andExpect(status().isNotFound());
    }

    @Test void rejectsMachineSectorMismatchAndInactiveShift() throws Exception {
        var f = fixture("F", "47666666666666"); var laser = createSecondSector(f.empresa()); createMachine(f, "LASER-01", laser); var machine = maquinaRepository.findByEmpresaIdAndCodigo(f.empresa(), "LASER-01").orElseThrow().getId(); var prod = createProduction(f, null, null);
        mvc.perform(put(prodPath(f.empresa(), prod)).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00", machine, null))).andExpect(status().isConflict());
        createShift(f.empresa(), "T1", "06:00", "14:00"); var shift = turnoRepository.findByEmpresaIdAndCodigo(f.empresa(), "T1").orElseThrow().getId(); mvc.perform(patch("/api/v1/empresas/" + f.empresa() + "/turnos/" + shift + "/desativar")).andExpect(status().isOk());
        mvc.perform(put(prodPath(f.empresa(), prod)).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00", null, shift))).andExpect(status().isConflict());
    }

    private org.springframework.test.web.servlet.ResultActions createMachine(Fixture f, String c) throws Exception { return createMachine(f, c, f.setor()); }
    private org.springframework.test.web.servlet.ResultActions createMachine(Fixture f, String c, long setor) throws Exception { return mvc.perform(post("/api/v1/empresas/" + f.empresa() + "/maquinas").contentType(MediaType.APPLICATION_JSON).content(machineJson(new Fixture(f.empresa(), setor, f.funcionario(), f.peca()), c))).andExpect(status().isCreated()); }
    private org.springframework.test.web.servlet.ResultActions createShift(long e, String c, String start, String end) throws Exception { return mvc.perform(post("/api/v1/empresas/" + e + "/turnos").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"" + c + "\",\"nome\":\"Turno " + c + "\",\"horaInicio\":\"" + start + "\",\"horaFim\":\"" + end + "\"}" )).andExpect(status().isCreated()); }
    private long createProduction(Fixture f, Long machine, Long shift) throws Exception { mvc.perform(post(prodPath(f.empresa())).contentType(MediaType.APPLICATION_JSON).content(productionJson(f, "1", "2026-09-26T08:00:00", "2026-09-26T09:00:00", machine, shift))).andExpect(status().isCreated()); return producaoRepository.findAll().stream().filter(p -> p.getEmpresa().getId().equals(f.empresa())).findFirst().orElseThrow().getId(); }
    private long createMotivo(long e) throws Exception { mvc.perform(post("/api/v1/empresas/" + e + "/motivos-parada").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"MATERIAL\",\"nome\":\"Material\"}")).andExpect(status().isCreated()); return motivoRepository.findByEmpresaIdAndCodigo(e, "MATERIAL").orElseThrow().getId(); }
    private long createSecondSector(long e) throws Exception { mvc.perform(post("/api/v1/empresas/" + e + "/setores").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"LASER\",\"nome\":\"Laser\"}")).andExpect(status().isCreated()); return setorRepository.findByEmpresaIdAndCodigo(e, "LASER").orElseThrow().getId(); }
    private Fixture fixture(String n, String cnpj) throws Exception { mvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON).content("{\"razaoSocial\":\"Empresa " + n + "\",\"nomeFantasia\":\"" + n + "\",\"cnpj\":\"" + cnpj + "\"}")).andExpect(status().isCreated()); long e=empresaRepository.findByCnpj(cnpj).orElseThrow().getId(); mvc.perform(post("/api/v1/empresas/"+e+"/setores").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"PINT\",\"nome\":\"Pintura\"}")).andExpect(status().isCreated()); mvc.perform(post("/api/v1/empresas/"+e+"/funcionarios").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"00147\",\"nome\":\"Operador\"}")).andExpect(status().isCreated()); mvc.perform(post("/api/v1/empresas/"+e+"/pecas").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"54871\",\"descricao\":\"Suporte\"}")).andExpect(status().isCreated()); return new Fixture(e, setorRepository.findByEmpresaIdAndCodigo(e,"PINT").orElseThrow().getId(), funcionarioRepository.findByEmpresaIdAndCodigo(e,"00147").orElseThrow().getId(), pecaRepository.findByEmpresaIdAndCodigo(e,"54871").orElseThrow().getId()); }
    private String machineJson(Fixture f, String c) { return "{\"setorId\":"+f.setor()+",\"codigo\":\""+c+"\",\"nome\":\"Cabine 01\",\"tipo\":\"CABINE\"}"; }
    private String productionJson(Fixture f,String q,String i,String end,Long m,Long t) { return "{\"setorId\":"+f.setor()+",\"funcionarioId\":"+f.funcionario()+",\"pecaId\":"+f.peca()+",\"quantidade\":"+q+",\"inicio\":\""+i+"\",\"fim\":\""+end+"\",\"maquinaId\":"+(m==null?"null":m)+",\"turnoId\":"+(t==null?"null":t)+"}"; }
    private String stopJson(long motivo, Long m) { return "{\"motivoParadaId\":"+motivo+",\"inicio\":\"2026-09-26T08:10:00\",\"fim\":\"2026-09-26T08:20:00\",\"maquinaId\":"+(m==null?"null":m)+"}"; }
    private String prodPath(long e) { return "/api/v1/empresas/"+e+"/producoes"; } private String prodPath(long e,long p) { return prodPath(e)+"/"+p; }
    private record Fixture(long empresa,long setor,long funcionario,long peca) { }
}
