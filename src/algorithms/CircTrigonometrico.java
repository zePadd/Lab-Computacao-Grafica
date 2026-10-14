package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Desenho de circunferência pelo método trigonometrico.
 *
 * Calcula os pontos de um oitante com:
 *   x = r * cos(theta)
 *   y = r * sin(theta)
 * variando theta de 0 a PI/4, conforme os slides do professor.
 * Arredonda os pontos, aplica oito simetrias e soma o centro (cx, cy).
 * O passo angular pode deixar lacunas, inclusive na junção dos oitantes.
 *
 * Referencia: aula 3 de Computacao Grafica — UEPB.
 *
 * @author João Victor
 */
public class CircTrigonometrico {

    // <-------------------CÁLCULO E REGISTRO DOS PASSOS------------------->
    /**
     * Desenha uma circunferencia centrada em (cx, cy) com raio r.
     * Escolhe automaticamente o passo angular de 1/r radiano.
     *
     * @param imagem framebuffer
     * @param cx     coordenada x do centro
     * @param cy     coordenada y do centro
     * @param raio   raio da circunferencia (em pixels)
     * @param cor    cor dos pixels
     */
    public static ResultadoRasterizacao trigonometrico(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor) {
        return trigonometrico(imagem, cx, cy, raio, cor, Math.toDegrees(1.0 / Math.max(raio, 1)));
    }

    /** O campo da interface está em graus; seno e cosseno recebem radianos. */
    public static ResultadoRasterizacao trigonometrico(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor, double passoGraus) {
        return trigonometrico(imagem, cx, cy, raio, cor, passoGraus, false);
    }

    // <-------------------COORDENADAS CARTESIANAS OPCIONAIS------------------->
    public static ResultadoRasterizacao trigonometrico(BufferedImage imagem, int cx, int cy,
                                                 int raio, Color cor, double passoGraus, boolean cartesiano) {
        ResultadoRasterizacao.validarCirculo(cx, cy, raio);
        if (!Double.isFinite(passoGraus) || passoGraus <= 0 || passoGraus > 360
                || 360.0 / passoGraus > 20000) {
            throw new IllegalArgumentException("Passo deve estar entre 0,018° e 360° (até 20.000 amostras).");
        }
        ResultadoRasterizacao registro = new ResultadoRasterizacao(imagem, cor, cartesiano);
        // Valores compactos são registrados junto com as contas completas.
        registro.definirColunas("θ (graus)", "X calculado", "Y calculado", "X arred.", "Y arred.");
        double i = Math.toRadians(passoGraus); // Passo angular i do slide 9, em radianos.
        double thetaEnd = Math.PI / 4.0;
        double passo = i; // Mantém o nome usado nas mensagens da interface.
        registro.explicar("Trigonométrico: x=r*cos(θ), y=r*sin(θ); arredondar, aplicar oito simetrias e somar o centro.");
        registro.explicar("Centro informado=(" + cx + "," + cy + "); thetaEnd=π/4=" + thetaEnd + " rad (45°).");
        registro.explicar(cartesiano ? "Y cresce para cima: ângulo positivo percorre no sentido anti-horário." : "Y cresce para baixo: ângulo positivo percorre a tela no sentido horário.");
        registro.explicar("Δθ = " + passoGraus + "° * π/180 = " + passo + " rad.");
        registro.explicar("Arco entre amostras = r*Δθ = " + raio + "*" + passo + " = " + (raio * passo) + " pixels.");
        if (raio * passo > 1.000001) registro.explicar("ATENÇÃO: passo grande pode deixar lacunas; não são ligadas por outro algoritmo.");
        registro.explicar("Intervalo: 0 ≤ θ ≤ π/4. Parar quando θ > thetaEnd; completar com ponto_circulo.");
        registro.explicar("Simetrias de ordem 8: (cx±x,cy±y) e (cx±y,cy±x).");
        registro.explicar("Se o passo não atingir 45°, a junção entre oitantes pode ter lacunas; não inserimos amostras extras.");
        if (passoGraus > 45) registro.explicar("Passo maior que o arco: só θ=0 será calculado, seguido das simetrias.");
        // <-------------------ARCO E INCREMENTO DO SLIDE 9------------------->
        int etapa = 0;
        for (double theta = 0; theta <= thetaEnd; theta = theta + i) {
            double cosseno = Math.cos(theta);
            double seno = Math.sin(theta);
            double x = raio * cosseno;
            double y = raio * seno;
            int arredondadoX = (int) Math.round(x);
            int arredondadoY = (int) Math.round(y);
            ponto_circulo(registro, String.valueOf(etapa++), "θ=" + theta + " rad=" + Math.toDegrees(theta)
                            + "°; cos=" + cosseno + "; sin=" + seno
                            + "; x=" + raio + "*(" + cosseno + ")=" + x
                            + "; y=" + raio + "*(" + seno + ")=" + y
                            + "; round(x)=" + arredondadoX + "; round(y)=" + arredondadoY
                            + "; próximo θ=" + theta + "+" + i + "=" + (theta + i),
                    cx, cy, arredondadoX, arredondadoY);
            registro.resumirPasso(Math.toDegrees(theta), x, y, arredondadoX, arredondadoY);
        }
        return registro;
    }

    // <-------------------OITO SIMETRIAS------------------->
    private static void ponto_circulo(ResultadoRasterizacao registro, String etapa,
                                      String conta, int cx, int cy, int x, int y) {
        registro.registrar(etapa, conta + "; centro=(" + cx + "," + cy + "), oito simetrias de (" + x + "," + y + ")",
                cx + x, cy + y, cx + y, cy + x, cx + y, cy - x, cx + x, cy - y,
                cx - x, cy - y, cx - y, cy - x, cx - y, cy + x, cx - x, cy + y);
    }
}
