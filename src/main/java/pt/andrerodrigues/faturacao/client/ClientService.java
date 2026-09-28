package pt.andrerodrigues.faturacao.client;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.andrerodrigues.faturacao.common.DuplicateResourceException;
import pt.andrerodrigues.faturacao.common.ResourceNotFoundException;

import java.util.List;

/**
 * SERVICE - regras de negócio dos clientes.
 *
 * Fala com:     ClientRepository (para ler e gravar)
 * Usa:          Client (entidade), ClientRequest (dados recebidos),
 *               ClientResponse (converte a entidade antes de devolver)
 * Lança:        ResourceNotFoundException, DuplicateResourceException (pasta common)
 * É usado por:  ClientController
 */
@Service
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public List<ClientResponse> findAll(String search) {
        List<Client> clients;

        if (search == null || search.isBlank()) {
            clients = repository.findAllByOrderByNameAsc();
        } else {
            String term = search.trim();
            clients = repository.findByNameContainingIgnoreCaseOrNifContainingOrderByNameAsc(term, term);
        }

        return clients.stream()
                .map(ClientResponse::from)
                .toList();
    }

    public ClientResponse findById(Long id) {
        return ClientResponse.from(getOrThrow(id));
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        String nif = normalizeNif(request.nif());

        if (repository.existsByNif(nif)) {
            throw new DuplicateResourceException("Já existe um cliente com o NIF " + nif);
        }

        Client client = new Client(
                request.name().trim(),
                nif,
                blankToNull(request.email()),
                blankToNull(request.phone()),
                blankToNull(request.address()),
                blankToNull(request.postalCode()),
                blankToNull(request.city())
        );

        return ClientResponse.from(repository.save(client));
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = getOrThrow(id);
        String nif = normalizeNif(request.nif());

        if (repository.existsByNifAndIdNot(nif, id)) {
            throw new DuplicateResourceException("Já existe um cliente com o NIF " + nif);
        }

        client.update(
                request.name().trim(),
                nif,
                blankToNull(request.email()),
                blankToNull(request.phone()),
                blankToNull(request.address()),
                blankToNull(request.postalCode()),
                blankToNull(request.city())
        );

        return ClientResponse.from(client);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Client getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente " + id + " não encontrado"));
    }

    private static String normalizeNif(String nif) {
        return nif.replaceAll("\\s", "");
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}