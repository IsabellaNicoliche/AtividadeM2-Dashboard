package com.pfc.thindesk.service;

import com.pfc.thindesk.dto.ContagemDTO;
import com.pfc.thindesk.dto.IndicadoresGeraisDTO;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.repository.ChamadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private ChamadoRepository chamadoRepository;

    public IndicadoresGeraisDTO obterIndicadoresGerais(String status, String tipo, String tecnico,
                                                         LocalDate dataInicio, LocalDate dataFim) {
        List<Chamado> chamados = buscarComFiltros(status, tipo, tecnico, dataInicio, dataFim);

        long abertos = chamados.stream().filter(c -> statusCorresponde(c, "ABERTO")).count();
        long emAndamento = chamados.stream().filter(c -> statusCorresponde(c, "EM_ANDAMENTO")).count();
        long concluidos = chamados.stream().filter(c -> statusCorresponde(c, "CONCLUIDO")).count();

        List<Double> horasAtendimento = chamados.stream()
                .filter(c -> c.getDataAbertura() != null && c.getDataFechamento() != null)
                .map(c -> Duration.between(c.getDataAbertura(), c.getDataFechamento()).toMinutes() / 60.0)
                .collect(Collectors.toList());

        Double tempoMedio = horasAtendimento.isEmpty()
                ? null
                : horasAtendimento.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        return new IndicadoresGeraisDTO(chamados.size(), abertos, emAndamento, concluidos, tempoMedio);
    }

    public List<ContagemDTO> obterSituacao(String status, String tipo, String tecnico,
                                            LocalDate dataInicio, LocalDate dataFim) {
        List<Chamado> chamados = buscarComFiltros(status, tipo, tecnico, dataInicio, dataFim);

        Map<String, Long> contagem = chamados.stream()
                .collect(Collectors.groupingBy(
                        c -> normalizarStatusLabel(c.getStatus()),
                        Collectors.counting()));

        return contagem.entrySet().stream()
                .map(e -> new ContagemDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(ContagemDTO::getTotal).reversed())
                .collect(Collectors.toList());
    }

    public List<ContagemDTO> obterTiposMaisAtendidos(String status, String tipo, String tecnico,
                                                       LocalDate dataInicio, LocalDate dataFim, int limite) {
        List<Chamado> chamados = buscarComFiltros(status, tipo, tecnico, dataInicio, dataFim);

        Map<String, Long> contagem = chamados.stream()
                .filter(c -> c.getTipo() != null && !c.getTipo().isBlank())
                .collect(Collectors.groupingBy(
                        c -> c.getTipo().trim(),
                        Collectors.counting()));

        return contagem.entrySet().stream()
                .map(e -> new ContagemDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(ContagemDTO::getTotal).reversed())
                .limit(limite > 0 ? limite : Long.MAX_VALUE)
                .collect(Collectors.toList());
    }

    public List<ContagemDTO> obterPorPeriodo(String status, String tipo, String tecnico,
                                              LocalDate dataInicio, LocalDate dataFim, String agrupamento) {
        List<Chamado> chamados = buscarComFiltros(status, tipo, tecnico, dataInicio, dataFim);

        DateTimeFormatter formato = "MES".equalsIgnoreCase(agrupamento) ? FORMATO_MES : FORMATO_DIA;

        Map<String, Long> contagem = chamados.stream()
                .filter(c -> c.getDataAbertura() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getDataAbertura().format(formato),
                        Collectors.counting()));

        Map<String, Long> ordenado = new LinkedHashMap<>();
        contagem.keySet().stream().sorted().forEach(chave -> ordenado.put(chave, contagem.get(chave)));

        List<ContagemDTO> resultado = new ArrayList<>();
        ordenado.forEach((chave, total) -> resultado.add(new ContagemDTO(chave, total)));
        return resultado;
    }

    public List<String> obterTecnicosDistintos() {
        return chamadoRepository.findAll().stream()
                .map(Chamado::getTecnico)
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> obterTiposDistintos() {
        return chamadoRepository.findAll().stream()
                .map(Chamado::getTipo)
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private List<Chamado> buscarComFiltros(String status, String tipo, String tecnico,
                                            LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data de início não pode ser depois da data de fim.");
        }

        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.atTime(23, 59, 59) : null;

        return chamadoRepository.findAll().stream()
                .filter(c -> status == null || status.isBlank() || statusCorresponde(c, status))
                .filter(c -> tipo == null || tipo.isBlank()
                        || (c.getTipo() != null && c.getTipo().equalsIgnoreCase(tipo)))
                .filter(c -> tecnico == null || tecnico.isBlank()
                        || (c.getTecnico() != null && c.getTecnico().equalsIgnoreCase(tecnico)))
                .filter(c -> inicio == null || (c.getDataAbertura() != null && !c.getDataAbertura().isBefore(inicio)))
                .filter(c -> fim == null || (c.getDataAbertura() != null && !c.getDataAbertura().isAfter(fim)))
                .collect(Collectors.toList());
    }

    private boolean statusCorresponde(Chamado chamado, String statusEsperado) {
        return chamado.getStatus() != null && chamado.getStatus().equalsIgnoreCase(statusEsperado);
    }

    private String normalizarStatusLabel(String status) {
        if (status == null || status.isBlank()) {
            return "Sem status";
        }
        if (status.equalsIgnoreCase("ABERTO")) {
            return "Aberto";
        }
        if (status.equalsIgnoreCase("EM_ANDAMENTO")) {
            return "Em Andamento";
        }
        if (status.equalsIgnoreCase("CONCLUIDO")) {
            return "Concluído";
        }
        return status.trim();
    }
}
