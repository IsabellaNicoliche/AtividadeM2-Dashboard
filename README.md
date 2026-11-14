# Thindesk — Dashboard Administrativo

Sistema de chamados técnicos (Thindesk) com um Dashboard Administrativo e API própria, desenvolvido como atividade individual da disciplina Projeto e Desenvolvimento de Software Corporativo.

## Vídeo demonstrativo

<video src="docs/video-demonstrativo.mp4" controls width="700"></video>

## O que o sistema faz

- Gestão de chamados técnicos, clientes e horários de atendimento.
- Dashboard administrativo com:
    - Indicadores gerais (total, abertos, em andamento, concluídos, tempo médio de atendimento).
    - Gráfico de chamados por período.
    - Gráfico de situação dos chamados (status).
    - Ranking dos tipos de chamado mais atendidos.
    - Filtros por status, tipo, técnico e período.
- API REST própria (`/api/dashboard/...`, `/api/chamados`, `/api/clientes`) que alimenta o dashboard.

## Stack

- Java 17, Spring Boot 3.4
- Spring Security (login)
- Spring Data MongoDB (MongoDB Atlas)
- Thymeleaf + Bootstrap 5
- Chart.js (gráficos, via CDN)

## Como rodar

```bash
git clone https://github.com/IsabellaNicoliche/AtividadeM2-Dashboard.git
cd AtividadeM2-Dashboard
./mvnw spring-boot:run
```

Acesse `http://localhost:8080/login` (usuário `admin`, senha `thindesk`) e depois `http://localhost:8080/dashboard`.

> É necessário ter uma connection string válida do MongoDB Atlas configurada em `src/main/resources/application.properties` (propriedade `spring.data.mongodb.uri`).

## Documentação completa

- [Requisitos funcionais/não funcionais, documentação dos endpoints e evidências de teste](docs/DOCUMENTACAO.md)
- [O que foi adicionado nesta atividade](docs/LEIA-ME-DASHBOARD.md)