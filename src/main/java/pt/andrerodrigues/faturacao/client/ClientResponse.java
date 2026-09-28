package pt.andrerodrigues.faturacao.client;

import java.time.Instant;

/**
 * DTO DE SAÍDA - formato do JSON devolvido pela API de clientes.
 *
 * Fala com:     Client (lê os seus dados no método from)
 * É usado por:  ClientService (cria-o a partir da entidade),
 *               ClientController (devolve-o ao cliente)
 */
public record ClientResponse(
        Long id,
        String name,
        String nif,
        String email,
        String phone,
        String address,
        String postalCode,
        String city,
        Instant createdAt,
        Instant updatedAt
) {

    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getNif(),
                client.getEmail(),
                client.getPhone(),
                client.getAddress(),
                client.getPostalCode(),
                client.getCity(),
                client.getCreatedAt(),
                client.getUpdatedAt()
        );
    }
}