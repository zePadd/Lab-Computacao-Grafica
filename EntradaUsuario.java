/**
 * Agrupa os dois dados que o usuario fornece no requisito 1 do
 * laboratorio: a janela de mundo e o ponto de mundo a ser transformado.
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
