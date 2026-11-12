package br.com.atelie.migrations;

import br.com.atelie.infra.teste.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Garante que as migrations criam o schema completo que sera aplicado no Supabase. */
class SchemaCompletoIT extends IntegrationTestBase {

    private static final List<String> TABELAS_ESPERADAS = List.of(
            "categorias", "item_estoque", "produtos", "insumos", "movimentacoes_estoque",
            "usuarios", "clientes", "vendas", "itens_venda",
            "encomendas", "encomenda_insumos", "flyway_schema_history");

    @Test
    void schemaPublicoContemTodasAsTabelasDoProjeto() {
        List<String> tabelas = jdbc.queryForList(
                "select tablename from pg_tables where schemaname = 'public'", String.class);

        assertThat(tabelas).containsAll(TABELAS_ESPERADAS);
    }

    @Test
    void produtoReferenciaCategoriaEItemEstoque() {
        List<String> tabelasReferenciadas = jdbc.queryForList("""
                select r.relname
                from pg_constraint c
                join pg_class t on t.oid = c.conrelid
                join pg_class r on r.oid = c.confrelid
                where t.relname = 'produtos' and c.contype = 'f'
                """, String.class);

        assertThat(tabelasReferenciadas).contains("categorias", "item_estoque");
    }
}
