package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Desenho de circunferência pelo método trigonometrico.
 *
 * Calcula cada ponto da circunferência com:
 *   x = cx + r * cos(theta)
 *   y = cy + r * sin(theta)
 * variando theta de 0 a 2PI com passo pequeno o suficiente para não
 * deixar buracos visíveis.
 *
 * Referencia: aula 3 de Computacao Grafica — UEPB.
 *
 * @author João Victor
 */
public class CircTrigonometrico {

    /**
     * Desenha uma circunferencia centrada em (cx, cy) com raio r.
     *
     * @param imagem framebuffer
     * @param cx     coordenada x do centro
     * @param cy     coordenada y do centro
     * @param raio   raio da circunferência (em pixels)
     * @param cor    cor dos pixels
     */
    public static void desenhar(BufferedImage imagem, int cx, int cy, int raio, Color cor) {
        calcular(imagem, cx, cy, raio, cor);
    }

    // <-------------------CÁLCULO E REGISTRO DOS PASSOS------------------->
    // Escolhe o passo automático de 1/r radiano.
    public static ResultadoRasterizacao calcular(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor) {
        return calcular(imagem, cx, cy, raio, cor, Math.toDegrees(1.0 / Math.max(raio, 1)));
    }

    // O campo da interface está em graus; seno e cosseno recebem radianos.
    public static ResultadoRasterizacao calcular(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor, double passoGraus) {
        return calcular(imagem, cx, cy, raio, cor, passoGraus, false);
    }

    // <-------------------COORDENADAS CARTESIANAS OPCIONAIS------------------->
    public static ResultadoRasterizacao calcular(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor, double passoGraus, boolean cartesiano) {
        ResultadoRasterizacao.validarCirculo(cx, cy, raio);
        if (!Double.isFinite(passoGraus) || passoGraus <= 0 || passoGraus > 360
                || 360.0 / passoGraus > 20000) {
            throw new IllegalArgumentException("Passo deve estar entre 0,018° e 360° (até 20.000 amostras).");
        }
        ResultadoRasterizacao registro = new ResultadoRasterizacao(imagem, cor, cartesiano);
        double passo = Math.toRadians(passoGraus);
        registro.explicar("Trigonométrico: x=cx+r*cos(θ), y=cy+r*sin(θ), nas coordenadas informadas.");
        registro.explicar(cartesiano ? "Y cresce para cima: ângulo positivo percorre no sentido anti-horário." : "Y cresce para baixo: ângulo positivo percorre a tela no sentido horário.");
        registro.explicar("Δθ = " + passoGraus + "° * π/180 = " + passo + " rad.");
        registro.explicar("Arco entre amostras = r*Δθ = " + raio + "*" + passo + " = " + (raio * passo) + " pixels.");
        if (raio * passo > 1.000001) registro.explicar("ATENÇÃO: passo grande pode deixar lacunas; não são ligadas por outro algoritmo.");
        registro.explicar("Intervalo: 0 ≤ θ < 2π. O ponto de 360° repete o de 0°; não desenhamos segmentos entre amostras.");
        // O passo automático mantém o arco entre amostras em aproximadamente 1 pixel.
        // O índice evita acumular θ += passo indefinidamente por erro de arredondamento.
        int amostras = (int) Math.ceil(360.0 / passoGraus);
        for (int indice = 0; indice < amostras; indice++) {
            double graus = indice * passoGraus;
            double theta = Math.toRadians(graus);
            double cosseno = Math.cos(theta);
            double seno = Math.sin(theta);
            double horizontal = cx + raio * cosseno;
            double vertical = cy + raio * seno;
            int x = (int) Math.round(horizontal);
            int y = (int) Math.round(vertical);
            registro.registrar(String.valueOf(indice), "θ=" + indice + "*" + passoGraus + "°=" + graus
                    + "°=" + theta + " rad; cos=" + cosseno + "; sin=" + seno
                    + "; x=" + cx + "+" + raio + "*(" + cosseno + ")=" + horizontal
                    + "; y=" + cy + "+" + raio + "*(" + seno + ")=" + vertical
                    + "; arredondados=(" + x + "," + y + ")", x, y);
        }
        return registro;
    }

}
