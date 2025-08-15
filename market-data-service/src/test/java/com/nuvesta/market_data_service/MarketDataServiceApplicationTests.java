package com.nuvesta.market_data_service;

import com.nuvesta.market_data_service.service.impl.StooqService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class MarketDataServiceApplicationTests {

	@MockitoBean
	private StooqService stooqService;

	@Test
	void contextLoads() {
	}

}
