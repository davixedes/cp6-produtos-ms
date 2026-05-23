package br.com.fiap.produtosms.controller;

import br.com.fiap.produtosms.dto.ProdutoDto;
import br.com.fiap.produtosms.entities.Produto;
import br.com.fiap.produtosms.service.ProdutoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@Controller
@RequestMapping("/produtos")
public class ProdutoController extends CommonController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("produtos", ProdutoDto.from(this.produtoService.findAll()));
        return "produtos";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", ProdutoDto.empty(null));
        return "detalhe-produto";
    }

    @GetMapping("/detalhe/{codigo}")
    public String detalhe(@PathVariable("codigo") UUID codigo, Model model) {
        ProdutoDto produtoDto;
        try {
            Produto produto = this.produtoService.findByCodigo(codigo);
            produtoDto = ProdutoDto.from(produto);
        } catch (NoSuchElementException e) {
            produtoDto = ProdutoDto.empty(codigo);
        }
        model.addAttribute("produto", produtoDto);
        return "detalhe-produto";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ProdutoDto produto) {
        this.produtoService.saveOrUpdate(produto.toEntity());
        return "redirect:/produtos";
    }

    @PostMapping("/delete/{codigo}")
    public String delete(@PathVariable("codigo") UUID codigo) {
        this.produtoService.deleteByCodigo(codigo);
        return "redirect:/produtos";
    }

}
