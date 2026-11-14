package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.ContagemDTO;
import com.pfc.thindesk.dto.ErroRespostaDTO;
import com.pfc.thindesk.dto.IndicadoresGeraisDTO;
import com.pfc.thindesk.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/indicadores")
    public IndicadoresGeraisDTO indicadores(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tecnico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return dashboardService.obterIndicadoresGerais(status, tipo, tecnico, dataInicio, dataFim);
    }

    @GetMapping("/situacao")
    public List<ContagemDTO> situacao(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tecnico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return dashboardService.obterSituacao(status, tipo, tecnico, dataInicio, dataFim);
    }

    @GetMapping("/tipos-mais-atendidos")
    public List<ContagemDTO> tiposMaisAtendidos(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tecnico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false, defaultValue = "5") int limite) {
        return dashboardService.obterTiposMaisAtendidos(status, tipo, tecnico, dataInicio, dataFim, limite);
    }

    @GetMapping("/por-periodo")
    public List<ContagemDTO> porPeriodo(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tecnico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false, defaultValue = "DIA") String agrupamento) {
        return dashboardService.obterPorPeriodo(status, tipo, tecnico, dataInicio, dataFim, agrupamento);
    }

    @GetMapping("/filtros/tecnicos")
    public List<String> tecnicosDisponiveis() {
        return dashboardService.obterTecnicosDistintos();
    }

    @GetMapping("/filtros/tipos")
    public List<String> tiposDisponiveis() {
        return dashboardService.obterTiposDistintos();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroRespostaDTO> tratarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroRespostaDTO(ex.getMessage()));
    }
}
