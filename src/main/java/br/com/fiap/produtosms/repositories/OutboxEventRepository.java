package br.com.fiap.produtosms.repositories;

import br.com.fiap.produtosms.entities.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query(nativeQuery = true, value = "select distinct o.* from outbox_event o where o.status = 'PENDENTE' LIMIT 10 ")
    List<OutboxEvent> findDistinctPendentes();

}
