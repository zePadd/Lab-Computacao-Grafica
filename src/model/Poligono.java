package model;

import transformer.MatrizUtils;

/**
 * Polígono imutável representado por colunas (x, y, 1), conforme os exemplos de sala.
 *
 * @author Suelle Maciel
 * @date 02/11/2026
 */
public final class Poligono {
    private final String nome;
    private final double[][] pontos;

    /**
     * Guarda uma cópia dos vértices homogêneos. Exige pelo menos três vértices com w = 1.
     *
     * @author Suelle Maciel
     * @date 02/11/2026
     * @param nome nome descritivo da forma ou operação
     * @param pontos matriz homogênea de vértices em colunas
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public Poligono(String nome, double[][] pontos) {
        MatrizUtils.validar(pontos);
        if (pontos.length != 3 || pontos[0].length < 3)
            throw new IllegalArgumentException("Use uma matriz 3 x N, com N >= 3.");
        for (double w : pontos[2]) if (w != 1) throw new IllegalArgumentException("Cada ponto deve ter w = 1.");
        this.nome = nome;
        this.pontos = MatrizUtils.copiar(pontos);
    }

    // <-------------------FORMAS COM A NA ORIGEM------------------->
    /**
     * Cria triângulo equilátero, quadrado ou seta côncava, sempre com A = (0,0).
     *
     * @author Suelle Maciel
     * @date 02/11/2026
     * @param tipo índice da opção descrita neste método
     * @return resultado da operação descrita
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static Poligono criar(int tipo) {
        if (tipo == 0) return new Poligono("Triângulo equilátero", new double[][] {
            {0, 4, 2}, {0, 0, 2 * Math.sqrt(3)}, {1, 1, 1}});
        if (tipo == 1) return new Poligono("Quadrado", new double[][] {
            {0, 4, 4, 0}, {0, 0, 4, 4}, {1, 1, 1, 1}});
        if (tipo == 2) return new Poligono("Seta côncava", new double[][] {
            {0, 3, 3, 5, 3, 3, 0}, {0, 0, -1, 1, 3, 2, 2}, {1, 1, 1, 1, 1, 1, 1}});
        throw new IllegalArgumentException("Polígono desconhecido.");
    }

    /**
     * Devolve uma cópia dos vértices, sem expor o estado interno.
     *
     * @author Suelle Ferreira Maciel
     * @date 02/11/2006
     * @return cópia ou resultado dos vértices homogêneos em colunas
     */
    public double[][] getPontos() { return MatrizUtils.copiar(pontos); }

    /**
     * Obtém o nome da forma selecionada.
     *
     * @author Suelle Maciel
     * @date 02/11/2026
     * @return nome da forma
     */
    public String getNome() { return nome; }
}
