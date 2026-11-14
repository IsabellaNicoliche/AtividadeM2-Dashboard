# O que foi adicionado neste pacote

Este zip é o projeto `thindesk` que você recebeu do professor, com o módulo de **Dashboard Administrativo** (atividade M2) já implementado em cima dele. Nada do que já existia foi removido — só adicionei/ajustei o necessário:

**Novo:**
- `dto/IndicadoresGeraisDTO.java`, `dto/ContagemDTO.java`, `dto/ErroRespostaDTO.java`
- `service/DashboardService.java`
- `controller/DashboardController.java` (endpoints em `/api/dashboard/...`)
- `templates/dashboard.html` + `static/js/dashboard.js`
- `docs/DOCUMENTACAO.md` (requisitos, documentação dos endpoints, espaço para as evidências de teste)

**Alterado:**
- `entity/Chamado.java` — adicionados `dataAbertura` e `dataFechamento`.
- `repository/ChamadoRepository.java` — métodos novos de busca usados pelos filtros.
- `service/ChamadoService.java` — preenche `dataAbertura` ao criar um chamado e `dataFechamento` quando o status vira `CONCLUIDO`.
- `controller/HomeController.java` — rota `GET /dashboard`.
- `templates/fragments/sidebar.html` — link "Dashboard" no menu.

## Como rodar

```powershell
cd thindesk
.\mvnw.cmd spring-boot:run
```

Acesse `http://localhost:8080/login` (usuário `admin`, senha `thindesk`, conforme já configurado em `application.properties`) e depois `http://localhost:8080/dashboard`.

> **Atenção com a connection string do MongoDB:** o `application.properties` do zip do professor já vem com uma connection string de um cluster Atlas em texto puro. Como a atividade pede o repositório **público** no GitHub, isso vai expor essas credenciais pra qualquer pessoa. Recomendo fortemente trocar para a sua própria connection string antes de subir pro GitHub (e, idealmente, não deixar nenhuma senha real commitada — dá pra usar variável de ambiente, como fiz no BookRats). Isso é só um aviso, não alterei esse arquivo.

## Por que o dashboard pode aparecer vazio no primeiro teste

O `chamados.json` incluído no projeto não é carregado automaticamente — a propriedade `spring.mongodb.embedded.data` no `application.properties` não faz isso (não é uma propriedade real do Spring Boot para esse fim; é resquício do código-base). Ou seja, o dashboard só mostra dados que já estiverem na coleção `chamados` do seu banco.

Pra testar os gráficos com dados de verdade, cadastre alguns chamados: pela tela (`/chamados`, se o fluxo de criação estiver ligado) ou direto pela API, por exemplo:

```
POST http://localhost:8080/api/chamados
Content-Type: application/json

{
  "descricao": "Impressora não liga",
  "status": "ABERTO",
  "tipo": "Manutenção",
  "tecnico": "Ana",
  "usuario": "Carla"
}
```

Como a entidade agora tem `dataAbertura`, criar pela API já preenche essa data automaticamente — então o gráfico "Chamados por período" só mostra dados a partir de chamados criados depois dessa atualização (os que já estavam no banco antes, sem essa data, continuam valendo para os indicadores gerais e para os outros 2 gráficos, só não entram no gráfico de período).

## Validação feita aqui

Revisei manualmente cada classe/endpoint e rodei:
- checagem de sintaxe Java em todos os arquivos (`javalang`) — sem erros;
- checagem de balanceamento de tags nos templates HTML novos/alterados — sem erros;
- conferência cruzada: todos os ids usados no `dashboard.js` existem no `dashboard.html`, e todas as URLs chamadas pelo `dashboard.js` batem com os endpoints do `DashboardController`.

Não consegui rodar `mvn spring-boot:run` de fato aqui (este ambiente bloqueia o acesso ao repositório do Maven para baixar as dependências), então o teste real de ponta a ponta — com o MongoDB de verdade — precisa ser feito no seu ambiente. Se der algum erro ao rodar, me manda a mensagem completa que eu corrijo.
