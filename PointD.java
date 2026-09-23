/**
 * Representa um ponto bidimensional com coordenadas em ponto flutuante
 * (double). E usado tanto para coordenadas de mundo quanto para coordenadas
 * NDC, ja que as duas sao continuas.
 *
 * @author Gabriela 
 * @date 22/09/2026
 */
public class PointD {

    private final double x;
    private final double y;

    /**
     * Cria um novo ponto.
     *
     * @param x coordenada horizontal
     * @param y coordenada vertical
     */
    public PointD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** @return a coordenada horizontal do ponto */
    public double getX() {
        return x;
    }

    /** @return a coordenada vertical do ponto */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("(%.4f, %.4f)", x, y);
    }
}
