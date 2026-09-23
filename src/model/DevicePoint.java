package model;

/**
 * Representa um ponto em coordenadas de dispositivo (DC): um par de
 * indices inteiros de pixel, dcx e dcy, cada um variando de 0 ate
 * (resolucao - 1) no eixo correspondente.
 *
 * @author Gabriela 
 * @date 22/09/2026
 */
public class DevicePoint {

    private final int dcx;
    private final int dcy;

    /**
     * Cria um novo ponto de dispositivo.
     *
     * @param dcx indice horizontal do pixel (coluna)
     * @param dcy indice vertical do pixel (linha)
     */
    public DevicePoint(int dcx, int dcy) {
        this.dcx = dcx;
        this.dcy = dcy;
    }

    /** @return o indice horizontal do pixel */
    public int getDcx() {
        return dcx;
    }

    /** @return o indice vertical do pixel */
    public int getDcy() {
        return dcy;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", dcx, dcy);
    }
}
