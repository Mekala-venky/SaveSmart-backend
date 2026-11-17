package com.SaveSmart;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full application context smoke test.
 *
 * <p>This requires a reachable MySQL instance and a JWT_SECRET environment
 * variable, so it is disabled for the default unit-test run. Enable it
 * manually after starting the database when you want an end-to-end check:</p>
 *
 * <pre>
 * mvnw test -Dtest=SaveSmartBackendApplicationTests
 * </pre>
 */
@Disabled("Requires a running MySQL instance and JWT_SECRET")
@SpringBootTest
class SaveSmartBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
