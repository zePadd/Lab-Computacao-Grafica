package ui;

import algorithms.AlgoritmoBresenham;
import model.EstadoTransformacoes;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Painel de visualização do plano cartesiano utilizado nas transformações geométricas.
 * O componente renderiza o polígono atual e, quando habilitado, a sua referência original,
 * além de uma grade auxiliar, eixos coordenados e rótulos dos vértices. Todas as arestas
 * são rasterizadas por meio do algoritmo de ponto médio (Bresenham), seguindo a abordagem
 * aplicada no laboratório anterior para manter a representação visual fiel ao modelo matemático.
 *
 * @author João Victor
 * @date 03/11/2026
 */
public final class CanvasTransformacoes extends JPanel {
    private final EstadoTransformacoes estado;
    private boolean originalVisivel = true;
    private double unidade = 45;
    private static final Color ATUAL = new Color(0x007F87);
    private static final Color ORIGINAL = new Color(0xA3ADBA);

    /**
     * Cria o canvas de transformações e associa o painel ao estado compartilhado da aplicação.
     * Além disso, define o tamanho inicial e o nome do componente para facilitar a identificação
     * em testes, depuração e integração com a interface gráfica.
     *
     * @author João Victor
     * @date 03/11/2026
     * @param estado estado matemático compartilhado com a interface
     */
    public CanvasTransformacoes(EstadoTransformacoes estado) {
        this.estado = estado;
        setBackground(new Color(0xF8FAFD));
        setPreferredSize(new Dimension(850,600));
        setMinimumSize(new Dimension(300,260));
        setName("canvasTransformacoes");
    }

    /**
     * Alterna a exibição do polígono original como referência visual para comparação com o estado
     * transformado em tempo real. Quando habilitado, o desenho original permanece sobreposto na tela,
     * permitindo avaliar o efeito das operações de escala, translação, rotação e reflexão.
     *
     * @author João Victor
     * @date 03/11/2026
     * @param mostrar true para exibir a referência original
     */
    public void mostrarOriginal(boolean mostrar) { originalVisivel = mostrar; repaint(); }

    // <-------------------RASTERIZAÇÃO DAS ARESTAS------------------->
    /**
     * Rasteriza uma forma fechada a partir de uma matriz de pontos no sistema cartesiano.
     * A conversão para pixels ocorre somente nesta etapa, respeitando a escala atual do canvas
     * e usando o algoritmo de Bresenham para desenhar cada aresta entre pontos consecutivos.
     *
     * @author João Victor
     * @date 03/11/2026
     * @param imagem framebuffer que recebe os pixels das arestas
     * @param p matriz de pontos em coordenadas homogêneas
     * @param cor cor dos pixels da forma
     */
    private void desenharPoligono(BufferedImage imagem, double[][] p, Color cor) {
        int n = p[0].length;
        for (int i = 0; i < n; i++) {
            int j = (i+1) % n;
            AlgoritmoBresenham.bresenham(imagem,
                    (int)Math.round(p[0][i]*unidade), (int)Math.round(p[1][i]*unidade),
                    (int)Math.round(p[0][j]*unidade), (int)Math.round(p[1][j]*unidade), cor, true);
        }
    }

    /**
     * Redesenha o painel completo a cada atualização do estado de transformações.
     * O método calcula automaticamente a escala visual adequada ao tamanho do componente,
     * gera a grade e os eixos do plano cartesiano, desenha o polígono atual e opcionalmente a
     * versão original e, por fim, marca os vértices com letras para tornar a análise visual mais
     * clara durante as operações geométricas.
     *
     * @author João Victor
     * @date 03/11/2026
     * @param g contexto gráfico recebido pelo Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int largura = getWidth(), altura = getHeight();
        if (largura < 2 || altura < 2) return;
        double[][] atual = estado.getPontos(), inicial = estado.getOriginal();
        // Ajuste uniforme apenas da visualização: as coordenadas e a matriz do objeto não mudam.
        double maxX = 6, maxY = 5;
        for (double[][] p : new double[][][] {atual,inicial})
            for (int i = 0; i < p[0].length; i++) {
                maxX = Math.max(maxX,Math.abs(p[0][i])); maxY = Math.max(maxY,Math.abs(p[1][i]));
            }
        unidade = Math.min(60, Math.min(Math.max(1,largura/2.0-65)/maxX, Math.max(1,altura/2.0-65)/maxY));
        // Limita o percurso rasterizado mesmo em monitores excepcionalmente grandes.
        unidade = Math.min(unidade, 4000/Math.max(maxX,maxY));
        BufferedImage imagem = new BufferedImage(largura,altura,BufferedImage.TYPE_INT_ARGB);
        if (originalVisivel) desenharPoligono(imagem,inicial,ORIGINAL);
        desenharPoligono(imagem,atual,ATUAL);
        Graphics2D tela = (Graphics2D)g.create();
        int cx = largura/2, cy = altura/2;
        double passo = Math.pow(10,Math.floor(Math.log10(55/unidade)));
        if (passo*unidade < 30) passo *= 5;
        tela.setFont(getFont().deriveFont(11f));
        for (int k = -(int)(largura/(2*passo*unidade)); k <= largura/(2*passo*unidade); k++) {
            int x = cx+(int)Math.round(k*passo*unidade);
            tela.setColor(new Color(0xE4EAF2)); tela.drawLine(x,0,x,altura);
            if (k != 0) { tela.setColor(new Color(0x637187)); tela.drawString(String.format(java.util.Locale.ROOT,"%.3g",k*passo),x+3,cy+16); }
        }
        for (int k = -(int)(altura/(2*passo*unidade)); k <= altura/(2*passo*unidade); k++) {
            int y = cy-(int)Math.round(k*passo*unidade);
            tela.setColor(new Color(0xE4EAF2)); tela.drawLine(0,y,largura,y);
            if (k != 0) { tela.setColor(new Color(0x637187)); tela.drawString(String.format(java.util.Locale.ROOT,"%.3g",k*passo),cx+5,y-4); }
        }
        tela.setColor(new Color(0x7B8AA0)); tela.drawLine(0,cy,largura,cy); tela.drawLine(cx,0,cx,altura);
        tela.drawString("0",cx-13,cy+16); tela.drawString("X",largura-20,cy-8); tela.drawString("Y",cx+8,20);
        tela.drawImage(imagem,0,0,null);
        tela.setColor(ATUAL); tela.setFont(getFont().deriveFont(Font.BOLD,13f));
        for (int i = 0; i < atual[0].length; i++) {
            int x = cx+(int)Math.round(atual[0][i]*unidade), y = cy-(int)Math.round(atual[1][i]*unidade);
            tela.drawString(String.valueOf((char)('A'+i)),x+6,y-7);
        }
        tela.setColor(new Color(0x46566D));
        tela.drawString("ATUAL: verde petróleo   |   ORIGINAL: cinza",16,altura-34);
        tela.setFont(getFont().deriveFont(11f));
        tela.drawString("Enquadramento automático • origem fixa no centro • arestas por Ponto Médio",16,altura-15);
        tela.dispose();
    }
}
