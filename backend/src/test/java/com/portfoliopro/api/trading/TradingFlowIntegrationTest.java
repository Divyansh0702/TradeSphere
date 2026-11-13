package com.portfoliopro.api.trading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfoliopro.api.market.MarketSimulationService;
import com.portfoliopro.api.market.Stock;
import com.portfoliopro.api.market.StockRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TradingFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private MarketSimulationService marketSimulationService;

        @Test
        void tradePreflightAllowsIdempotencyHeader() throws Exception {
                MvcResult result = mockMvc.perform(options("/api/orders/buy")
                                                .header("Origin", "http://localhost:5173")
                                                .header("Access-Control-Request-Method", "POST")
                                                .header("Access-Control-Request-Headers", "content-type,idempotency-key"))
                                .andExpect(status().isOk())
                                .andReturn();
                assertEquals("http://localhost:5173", result.getResponse().getHeader("Access-Control-Allow-Origin"));
                assertTrue(result.getResponse().getHeader("Access-Control-Allow-Headers").toLowerCase().contains("idempotency-key"));
                assertEquals("true", result.getResponse().getHeader("Access-Control-Allow-Credentials"));
        }

    @Test
    void catalogSearchDetailAndPriceHistoryAreAvailable() throws Exception {
        Cookie cookie = registerUser();
        MvcResult allStocks = mockMvc.perform(get("/api/stocks").cookie(cookie))
                .andExpect(status().isOk()).andReturn();
        assertEquals(15, objectMapper.readTree(allStocks.getResponse().getContentAsString()).size());

        mockMvc.perform(get("/api/stocks").param("query", "tcs").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("TCS"));
        mockMvc.perform(get("/api/stocks/RELIANCE").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exchange").value("NSE"))
                .andExpect(jsonPath("$.currency").value("INR"));

        marketSimulationService.updatePrices();
        mockMvc.perform(get("/api/stocks/RELIANCE/history").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").isNumber())
                .andExpect(jsonPath("$[0].currency").value("INR"))
                .andExpect(jsonPath("$[1].price").isNumber());
    }

    @Test
    void walletWatchlistTradeIdempotencyHoldingsAndPortfolioAreConsistent() throws Exception {
        Cookie ownerCookie = registerUser();
        Cookie otherCookie = registerUser();
        Stock stock = stockRepository.findBySymbolIgnoreCase("TCS").orElseThrow();
        BigDecimal firstPrice = stock.getCurrentPrice();

        mockMvc.perform(post("/api/wallet/top-ups").cookie(ownerCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"amount\":1000000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.simulated").value(true));

        mockMvc.perform(post("/api/watchlists").cookie(ownerCookie))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/watchlists/me/stocks/TCS").cookie(ownerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stocks.length()").value(1));
        mockMvc.perform(post("/api/watchlists/me/stocks/TCS").cookie(ownerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stocks.length()").value(1));
        mockMvc.perform(get("/api/watchlists/me").cookie(otherCookie))
                .andExpect(status().isNotFound());

        MvcResult firstOrderResult = mockMvc.perform(post("/api/orders/buy")
                        .cookie(ownerCookie)
                        .header("Idempotency-Key", "tcs-buy-001")
                        .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"symbol\":\"TCS\",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("INR")).andReturn();
        TradeOrderResponse firstOrder = objectMapper.readValue(
                firstOrderResult.getResponse().getContentAsString(), TradeOrderResponse.class);

        MvcResult replayResult = mockMvc.perform(post("/api/orders/buy")
                        .cookie(ownerCookie)
                        .header("Idempotency-Key", "tcs-buy-001")
                        .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"symbol\":\"TCS\",\"quantity\":2}"))
                .andExpect(status().isCreated()).andReturn();
        TradeOrderResponse replay = objectMapper.readValue(replayResult.getResponse().getContentAsString(),
                TradeOrderResponse.class);
        assertEquals(firstOrder.id(), replay.id());

        mockMvc.perform(post("/api/orders/buy")
                        .cookie(ownerCookie)
                        .header("Idempotency-Key", "tcs-buy-001")
                        .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"symbol\":\"TCS\",\"quantity\":1}"))
                .andExpect(status().isConflict());

        Stock refreshedStock = stockRepository.findBySymbolIgnoreCase("TCS").orElseThrow();
        refreshedStock.updatePrice(firstPrice.add(new BigDecimal("100.00")));
        stockRepository.saveAndFlush(refreshedStock);
        BigDecimal secondPrice = refreshedStock.getCurrentPrice();

        mockMvc.perform(post("/api/orders/buy")
                        .cookie(ownerCookie)
                        .header("Idempotency-Key", "tcs-buy-002")
                        .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"symbol\":\"TCS\",\"quantity\":1}"))
                .andExpect(status().isCreated());

        BigDecimal averagePrice = firstPrice.multiply(BigDecimal.valueOf(2)).add(secondPrice)
                .divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP);
        BigDecimal expectedRealized = secondPrice.subtract(averagePrice).setScale(2, RoundingMode.HALF_UP);
        mockMvc.perform(post("/api/orders/sell")
                        .cookie(ownerCookie)
                        .header("Idempotency-Key", "tcs-sell-001")
                        .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"symbol\":\"TCS\",\"quantity\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.realizedPnl").value(expectedRealized.doubleValue()));

        mockMvc.perform(post("/api/orders/sell")
                        .cookie(ownerCookie)
                        .header("Idempotency-Key", "tcs-sell-too-many")
                        .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"symbol\":\"TCS\",\"quantity\":3}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/portfolio/holdings").cookie(ownerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].currency").value("INR"))
                .andExpect(jsonPath("$[0].averagePurchasePrice").value(averagePrice.doubleValue()));
        mockMvc.perform(get("/api/portfolio/summary").cookie(ownerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realizedPnl").value(expectedRealized.doubleValue()))
                .andExpect(jsonPath("$.currency").value("INR"));
        mockMvc.perform(get("/api/orders").cookie(ownerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        mockMvc.perform(get("/api/orders").cookie(otherCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/portfolio/holdings").cookie(otherCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(delete("/api/watchlists/me/stocks/TCS").cookie(ownerCookie))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/wallet").cookie(ownerCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").isNumber());
    }

    @Test
    void insufficientFundsAndInvalidQuantitiesDoNotCreateOrders() throws Exception {
        Cookie cookie = registerUser();
        mockMvc.perform(post("/api/orders/buy")
                        .cookie(cookie)
                        .header("Idempotency-Key", "no-funds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"RELIANCE\",\"quantity\":1}"))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/orders/buy")
                        .cookie(cookie)
                        .header("Idempotency-Key", "invalid-quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"RELIANCE\",\"quantity\":0}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/orders").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Cookie registerUser() throws Exception {
        String email = "phase2-" + UUID.randomUUID() + "@example.com";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "name", "Phase Two User", "email", email, "password", "secure-pass-123"))))
                .andExpect(status().isCreated()).andReturn();
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        if (setCookie == null || !setCookie.contains("HttpOnly")) {
            throw new AssertionError("Expected an HttpOnly authentication cookie");
        }
        String token = setCookie.substring(setCookie.indexOf('=') + 1, setCookie.indexOf(';'));
        return new Cookie("PORTFOLIOPRO_TOKEN", token);
    }
}