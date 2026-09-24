package com.yakuba.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.yakuba.repository.CryptoRepository;
import com.yakuba.repository.PriceSnapshotRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class CryptoAggregationServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine");

    static WireMockServer server;

    @Autowired
    CryptoRepository cryptoRepository;
    @Autowired
    PriceSnapshotRepository priceSnapshotRepository;
    @Autowired
    CryptoAggregationService cryptoAggregationService;

    @BeforeAll
    static void setUp() {
        server = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        server.resetAll();
        priceSnapshotRepository.deleteAll();
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("crypto.api.base-url", () -> server.baseUrl());
    }

    @Test
    void fetchAndSavePrice_shouldFetchAndSavePriceSuccessfully() {
        String url = "/api/v1/ticker?symbol=BTCUSDT";
        String scenarioName = "Retry Scenario";

        server.stubFor(get(urlEqualTo(url))
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("Retry 1")
                .willReturn(serviceUnavailable()));
        server.stubFor(get(urlEqualTo(url))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Retry 1")
                .willSetStateTo("Success")
                .willReturn(serviceUnavailable()));
        server.stubFor(get(urlEqualTo(url))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Success")
                .willReturn(okJson("""
                        {
                          "symbol": "BTCUSDT",
                          "price": "62150.50000000",
                          "timestamp": 1700000000000
                        }
                        """)));

        cryptoAggregationService.fetchAndSavePrice("BTCUSDT");

        var crypto = cryptoRepository.findBySymbol("BTCUSDT");
        assertThat(crypto).isPresent();

        var snapshot = priceSnapshotRepository.findAll();
        assertThat(snapshot).hasSize(1).satisfies(it ->
                assertThat(it.getFirst().getPrice()).isEqualByComparingTo("62150.50"));

        server.verify(3, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void fetchAndSavePrice_shouldThrowExceptionWhenRetryLimitExceeded() {
        String url = "/api/v1/ticker?symbol=BTCUSDT";
        String scenarioName = "Retry exceeded scenario";

        server.stubFor(get(urlEqualTo(url))
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("Retry 1")
                .willReturn(status(429)));
        server.stubFor(get(urlEqualTo(url))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Retry 1")
                .willSetStateTo("Retry 2")
                .willReturn(status(429)));
        server.stubFor(get(urlEqualTo(url))
                .inScenario(scenarioName)
                .whenScenarioStateIs("Retry 2")
                .willSetStateTo("Retry 3")
                .willReturn(status(429)));

        assertThatThrownBy(() -> cryptoAggregationService.fetchAndSavePrice("BTCUSDT")).isInstanceOf(RuntimeException.class);
    }
}
