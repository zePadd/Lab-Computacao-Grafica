package model;

import java.util.ArrayList;
import java.util.List;
import transformer.MatrizUtils;

/**
 * Mantém o polígono original e o histórico de matrizes; desfazer restaura estados sem calcular inversas.
 *
 * @author José Araújo Agra Neto
 * @date 02/11/2026
 */
public final class EstadoTransformacoes {
    private Poligono original = Poligono.criar(1);
    private final List<double[][]> matrizes = new ArrayList<>();
    private final List<String> nomes = new ArrayList<>();

    /**
     * Inicializa a composição identidade.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     */
    public EstadoTransformacoes() { reiniciar(); }

    /**
     * Troca a forma e inicia um histórico independente.
     *
     * @author José Araújo Agra Neto
     * @date
     * @param tipo índice da opção descrita neste método
     */
    public void selecionar(int tipo) { original = Poligono.criar(tipo); reiniciar(); }

    /**
     * Restaura os vértices originais e limpa o histórico.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     */
    public void reiniciar() { matrizes.clear(); nomes.clear(); matrizes.add(MatrizUtils.identidade()); }

    /**
     * Aplica uma transformação à esquerda da composição atual. Rejeita resultados fora do limite didático antes de alterar o estado.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param m matriz de transformação
     * @param nome nome descritivo da forma ou operação
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public void aplicar(double[][] m, String nome) {
        MatrizUtils.validar(m);
        if (m.length != 3 || m[0].length != 3 || m[2][0] != 0 || m[2][1] != 0 || m[2][2] != 1)
            throw new IllegalArgumentException("A transformação deve ser afim 3 x 3.");
        if (nomes.size() >= 100) throw new IllegalArgumentException("Limite de 100 operações. Restaure ou desfaça.");
        double[][] nova = MatrizUtils.multiplicar(m, getMatriz());
        double[][] pontos = MatrizUtils.multiplicar(nova, original.getPontos());
        for (int i = 0; i < 2; i++) for (double valor : pontos[i])
            if (Math.abs(valor) > 10000) throw new IllegalArgumentException("Coordenadas devem permanecer entre -10.000 e 10.000.");
        matrizes.add(nova); nomes.add(nome);
    }

    /**
     * Remove a última operação, inclusive transformações singulares como escala zero.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     */
    public void desfazer() {
        if (!nomes.isEmpty()) { nomes.remove(nomes.size()-1); matrizes.remove(matrizes.size()-1); }
    }

    /**
     * Obtém a composição atual como cópia independente.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @return cópia da matriz acumulada
     */
    public double[][] getMatriz() { return MatrizUtils.copiar(matrizes.get(matrizes.size()-1)); }

    /**
     * Calcula os vértices atuais a partir do original, sem arredondamento intermediário.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @return cópia ou resultado dos vértices homogêneos em colunas
     */
    public double[][] getPontos() { return MatrizUtils.multiplicar(getMatriz(), original.getPontos()); }

    /**
     * Obtém os vértices da forma original.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @return cópia dos vértices originais
     */
    public double[][] getOriginal() { return original.getPontos(); }

    /**
     * Devolve uma cópia da sequência de operações.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @return lista independente com os nomes das operações
     */
    public List<String> getHistorico() { return new ArrayList<>(nomes); }
}
