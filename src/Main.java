import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JOptionPane;

import model.*;
import transformer.CoordinateTransformer;
import ui.EntradaUsuario;
import ui.PixelRenderer;


/**
 * Classe principal do laboratorio de transformacoes de coordenadas.
 * Integra a entrada do usuario ({@link UserInputReader}), o nucleo
 * matematico ({@link CoordinateTransformer}) e a renderizacao do pixel
 * ativo ({@link PixelRenderer}), cobrindo todos os requisitos do
 * enunciado: janela de mundo definida pelo usuario, calculo nos dois
 * cenarios de NDC, e exibicao do pixel ativo na resolucao real da tela.
 *
 * @author João Victor
 * @since 23/09/2026
 */
public class Main {

    /** Cor do pixel ativo é verde (0x00FF00)*/
    private static final Color COR_PIXEL = new Color(0x00FF00);

    public static void main(String[] args) {
        EntradaUsuario entrada = UserInputReader.lerEntradaCompleta();
        WorldWindow janela = entrada.getJanela();
        PointD pontoMundo = entrada.getPonto();

        Dimension resolucao = PixelRenderer.obterResolucaoDaTela();
        int ndh = resolucao.width;
        int ndv = resolucao.height;

        DevicePoint[] pontosPorCenario = new DevicePoint[NdcScenario.values().length];
        StringBuilder resumo = new StringBuilder();
        resumo.append("Janela de mundo: ").append(janela).append('\n');
        resumo.append("Ponto informado: ").append(pontoMundo).append('\n');
        resumo.append("Resolucao do dispositivo (NDH x NDV): ").append(ndh).append(" x ").append(ndv).append("\n\n");

        for (NdcScenario cenario : NdcScenario.values()) {
            PointD ndc = CoordinateTransformer.userToNdc(janela, pontoMundo, cenario);
            DevicePoint dc = CoordinateTransformer.ndcToDc(ndc, ndh, ndv, cenario);
            pontosPorCenario[cenario.ordinal()] = dc;

            resumo.append("Cenario ").append(cenario).append(":\n");
            resumo.append("  NDC = ").append(ndc).append('\n');
            resumo.append("  Pixel no dispositivo (DC) = ").append(dc).append("\n\n");
        }

        System.out.println(resumo);
        JOptionPane.showMessageDialog(null, resumo.toString(),
            "Resultado do calculo", JOptionPane.INFORMATION_MESSAGE);

        NdcScenario cenarioEscolhido = UserInputReader.lerCenarioNdc();
        DevicePoint pontoParaExibir = pontosPorCenario[cenarioEscolhido.ordinal()];

        JOptionPane.showMessageDialog(null,
            "Ao clicar em OK, a tela cheia vai abrir com o pixel verde aceso em "
                + pontoParaExibir + " (cenario " + cenarioEscolhido + ").\n"
                + "Feche a janela em tela cheia (Alt+F4 ou fechar pela lupa) para continuar.",
            "Pronto para exibir", JOptionPane.INFORMATION_MESSAGE);

        PixelRenderer.exibirPixelAtivo(pontoParaExibir, resolucao, COR_PIXEL);

        NdcScenario outroCenario = (cenarioEscolhido == NdcScenario.ZERO_A_UM)
            ? NdcScenario.MENOS_UM_A_UM
            : NdcScenario.ZERO_A_UM;

        int resposta = JOptionPane.showConfirmDialog(null,
            "Deseja tambem visualizar o pixel do cenario " + outroCenario + "\n"
                + "para comparar com o cenario anterior?",
            "Mostrar outro cenario", JOptionPane.YES_NO_OPTION);

        if (resposta == JOptionPane.YES_OPTION) {
            PixelRenderer.exibirPixelAtivo(pontosPorCenario[outroCenario.ordinal()], resolucao, COR_PIXEL);
        }

        System.out.println("Execucao encerrada.");
    }
}
