package algorithms;

import raster.ResultadoRasterizacao;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Implementacao do algoritmo de Bresenham (ponto medio) para rasterizacao
 * de retas, com suporte a todos os oitantes por simetria.
 *
 * O nucleo do algoritmo trabalha apenas com aritmetica inteira e cobre
 * nativamente o 1o oitante (0 <= dy <= dx). Para os demais oitantes,
 * aplica-se troca e/ou inversao de coordenadas antes de chamar o nucleo,
 * e desfaz-se a transformacao na hora de acender o pixel.
 *
 * Referencia: aulag3_2026, slides 20, 25, 26 e 28 — UEPB.
 *
 * @author Horlan Silva de Lacerda
 */
public class AlgoritmoBresenham {

    /**
     * Desenha uma reta de (x1, y1) ate (x2, y2) usando Bresenham com
     * simetria de oitantes.
     *
     * @param imagem framebuffer onde os pixels serao acesos
     * @param x1     coordenada x do ponto inicial
     * @param y1     coordenada y do ponto inicial
     * @param x2     coordenada x do ponto final
     * @param y2     coordenada y do ponto final
     * @param cor    cor dos pixels da reta
     */
    public static void desenhar(BufferedImage imagem, int x1, int y1, int x2, int y2, Color cor) {
        calcular(imagem, x1, y1, x2, y2, cor);
    }

    // <-------------------CÁLCULO E REGISTRO DOS PASSOS------------------->
    public static ResultadoRasterizacao calcular(BufferedImage imagem, int x1, int y1,
                                                 int x2, int y2, Color cor) {
        return calcular(imagem, x1, y1, x2, y2, cor, false);
    }

    // <-------------------COORDENADAS CARTESIANAS OPCIONAIS------------------->
    public static ResultadoRasterizacao calcular(BufferedImage imagem, int x1, int y1,
                                                 int x2, int y2, Color cor, boolean cartesiano) {
        ResultadoRasterizacao.validarReta(x1, y1, x2, y2);
        ResultadoRasterizacao registro = new ResultadoRasterizacao(imagem, cor, cartesiano);
        int deslocamentoX = x2 - x1;
        int deslocamentoY = cartesiano ? y2 - y1 : y1 - y2; // Deslocamento matemático: norte é Y positivo.
        int dx = Math.abs(deslocamentoX);
        int dy = Math.abs(deslocamentoY);

        // Sinais de incremento para cada eixo
        int sx = deslocamentoX < 0 ? -1 : 1;
        int sy = deslocamentoY < 0 ? -1 : 1;
        boolean trocou = dy > dx;
        registro.explicar(cartesiano ? "Bresenham — entrada cartesiana; Y matemático para cima." : "Bresenham — entrada em pixels; Y matemático é invertido em relação à tela.");
        registro.explicar("ΔX = " + x2 + " - (" + x1 + ") = " + deslocamentoX
                + "; ΔY matemático = " + (cartesiano ? y2 : y1) + " - (" + (cartesiano ? y1 : y2) + ") = " + deslocamentoY);
        registro.explicar("Classificação: " + oitante(deslocamentoX, deslocamentoY)
                + "; sinais sx=" + sx + ", sy=" + sy + "; troca de eixos=" + trocou);
        // Se a reta e mais "vertical" que "horizontal", trocamos dx e dy
        // para que o laco sempre caminhe pelo eixo de maior variacao.
        if (trocou) {
            int tmp = dx;
            dx = dy;
            dy = tmp;
        }
        registro.explicar("Núcleo canônico: (0,0) → (" + dx + "," + dy + "), com 0 ≤ dy ≤ dx.");
        registro.explicar(cartesiano ? (trocou
                ? "Desfazer simetria: X=x1+sx*v; Y=y1+sy*u."
                : "Desfazer simetria: X=x1+sx*u; Y=y1+sy*v.") : trocou
                ? "Desfazer simetria: X=telaX inicial+sx*v; Y=telaY inicial-sy*u."
                : "Desfazer simetria: X=telaX inicial+sx*u; Y=telaY inicial-sy*v.");
        // Variavel de decisao inicial: d = 2*dy - dx
        // Núcleo inc_line do slide 26, aplicado ao segmento canônico (0,0) → (dx,dy).
        // Mantemos E quando d==0; o slide 27 apresenta outra convenção de empate.
        int d = 2 * dy - dx;
        int incE = 2 * dy;         // incremento se escolhe E (só eixo principal avança)
        int incNE = 2 * (dy - dx); // incremento se escolhe NE (ambos avançam)
        registro.explicar("d inicial = 2*" + dy + " - " + dx + " = " + d);
        registro.explicar("IncE = 2*" + dy + " = " + incE
                + "; IncNE = 2*(" + dy + "-" + dx + ") = " + incNE);
        registro.explicar("E/NE são escolhas no núcleo canônico; a simetria recupera as coordenadas de entrada.");
        int alturaCanonica = 0;
        String decisao = "Ponto inicial; d=" + d;
        for (int avanco = 0; avanco <= dx; avanco++) {
            int x = x1 + sx * (trocou ? alturaCanonica : avanco);
            int y = y1 + (cartesiano ? 1 : -1) * sy * (trocou ? avanco : alturaCanonica);
            registro.registrar(String.valueOf(avanco), decisao + "; (u,v)=(" + avanco
                    + "," + alturaCanonica + "); X=" + x1 + "+(" + sx + ")*"
                    + (trocou ? alturaCanonica : avanco) + "=" + x
                    + "; Y=" + y1 + (cartesiano ? "+(" : "-(") + sy + ")*"
                    + (trocou ? avanco : alturaCanonica) + "=" + y, x, y);
            if (avanco == dx) break;
            int anterior = d;
            if (d <= 0) {
                d += incE;
                decisao = "d=" + anterior + " ≤ 0 → E: u avança 1, v não muda; d="
                        + anterior + "+(" + incE + ")=" + d;
            } else {
                // Avanca no eixo "secundario"
                alturaCanonica++;
                d += incNE;
                decisao = "d=" + anterior + " > 0 → NE: u e v avançam 1; d="
                        + anterior + "+(" + incNE + ")=" + d;
            }
            // Avanca no eixo "principal" no incremento do for.
        }
        return registro;
    }

    // <-------------------CLASSIFICAÇÃO ANTES DAS SIMETRIAS------------------->
    private static String oitante(int horizontal, int vertical) {
        if (horizontal == 0 && vertical == 0) return "ponto único";
        if (horizontal == 0) return "eixo vertical (fronteira entre oitantes)";
        if (vertical == 0) return "eixo horizontal (fronteira entre oitantes)";
        if (Math.abs(horizontal) == Math.abs(vertical)) return "diagonal (fronteira entre oitantes)";
        boolean horizontalDomina = Math.abs(horizontal) > Math.abs(vertical);
        int numero;
        if (horizontal > 0 && vertical > 0) numero = horizontalDomina ? 1 : 2;
        else if (horizontal < 0 && vertical > 0) numero = horizontalDomina ? 4 : 3;
        else if (horizontal < 0) numero = horizontalDomina ? 5 : 6;
        else numero = horizontalDomina ? 8 : 7;
        return "oitante " + numero + " (sentido anti-horário, Y matemático para cima)";
    }

}