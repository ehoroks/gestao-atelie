package br.com.atelie.infra.teste;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

/** Base D9: um PostgreSQL real por processo e limpeza de dados antes de cada IT. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Tag("integration")
public abstract class IntegrationTestBase {

    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("atelie_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        try {
            POSTGRES.start();
        } catch (RuntimeException falha) {
            throw new IllegalStateException(
                    "Os testes *IT precisam de um Docker em execucao: o Testcontainers sobe um "
                            + "PostgreSQL real. Inicie o Docker e rode 'mvn verify' de novo, ou use "
                            + "'mvn test' para rodar apenas os testes unitarios e os de H2.", falha);
        }
    }

    @DynamicPropertySource
    static void configurarPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        List<String> tabelas = jdbc.queryForList(
                "select tablename from pg_tables where schemaname = 'public' "
                        + "and tablename <> 'flyway_schema_history'", String.class);
        if (!tabelas.isEmpty()) {
            String nomes = String.join(", ", tabelas.stream().map(nome -> "\"" + nome + "\"").toList());
            jdbc.execute("TRUNCATE TABLE " + nomes + " RESTART IDENTITY CASCADE");
        }
    }
}
