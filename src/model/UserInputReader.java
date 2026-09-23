package model;

import ui.EntradaUsuario;

import javax.swing.JOptionPane;

/**
 * Responsavel por coletar, junto ao usuario, a janela de mundo, o ponto de
 * mundo e o cenario de NDC (requisitos 1 e 2 do laboratorio), usando
 * caixas de dialogo simples do Swing (JOptionPane). Toda entrada invalida
 * e revalidada em loop, sem derrubar o programa.
 *
 * @author Suelle
 * @date 23/09/2026
 */
public final class UserInputReader {

    private UserInputReader() {
        // Classe utilitaria: nao deve ser instanciada.
    }

    /**
     * Coleta a janela de mundo e o ponto de mundo em sequencia.
     *
     * @return a entrada completa do usuario, pronta para ser transformada
     */
    public static EntradaUsuario lerEntradaCompleta() {
        WorldWindow janela = lerJanelaDeMundo();
        PointD ponto = lerPontoMundo(janela);
        return new EntradaUsuario(janela, ponto);
    }

    /**
     * Pergunta ao usuario os quatro limites da janela de mundo, repetindo
     * a pergunta enquanto os valores forem invalidos (ex: xMax menor que
     * xMin).
     *
     * @return a janela de mundo validada
     */
    public static WorldWindow lerJanelaDeMundo() {
        while (true) {
            try {
                double xMin = lerDouble("Digite xMin da janela de mundo:");
                double xMax = lerDouble("Digite xMax da janela de mundo:");
                double yMin = lerDouble("Digite yMin da janela de mundo:");
                double yMax = lerDouble("Digite yMax da janela de mundo:");
                return new WorldWindow(xMin, xMax, yMin, yMax);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null,
                    "Entrada invalida: " + e.getMessage() + "\nTente novamente.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Pergunta ao usuario o ponto de mundo a ser transformado.
     *
     * @param janela a janela de mundo ja definida, exibida como referencia
     * @return o ponto de mundo informado
     */
    public static PointD lerPontoMundo(WorldWindow janela) {
        double x = lerDouble("Digite a coordenada X do ponto\n(janela de mundo: " + janela + "):");
        double y = lerDouble("Digite a coordenada Y do ponto\n(janela de mundo: " + janela + "):");
        return new PointD(x, y);
    }

    /**
     * Pergunta ao usuario qual dos dois cenarios de NDC ele quer
     * visualizar primeiro (requisito 2 do laboratorio).
     *
     * @return o cenario de NDC escolhido
     */
    public static NdcScenario lerCenarioNdc() {
        String[] opcoes = {"[0,1] x [0,1]", "[-1,1] x [-1,1]"};
        int escolha = JOptionPane.showOptionDialog(
            null,
            "Escolha o cenario de coordenadas normalizadas de dispositivo (NDC)\nque deseja visualizar agora:",
            "Cenario de NDC",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcoes,
            opcoes[0]
        );
        return (escolha == 1) ? NdcScenario.MENOS_UM_A_UM : NdcScenario.ZERO_A_UM;
    }

    /**
     * Pede um numero em ponto flutuante ao usuario, revalidando
     * enquanto o texto digitado nao puder ser interpretado como numero.
     * Aceita tanto ponto quanto virgula como separador decimal.
     *
     * @param mensagem o texto exibido na caixa de dialogo
     * @return o numero digitado pelo usuario
     */
    private static double lerDouble(String mensagem) {
        while (true) {
            String texto = JOptionPane.showInputDialog(mensagem);
            if (texto == null) {
                // Usuario cancelou a caixa de dialogo.
                System.exit(0);
            }
            try {
                return Double.parseDouble(texto.trim().replace(",", "."));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                    "Digite um numero valido (use ponto ou virgula como separador decimal).",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
