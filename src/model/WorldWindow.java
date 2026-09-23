package model;

/**
 * Representa a janela de mundo definida pelo usuario: os limites minimo e
 * maximo, em cada eixo, da regiao de coordenadas de mundo que sera mapeada
 * para a tela (requisito 1 do laboratorio).
 *
 * @author Horlan
 * @since 23/09/2026
 */
public class WorldWindow {

    private final double xMin;
    private final double xMax;
    private final double yMin;
    private final double yMax;

    /**
     * Cria uma nova janela de mundo.
     *
     * @param xMin limite inferior de x
     * @param xMax limite superior de x
     * @param yMin limite inferior de y
     * @param yMax limite superior de y
     * @throws IllegalArgumentException se xMax menor ou igual a xMin, ou
     *         yMax menor ou igual a yMin
     */
    public WorldWindow(double xMin, double xMax, double yMin, double yMax) {
        if (xMax <= xMin || yMax <= yMin) {
            throw new IllegalArgumentException(
                "xMax deve ser maior que xMin, e yMax deve ser maior que yMin.");
        }
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    /** @return limite inferior de x da janela */
    public double getXMin() {
        return xMin;
    }

    /** @return limite superior de x da janela */
    public double getXMax() {
        return xMax;
    }

    /** @return limite inferior de y da janela */
    public double getYMin() {
        return yMin;
    }

    /** @return limite superior de y da janela */
    public double getYMax() {
        return yMax;
    }

    /** @return a largura da janela, ou seja, xMax - xMin */
    public double getWidth() {
        return xMax - xMin;
    }

    /** @return a altura da janela, ou seja, yMax - yMin */
    public double getHeight() {
        return yMax - yMin;
    }

    @Override
    public String toString() {
        return String.format("x em [%.2f, %.2f], y em [%.2f, %.2f]", xMin, xMax, yMin, yMax);
    }
}
