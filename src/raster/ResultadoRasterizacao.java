package raster;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ui.PixelRenderer;

/**
 * Reúne imagem, fórmulas e passos da MESMA execução matemática de um algoritmo.
 * Todo pixel é escrito por {@link PixelRenderer#drawPixel}; a tabela da interface
 * usa os passos registrados aqui, sem recalcular nada.
 *
 * @author Gabriela
 * @date 14/10/2026
 */
public final class ResultadoRasterizacao {
    // <-------------------DADOS DA EXECUÇÃO------------------->
    // Framebuffer: matriz de pixels que recebe o desenho e será exibida na interface.
    private final BufferedImage imagem;
    private final Color cor;
    // true: entrada com origem central e Y para cima; false: coluna/linha da imagem.
    private final boolean cartesiano;
    // Cada algoritmo define os títulos das suas colunas e registra uma linha por etapa.
    private List<String> colunas = Collections.emptyList();
    private final List<Passo> passos = new ArrayList<>();
    // Texto acumulado para a aba de fórmulas e dados iniciais.
    private final StringBuilder resumo = new StringBuilder();

    // <-------------------INFORMAÇÕES DE UMA ETAPA------------------->
    // A classe interna reúne a explicação completa, os pixels e os valores compactos.
    // static permite representar uma etapa sem depender de uma instância externa implícita.
    /** Um passo guarda as coordenadas geradas, inclusive as descartadas pelo recorte. */
    public static final class Passo {
        private final String etapa;
        private final String operacao;
        private final List<Point> pixels;
        private final String descricaoPixels;
        // Object permite misturar números e textos, como a decisão "E" ou "Inicial".
        // A lista começa vazia e é preenchida por resumirPasso após registrar.
        private List<Object> valores = Collections.emptyList();

        /** Valores da mesma execução, usados na tabela compacta. */
        public List<Object> getValores() { return valores; }

        private Passo(String etapa, String operacao, List<Point> pixels, String descricaoPixels) {
            this.etapa = etapa;
            this.operacao = operacao;
            this.pixels = pixels;
            this.descricaoPixels = descricaoPixels;
        }
        /** @return rótulo da etapa (ex.: número da iteração) */
        public String getEtapa() { return etapa; }
        /** @return conta ou decisão feita nesta etapa */
        public String getOperacao() { return operacao; }
        /** @return pixels do passo em texto, com [fora da área] nos descartados */
        public String getDescricaoPixels() { return descricaoPixels; }
        /** @return cópia não modificável dos pixels (coluna, linha) do passo */
        public List<Point> getPixels() {
            // Point é mutável: copiar só a lista ainda permitiria alterar seus pontos.
            // Por isso criamos também um novo Point para cada coordenada devolvida.
            List<Point> copia = new ArrayList<>();
            for (Point ponto : pixels) copia.add(new Point(ponto));
            // Impede adicionar, remover ou substituir elementos na lista devolvida.
            return Collections.unmodifiableList(copia);
        }
    }

    // <-------------------CRIAÇÃO E CONVENÇÃO DE COORDENADAS------------------->
    /**
     * Cria o resultado em coordenadas de imagem (origem no canto superior esquerdo).
     *
     * @param imagem framebuffer onde os pixels serão acesos
     * @param cor cor dos pixels desenhados
     */
    public ResultadoRasterizacao(BufferedImage imagem, Color cor) {
        // Reutiliza o construtor completo, escolhendo coordenadas de dispositivo.
        this(imagem, cor, false);
    }

    /**
     * Modo cartesiano: converte somente na escrita do pixel; contas continuam em X,Y.
     * Origem no centro, com {@code coluna = largura/2 + X} e {@code linha = altura/2 - Y}.
     *
     * @param imagem framebuffer onde os pixels serão acesos
     * @param cor cor dos pixels desenhados
     * @param cartesiano {@code true} para origem no centro e Y para cima
     */
    public ResultadoRasterizacao(BufferedImage imagem, Color cor, boolean cartesiano) {
        if (imagem == null || cor == null) throw new IllegalArgumentException("Imagem e cor são obrigatórias.");
        // Guardamos a própria imagem recebida: não criamos um segundo desenho.
        this.imagem = imagem;
        this.cor = cor;
        this.cartesiano = cartesiano;
        if (cartesiano) {
            explicar("Origem cartesiana (0,0) no centro; X à direita, Y para cima.");
            explicar("Pixel: coluna=" + imagem.getWidth()/2 + "+X; linha=" + imagem.getHeight()/2 + "-Y.");
        }
    }

    // <-------------------RESUMO E CONSULTA DOS RESULTADOS------------------->
    // Estes métodos entregam à interface os dados produzidos pelo algoritmo.
    // getImagem devolve a imagem original; getPassos bloqueia alterações na lista.
    /** Acrescenta uma linha ao resumo (valores iniciais, fórmulas, avisos). */
    public void explicar(String texto) { resumo.append(texto).append('\n'); }
    /** @return resumo da execução, uma explicação por linha */
    public String getResumo() { return resumo.toString(); }
    /** @return framebuffer com o desenho */
    public BufferedImage getImagem() { return imagem; }
    /** @return passos na ordem de execução */
    public List<Passo> getPassos() { return Collections.unmodifiableList(passos); }

