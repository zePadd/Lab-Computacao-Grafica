package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Desenho de circunferência pelo método polinomial (equação explícita).
 *
 * Usa a fórmula y = sqrt(r^2 - x^2) em uma única varredura de x,
 * de 0 até o raio. Calcula um quadrante e espelha os pontos nos outros
 * três, somando o centro informado. Esta versão simples pode deixar
 * lacunas perto das extremidades laterais da circunferência, mas foi a recomendada pelo professor
 * para demonstrar a fragilidade do algoritmo. A versão com simetria nos 8 octantes diminui as falhas
 *
 * Referência: aula 3 de Computação Gráfica - UEPB.
 *
 * @author Georis
 * @date 10/10/2026
 */
public class CircPolinomial {

    // <-------------------CÁLCULO E REGISTRO DOS PASSOS------------------->
    /**
     * Desenha uma circunferência centrada em (cx, cy) com raio r.
     *
     * @param imagem framebuffer
     * @param cx     coordenada x do centro
     * @param cy     coordenada y do centro
     * @param raio   raio da circunferencia (em pixels)
     * @param cor    cor dos pixels
     */
    public static ResultadoRasterizacao polinomial(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor) {
        return polinomial(imagem, cx, cy, raio, cor, false);
    }

    // <-------------------COORDENADAS CARTESIANAS OPCIONAIS------------------->
    public static ResultadoRasterizacao polinomial(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor, boolean cartesiano) {
        ResultadoRasterizacao.validarCirculo(cx, cy, raio);
        ResultadoRasterizacao registro = new ResultadoRasterizacao(imagem, cor, cartesiano);
        // Valores compactos são registrados junto com as contas completas.
        registro.definirColunas("X local", "r² − x²", "Y calculado", "Y arredondado");
        registro.explicar("Polinomial: x²+y²=r² → y=√(r²-x²). Centro informado=(" + cx + "," + cy + ").");
        registro.explicar("r² = " + raio + "*" + raio + " = " + ((double) raio * raio));
        registro.explicar("Quatro simetrias: (cx+x,cy+y), (cx+x,cy-y), (cx-x,cy-y), (cx-x,cy+y).");
        registro.explicar("Uma única varredura: x de 0 até r=" + raio + ", com incremento de 1 pixel.");
        registro.explicar("Perto de x=r, Y pode saltar vários pixels. As lacunas são mantidas, sem varredura em Y nem ligação entre pontos.");


        // <-------------------VARREDURA ÚNICA EM X------------------->

        // A raiz positiva fornece o quadrante com x >= 0 e y >= 0.
        // Incluímos x=0 e x=raio: são raio+1 etapas, incluindo os extremos.
        for (int x = 0; x <= raio; x++) {
            double radicando = (double) raio * raio - (double) x * x;
            double raiz = Math.sqrt(radicando);
            int y = (int) Math.round(raiz); // A imagem usa posições inteiras; guardamos também a raiz na tabela.
            // Espelha o ponto nos quatro quadrantes, sem trocar x por y (como no caso dos oitantes)
            ponto_circulo(registro, "X=" + x, "r²-x²=" + raio + "²-" + x + "²="
                            + radicando + "; y=√(" + radicando + ")=" + raiz + "; round(y)=" + y,
                    cx, cy, x, y);
            registro.resumirPasso(x, radicando, raiz, y);
        }
        return registro;
    }

    // <-------------------SIMETRIAS E TRANSLAÇÃO DO CENTRO------------------->
    /** Espelha o deslocamento (x,y) em relação ao centro (cx,cy). */
    private static void ponto_circulo(ResultadoRasterizacao registro, String etapa,
                                      String conta, int cx, int cy, int x, int y) {

        // Nos eixos, alguns pares coincidem; o registro agrupa essas repetições.
        registro.registrar(etapa, conta + "; centro=(" + cx + "," + cy
                        + "), quatro simetrias de (" + x + "," + y + ")",
                cx + x, cy + y, // Direita e acima do centro.
                cx + x, cy - y, // Direita e abaixo do centro.
                cx - x, cy - y, // Esquerda e abaixo do centro.
                cx - x, cy + y  // Esquerda e acima do centro.
        );
    }
}
