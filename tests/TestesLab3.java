import algorithms.AlgoritmoBresenham;
import model.*;
import transformer.*;
import ui.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Testes executáveis sem dependências externas para álgebra, exemplos de aula, rasterização e interface.
 *
 * @author Gabriela Almeida de Andrade
 * @date 03/11/2026
 */
public final class TestesLab3 {
    private static int verificacoes;

    /**
     * Impede instâncias da suíte.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     */
    private TestesLab3() {}

    /**
     * Executa testes matemáticos e de interface; a pasta opcional recebe capturas PNG.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     * @param args argumentos da linha de comando
     * @throws Exception se a execução dos testes ou da interface falhar
     */
    public static void main(String[] args) throws Exception {
        matematica(); rasterizacao();
        SwingUtilities.invokeAndWait(() -> interfaceGrafica(args.length>0?args[0]:null));
        System.out.println("OK: "+verificacoes+" verificações concluídas.");
    }

    /**
     * Compara um escalar com tolerância de ponto flutuante.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     * @param atual resultado observado
     * @param esperado resultado de referência
     */
    private static void perto(double atual, double esperado) {
        verificacoes++;
        if (!Double.isFinite(atual) || Math.abs(atual-esperado)>1e-8)
            throw new AssertionError("Esperado "+esperado+"; obtido "+atual);
    }

    /**
     * Compara matrizes elemento a elemento.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     * @param atual resultado observado
     * @param esperado resultado de referência
     */
    private static void matriz(double[][] atual, double[][] esperado) {
        perto(atual.length,esperado.length); perto(atual[0].length,esperado[0].length);
        for (int i=0;i<atual.length;i++) for(int j=0;j<atual[0].length;j++) perto(atual[i][j],esperado[i][j]);
    }

    /**
     * Exige rejeição de entradas inválidas por IllegalArgumentException.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     * @param acao operação que deve lançar a exceção esperada
     */
    private static void rejeita(Runnable acao) {
        verificacoes++;
        try { acao.run(); } catch (IllegalArgumentException esperado) { return; }
        throw new AssertionError("Entrada inválida aceita.");
    }

