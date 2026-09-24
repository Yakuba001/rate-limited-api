package com.yakuba.service;

import com.yakuba.client.TickerClient;
import com.yakuba.model.CryptocurrencyEntity;
import com.yakuba.model.PriceSnapshotEntity;
import com.yakuba.model.TickerResponseDto;
import com.yakuba.repository.CryptoRepository;
import com.yakuba.repository.PriceSnapshotRepository;
import org.springframework.stereotype.Service;

@Service
public class CryptoAggregationService {

    private final CryptoRepository cryptoRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final TickerClient tickerClient;

    public CryptoAggregationService(CryptoRepository cryptoRepository,
                                    PriceSnapshotRepository priceSnapshotRepository,
                                    TickerClient tickerClient) {
        this.cryptoRepository = cryptoRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.tickerClient = tickerClient;
    }

    public void fetchAndSavePrice(String symbol) {
        CryptocurrencyEntity cryptoEntity = cryptoRepository.findBySymbol(symbol)
                .orElseGet(() -> cryptoRepository.save(new CryptocurrencyEntity(symbol, symbol)));

        TickerResponseDto dto = tickerClient.fetchTicker(symbol);

        PriceSnapshotEntity priceEntity =
                new PriceSnapshotEntity(cryptoEntity, dto.price(), "BINANCE", dto.toInstant());
        priceSnapshotRepository.save(priceEntity);
    }
}