    // <-------------------DADOS DA TABELA COMPACTA------------------->
    // String... aceita vários títulos: definirColunas("X", "Y", "Decisão").
    public void definirColunas(String... nomes) {
        // Copiamos os títulos e impedimos alterações externas na lista.
        colunas = Collections.unmodifiableList(new ArrayList<>(java.util.Arrays.asList(nomes)));
    }
    public List<String> getColunas() { return colunas; }

    /** Associa os valores já calculados ao último passo, sem repetir o algoritmo. */
    public void resumirPasso(Object... valores) {
        // Deve haver um passo registrado e exatamente um valor para cada coluna.
        // A coluna Etapa é acrescentada pela interface, por isso não entra nesta conta.
        if (passos.isEmpty() || valores.length != colunas.size()) {
            throw new IllegalStateException("Valores incompatíveis com as colunas do passo.");
        }
        // O índice size()-1 aponta para a etapa que acabou de ser registrada.
        // A ordem dos valores deve acompanhar a ordem definida em definirColunas.
        passos.get(passos.size() - 1).valores = Collections.unmodifiableList(
                new ArrayList<>(java.util.Arrays.asList(valores)));
    }

    // <-------------------REGISTRO E ESCRITA DOS MESMOS PIXELS------------------->
    /**
     * Recebe pares X,Y. Registra e desenha sem calcular outra versão do algoritmo.
     * Pontos fora da imagem são identificados, mas não escritos. Repetições dentro da
     * mesma chamada (simetrias) são agrupadas.
     *
     * @param etapa rótulo da etapa
     * @param operacao conta ou decisão que originou os pixels
     * @param coordenadas pares X,Y consecutivos
     */
    public void registrar(String etapa, String operacao, int... coordenadas) {
        // int... recebe uma sequência como x1,y1,x2,y2.
        // Uma quantidade ímpar deixaria o último X sem seu Y correspondente.
        if (coordenadas.length % 2 != 0) throw new IllegalArgumentException("Informe pares X,Y.");
        List<Point> pixels = new ArrayList<>();
        StringBuilder descricao = new StringBuilder();
        // Avançamos de dois em dois: cada passagem lê um par (X,Y).
        // As simetrias já vêm calculadas pelo algoritmo; aqui apenas registramos.
        for (int i = 0; i < coordenadas.length; i += 2) {
            int x = coordenadas[i];
            int y = coordenadas[i + 1];
            // Desloca a origem para o centro e inverte Y, pois as linhas crescem
            // para baixo na imagem. Ex.: imagem 100x100, (0,10) vira (50,40).
            // Quando cartesiano é false, X e Y já são coluna e linha.
            int coluna = cartesiano ? imagem.getWidth()/2 + x : x;
            int linha = cartesiano ? imagem.getHeight()/2 - y : y;
            Point ponto = new Point(coluna, linha);
            // Evita repetir um pixel nesta etapa, por exemplo nas simetrias de (0,r).
            // Um pixel que reaparece em outra etapa continua registrado naquela etapa.
            if (pixels.contains(ponto)) continue; // Eixos e diagonais podem repetir simetrias.
            pixels.add(ponto);
            // Monta a correspondência entre a entrada e o pixel para o detalhe da tabela.
            if (descricao.length() > 0) descricao.append("; ");
            if (cartesiano) descricao.append("XY=(").append(x).append(',').append(y).append(") → pixel ");
            descricao.append('(').append(coluna).append(',').append(linha).append(')');
            // Índices válidos vão de zero até largura-1 e altura-1.
            // O recorte evita escrever fora da matriz, mas não apaga o ponto do relatório.
            if (coluna >= 0 && coluna < imagem.getWidth() && linha >= 0 && linha < imagem.getHeight()) {
                PixelRenderer.drawPixel(imagem, coluna, linha, cor);
            } else {
                descricao.append(" [fora da área]");
            }
        }
        // Só depois de reunir todos os pontos criamos a etapa completa.
        // Em seguida, o algoritmo pode chamar resumirPasso com os valores da tabela.
        passos.add(new Passo(etapa, operacao, pixels, descricao.toString()));
    }

    // <-------------------LIMITES DO LABORATÓRIO------------------->
    /**
     * Evita estouro de inteiros e tabelas gigantes em chamadas diretas.
     * Coordenadas de -100.000 a 100.000 e no máximo 10.000 passos.
     */
    public static void validarReta(int x1, int y1, int x2, int y2) {
        validarCoordenada(x1); validarCoordenada(y1);
        validarCoordenada(x2); validarCoordenada(y2);
        // O maior deslocamento define a quantidade de avanços da reta.
        // O ponto inicial também será registrado: até 10.001 linhas na tabela.
        // A conversão para long ocorre antes da subtração para ampliar a faixa numérica.
        if (Math.max(Math.abs((long) x2 - x1), Math.abs((long) y2 - y1)) > 10000) {
            throw new IllegalArgumentException("A reta pode ter até 10.000 passos neste laboratório.");
        }
    }

    /** Centro de -100.000 a 100.000 e raio de 0 a 2.000. */
    public static void validarCirculo(int cx, int cy, int raio) {
        validarCoordenada(cx); validarCoordenada(cy);
        // Raio zero representa um ponto; o limite superior controla o trabalho
        // dos algoritmos. A interface pode impor limites mais restritos.
        if (raio < 0 || raio > 2000) throw new IllegalArgumentException("Raio permitido: 0 a 2.000.");
    }

    private static void validarCoordenada(int valor) {
        if (valor < -100000 || valor > 100000) {
            throw new IllegalArgumentException("Coordenadas devem estar entre -100.000 e 100.000.");
        }
    }
}