    // <-------------------ORÁCULOS MATEMÁTICOS INDEPENDENTES------------------->
    /**
     * Verifica exemplos de sala, invariantes, composição não comutativa, singularidades e cópias defensivas.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     */
    private static void matematica() {
        matriz(MatrizUtils.multiplicar(new double[][]{{1,2,3},{4,5,6}},new double[][]{{7,8},{9,10},{11,12}}),new double[][]{{58,64},{139,154}});
        rejeita(() -> MatrizUtils.multiplicar(new double[][]{{1,2}},new double[][]{{1,2}}));
        rejeita(() -> MatrizUtils.validar(new double[][]{{1},{1,2}}));
        rejeita(() -> MatrizUtils.validar(new double[][]{{Double.NaN}}));
        rejeita(() -> MatrizUtils.validar(null));
        rejeita(() -> MatrizUtils.multiplicar(new double[][]{{Double.MAX_VALUE}},new double[][]{{2}}));
        double[][] p = {{0,1,1,0},{0,0,1,1},{1,1,1,1}};
        matriz(MatrizUtils.multiplicar(Transformacoes2D.translacao(1,1),p),new double[][]{{1,2,2,1},{1,1,2,2},{1,1,1,1}});
        matriz(MatrizUtils.multiplicar(Transformacoes2D.rotacao(90),p),new double[][]{{0,0,-1,-1},{0,1,1,0},{1,1,1,1}});
        matriz(MatrizUtils.multiplicar(Transformacoes2D.escala(2,2),p),new double[][]{{0,2,2,0},{0,0,2,2},{1,1,1,1}});
        double[][] ponto={{2},{3},{1}};
        double[][][] reflexos={{{2},{-3},{1}},{{-2},{3},{1}},{{-2},{-3},{1}},{{3},{2},{1}}};
        for(int i=0;i<4;i++) matriz(MatrizUtils.multiplicar(Transformacoes2D.reflexao(i),ponto),reflexos[i]);
        matriz(MatrizUtils.multiplicar(Transformacoes2D.cisalhamento(1,0),p),new double[][]{{0,1,2,1},{0,0,1,1},{1,1,1,1}});
        matriz(MatrizUtils.multiplicar(Transformacoes2D.cisalhamento(0,1),ponto),new double[][]{{2},{5},{1}});
        matriz(MatrizUtils.multiplicar(Transformacoes2D.cisalhamento(.5,1),ponto),new double[][]{{3.5},{5},{1}});
        double[][] sequencial=Transformacoes2D.compor(Transformacoes2D.cisalhamento(.5,0),Transformacoes2D.cisalhamento(0,1));
        perto(sequencial[1][1],1.5); perto(Transformacoes2D.cisalhamento(.5,1)[1][1],1);
        double[][] escalaFixa=Transformacoes2D.pontoFixo(Transformacoes2D.escala(2,2),2,1);
        matriz(MatrizUtils.multiplicar(escalaFixa,new double[][]{{2,4},{1,1},{1,1}}),new double[][]{{2,6},{1,1},{1,1}});
        matriz(MatrizUtils.multiplicar(Transformacoes2D.pontoFixo(Transformacoes2D.rotacao(90),2,1),new double[][]{{2,4},{1,1},{1,1}}),new double[][]{{2,2},{1,3},{1,1}});
        double[][] tr=Transformacoes2D.compor(Transformacoes2D.translacao(1,0),Transformacoes2D.rotacao(90));
        double[][] rt=Transformacoes2D.compor(Transformacoes2D.rotacao(90),Transformacoes2D.translacao(1,0));
        matriz(MatrizUtils.multiplicar(tr,new double[][]{{1},{0},{1}}),new double[][]{{0},{2},{1}});
        matriz(MatrizUtils.multiplicar(rt,new double[][]{{1},{0},{1}}),new double[][]{{1},{1},{1}});
        // Oráculo analítico de reflexão: distância assinada à reta, independente da composição implementada.
        for(double m:new double[]{-2,0,.5,1,3}) for(double b:new double[]{-3,0,2}) {
            double d=(2*m-3+b)/(m*m+1);
            double[][] r=Transformacoes2D.reflexaoReta(m,b);
            matriz(MatrizUtils.multiplicar(r,ponto),new double[][]{{2-2*m*d},{3+2*d},{1}});
            matriz(MatrizUtils.multiplicar(r,r),MatrizUtils.identidade());
            matriz(MatrizUtils.multiplicar(r,new double[][]{{2},{2*m+b},{1}}),new double[][]{{2},{2*m+b},{1}});
        }
        for(int i=0;i<3;i++) { Poligono forma=Poligono.criar(i); perto(forma.getPontos()[0][0],0); perto(forma.getPontos()[1][0],0); }
        double[][] tri=Poligono.criar(0).getPontos();
        for(int i=0;i<3;i++) perto(Math.hypot(tri[0][i]-tri[0][(i+1)%3],tri[1][i]-tri[1][(i+1)%3]),4);
        EstadoTransformacoes e=new EstadoTransformacoes();
        double[][] copia=e.getOriginal(); copia[0][0]=99; perto(e.getOriginal()[0][0],0);
        e.aplicar(Transformacoes2D.translacao(1,2),"T"); double[][] antes=e.getPontos();
        e.aplicar(Transformacoes2D.escala(0,0),"S zero"); e.desfazer(); matriz(e.getPontos(),antes);
        rejeita(() -> e.aplicar(Transformacoes2D.translacao(20000,0),"fora")); matriz(e.getPontos(),antes);
        e.reiniciar(); matriz(e.getMatriz(),MatrizUtils.identidade()); e.desfazer(); perto(e.getHistorico().size(),0);
        e.selecionar(2); perto(e.getPontos()[0].length,7);
    }

