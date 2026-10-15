
package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Implementa o algoritmo de Bresenham para a rasterização de retas,
 * utilizando o método do ponto médio.
 *
 * <p>A implementação apresenta cálculos explícitos para os oito
 * octantes, determinando os pixels que representam uma reta em
 * uma imagem digital.</p>
 *
 * <p>Permite utilizar coordenadas de tela ou coordenadas cartesianas
 * e registra as etapas executadas durante a rasterização.</p>
 *
 * @author Horlan Lacerda
 * @version 1.0
 */
public class AlgoritmoBresenham {

    /**
     * Rasteriza uma reta entre dois pontos utilizando o algoritmo
     * de Bresenham no sistema de coordenadas de tela.
     *
     * @param imagem imagem na qual a reta será desenhada
     * @param x1 coordenada X do ponto inicial
     * @param y1 coordenada Y do ponto inicial
     * @param x2 coordenada X do ponto final
     * @param y2 coordenada Y do ponto final
     * @param cor cor utilizada para desenhar a reta
     * @return objeto contendo os resultados da rasterização
     */
    public static ResultadoRasterizacao bresenham(
            BufferedImage imagem,
            int x1, int y1, int x2, int y2,
            Color cor) {

        return bresenham(imagem, x1, y1, x2, y2, cor, false);
    }

