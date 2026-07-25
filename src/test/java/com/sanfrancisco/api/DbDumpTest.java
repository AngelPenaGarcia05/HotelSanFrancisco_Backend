package com.sanfrancisco.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest(properties = "spring.security.jwt.secret-key=clave_de_prueba_solo_para_tests_1234567890")
class DbDumpTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void dumpTransactions() {
        System.out.println("=== DUMPING LAST 5 TRANSACTIONS ===");
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT transaccion_id, purchase_number, monto, estado, descripcion_estado, marca_tarjeta FROM transacciones_pasarela ORDER BY transaccion_id DESC LIMIT 5"
            );
            for (Map<String, Object> row : rows) {
                System.out.println(row);
            }
        } catch (Exception e) {
            System.err.println("Error querying DB: " + e.getMessage());
        }
        System.out.println("===================================");
    }
}
