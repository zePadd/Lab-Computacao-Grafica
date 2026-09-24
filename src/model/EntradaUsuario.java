package model;

/**
 * Agrupa os dois dados que o usuario fornece: a janela de mundo e o ponto de mundo a ser transformado.
 * É o DTO (Data Transfer Object) porque recebe diretamento os dados de {@link ui.UserInputReader}, garantindo um fluxo
 * ui >> Model
 *
 * @author Suelle
 * @date 22/09/2026
 */
public class EntradaUsuario {

    private final WorldWindow janela;
    private final PointD ponto;

    /**
     * Cria um novo agrupamento de entrada do usuario.
     *
     * @param janela a janela de mundo informada
     * @param ponto o ponto de mundo informado
     */
    public EntradaUsuario(WorldWindow janela, PointD ponto) {
        this.janela = janela;
        this.ponto = ponto;
    }

    /** @return a janela de mundo informada pelo usuario */
    public WorldWindow getJanela() {
        return janela;
    }

    /** @return o ponto de mundo informado pelo usuario */
    public PointD getPonto() {
        return ponto;
    }
}
