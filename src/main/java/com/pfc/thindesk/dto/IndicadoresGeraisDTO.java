package com.pfc.thindesk.dto;

public class IndicadoresGeraisDTO {

    private long total;
    private long abertos;
    private long emAndamento;
    private long concluidos;
    private Double tempoMedioAtendimentoHoras;

    public IndicadoresGeraisDTO() {
    }

    public IndicadoresGeraisDTO(long total, long abertos, long emAndamento, long concluidos,
                                 Double tempoMedioAtendimentoHoras) {
        this.total = total;
        this.abertos = abertos;
        this.emAndamento = emAndamento;
        this.concluidos = concluidos;
        this.tempoMedioAtendimentoHoras = tempoMedioAtendimentoHoras;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAbertos() {
        return abertos;
    }

    public void setAbertos(long abertos) {
        this.abertos = abertos;
    }

    public long getEmAndamento() {
        return emAndamento;
    }

    public void setEmAndamento(long emAndamento) {
        this.emAndamento = emAndamento;
    }

    public long getConcluidos() {
        return concluidos;
    }

    public void setConcluidos(long concluidos) {
        this.concluidos = concluidos;
    }

    public Double getTempoMedioAtendimentoHoras() {
        return tempoMedioAtendimentoHoras;
    }

    public void setTempoMedioAtendimentoHoras(Double tempoMedioAtendimentoHoras) {
        this.tempoMedioAtendimentoHoras = tempoMedioAtendimentoHoras;
    }
}
