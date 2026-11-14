let graficoPeriodo = null;
let graficoSituacao = null;
let graficoTipos = null;

function montarQueryString() {
    const params = new URLSearchParams();

    const status = document.getElementById('filtroStatus').value;
    const tipo = document.getElementById('filtroTipo').value;
    const tecnico = document.getElementById('filtroTecnico').value;
    const dataInicio = document.getElementById('filtroDataInicio').value;
    const dataFim = document.getElementById('filtroDataFim').value;

    if (status) params.append('status', status);
    if (tipo) params.append('tipo', tipo);
    if (tecnico) params.append('tecnico', tecnico);
    if (dataInicio) params.append('dataInicio', dataInicio);
    if (dataFim) params.append('dataFim', dataFim);

    return params;
}

function buscarJson(url) {
    return fetch(url).then(resposta => {
        if (!resposta.ok) {
            return resposta.json()
                .catch(() => ({ mensagem: 'Não foi possível carregar os dados do dashboard.' }))
                .then(erro => { throw new Error(erro.mensagem); });
        }
        return resposta.json();
    });
}

function mostrarErro(mensagem) {
    const elemento = document.getElementById('dashboardMensagem');
    elemento.textContent = mensagem;
    elemento.classList.remove('d-none');
}

function esconderErro() {
    document.getElementById('dashboardMensagem').classList.add('d-none');
}

function definirCarregando(carregando) {
    document.getElementById('dashboardCarregando').classList.toggle('d-none', !carregando);
}

function carregarFiltrosDisponiveis() {
    buscarJson('/api/dashboard/filtros/tipos').then(tipos => {
        const select = document.getElementById('filtroTipo');
        tipos.forEach(tipo => {
            const option = document.createElement('option');
            option.value = tipo;
            option.textContent = tipo;
            select.appendChild(option);
        });
    }).catch(() => {});

    buscarJson('/api/dashboard/filtros/tecnicos').then(tecnicos => {
        const select = document.getElementById('filtroTecnico');
        tecnicos.forEach(tecnico => {
            const option = document.createElement('option');
            option.value = tecnico;
            option.textContent = tecnico;
            select.appendChild(option);
        });
    }).catch(() => {});
}

function atualizarIndicadores(params) {
    return buscarJson('/api/dashboard/indicadores?' + params.toString()).then(dados => {
        document.getElementById('indicadorTotal').textContent = dados.total;
        document.getElementById('indicadorAbertos').textContent = dados.abertos;
        document.getElementById('indicadorEmAndamento').textContent = dados.emAndamento;
        document.getElementById('indicadorConcluidos').textContent = dados.concluidos;

        const tempoMedio = document.getElementById('indicadorTempoMedio');
        tempoMedio.textContent = dados.tempoMedioAtendimentoHoras != null
            ? 'Tempo médio: ' + dados.tempoMedioAtendimentoHoras.toFixed(1) + 'h'
            : '';
    });
}

function atualizarGraficoPeriodo(params) {
    const agrupamento = document.getElementById('filtroAgrupamento').value;
    const url = '/api/dashboard/por-periodo?' + params.toString() + '&agrupamento=' + agrupamento;

    return buscarJson(url).then(dados => {
        const labels = dados.map(item => item.chave);
        const valores = dados.map(item => item.total);

        if (graficoPeriodo) {
            graficoPeriodo.destroy();
        }
        graficoPeriodo = new Chart(document.getElementById('graficoPeriodo'), {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Chamados abertos',
                    data: valores,
                    borderColor: '#0d6efd',
                    backgroundColor: 'rgba(13,110,253,0.15)',
                    fill: true,
                    tension: 0.2
                }]
            },
            options: {
                responsive: true,
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    });
}

function atualizarGraficoSituacao(params) {
    return buscarJson('/api/dashboard/situacao?' + params.toString()).then(dados => {
        const labels = dados.map(item => item.chave);
        const valores = dados.map(item => item.total);

        if (graficoSituacao) {
            graficoSituacao.destroy();
        }
        graficoSituacao = new Chart(document.getElementById('graficoSituacao'), {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: valores,
                    backgroundColor: ['#ffc107', '#0dcaf0', '#198754', '#6c757d']
                }]
            },
            options: { responsive: true }
        });
    });
}

function atualizarGraficoTipos(params) {
    return buscarJson('/api/dashboard/tipos-mais-atendidos?' + params.toString() + '&limite=5').then(dados => {
        const labels = dados.map(item => item.chave);
        const valores = dados.map(item => item.total);

        if (graficoTipos) {
            graficoTipos.destroy();
        }
        graficoTipos = new Chart(document.getElementById('graficoTipos'), {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Chamados',
                    data: valores,
                    backgroundColor: '#0d6efd'
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                scales: { x: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    });
}

function carregarDashboard() {
    esconderErro();
    definirCarregando(true);

    const params = montarQueryString();

    Promise.all([
        atualizarIndicadores(params),
        atualizarGraficoPeriodo(params),
        atualizarGraficoSituacao(params),
        atualizarGraficoTipos(params)
    ]).catch(erro => {
        mostrarErro(erro.message);
    }).finally(() => {
        definirCarregando(false);
    });
}

document.addEventListener('DOMContentLoaded', () => {
    carregarFiltrosDisponiveis();
    carregarDashboard();

    document.getElementById('btnAtualizar').addEventListener('click', carregarDashboard);
    document.getElementById('btnFiltrar').addEventListener('click', carregarDashboard);
    document.getElementById('btnLimpar').addEventListener('click', () => {
        document.getElementById('filtroStatus').value = '';
        document.getElementById('filtroTipo').value = '';
        document.getElementById('filtroTecnico').value = '';
        document.getElementById('filtroDataInicio').value = '';
        document.getElementById('filtroDataFim').value = '';
        document.getElementById('filtroAgrupamento').value = 'DIA';
        carregarDashboard();
    });
});
