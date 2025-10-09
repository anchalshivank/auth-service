package com.renter.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.renter.auth.service.KeycloakService;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@MockBean
	private KeycloakService keycloakService;


	@Test
	void contextLoads() {
	}

}
