
package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class AlgoritmoBresenham {

    public static ResultadoRasterizacao bresenham(BufferedImage imagem, int x1, int y1, int x2, int y2, Color cor) {

        return bresenham(imagem, x1, y1, x2, y2, cor, false);
    }

    public static ResultadoRasterizacao bresenham(BufferedImage imagem,int x1, int y1, int x2, int y2, Color cor, boolean cartesiano) {

        ResultadoRasterizacao.validarReta(x1, y1, x2, y2);

        ResultadoRasterizacao registro =
                new ResultadoRasterizacao(imagem, cor, cartesiano);

        registro.definirColunas(
                "X", "Y", "d antes", "Escolha",
                "Incremento", "d depois");

        int deltaX = x2 - x1;
        int deltaY = cartesiano ? y2 - y1 : y1 - y2;

        int dx = Math.abs(deltaX);
        int dy = Math.abs(deltaY);

        // No sistema de tela, Y cresce para baixo.
        int passoY = cartesiano ? 1 : -1;

        int oitante = classificarOitante(deltaX, deltaY);

        registro.explicar("Bresenham com cálculos explícitos para os oito octantes.");
        registro.explicar("Delta X = " + deltaX + "; Delta Y matemático = " + deltaY);
        registro.explicar("Oitante selecionado: " + oitante);

        int x = x1;
        int y = y1;

        // Reta degenerada: apenas um pixel.
        if (dx == 0 && dy == 0) {
            registro.explicar("Ponto único: não há incrementos.");
            registro.registrar("0", "Ponto inicial e final", x, y);
            registro.resumirPasso(x, y, "—", "Inicial", "—", 0);
            return registro;
        }

        int d;
        int incE;
        int incNE;
        int passos;

        /*
         * Cada caso define explicitamente:
         * - eixo principal;
         * - variável de decisão;
         * - incremento principal;
         * - incremento diagonal.
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

        registro.explicar("d inicial = " + d);
        registro.explicar("Incremento principal = " + incE);
        registro.explicar("Incremento diagonal = " + incNE);

        String movimentoAnterior = "Inicial";
        Object decisaoAnterior = "—";
        Object incrementoAnterior = "—";

        for (int i = 0; i <= passos; i++) {

            registro.registrar(
                    String.valueOf(i),
                    "Pixel (" + x + ", " + y + "), d = " + d,
                    x, y);

            registro.resumirPasso(
                    x, y,
                    decisaoAnterior,
                    movimentoAnterior,
                    incrementoAnterior,
                    d);

            if (i == passos) {
                break;
            }

            int dAnterior = d;

            /*
             * A decisão é feita explicitamente para cada octante.
             * d <= 0: movimento principal.
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

        return registro;
    }

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
