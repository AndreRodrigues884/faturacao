package pt.andrerodrigues.faturacao.client;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * ENTIDADE - representa uma linha da tabela clients.
 *
 * Fala com:     ninguém (não depende de nenhum outro ficheiro)
 * É usado por:  ClientRepository (lê/grava), ClientService (cria/altera),
 *               ClientResponse (é convertida em DTO)
 *
 * Nunca sai do backend: a API devolve sempre ClientResponse.
 */
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 9)
    private String nif;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "postal_code", length = 8)
    private String postalCode;

    @Column(length = 100)
    private String city;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Client() {
    }

    public Client(String name, String nif, String email, String phone,
                  String address, String postalCode, String city) {
        this.name = name;
        this.nif = nif;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void update(String name, String nif, String email, String phone,
                       String address, String postalCode, String city) {
        this.name = name;
        this.nif = nif;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNif() { return nif; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getPostalCode() { return postalCode; }
    public String getCity() { return city; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}