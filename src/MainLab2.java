import javax.swing.SwingUtilities;
import ui.JanelaLab2;

/** Entrada independente: Main.java continua executando o Lab1 original. */
public final class MainLab2 {
    // <-------------------INICIALIZAÇÃO DA INTERFACE------------------->
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new JanelaLab2().setVisible(true));
    }
}
