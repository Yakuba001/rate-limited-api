package com.yakuba.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_snapshot")
public class PriceSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "price_snapshot_seq")
    @SequenceGenerator(name = "price_snapshot_seq", sequenceName = "price_snapshot_id_seq", allocationSize = 1)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crypto_id")
    private CryptocurrencyEntity cryptocurrency;
    private BigDecimal price;
    private String source;
    @Column(name = "fetched_at")
    private Instant fetchedAt;

    public PriceSnapshotEntity() {}

    public PriceSnapshotEntity(CryptocurrencyEntity cryptocurrency, BigDecimal price, String source, Instant fetchedAt) {
        this.cryptocurrency = cryptocurrency;
        this.price = price;
        this.source = source;
        this.fetchedAt = fetchedAt;
    }

    public Long getId() {
        return id;
    }

    public CryptocurrencyEntity getCryptocurrency() {
        return cryptocurrency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getSource() {
        return source;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setCryptocurrency(CryptocurrencyEntity cryptocurrency) {
        this.cryptocurrency = cryptocurrency;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceSnapshotEntity that)) return false;
        if (this.getId() == null || that.getId() == null) return false;
        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
