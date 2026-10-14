package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Implementação do algoritmo dda (Digital Differential Analyzer) para
 * rasterização de retas. O DDA calcula incrementos fracionários em x e y
 * e arredonda a cada passo para determinar qual pixel acender.
 *
 * Referência: aula 3 de Computação Gráfica — UEPB.
 *
 * @author Neto
 * @date 10/10/2026
 */
public class AlgoritmoDDA {

    /**
     * Desenha uma reta do ponto (x1, y1) até (x2, y2) usando o algoritmo DDA.
     *
     * @param imagem framebuffer onde os pixels serão acesos
     * @param x1     coordenada x do ponto inicial
     * @param y1     coordenada y do ponto inicial
     * @param x2     coordenada x do ponto final
     * @param y2     coordenada y do ponto final
     * @param cor    cor dos pixels da reta
     */
    public static ResultadoRasterizacao dda(BufferedImage imagem, int x1, int y1, int x2, int y2, Color cor, boolean cartesiano) {
        ResultadoRasterizacao.validarReta(x1, y1, x2, y2);
        ResultadoRasterizacao registro = new ResultadoRasterizacao(imagem, cor, cartesiano);
        // Valores compactos são registrados junto com as contas completas.
        registro.definirColunas("X calculado", "Y calculado", "X pixel", "Y pixel");
        int dx = x2 - x1;
        int dy = y2 - y1;

        // Length e o maior deslocamento absoluto entre dx e dy
        int length = Math.max(Math.abs(dx), Math.abs(dy));
        registro.explicar(cartesiano ? "DDA — coordenadas cartesianas (Y cresce para cima)." : "DDA — coordenadas de dispositivo (Y cresce para baixo).");
        registro.explicar("dx = " + x2 + " - (" + x1 + ") = " + dx);
        registro.explicar("dy = " + y2 + " - (" + y1 + ") = " + dy);
        registro.explicar("Length = max(|" + dx + "|, |" + dy + "|) = " + length);
        if (length == 0) {
            // Ponto unico: os dois extremos sao o mesmo pixel
            registro.registrar("0", "Extremos coincidentes: não dividir por zero.", x1, y1);
            registro.resumirPasso(x1, y1, x1, y1);
            return registro;
        }

        // Incrementos fracionários por passo
        double xInc = (double) dx / length;
        double yInc = (double) dy / length;
        registro.explicar("Xinc = " + dx + "/" + length + " = " + xInc);
        registro.explicar("Yinc = " + dy + "/" + length + " = " + yInc);
        double x = x1;
        double y = y1;
        String chegada = "Ponto inicial";
        for (int i = 0; i <= length; i++) {
            int px = (int) Math.round(x);
            int py = (int) Math.round(y);
            // Garante que o pixel esta dentro dos limites da imagem no registro.
            registro.registrar(String.valueOf(i), chegada + "; round(" + x + ") = " + px
                    + "; round(" + y + ") = " + py, px, py);
            registro.resumirPasso(x, y, px, py);
            if (i < length) {
                chegada = "x = " + x + " + (" + xInc + ") = " + (x + xInc)
                        + "; y = " + y + " + (" + yInc + ") = " + (y + yInc);
                x += xInc;
                y += yInc;
            }
        }
        return registro;
    }

}
