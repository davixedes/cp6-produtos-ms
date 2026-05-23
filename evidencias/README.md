# Evidências

Coloque aqui o print do Zipkin com o trace completo do fluxo:

1. Span HTTP da requisição `POST /produtos/save` no `produtos-ms`
2. Span `jms.outbox.publish` do `OutBoxJob` do `produtos-ms`
3. Span `jms.produto.consume` do `ProdutoConsumer` no `vendas-ms`

Os três spans devem aparecer sob o mesmo `traceId` na timeline do Zipkin.

## Como gerar a evidência

1. Suba a infraestrutura: `cd ../../vendas-ms && docker compose up -d`
2. Suba o MySQL do produtos-ms: `cd ../produtos-ms && docker compose up -d`
3. Rode os dois serviços:
   - Terminal 1: `cd vendas-ms && ./mvnw spring-boot:run`
   - Terminal 2: `cd produtos-ms && ./mvnw spring-boot:run`
4. Acesse http://localhost:8082/produtos, faça login e cadastre um produto
5. Espere até 10s para o job da outbox publicar
6. Abra http://localhost:9411 (Zipkin), procure por `produtos-ms` em
   "Service Name" e abra o trace mais recente
7. Capture a tela mostrando os três spans encadeados e salve aqui como
   `zipkin-trace-completo.png`
