# produtos-ms

Microsserviço de gestão do catálogo de produtos. Espelha o padrão arquitetural do
`vendas-ms`: Spring Boot 3.4 + Java 21 + MySQL + Flyway + ActiveMQ + OAuth2 GitHub.

## Funcionalidades

### CP4 — Gestão do catálogo

- Listagem de produtos com botão de cadastro e link para detalhe
- Cadastro e edição via formulário único (rota `/produtos/detalhe/{codigo}` aceita
  códigos inexistentes e exibe formulário em branco com o código já preenchido,
  igual ao fluxo de cadastro de cliente do `vendas-ms`)
- Exclusão com confirmação no frontend (sem rota dedicada de confirmação)
- Internacionalização PT/EN
- Login via OAuth2 GitHub
- Rota de listagem aberta a qualquer autenticado; rotas de escrita protegidas
  pela role `ROLE_PRODUTO`

### CP5 — Observabilidade e Outbox Pattern

- Logs SLF4J com placeholders `{}` e `traceId`/`spanId` injetados via MDC pelo
  Micrometer Tracing
- `/actuator/metrics` exposto, incluindo a métrica customizada `produtos.salvos`
  (Counter incrementado a cada produto persistido)
- Rastreamento distribuído com amostragem 100% e exportação para Zipkin
- Outbox Pattern: ao salvar um produto, um `OutboxEvent` é criado na mesma
  transação. O job `OutBoxJob` publica os eventos pendentes em `produto.queue`
  com injeção de headers B3 nas propriedades da mensagem JMS

## Pré-requisitos

- Java 21
- Docker e Docker Compose
- O `vendas-ms` precisa estar com o `compose.yaml` rodando — esse compose sobe
  ActiveMQ, Zipkin e demais infraestruturas compartilhadas

## Variáveis de ambiente

```bash
export OUATH_PRODUTOS_MS_CLIENT_ID_GIT=<client_id_do_seu_app_github>
export OUATH_PRODUTOS_MS_SECRET_ID_GIT=<client_secret_do_seu_app_github>
```

Crie um OAuth App em https://github.com/settings/developers com
`Authorization callback URL = http://localhost:8082/login/oauth2/code/github`.

## Como executar

```bash
# 1. Suba a infraestrutura compartilhada (a partir do diretorio do vendas-ms)
cd ../vendas-ms
docker compose up -d

# 2. Suba o MySQL especifico do produtos-ms
cd ../produtos-ms
docker compose up -d

# 3. Rode a aplicacao
./mvnw spring-boot:run
```

A aplicação fica em http://localhost:8082.

## Endpoints principais

| Rota | Descrição |
|---|---|
| `GET /` | Página inicial |
| `GET /produtos` | Listagem |
| `GET /produtos/novo` | Formulário em branco |
| `GET /produtos/detalhe/{codigo}` | Detalhe (criar ou editar) |
| `POST /produtos/save` | Persiste produto e cria OutboxEvent |
| `POST /produtos/delete/{codigo}` | Exclui produto |
| `GET /actuator/health` | Health check |
| `GET /actuator/metrics/produtos.salvos` | Métrica customizada |

## Banco de dados

- MySQL 8.0 em `localhost:3307`, schema `produtosdb`
- Migrações sequenciais em `src/main/resources/db/migration/`
- `spring.jpa.hibernate.ddl-auto=none` — schema gerenciado exclusivamente
  pelo Flyway

## Observabilidade

- Zipkin: http://localhost:9411 (do compose do `vendas-ms`)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Spring Boot Admin: http://localhost:8081

## Evidência de rastreabilidade

O arquivo [`evidencias/zipkin-trace-completo.png`](evidencias/zipkin-trace-completo.png)
mostra um trace de 12 spans capturado no Zipkin (traceId
`6a110530124be5864f0cdc06cb411617`), atravessando os dois serviços sob o
mesmo identificador:

- `produtos-ms: task out-box-job.produtos-pendentes` — execução do job
  agendado que lê os eventos pendentes do Outbox
- `produtos-ms: jms.outbox.publish` + `produtos-ms: produto.queue publish` —
  publicação da mensagem na fila `produto.queue` com injeção de headers B3
  nas propriedades da mensagem JMS
- `vendas-ms: produto.queue process` + `vendas-ms: jms.produto.consume` —
  consumo da mensagem, com restauração do contexto de trace a partir dos
  headers B3, comprovando a propagação distribuída via JMS