    /**
     * Verifica Ponto Médio em oito oitantes, eixos, ponto degenerado e exemplo numérico da G3.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     */
    private static void rasterizacao() {
        int[][] destinos={{10,8},{8,10},{-8,10},{-10,8},{-10,-8},{-8,-10},{8,-10},{10,-8},{10,0},{0,10},{0,0}};
        for(int[] fim:destinos) {
            BufferedImage im=new BufferedImage(61,61,BufferedImage.TYPE_INT_ARGB);
            AlgoritmoBresenham.bresenham(im,0,0,fim[0],fim[1],Color.RED,true);
            perto(im.getRGB(30,30),Color.RED.getRGB()); perto(im.getRGB(30+fim[0],30-fim[1]),Color.RED.getRGB());
            int pixels=0; for(int y=0;y<61;y++)for(int x=0;x<61;x++)if(im.getRGB(x,y)==Color.RED.getRGB())pixels++;
            perto(pixels,Math.max(Math.abs(fim[0]),Math.abs(fim[1]))+1);
        }
        BufferedImage im=new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
        AlgoritmoBresenham.bresenham(im,20,10,30,18,Color.RED,true);
        int[] ys={10,11,12,12,13,14,15,16,16,17,18};
        for(int i=0;i<ys.length;i++)perto(im.getRGB(70+i,50-ys[i]),Color.RED.getRGB());
    }

    // <-------------------INTEGRAÇÃO SWING E CAPTURAS------------------->
    /**
     * Procura um controle pelo nome atribuído para acessibilidade e automação.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     * @param pai contêiner onde a busca começa
     * @param nome nome descritivo da forma ou operação
     * @return componente encontrado ou null
     */
    private static Component buscar(Container pai, String nome) {
        for(Component c:pai.getComponents()) {
            if(nome.equals(c.getName()))return c;
            if(c instanceof Container) {Component achado=buscar((Container)c,nome);if(achado!=null)return achado;}
        }
        return null;
    }

    /**
     * Executa o layout recursivamente para renderizar o painel sem monitor físico.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     * @param c contêiner cujo layout será calculado
     */
    private static void layout(Container c) {
        c.doLayout();for(Component filho:c.getComponents())if(filho instanceof Container)layout((Container)filho);
    }

    /**
     * Testa seleção, aplicação, desfazer e restauração na thread do Swing e exporta capturas opcionais.
     *
     * @author Gabriela Almeida de Andrade
     * @date 03/11/2026
     * @param pasta diretório opcional para salvar capturas
     */
    private static void interfaceGrafica(String pasta) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception erro) { throw new AssertionError(erro); }
        PainelLab3 painel=new PainelLab3();painel.setSize(1380,860);layout(painel);
        JComboBox<?> op=(JComboBox<?>)buscar(painel,"operacao");
        JButton aplicar=(JButton)buscar(painel,"aplicar"), desfazer=(JButton)buscar(painel,"desfazer");
        for(int i=0;i<13;i++) {
            op.setSelectedIndex(i);aplicar.doClick();
            if(!desfazer.isEnabled())throw new AssertionError("Operação não aplicada: "+i);
            verificacoes++;desfazer.doClick();if(desfazer.isEnabled())throw new AssertionError("Desfazer falhou.");
        }
        ((JComboBox<?>)buscar(painel,"forma")).setSelectedIndex(2);
        op.setSelectedIndex(2);((JTextField)buscar(painel,"parametro0")).setText("30");aplicar.doClick();
        op.setSelectedIndex(0);((JTextField)buscar(painel,"parametro0")).setText("1,5");aplicar.doClick();
        if(pasta!=null) {
            new File(pasta).mkdirs();
            for(int largura:new int[]{1380,1024}) {
                painel.setSize(largura,largura==1380?860:720);layout(painel);
                BufferedImage captura=new BufferedImage(painel.getWidth(),painel.getHeight(),BufferedImage.TYPE_INT_RGB);
                Graphics2D g=captura.createGraphics();painel.printAll(g);g.dispose();
                try { ImageIO.write(captura,"png",new File(pasta,"lab3-"+largura+".png")); }
                catch(Exception erro) {throw new AssertionError(erro);}
            }
        }
        ((JButton)buscar(painel,"restaurar")).doClick();if(desfazer.isEnabled())throw new AssertionError("Restaurar falhou.");
        verificacoes++;
    }
}
