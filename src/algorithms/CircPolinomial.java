package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Desenho de circunferência pelo método polinomial (equação explícita).
 *
 * Usa a fórmula y = sqrt(r^2 - x^2) para calcular os pontos do primeiro
 * quadrante (1/4 do círculo) e espelha para os outros três quadrantes.
 *
 * Referência: aula 3 de Computação Gráfica - UEPB.
 *
 * @author Georis
 * @date 10/10/2026
 */
public class CircPolinomial {

    /**
     * Desenha uma circunferência centrada em (cx, cy) com raio r.
     *
     * @param imagem framebuffer
     * @param cx     coordenada x do centro
     * @param cy     coordenada y do centro
     * @param raio   raio da circunferencia (em pixels)
     * @param cor    cor dos pixels
     */
    public static void desenhar(BufferedImage imagem, int cx, int cy, int raio, Color cor) {
        calcular(imagem, cx, cy, raio, cor);
    }

    // <-------------------CÁLCULO E REGISTRO DOS PASSOS------------------->
    public static ResultadoRasterizacao calcular(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor) {
        return calcular(imagem, cx, cy, raio, cor, false);
    }

    // <-------------------COORDENADAS CARTESIANAS OPCIONAIS------------------->
    public static ResultadoRasterizacao calcular(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor, boolean cartesiano) {
        ResultadoRasterizacao.validarCirculo(cx, cy, raio);
        ResultadoRasterizacao registro = new ResultadoRasterizacao(imagem, cor, cartesiano);
        registro.explicar("Polinomial: x²+y²=r² → y=√(r²-x²). Centro informado=(" + cx + "," + cy + ").");
        registro.explicar("r² = " + raio + "*" + raio + " = " + ((double) raio * raio));
        registro.explicar("Simetrias: (cx+x,cy+y), (cx-x,cy+y), (cx+x,cy-y), (cx-x,cy-y).");
        registro.explicar("Varredura simples: X de 0 até r. Os saltos de Y perto das laterais podem deixar lacunas.");
        // Varia x de 0 ate r; para cada x calcula y = sqrt(r^2 - x^2)
        for (int x = 0; x <= raio; x++) {
            double radicando = (double) raio * raio - (double) x * x;
            double raiz = Math.sqrt(radicando);
            int y = (int) Math.round(raiz);
            // Espelha nos 4 quadrantes
            plotar4Simetrias(registro, "X=" + x, "r²-x²=" + raio + "²-" + x + "²="
                            + radicando + "; y=√(" + radicando + ")=" + raiz + "; round(y)=" + y,
                    cx, cy, x, y);
        }
        return registro;
    }

    // <-------------------SIMETRIAS E TRANSLAÇÃO DO CENTRO------------------->
    /** Acende os 4 pixels simetricos em relacao ao centro (cx, cy). */
    private static void plotar4Simetrias(ResultadoRasterizacao registro, String etapa,
                                         String conta, int cx, int cy, int x, int y) {
        registro.registrar(etapa, conta + "; aplicar (" + cx + " ± " + x + ", " + cy + " ± " + y + ")",
                cx + x, cy + y, cx - x, cy + y, cx + x, cy - y, cx - x, cy - y);
    }

}
