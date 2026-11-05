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
    private Double retaM, retaB; // reta de reflexão visível (y = m*x + b); null quando não há reflexão em reta
    private static final Color ATUAL = new Color(0x007F87);
    private static final Color ORIGINAL = new Color(0xA3ADBA);
    private static final Color RETA = new Color(0xE8590C);

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

    /**
     * Exibe a reta y = m*x + b usada como eixo de reflexão. Para y = x use m = 1 e b = 0.
     *
     * @author João Victor Araújo Leite dos Santos
     * @date 05/11/2026
     * @param m coeficiente angular da reta
     * @param b intercepto da reta no eixo Y
     */
    public void mostrarReta(double m, double b) {
        retaM = m; retaB = b; repaint();
    }

    /**
     * Oculta a reta de reflexão.
     *
     * @author João Victor Araújo Leite dos Santos
     * @date 05/11/2026
     */
    public void ocultarReta() { retaM = null; retaB = null; repaint(); }

    /**
     * Monta o texto da equação da reta, por exemplo "y = x", "y = -0.5x + 2" ou "y = 3".
     *
     * @author João Victor Araújo Leite dos Santos
     * @date 05/11/2026
     * @param m coeficiente angular da reta
     * @param b intercepto da reta no eixo Y
     * @return equação formatada
     */
    private static String equacao(double m, double b) {
        java.util.function.DoubleFunction<String> f = v -> {
            String t = String.format(java.util.Locale.ROOT,"%.4f",Math.abs(v));
            t = t.replaceAll("0+$","").replaceAll("\\.$","");
            return t;
        };
        StringBuilder t = new StringBuilder("y = ");
        boolean temX = Math.abs(m) >= 0.00005, temB = Math.abs(b) >= 0.00005;
        if (temX) {
            if (m < 0) t.append('-');
            if (Math.abs(Math.abs(m)-1) >= 0.00005) t.append(f.apply(m));
            t.append('x');
        }
        if (temB) {
            if (temX) t.append(b < 0 ? " - " : " + "); else if (b < 0) t.append('-');
            t.append(f.apply(b));
        }
        if (!temX && !temB) t.append('0');
        return t.toString();
    }

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
     * Traça a reta de reflexão atravessando todo o plano visível, tracejada e com a equação ao lado.
     *
     * @author João Victor Araújo Leite dos Santos
     * @date 05/11/2026
     * @param tela contexto gráfico em uso
     * @param largura largura do canvas em pixels
     * @param altura altura do canvas em pixels
     * @param cx coluna do pixel que corresponde à origem
     * @param cy linha do pixel que corresponde à origem
     */
    private void desenharReta(Graphics2D tela, int largura, int altura, int cx, int cy) {
        double m = retaM, b = retaB;
        Graphics2D g2 = (Graphics2D)tela.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(RETA);
        g2.setStroke(new BasicStroke(2f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10f,new float[] {9f,6f},0f));
        // Duas extremidades fora da tela: y = m*x + b convertido para pixels (Y da tela cresce para baixo).
        double x1 = -largura, x2 = 2.0*largura;
        double y1 = cy-(m*((x1-cx)/unidade)+b)*unidade, y2 = cy-(m*((x2-cx)/unidade)+b)*unidade;
        g2.draw(new java.awt.geom.Line2D.Double(x1,y1,x2,y2));
        // Legenda: primeiro ponto visível da reta, partindo da borda direita.
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(getFont().deriveFont(Font.BOLD,13f));
        String texto = equacao(m,b);
        int larguraTexto = g2.getFontMetrics().stringWidth(texto);
        for (int px = largura-10; px > larguraTexto+10; px -= 2) {
            double py = cy-(m*((px-cx)/unidade)+b)*unidade;
            if (py > 24 && py < altura-48) {
                int tx = px-larguraTexto, ty = (int)Math.round(py)-10;
                g2.setColor(new Color(0xF8FAFD)); g2.fillRect(tx-3,ty-13,larguraTexto+6,18); // fundo para a reta não riscar o texto
                g2.setColor(RETA); g2.drawString(texto,tx,ty);
                break;
            }
        }
        g2.dispose();
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
        if (retaM != null) desenharReta(tela,largura,altura,cx,cy);
        tela.drawImage(imagem,0,0,null);
        tela.setColor(ATUAL); tela.setFont(getFont().deriveFont(Font.BOLD,13f));
        for (int i = 0; i < atual[0].length; i++) {
            int x = cx+(int)Math.round(atual[0][i]*unidade), y = cy-(int)Math.round(atual[1][i]*unidade);
            tela.drawString(String.valueOf((char)('A'+i)),x+6,y-7);
        }
        tela.setColor(new Color(0x46566D));
        tela.drawString("ATUAL: verde petróleo   |   ORIGINAL: cinza"+(retaM != null?"   |   RETA DE REFLEXÃO: laranja":""),16,altura-34);
        tela.setFont(getFont().deriveFont(11f));
        tela.drawString("Enquadramento automático • origem fixa no centro • arestas por Ponto Médio",16,altura-15);
        tela.dispose();
    }
}
