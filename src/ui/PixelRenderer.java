package ui;

import model.*;
import transformer.*;

/*
> AWT (Abstract Window Toolkit)
é o pacote original do Java (existe desde a versão 1.0) voltado para a criação de
interfaces gráficas de usuário para aplicações de desktop

> A ideia central do AWT é fazer uma ponte direta com os componentes gráficos nativos do sistema operacional:
quando você cria um botão AWT no Windows, por baixo dos panos ele é literalmente um botão do Windows;
no Linux, é um botão do toolkit gráfico do Linux

> Ela foi a precursora do Java Swing (mais leve e com mais recursos) inclusive, que só veio na versão 1.2 do Java

> Qualque objeto de interface é chamado de 'Component'
 */
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Responsável por descobrir a resolução real do dispositivo e por acender
 * exatamente um pixel (com cor verde) na posição calculada.
 * Todo o desenho visual (a saída do programa) acontece aqui. O Uso das bibliotecas segue assim:
 * <ul>
 *     <li>{@code AWT} - Responsável pela manipulação de baixo nível e comunicação com o hardware. Utilizado para obter a resolução física do monitor (Toolkit, Dimension), definir a cor (Color) e alocar o framebuffer virtual (BufferedImage/Graphics2D) para a manipulação direta da matriz de pixels na memória.</li>
 *     <li>{@code Swing} - Responsável pela interface gráfica de alto nível. Fornece a estrutura da janela em tela cheia e sem bordas (JFrame), o contêiner para projetar a imagem na tela (JLabel) e as caixas de diálogo interativas (JOptionPane).</li>
 * </ul>
 *
 * @author Georis
 * @author Neto
 * @date 23/09/2026
 */
public final class PixelRenderer {

    private static final Color COR_FUNDO = Color.BLACK;

    private PixelRenderer() {
        // Contrutor privado porque é Classe utilitária, não é para ser instanciada
    }

    /**
     * Descobre a resolução real da tela do computador em uso, através do toolkit de awt nativo de java
     *
     * @return a largura e altura, em pixels, da tela atual
     */
    public static Dimension obterResolucaoDaTela() {return Toolkit.getDefaultToolkit().getScreenSize();}

    /**
     * Ativa exatamente um pixel na posicao (x, y) da imagem, na cor
     * informada. Esta e a operação de mais baixo nivel disponivel em Java para setar a cor
     * de um unico pixel do framebuffer, sem envolver nenhuma primitiva
     * geometrica. Deve ser chamada somente depois que as coordenadas de
     * dispositivo (DC_x, DC_y) já tiverem sido calculadas pelas
     * transformações de {@link CoordinateTransformer}.
     *
     * @param imagem o framebuffer (BufferedImage) onde o pixel será aceso
     * @param x a coluna do pixel (coordenada horizontal de dispositivo)
     * @param y a linha do pixel (coordenada vertical de dispositivo)
     * @param cor a cor a ser atribuida ao pixel (ex: 0x00FF00)
     */
    public static void ligaPixel(BufferedImage imagem, int x, int y, Color cor) {
        imagem.setRGB(x, y, cor.getRGB());
    }

    /**
     * Abre uma janela do tamanho exato da tela, idêntica a um display
     * comum: sem moldura, sem reticulados, com fundo totalmente limpo
     * (uma unica cor solida), e acende um único pixel na posicao
     * informada usando {@link #ligaPixel}. Nenhum outro elemento visual
     * e desenhado, conforme exigido pelo professor.
     *
     * @param pontoAtivo o ponto em coordenadas de dispositivo a ser aceso
     * @param resolucao a resolucao real da tela (de {@link #obterResolucaoDaTela})
     * @param corDoPixel a cor do pixel a ser aceso (0x00FF00, conforme solicitado)
     */
    public static void exibirPixelAtivo(DevicePoint pontoAtivo, Dimension resolucao, Color corDoPixel) {
        int largura = resolucao.width;
        int altura = resolucao.height;

        if (pontoAtivo.getDcx() < 0 || pontoAtivo.getDcx() >= largura
                || pontoAtivo.getDcy() < 0 || pontoAtivo.getDcy() >= altura) {
            JOptionPane.showMessageDialog(null,
                    "O ponto calculado " + pontoAtivo + " cai fora da resolucao da tela ("
                            + largura + "x" + altura + "). Verifique se o ponto informado esta dentro "
                            + "da janela de mundo definida.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Cria uma imagem em branco na memória, exatamente do tamanho do monitor do usuário, usando o padrão de cor RGB (display virtual)
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = imagem.createGraphics(); // objeto gráfico de 2 dimensões a partir da nossa magem
        g2d.setColor(COR_FUNDO); // Pinta o fundo de preto
        g2d.fillRect(0, 0, largura, altura);
        g2d.dispose(); // Libera da memória

        // Acende o pixel ativo, ja depois de todas as transformacoes de
        // coordenadas terem sido calculadas em Main.
        drawPixel(imagem, pontoAtivo.getDcx(), pontoAtivo.getDcy(), corDoPixel);

        exibirJanelaComImagem(imagem);
    }

    /**
     * Pega a imagem que acabamos de manipular e a exibe em tela cheia.
     */
    private static void exibirJanelaComImagem(BufferedImage imagem) {
        JFrame frame = new JFrame(); // Cria uma janela padrão do Windows/Linux/Mac (Swing)
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true); // Remove qualquer decoração da janela

        // Coloca a nossa imagem (com fundo preto e 1 pixel verde) dentro de um componente visual (JLabel)
        JLabel labelImagem = new JLabel(new ImageIcon(imagem)); // ImageIcon pega a sua matriz crua e a converte em um ativo visual

        // Centraliza o componente
        frame.getContentPane().add(labelImagem, BorderLayout.CENTER);

        frame.pack(); // Ajusta o tamanho da janela para abraçar o tamanho exato da imagem
        frame.setLocation(0, 0); // Posiciona a janela no canto superior esquerdo do monitor, paar o fullscreen
        frame.setVisible(true); // RAM -> User
    }
}
