package br.com.atelie.catalogo;

import br.com.atelie.catalogo.dto.*;
import br.com.atelie.comum.RecursoJaExistenteException;
import br.com.atelie.comum.RecursoNaoEncontradoException;
import br.com.atelie.comum.RegraNegocioException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/** Casos de uso RF02-RF07, preservando entidades e migrations existentes. */
@Service
public class ServicoCatalogo {
    private final ProdutoRepository produtos;
    private final CategoriaRepository categorias;
    private final InsumoRepository insumos;

    public ServicoCatalogo(ProdutoRepository produtos, CategoriaRepository categorias,
                           InsumoRepository insumos) {
        this.produtos = produtos;
        this.categorias = categorias;
        this.insumos = insumos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categorias.findAll(Sort.by("nome")).stream().map(CategoriaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarCategoria(Long id) {
        return CategoriaResponse.de(obterCategoria(id));
    }

    @Transactional
    public CategoriaResponse criarCategoria(CategoriaRequest request) {
        if (categorias.existsByNomeIgnoreCase(request.nome().trim())) {
            throw new RecursoJaExistenteException("Categoria", request.nome());
        }
        return CategoriaResponse.de(categorias.saveAndFlush(new Categoria(request.nome())));
    }

    @Transactional
    public CategoriaResponse editarCategoria(Long id, CategoriaRequest request) {
        Categoria categoria = obterCategoria(id);
        if (!categoria.getNome().equalsIgnoreCase(request.nome().trim())
                && categorias.existsByNomeIgnoreCase(request.nome().trim())) {
            throw new RecursoJaExistenteException("Categoria", request.nome());
        }
        categoria.renomear(request.nome());
        categorias.flush();
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void excluirCategoria(Long id) {
        Categoria categoria = obterCategoria(id);
        if (produtos.findAll().stream().anyMatch(p -> p.getCategoria() != null
                && id.equals(p.getCategoria().getId()))) {
            throw new RegraNegocioException("nao e permitido excluir categoria com produtos");
        }
        categorias.delete(categoria);
        categorias.flush();
    }

    @Transactional
    public ProdutoResponse criarProduto(ProdutoCadastroRequest request) {
        Categoria categoria = categoriaOpcional(request.categoriaId());
        Produto produto = new Produto(request.nome(), request.unidadeMedida(),
                request.estoqueMinimo(), categoria, request.preco());
        produto.atualizarDados(request.nome(), categoria, request.tamanho(), request.cor(),
                request.preco(), request.descricao(), request.foto());
        return ProdutoResponse.de(produtos.saveAndFlush(produto));
    }

    @Transactional
    public ProdutoResponse editarProduto(Long id, ProdutoEdicaoRequest request) {
        Produto produto = obterProduto(id);
        produto.atualizarDados(request.nome(), categoriaOpcional(request.categoriaId()),
                request.tamanho(), request.cor(), request.preco(), request.descricao(), request.foto());
        produtos.flush();
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public ProdutoResponse desativarProduto(Long id) {
        Produto produto = obterProduto(id);
        produto.desativar();
        produtos.flush();
        return ProdutoResponse.de(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarProduto(Long id) {
        return ProdutoResponse.de(obterProduto(id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarProdutos(String nome, Long categoriaId, Boolean ativo,
                                                String cor, String tamanho, BigDecimal precoMin,
                                                BigDecimal precoMax) {
        if (precoMin != null && precoMin.signum() < 0 || precoMax != null && precoMax.signum() < 0
                || precoMin != null && precoMax != null && precoMin.compareTo(precoMax) > 0) {
            throw new RegraNegocioException("intervalo de preco invalido");
        }
        return produtos.findAll().stream()
                .filter(p -> contem(p.getNome(), nome))
                .filter(p -> categoriaId == null || p.getCategoria() != null
                        && categoriaId.equals(p.getCategoria().getId()))
                .filter(p -> ativo == null || p.isAtivo() == ativo)
                .filter(p -> contem(p.getCor(), cor))
                .filter(p -> contem(p.getTamanho(), tamanho))
                .filter(p -> precoMin == null || p.getPreco().compareTo(precoMin) >= 0)
                .filter(p -> precoMax == null || p.getPreco().compareTo(precoMax) <= 0)
                .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getNome(), b.getNome()))
                .map(ProdutoResponse::de).toList();
    }

    @Transactional
    public InsumoResponse criarInsumo(InsumoCadastroRequest request) {
        Insumo insumo = new Insumo(request.nome(), request.unidadeMedida(),
                request.estoqueMinimo(), request.custoUnitario());
        return InsumoResponse.de(insumos.saveAndFlush(insumo));
    }

    @Transactional(readOnly = true)
    public List<InsumoResponse> listarInsumos(String nome) {
        return insumos.findAll().stream().filter(i -> contem(i.getNome(), nome))
                .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getNome(), b.getNome()))
                .map(InsumoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public InsumoResponse buscarInsumo(Long id) { return InsumoResponse.de(obterInsumo(id)); }

    @Transactional
    public InsumoResponse atualizarCusto(Long id, AtualizarCustoRequest request) {
        Insumo insumo = obterInsumo(id);
        insumo.atualizarCustoUnitario(request.custoUnitario());
        insumos.flush();
        return InsumoResponse.de(insumo);
    }

    private Categoria obterCategoria(Long id) {
        return categorias.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private Categoria categoriaOpcional(Long id) { return id == null ? null : obterCategoria(id); }

    private Produto obterProduto(Long id) {
        return produtos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    private Insumo obterInsumo(Long id) {
        return insumos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Insumo", id));
    }

    private static boolean contem(String texto, String termo) {
        return termo == null || termo.isBlank() || texto != null
                && texto.toLowerCase(Locale.ROOT).contains(termo.trim().toLowerCase(Locale.ROOT));
    }
}
