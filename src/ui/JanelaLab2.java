package ui;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;

/** Moldura da aplicação; o conteúdo fica no painel para facilitar a verificação. */
public final class JanelaLab2 extends JFrame {
    // <-------------------JANELA PERMANENTE DO LAB2------------------->
    public JanelaLab2() {
        super("Laboratório 2 - Rasterização e cálculos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(new PainelLab2());
        Rectangle disponivel = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(new Dimension(Math.min(1200, disponivel.width), Math.min(900, disponivel.height)));
        setLocationRelativeTo(null);
    }
}
