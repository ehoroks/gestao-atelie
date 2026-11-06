package br.com.atelie.migrations;

import br.com.atelie.infra.teste.IntegrationTestBase;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationsIT extends IntegrationTestBase {

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Test
    void todasAsMigrationsDoContextoEstaoAplicadas() {
        assertThat(flyway.info().all()).isNotEmpty()
                .allSatisfy(migration -> assertThat(migration.getState()).isEqualTo(MigrationState.SUCCESS));
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void migrationsAplicamDesdeUmSchemaVazio() {
        JdbcTemplate jdbcNovoSchema = new JdbcTemplate(dataSource);
        jdbcNovoSchema.execute("DROP SCHEMA IF EXISTS teste_do_zero CASCADE");

        MigrateResult resultado = Flyway.configure()
                .dataSource(dataSource)
                .schemas("teste_do_zero")
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try {
            assertThat(resultado.success).isTrue();
            assertThat(resultado.migrationsExecuted).isEqualTo(flyway.info().all().length);
        } finally {
            jdbcNovoSchema.execute("DROP SCHEMA IF EXISTS teste_do_zero CASCADE");
        }
    }

    @Test
    void schemaIncluiTabelasDeEncomendaEEstoque() {
        Integer quantidade = jdbc.queryForObject("""
                select count(*) from information_schema.tables
                where table_schema = 'public'
                  and table_name in ('encomendas', 'encomenda_insumos', 'clientes',
                                     'item_estoque', 'produtos', 'insumos', 'movimentacoes_estoque')
                """, Integer.class);
        assertThat(quantidade).isEqualTo(7);
    }
}
