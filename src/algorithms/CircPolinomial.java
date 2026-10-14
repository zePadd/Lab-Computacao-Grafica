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
     * Desenha uma circunferencia centrada em (cx, cy) com raio r.
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
        registro.explicar("Simetrias de ordem 8: (cx±x,cy±y) e (cx±y,cy±x).");
        double xend = raio / Math.sqrt(2.0);
        int i = 1; // Passo espacial do slide 7: um pixel por iteração.
        registro.explicar("xend=r/√2=" + raio + "/√2=" + xend + "; passo i=" + i + ".");
        registro.explicar("Calcular somente o arco de 45 graus; parar quando x > xend.");
        registro.explicar("O passo pode não alcançar a diagonal: podem restar lacunas entre oitantes. Não acrescentamos pontos além do laço do slide.");
        // Varia x de 0 até xend; para cada x calcula y = sqrt(r^2 - x^2).
        for (int x = 0; x <= xend; x = x + i) {
            double radicando = (double) raio * raio - (double) x * x;
            double raiz = Math.sqrt(radicando);
            int y = (int) Math.round(raiz);
            // Completa os oito pontos pela função Círculo_ponto do slide 4.
            ponto_circulo(registro, "X=" + x, "r²-x²=" + raio + "²-" + x + "²="
                            + radicando + "; y=√(" + radicando + ")=" + raiz + "; round(y)=" + y,
                    cx, cy, x, y);
        }
        return registro;
    }

    // <-------------------SIMETRIAS E TRANSLAÇÃO DO CENTRO------------------->
    /** oito simetrias, transladadas pelo centro informado. */
    private static void ponto_circulo(ResultadoRasterizacao registro, String etapa,
                                      String conta, int cx, int cy, int x, int y) {
        registro.registrar(etapa, conta + "; aplicar ao centro (" + cx + "," + cy + ") as oito simetrias de (" + x + "," + y + ")",
                cx + x, cy + y, cx + y, cy + x, cx + y, cy - x, cx + x, cy - y,
                cx - x, cy - y, cx - y, cy - x, cx - y, cy + x, cx - x, cy + y);
    }

}


