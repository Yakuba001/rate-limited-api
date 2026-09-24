package com.yakuba.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "cryptocurrency")
public class CryptocurrencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String symbol;
    private String title;
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public CryptocurrencyEntity() {}

    public CryptocurrencyEntity(String symbol, String title) {
        this.symbol = symbol;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getTitle() {
        return title;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CryptocurrencyEntity that = (CryptocurrencyEntity) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(symbol);
    }
}