    /**
     * Rasteriza uma reta utilizando o algoritmo de Bresenham,
     * com cálculos explícitos para o octante correspondente.
     *
     * <p>O método calcula os deslocamentos horizontal e vertical,
     * identifica o octante, inicializa a variável de decisão e
     * determina os pixels por meio dos movimentos principal
     * e diagonal.</p>
     *
     * @param imagem imagem na qual a reta será desenhada
     * @param x1 coordenada X do ponto inicial
     * @param y1 coordenada Y do ponto inicial
     * @param x2 coordenada X do ponto final
     * @param y2 coordenada Y do ponto final
     * @param cor cor utilizada para desenhar a reta
     * @param cartesiano indica se devem ser utilizadas coordenadas
     *                   cartesianas; false utiliza coordenadas de tela
     * @return objeto contendo os resultados e registros da rasterização
     */
    public static ResultadoRasterizacao bresenham(
            BufferedImage imagem,
            int x1, int y1, int x2, int y2,
            Color cor, boolean cartesiano) {

        // Valida as coordenadas da reta.
        ResultadoRasterizacao.validarReta(x1, y1, x2, y2);

        // Cria o objeto responsável pelo registro da rasterização.
        ResultadoRasterizacao registro =
                new ResultadoRasterizacao(imagem, cor, cartesiano);

        // Define as colunas utilizadas para apresentar os resultados.
        registro.definirColunas(
                "X", "Y", "d antes", "Escolha",
                "Incremento", "d depois");

        // Calcula os deslocamentos horizontal e vertical.
        int deltaX = x2 - x1;
        int deltaY = cartesiano ? y2 - y1 : y1 - y2;

        // Obtém as magnitudes dos deslocamentos.
        int dx = Math.abs(deltaX);
        int dy = Math.abs(deltaY);

        // No sistema de tela, o eixo Y cresce para baixo.
        int passoY = cartesiano ? 1 : -1;

        // Identifica o octante ao qual a reta pertence.
        int oitante = classificarOitante(deltaX, deltaY);

        registro.explicar(
                "Bresenham com cálculos explícitos para os oito octantes.");
        registro.explicar(
                "Delta X = " + deltaX
                + "; Delta Y matemático = " + deltaY);
        registro.explicar("Oitante selecionado: " + oitante);

        // Inicializa as coordenadas do pixel atual.
        int x = x1;
        int y = y1;

        // Trata o caso em que os pontos inicial e final são iguais.
        if (dx == 0 && dy == 0) {
            registro.explicar("Ponto único: não há incrementos.");
            registro.registrar(
                    "0", "Ponto inicial e final", x, y);
            registro.resumirPasso(
                    x, y, "—", "Inicial", "—", 0);

            return registro;
        }

        int d;
        int incE;
        int incNE;
        int passos;

        /*
         * Cada caso define explicitamente:
         * - o eixo principal;
         * - a variável de decisão;
         * - o incremento principal;
         * - o incremento diagonal.
         */
        switch (oitante) {

            case 1: // E / NE
                passos = dx;
                d = 2 * dy - dx;
                incE = 2 * dy;
                incNE = 2 * (dy - dx);
                break;

            case 2: // N / NE
                passos = dy;
                d = 2 * dx - dy;
                incE = 2 * dx;
                incNE = 2 * (dx - dy);
                break;

            case 3: // N / NW
                passos = dy;
                d = 2 * dx - dy;
                incE = 2 * dx;
                incNE = 2 * (dx - dy);
                break;

            case 4: // W / NW
                passos = dx;
                d = 2 * dy - dx;
                incE = 2 * dy;
                incNE = 2 * (dy - dx);
                break;

            case 5: // W / SW
                passos = dx;
                d = 2 * dy - dx;
                incE = 2 * dy;
                incNE = 2 * (dy - dx);
                break;

            case 6: // S / SW
                passos = dy;
                d = 2 * dx - dy;
                incE = 2 * dx;
                incNE = 2 * (dx - dy);
                break;

            case 7: // S / SE
                passos = dy;
                d = 2 * dx - dy;
                incE = 2 * dx;
                incNE = 2 * (dx - dy);
                break;

            case 8: // E / SE
                passos = dx;
                d = 2 * dy - dx;
                incE = 2 * dy;
                incNE = 2 * (dy - dx);
                break;

            default:
                throw new IllegalStateException("Octante inválido.");
        }

        // Registra os valores iniciais utilizados no algoritmo.
        registro.explicar("d inicial = " + d);
        registro.explicar("Incremento principal = " + incE);
        registro.explicar("Incremento diagonal = " + incNE);

        String movimentoAnterior = "Inicial";
        Object decisaoAnterior = "—";
        Object incrementoAnterior = "—";

        /*
         * Percorre os pixels da reta.
         * A quantidade de passos depende do eixo principal do octante.
         */
        for (int i = 0; i <= passos; i++) {

            // Registra as coordenadas e o valor atual da decisão.
            registro.registrar(
                    String.valueOf(i),
                    "Pixel (" + x + ", " + y + "), d = " + d,
                    x, y);

            // Armazena o resumo da iteração atual.
            registro.resumirPasso(
                    x, y,
                    decisaoAnterior,
                    movimentoAnterior,
                    incrementoAnterior,
                    d);

            // Encerra o laço após registrar o último pixel.
            if (i == passos) {
                break;
            }

            int dAnterior = d;

            /*
             * A decisão é realizada explicitamente para cada octante:
             * d <= 0: movimento principal;
             * d > 0: movimento diagonal.
             */
            switch (oitante) {

                case 1: // E / NE
                    if (d <= 0) {
                        x++;
                        d += incE;
                        movimentoAnterior = "E";
                        decisaoAnterior = dAnterior + " <= 0";
                        incrementoAnterior = incE;
                    } else {
                        x++;
                        y += passoY;
                        d += incNE;
                        movimentoAnterior = "NE";
                        decisaoAnterior = dAnterior + " > 0";
                        incrementoAnterior = incNE;
                    }
                    break;

                case 2: // N / NE
                    if (d <= 0) {
                        y += passoY;
                        d += incE;
                        movimentoAnterior = "N";
                        decisaoAnterior = dAnterior + " <= 0";
                        incrementoAnterior = incE;
                    } else {
                        x++;
                        y += passoY;
                        d += incNE;
                        movimentoAnterior = "NE";
                        decisaoAnterior = dAnterior + " > 0";
                        incrementoAnterior = incNE;
                    }
                    break;

                case 3: // N / NW
                    if (d <= 0) {
                        y += passoY;
                        d += incE;
                        movimentoAnterior = "N";
                        decisaoAnterior = dAnterior + " <= 0";
                        incrementoAnterior = incE;
                    } else {
                        x--;
                        y += passoY;
                        d += incNE;
                        movimentoAnterior = "NW";
                        decisaoAnterior = dAnterior + " > 0";
                        incrementoAnterior = incNE;
                    }
                    break;

                case 4: // W / NW
                    if (d <= 0) {
                        x--;
                        d += incE;
                        movimentoAnterior = "W";
                        decisaoAnterior = dAnterior + " <= 0";
                        incrementoAnterior = incE;
                    } else {
                        x--;
                        y += passoY;
                        d += incNE;
                        movimentoAnterior = "NW";
                        decisaoAnterior = dAnterior + " > 0";
                        incrementoAnterior = incNE;
                    }
                    break;

                case 5: // W / SW
                    if (d <= 0) {
                        x--;
                        d += incE;
                        movimentoAnterior = "W";
                        decisaoAnterior = dAnterior + " <= 0";
                        incrementoAnterior = incE;
                    } else {
                        x--;
                        y -= passoY;
                        d += incNE;
                        movimentoAnterior = "SW";
                        decisaoAnterior = dAnterior + " > 0";
                        incrementoAnterior = incNE;
                    }
                    break;

                case 6: // S / SW
                    if (d <= 0) {
                        y -= passoY;
                        d += incE;
                        movimentoAnterior = "S";
                        decisaoAnterior = dAnterior + " <= 0";
                        incrementoAnterior = incE;
                    } else {
                        x--;
                        y -= passoY;
                        d += incNE;
                        movimentoAnterior = "SW";
                        decisaoAnterior = dAnterior + " > 0";
                        incrementoAnterior = incNE;
                    }
                    break;

                case 7: // S / SE
                    if (d <= 0) {
                        y -= passoY;
                        d += incE;
                        movimentoAnterior = "S";
                        decisaoAnterior = dAnterior + " <= 0";
                        incrementoAnterior = incE;
                    } else {
                        x++;
                        y -= passoY;
                        d += incNE;
                        movimentoAnterior = "SE";
                        decisaoAnterior = dAnterior + " > 0";
                        incrementoAnterior = incNE;
                    }
                    break;

                case 8: // E / SE
                    if (d <= 0) {
                        x++;
                        d += incE;
                        movimentoAnterior = "E";
                        decisaoAnterior = dAnterior + " <= 0";
                        incrementoAnterior = incE;
                    } else {
                        x++;
                        y -= passoY;
                        d += incNE;
                        movimentoAnterior = "SE";
                        decisaoAnterior = dAnterior + " > 0";
                        incrementoAnterior = incNE;
                    }
                    break;
            }
        }

        // Retorna o objeto com os resultados da rasterização.
        return registro;
    }

    /**
     * Classifica a reta em um dos oito octantes, considerando
     * os sinais e as magnitudes dos deslocamentos horizontal
     * e vertical.
     *
     * @param deltaX deslocamento horizontal entre os pontos
     * @param deltaY deslocamento vertical no sentido matemático
     * @return número do octante correspondente, de 1 a 8
     */
    private static int classificarOitante(int deltaX, int deltaY) {

        int dx = Math.abs(deltaX);
        int dy = Math.abs(deltaY);

        // Direção para a direita e para cima.
        if (deltaX >= 0 && deltaY >= 0) {
            return dx >= dy ? 1 : 2;
        }

        // Direção para a esquerda e para cima.
        if (deltaX < 0 && deltaY >= 0) {
            return dy >= dx ? 3 : 4;
        }

        // Direção para a esquerda e para baixo.
        if (deltaX < 0 && deltaY < 0) {
            return dx >= dy ? 5 : 6;
        }

        // Direção para a direita e para baixo.
        return dy >= dx ? 7 : 8;
    }
}

