package com.yakuba.repository;

import com.yakuba.model.CryptocurrencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoRepository extends JpaRepository<CryptocurrencyEntity, Long> {

    Optional<CryptocurrencyEntity> findBySymbol(String symbol);
}
