package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Desenho de circunferência pelo algoritmo do ponto médio (Bresenham).
 *
 * Usa apenas aritmética inteira: calcula 1/8 do arco (do topo até a
 * diagonal de 45 graus) e espelha em 8 octantes por simetria.
 *
 * Refêrencia: aula 3 de Computação Gráfica - UEPB
 *
 * @author Suelle
 * @date 10/10/2026
 */
public class CircPontoMedio {

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
        int x = 0;
        int y = raio;
        // Variavel de decisao inicial: d = 1 - r
        int d = 1 - raio;
        registro.explicar("Ponto médio — arco local matemático de (0,r) até a diagonal; completar por oito simetrias.");
        registro.explicar("d inicial = 1 - " + raio + " = " + d + "; d inteiro = decisão real - 1/4.");
        registro.explicar("d < 0 → E; d ≥ 0 → SE. Incrementos usam x,y ANTES do avanço.");
        registro.explicar("IncE=2*x+3; IncSE=2*(x-y)+5. Iniciais: IncE=3; IncSE=" + (5 - 2 * raio));
        registro.explicar("Coordenadas: (cx±x,cy±y) e (cx±y,cy±x). Os sinais cobrem ambos os sentidos de Y.");
        // Acende os 8 pontos simetricos do pixel inicial (0, r)
        plotar8Simetrias(registro, "0", "Inicial: (x,y)=(0," + raio + "), d=" + d, cx, cy, x, y);
        int etapa = 0;
        while (x < y) {
            int anteriorX = x;
            int anteriorY = y;
            int anteriorD = d;
            int incE = 2 * x + 3;
            int incSE = 2 * (x - y) + 5;
            String escolha;
            int incrementoUsado;
            if (d < 0) {
                // Ponto medio esta dentro do circulo: escolhe E
                incrementoUsado = incE;
                escolha = "E";
            } else {
                // Ponto medio esta fora ou sobre o circulo: escolhe SE
                incrementoUsado = incSE;
                escolha = "SE";
                y--;
            }
            d += incrementoUsado;
            x++;
            // Mesma recorrência da base, reorganizada para mostrar os valores anteriores.
            plotar8Simetrias(registro, String.valueOf(++etapa), "Antes: (" + anteriorX + "," + anteriorY
                    + "); IncE=2*" + anteriorX + "+3=" + incE + "; IncSE=2*(" + anteriorX
                    + "-" + anteriorY + ")+5=" + incSE + "; d=" + anteriorD
                    + (anteriorD < 0 ? " < 0" : " ≥ 0") + " → " + escolha
                    + "; x=" + anteriorX + "+1=" + x + "; y=" + anteriorY
                    + (escolha.equals("SE") ? "-1=" : "+0=") + y
                    + "; novo d=" + anteriorD + "+(" + incrementoUsado + ")=" + d,
                    cx, cy, x, y);
        }
        return registro;
    }

    // <-------------------OITO SIMETRIAS------------------->
    /** Acende os 8 pixels simetricos a partir de (x,y) relativo ao centro. */
    private static void plotar8Simetrias(ResultadoRasterizacao registro, String etapa,
                                        String conta, int cx, int cy, int x, int y) {
        registro.registrar(etapa, conta + "; centro=(" + cx + "," + cy + "), simetrias de (" + x + "," + y + ")",
                cx + x, cy + y, cx - x, cy + y, cx + x, cy - y, cx - x, cy - y,
                cx + y, cy + x, cx - y, cy + x, cx + y, cy - x, cx - y, cy - x);
    }

}
