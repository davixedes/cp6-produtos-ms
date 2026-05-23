package br.com.fiap.produtosms.service;

import br.com.fiap.produtosms.dto.ProdutoMessageOutput;
import br.com.fiap.produtosms.entities.OutboxEvent;
import br.com.fiap.produtosms.entities.Produto;
import br.com.fiap.produtosms.repositories.OutboxEventRepository;
import br.com.fiap.produtosms.repositories.ProdutoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
class ProdutoServiceImpl implements ProdutoService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoServiceImpl.class);

    private final ProdutoRepository repository;

    private final OutboxEventRepository outboxEventRepository;

    private final ObjectMapper mapper;

    private final Counter produtosSalvosCounter;

    private static String PRODUTO = "PRODUTO";

    private static String QUEUE = "produto.queue";

    public ProdutoServiceImpl(ProdutoRepository repository, OutboxEventRepository outboxEventRepository, ObjectMapper mapper, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.outboxEventRepository = outboxEventRepository;
        this.mapper = mapper;
        this.produtosSalvosCounter = Counter.builder("produtos.salvos")
                .description("Total de produtos salvos no catalogo (criacoes e atualizacoes)")
                .register(meterRegistry);
    }

    @Override
    public Produto findByCodigo(UUID codigo) {
        logger.debug("Buscando produto codigo={}", codigo);
        return this.repository.findById(codigo)
                .orElseThrow(() -> new NoSuchElementException("Produto nao encontrado: " + codigo));
    }

    @Override
    public List<Produto> findAll() {
        logger.debug("Listando todos os produtos");
        return this.repository.findAll();
    }

    @Override
    @Transactional
    public void saveOrUpdate(Produto produto) {
        logger.info("Salvando produto nome={} categoria={}", produto.getNome(), produto.getCategoria());
        final Produto saved = this.repository.save(produto);
        try {
            final String message = mapProdutoMessage(saved);
            this.outboxEventRepository.save(new OutboxEvent(saved.getCodigo().toString(), PRODUTO, QUEUE, message));
            this.produtosSalvosCounter.increment();
            logger.info("Evento outbox criado produtoCodigo={} queue={}", saved.getCodigo(), QUEUE);
        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar produto para outbox produtoCodigo={}", saved.getCodigo(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void deleteByCodigo(UUID codigo) {
        logger.info("Excluindo produto codigo={}", codigo);
        this.repository.deleteById(codigo);
    }

    private String mapProdutoMessage(Produto produto) throws JsonProcessingException {
        ProdutoMessageOutput message = new ProdutoMessageOutput(produto.getCodigo(), produto.getNome());
        return this.mapper.writeValueAsString(message);
    }

}
