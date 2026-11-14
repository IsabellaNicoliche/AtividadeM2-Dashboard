# Thindesk — Dashboard Administrativo (M2)

Documentação da atividade: Dashboard Administrativo integrado ao sistema de chamados Thindesk, com API própria para alimentar indicadores, gráficos, ranking e filtros.

## 1. Requisitos funcionais (RF)

| ID | Requisito | Onde foi implementado |
|----|-----------|------------------------|
| RF01 | Indicadores gerais de chamados técnicos (total, abertos, em andamento, concluídos, tempo médio de atendimento) | `GET /api/dashboard/indicadores` + cards no topo de `/dashboard` |
| RF02 | Análise de chamados atendidos por período, em gráfico | `GET /api/dashboard/por-periodo` + gráfico de linha em `/dashboard` |
| RF03 | Situação dos chamados (distribuição por status) | `GET /api/dashboard/situacao` + gráfico de rosca em `/dashboard` |
| RF04 | Tipos de chamados mais atendidos (ranking) | `GET /api/dashboard/tipos-mais-atendidos` + gráfico de barras em `/dashboard` |
| RF05 | Filtros de consulta (status, tipo, técnico, período) | Painel de filtros no topo de `/dashboard`, aplicados em todos os endpoints acima |
| RF06 | Integração com a API | O dashboard não recebe dados prontos do controller MVC: a página `dashboard.html` carrega vazia e o `dashboard.js` consome os endpoints REST via `fetch` |
| RF07 | Atualização e tratamento de dados | Botão "Atualizar" (reconsulta sem recarregar a página), validação de datas (data início > data fim retorna erro 400), mensagens de erro na tela e tolerância a status gravados com grafias diferentes (ex.: "Aberto" x "ABERTO") |

## 2. Requisitos não funcionais (RNF)

- **RNF01 — Persistência:** dados de chamados armazenados no MongoDB (coleção `chamados`), reaproveitando a conexão já configurada no projeto Thindesk.
- **RNF02 — Desacoplamento:** a camada de agregação/filtros (`DashboardService`) é independente da camada HTTP (`DashboardController`), podendo ser testada isoladamente.
- **RNF03 — Usabilidade:** dashboard responsivo (Bootstrap 5), com indicação visual de carregamento e de erro.
- **RNF04 — Desempenho aceitável:** consultas filtram em memória sobre a coleção de chamados; adequado ao volume de dados de um projeto acadêmico (não indicado, sem revisão, para grandes volumes em produção).
- **RNF05 — Segurança:** todos os endpoints de `/api/dashboard/**` exigem usuário autenticado (Spring Security), igual às demais páginas do sistema.

## 3. Documentação dos endpoints

Base: `/api/dashboard`. Todos exigem autenticação (sessão de login ativa). Parâmetros de filtro são opcionais e combináveis em todos os endpoints abaixo (exceto os dois últimos, que alimentam as opções dos filtros):

| Parâmetro | Tipo | Formato | Descrição |
|-----------|------|---------|-----------|
| `status` | string | `ABERTO`, `EM_ANDAMENTO` ou `CONCLUIDO` | Filtra pelo status do chamado |
| `tipo` | string | texto livre | Filtra pelo tipo do chamado |
| `tecnico` | string | texto livre | Filtra pelo técnico responsável |
| `dataInicio` | string | `yyyy-MM-dd` | Considera apenas chamados com `dataAbertura` a partir desta data |
| `dataFim` | string | `yyyy-MM-dd` | Considera apenas chamados com `dataAbertura` até esta data |

### 3.1 `GET /api/dashboard/indicadores`

Indicadores gerais (RF01).

**Requisição de exemplo:**
```
GET /api/dashboard/indicadores?status=CONCLUIDO&dataInicio=2026-11-01&dataFim=2026-11-30
```

**Resposta 200 (exemplo):**
```json
{
  "total": 42,
  "abertos": 0,
  "emAndamento": 0,
  "concluidos": 42,
  "tempoMedioAtendimentoHoras": 6.5
}
```
`tempoMedioAtendimentoHoras` vem `null` quando nenhum chamado filtrado tem `dataAbertura` **e** `dataFechamento` preenchidas.

### 3.2 `GET /api/dashboard/situacao`

Contagem de chamados por status (RF03), para o gráfico de rosca.

**Resposta 200 (exemplo):**
```json
[
  { "chave": "Concluído", "total": 20 },
  { "chave": "Aberto", "total": 15 },
  { "chave": "Em Andamento", "total": 7 }
]
```

### 3.3 `GET /api/dashboard/tipos-mais-atendidos`

Ranking de tipos de chamado (RF04). Parâmetro extra:

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `limite` | inteiro | `5` | Quantos tipos retornar, do mais atendido para o menos atendido |

**Resposta 200 (exemplo):**
```json
[
  { "chave": "Manutenção", "total": 18 },
  { "chave": "Instalação", "total": 10 }
]
```

### 3.4 `GET /api/dashboard/por-periodo`

Série temporal de chamados abertos (RF02). Parâmetro extra:

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `agrupamento` | string | `DIA` | `DIA` (chave `yyyy-MM-dd`) ou `MES` (chave `yyyy-MM`) |

**Resposta 200 (exemplo, agrupamento=DIA):**
```json
[
  { "chave": "2026-11-10", "total": 3 },
  { "chave": "2026-11-11", "total": 5 }
]
```

### 3.5 `GET /api/dashboard/filtros/tecnicos` e `GET /api/dashboard/filtros/tipos`

Listam os valores distintos já cadastrados (sem filtros), usados para popular os `<select>` de técnico e tipo no painel de filtros.

**Resposta 200 (exemplo):**
```json
["Ana", "João", "Marcos"]
```

### 3.6 Erros

Todos os endpoints acima retornam **400 Bad Request** quando `dataInicio` é posterior a `dataFim`:

```json
{ "mensagem": "A data de início não pode ser depois da data de fim." }
```

Um `dataInicio`/`dataFim` em formato inválido (diferente de `yyyy-MM-dd`) é rejeitado pelo Spring com **400 Bad Request** antes mesmo de chegar ao controller.

Um acesso sem estar autenticado é redirecionado para `/login` (comportamento padrão do Spring Security já configurado no projeto).

## 4. Evidências de teste

> Esta seção é para você completar depois de rodar o projeto localmente — veja o `README.md` para o passo a passo. Capture e cole aqui (ou anexe como imagens na pasta `docs/evidencias/`):
>
> 1. Captura do dashboard carregado, sem filtros, mostrando os indicadores e os 3 gráficos preenchidos.
> 2. Captura do dashboard com pelo menos um filtro aplicado (ex.: um técnico específico) mostrando os números mudando.
> 3. Uma chamada direta a um endpoint pelo navegador ou Postman, por exemplo `http://localhost:8080/api/dashboard/indicadores`, mostrando o JSON de resposta.
> 4. Uma tentativa de filtro inválido (data início depois da data fim) mostrando a mensagem de erro na tela.

## 5. Modelo de dados alterado

A entidade `Chamado` ganhou dois campos novos em relação ao código-base entregue pelo professor:

- `dataAbertura` (`LocalDateTime`) — preenchida automaticamente ao criar o chamado, se não informada.
- `dataFechamento` (`LocalDateTime`) — preenchida automaticamente quando o status é atualizado para `CONCLUIDO`.

Chamados já existentes no banco (criados antes dessa mudança) ficam com esses campos nulos e são automaticamente ignorados pelos filtros de período e pelo cálculo de tempo médio de atendimento, mas continuam contando nos indicadores gerais e nos demais gráficos.
