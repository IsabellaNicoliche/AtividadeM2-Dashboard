package com.pfc.thindesk.dto;

public class ContagemDTO {

    private String chave;
    private long total;

    public ContagemDTO() {
    }

    public ContagemDTO(String chave, long total) {
        this.chave = chave;
        this.total = total;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
