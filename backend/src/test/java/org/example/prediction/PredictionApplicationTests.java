package org.example.prediction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ImportAutoConfiguration
class PredictionApplicationTests {

	@Test
	void contextLoads() {
	}

}
