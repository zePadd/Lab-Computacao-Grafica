package transformer;

/**
 * Classe utilitária de operações matriciais usadas pelo laboratório, sem bibliotecas de álgebra.
 *
 * @author Georis
 * @date 02/11/2026
 */
public final class MatrizUtils {

    // <-------------------VALIDAÇÃO E MULTIPLICAÇÃO------------------->
    /**
     * Impede instâncias desta classe utilitária.
     *
     * @author Georis
     * @date 02/11/2026
     */
    private MatrizUtils() {}

    /**
     * Valida uma matriz retangular não vazia, com valores finitos. Lança IllegalArgumentException para dados inválidos.
     *
     * @author Georis
     * @date 02/11/2026
     * @param a matriz à esquerda
     * @throws IllegalArgumentException se os dados ou as dimensões forem inválidos
     */
    public static void validar(double[][] a) {
        if (a == null || a.length == 0 || a[0] == null || a[0].length == 0)
            throw new IllegalArgumentException("A matriz deve conter linhas e colunas.");
        int colunas = a[0].length;
        for (double[] linha : a) {
            if (linha == null || linha.length != colunas)
                throw new IllegalArgumentException("A matriz deve ser retangular.");
            for (double valor : linha)
                if (!Double.isFinite(valor)) throw new IllegalArgumentException("Use somente valores finitos.");
        }
    }

    /**
     * Calcula C[i][j] = soma de A[i][k] * B[k][j]. Lança IllegalArgumentException para dimensões incompatíveis ou resultado não finito.
     *
     * @author Georis
     * @date 02/11/2026
     * @param a matriz à esquerda
     * @param b matriz à direita
     * @return matriz resultante
     * @throws IllegalArgumentException se os dados ou as dimensões forem inválidos
     */
    public static double[][] multiplicar(double[][] a, double[][] b) {
        validar(a);
        validar(b);
        if (a[0].length != b.length)
            throw new IllegalArgumentException("Colunas de A devem coincidir com linhas de B.");
        double[][] c = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < b[0].length; j++)
                for (int k = 0; k < b.length; k++) c[i][j] += a[i][k] * b[k][j];
        validar(c);
        return c;
    }

    /**
     * Cria uma identidade homogênea 3 por 3.
     *
     * @author Georis
     * @date 02/11/2026
     * @return matriz resultante
     */
    public static double[][] identidade() {
        return new double[][] {{1,0,0}, {0,1,0}, {0,0,1}};
    }

    /**
     * Copia todas as linhas, evitando compartilhar matrizes mutáveis.
     *
     * @author Georis
     * @date 02/11/2026
     * @param a matriz à esquerda
     * @return matriz resultante
     */
    public static double[][] copiar(double[][] a) {
        validar(a);
        double[][] copia = new double[a.length][];
        for (int i = 0; i < a.length; i++) copia[i] = a[i].clone();
        return copia;
    }
}
