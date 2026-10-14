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
    private final BufferedImage imagem;
    private final Color cor;
    private final boolean cartesiano;
    private List<String> colunas = Collections.emptyList();
    private final List<Passo> passos = new ArrayList<>();
    private final StringBuilder resumo = new StringBuilder();

    /** Um passo guarda as coordenadas geradas, inclusive as descartadas pelo recorte. */
    public static final class Passo {
        private final String etapa;
        private final String operacao;
        private final List<Point> pixels;
        private final String descricaoPixels;
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
            List<Point> copia = new ArrayList<>();
            for (Point ponto : pixels) copia.add(new Point(ponto));
            return Collections.unmodifiableList(copia);
        }
    }

    /**
     * Cria o resultado em coordenadas de imagem (origem no canto superior esquerdo).
     *
     * @param imagem framebuffer onde os pixels serão acesos
     * @param cor cor dos pixels desenhados
     */
    public ResultadoRasterizacao(BufferedImage imagem, Color cor) {
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
        this.imagem = imagem;
        this.cor = cor;
        this.cartesiano = cartesiano;
        if (cartesiano) {
            explicar("Origem cartesiana (0,0) no centro; X à direita, Y para cima.");
            explicar("Pixel: coluna=" + imagem.getWidth()/2 + "+X; linha=" + imagem.getHeight()/2 + "-Y.");
        }
    }

    /** Acrescenta uma linha ao resumo (valores iniciais, fórmulas, avisos). */
    public void explicar(String texto) { resumo.append(texto).append('\n'); }
    /** @return resumo da execução, uma explicação por linha */
    public String getResumo() { return resumo.toString(); }
    /** @return framebuffer com o desenho */
    public BufferedImage getImagem() { return imagem; }
    /** @return passos na ordem de execução */
    public List<Passo> getPassos() { return Collections.unmodifiableList(passos); }

    // <-------------------DADOS DA TABELA COMPACTA------------------->
    public void definirColunas(String... nomes) {
        colunas = Collections.unmodifiableList(new ArrayList<>(java.util.Arrays.asList(nomes)));
    }
    public List<String> getColunas() { return colunas; }

    /** Associa os valores já calculados ao último passo, sem repetir o algoritmo. */
    public void resumirPasso(Object... valores) {
        if (passos.isEmpty() || valores.length != colunas.size()) {
            throw new IllegalStateException("Valores incompatíveis com as colunas do passo.");
        }
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
        if (coordenadas.length % 2 != 0) throw new IllegalArgumentException("Informe pares X,Y.");
        List<Point> pixels = new ArrayList<>();
        StringBuilder descricao = new StringBuilder();
        for (int i = 0; i < coordenadas.length; i += 2) {
            int x = coordenadas[i];
            int y = coordenadas[i + 1];
            int coluna = cartesiano ? imagem.getWidth()/2 + x : x;
            int linha = cartesiano ? imagem.getHeight()/2 - y : y;
            Point ponto = new Point(coluna, linha);
            if (pixels.contains(ponto)) continue; // Eixos e diagonais podem repetir simetrias.
            pixels.add(ponto);
            if (descricao.length() > 0) descricao.append("; ");
            if (cartesiano) descricao.append("XY=(").append(x).append(',').append(y).append(") → pixel ");
            descricao.append('(').append(coluna).append(',').append(linha).append(')');
            if (coluna >= 0 && coluna < imagem.getWidth() && linha >= 0 && linha < imagem.getHeight()) {
                PixelRenderer.drawPixel(imagem, coluna, linha, cor);
            } else {
                descricao.append(" [fora da área]");
            }
        }
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
        if (Math.max(Math.abs((long) x2 - x1), Math.abs((long) y2 - y1)) > 10000) {
            throw new IllegalArgumentException("A reta pode ter até 10.000 passos neste laboratório.");
        }
    }

    /** Centro de -100.000 a 100.000 e raio de 0 a 2.000. */
    public static void validarCirculo(int cx, int cy, int raio) {
        validarCoordenada(cx); validarCoordenada(cy);
        if (raio < 0 || raio > 2000) throw new IllegalArgumentException("Raio permitido: 0 a 2.000.");
    }

    private static void validarCoordenada(int valor) {
        if (valor < -100000 || valor > 100000) {
            throw new IllegalArgumentException("Coordenadas devem estar entre -100.000 e 100.000.");
        }
    }
}