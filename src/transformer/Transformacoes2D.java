package transformer;

/**
 * Constrói matrizes homogêneas para vetores-coluna; a primeira operação fica à direita na composição.
 *
 * @author José Araújo Agra Neto
 * @date 02/11/2026
 */
public final class Transformacoes2D {
    /**
     * Impede instâncias da fábrica de matrizes.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     */
    private Transformacoes2D() {}

    // <-------------------TRANSFORMAÇÕES BÁSICAS------------------->
    /**
     * Constrói a translação x' = x + tx e y' = y + ty.
     *
     * @author José Araújo Agra Neto
     * @date
     * @param x deslocamento horizontal
     * @param y deslocamento vertical
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][] translacao(double x, double y) {
        return conferir(new double[][] {{1,0,x}, {0,1,y}, {0,0,1}});
    }

    /**
     * Constrói escala em torno da origem, inclusive fatores negativos e zero.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param x fator de escala horizontal
     * @param y fator de escala vertical
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][] escala(double x, double y) {
        return conferir(new double[][] {{x,0,0}, {0,y,0}, {0,0,1}});
    }

    /**
     * Constrói rotação anti-horária em torno da origem, conforme a Aula G5.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param graus ângulo em graus, positivo no sentido anti-horário
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][] rotacao(double graus) {
        if (!Double.isFinite(graus)) throw new IllegalArgumentException("Ângulo deve ser finito.");
        double rad = Math.toRadians(graus % 360);
        double c = Math.cos(rad), s = Math.sin(rad);
        return new double[][] {{c,-s,0}, {s,c,0}, {0,0,1}};
    }

    /**
     * Constrói cisalhamento simultâneo x' = x + a*y e y' = b*x + y; não é o produto dos dois cisalhamentos simples.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param a coeficiente do cisalhamento em X
     * @param b coeficiente do cisalhamento em Y
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][] cisalhamento(double a, double b) {
        return conferir(new double[][] {{1,a,0}, {b,1,0}, {0,0,1}});
    }

    /**
     * Constrói reflexão em X (0), Y (1), origem (2) ou reta y = x (3).
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param tipo índice da opção descrita neste método
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][] reflexao(int tipo) {
        switch (tipo) {
            case 0: return escala(1,-1);
            case 1: return escala(-1,1);
            case 2: return escala(-1,-1);
            case 3: return new double[][] {{0,1,0}, {1,0,0}, {0,0,1}};
            default: throw new IllegalArgumentException("Reflexão desconhecida.");
        }
    }

    // <-------------------COMPOSIÇÃO E PONTO FIXO------------------->
    /**
     * Compõe matrizes na ordem em que são aplicadas: compor(M1,M2) devolve M2*M1.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param etapas matrizes em ordem de aplicação
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][] compor(double[][]... etapas) {
        double[][] total = MatrizUtils.identidade();
        for (double[][] etapa : etapas) total = MatrizUtils.multiplicar(conferir(etapa), total);
        return total;
    }

    /**
     * Aplica T(px,py)*M*T(-px,-py), preservando o pivô quando M fixa a origem.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param m matriz de transformação
     * @param x coordenada X do pivô
     * @param y coordenada Y do pivô
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][] pontoFixo(double[][] m, double x, double y) {
        return compor(translacao(-x,-y), m, translacao(x,y));
    }

    /**
     * Devolve as cinco etapas da reflexão em y = m*x + b, em ordem de aplicação.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param m coeficiente angular da reta
     * @param b intercepto da reta no eixo Y
     * @return cinco matrizes na ordem de aplicação
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][][] etapasReflexaoReta(double m, double b) {
        if (!Double.isFinite(m) || !Double.isFinite(b)) throw new IllegalArgumentException("Reta inválida.");
        double angulo = Math.toDegrees(Math.atan(m));
        return new double[][][] {translacao(0,-b), rotacao(-angulo), reflexao(0), rotacao(angulo), translacao(0,b)};
    }

    /**
     * Reflete em y = m*x + b por translação, alinhamento com X, reflexão e retorno.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param m coeficiente angular da reta
     * @param b intercepto da reta no eixo Y
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    public static double[][] reflexaoReta(double m, double b) {
        return compor(etapasReflexaoReta(m,b));
    }

    /**
     * Valida uma matriz afim homogênea 3 por 3 antes de compor.
     *
     * @author José Araújo Agra Neto
     * @date 02/11/2026
     * @param m matriz de transformação
     * @return matriz resultante
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    private static double[][] conferir(double[][] m) {
        MatrizUtils.validar(m);
        if (m.length != 3 || m[0].length != 3 || m[2][0] != 0 || m[2][1] != 0 || m[2][2] != 1)
            throw new IllegalArgumentException("Use uma matriz afim homogênea 3 x 3.");
        return m;
    }
}
