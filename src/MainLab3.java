import java.awt.*;
import javax.swing.*;
import ui.PainelLab3;

/**
 * Entrada independente do laboratório de transformações geométricas 2D.
 *
 * @author Horlan Silva de Lacerda
 * @date
 */
public final class MainLab3 {
    /**
     * Impede instâncias da classe de entrada.
     *
     * @author Horlan Silva de Lacerda
     * @date
     */
    private MainLab3() {}

    // <-------------------INICIALIZAÇÃO DO LAB3------------------->
    /**
     * Inicia a janela na thread de eventos do Swing.
     *
     * @author Horlan Silva de Lacerda
     * @date
     * @param args argumentos da linha de comando
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainLab3::iniciar);
    }

    /**
     * Configura a aparência e abre a janela dentro da área útil do monitor.
     *
     * @author Horlan Silva de Lacerda
     * @date
     */
    private static void iniciar() {
        configurarAparencia();
        JFrame janela = new JFrame("Lab3 • Transformações geométricas 2D");
        janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        janela.setContentPane(new PainelLab3());
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        janela.setSize(Math.min(1380,area.width),Math.min(900,area.height));
        janela.setLocationRelativeTo(null); janela.setVisible(true);
    }

    /**
     * Seleciona o tema Nimbus incluído no JDK e configura fontes legíveis.
     *
     * @author Horlan Silva de Lacerda
     * @date
     */
    public static void configurarAparencia() {
        try {
            for (UIManager.LookAndFeelInfo tema : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(tema.getName())) { UIManager.setLookAndFeel(tema.getClassName()); break; }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException erro) {
            // O tema padrão do Java mantém a aplicação funcional se Nimbus não estiver disponível.
            System.err.println("Tema padrão mantido: " + erro.getMessage());
        }
        UIManager.put("Button.font",new Font(Font.SANS_SERIF,Font.BOLD,13));
        UIManager.put("Label.font",new Font(Font.SANS_SERIF,Font.PLAIN,13));
        UIManager.put("ComboBox.font",new Font(Font.SANS_SERIF,Font.PLAIN,13));
        UIManager.put("TextField.font",new Font(Font.SANS_SERIF,Font.PLAIN,14));

    }
}
