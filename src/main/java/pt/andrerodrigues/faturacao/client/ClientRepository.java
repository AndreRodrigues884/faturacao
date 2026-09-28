package pt.andrerodrigues.faturacao.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * REPOSITORY - acesso à tabela clients no PostgreSQL.
 *
 * Fala com:     a base de dados (através do Hibernate)
 * Trabalha com: Client (lê e grava entidades)
 * É usado por:  ClientService (só por ele)
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findAllByOrderByNameAsc();

    List<Client> findByNameContainingIgnoreCaseOrNifContainingOrderByNameAsc(String name, String nif);

    boolean existsByNif(String nif);

    boolean existsByNifAndIdNot(String nif, Long id);
}