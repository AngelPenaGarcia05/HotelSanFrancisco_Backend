package com.sanfrancisco.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// La clave JWT ya no tiene default (falla el arranque sin ella); se inyecta una de prueba.
@SpringBootTest(properties = "spring.security.jwt.secret-key=clave_de_prueba_solo_para_tests_1234567890")
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
