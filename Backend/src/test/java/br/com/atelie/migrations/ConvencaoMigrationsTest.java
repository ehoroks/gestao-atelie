package br.com.atelie.migrations;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ConvencaoMigrationsTest {

    private static final Pattern NOME = Pattern.compile("^V(\\d+)__[a-z0-9_]+\\.sql$");

    @Test
    void migrationsAtendemAsFaixasDaTrilhaESaoUnicas() throws IOException {
        Resource[] migrations = new PathMatchingResourcePatternResolver()
                .getResources("classpath:db/migration/*.sql");
        Set<Integer> versoes = new HashSet<>();

        assertThat(migrations).isNotEmpty();
        for (Resource migration : migrations) {
            Matcher matcher = NOME.matcher(migration.getFilename());
            assertThat(matcher.matches()).as("nome da migration %s", migration.getFilename()).isTrue();
            int versao = Integer.parseInt(matcher.group(1));
            assertThat(versao == 1 || (versao >= 100 && versao <= 499))
                    .as("faixa da migration %s", migration.getFilename()).isTrue();
            assertThat(versoes.add(versao)).as("versao duplicada %d", versao).isTrue();
        }
    }
}
