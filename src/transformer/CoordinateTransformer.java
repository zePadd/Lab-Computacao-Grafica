package transformer;

import model.*;

/**
 * Implementa os quatro procedimentos de transformação de coordenadas
 * pedidos no exercicio da Aula G2: user_to_ndc, ndc_to_user, ndc_to_dc e
 * inp_to_ndc. Cada procedimento é parametrizado pelo {@link NdcScenario}
 * (intervalo [0,1] ou intervalo [-1,1]), como pede o requisito 2 do
 * laboratorio.
 *
 * Toda a matematica desta classe vem da mesma ideia: igualar a fração
 * percorrida dentro de um intervalo de origem com a fração percorrida
 * dentro do intervalo de destino (regra de três entre intervalos).
 *
 * @author Horlan
 * @since 23/09/2026
 */
public final class CoordinateTransformer {

    private CoordinateTransformer() {
        // Classe utilitaria que não deve ser instanciada
    }

    /**
     * Transforma um ponto de coordenadas de mundo para coordenadas NDC
     * (user_to_ndc).
     *
     * @param janela a janela de mundo definida pelo usuario
     * @param pontoMundo o ponto em coordenadas de mundo
     * @param cenario o intervalo de NDC desejado
     * @return o ponto correspondente em coordenadas NDC
     */
    public static PointD userToNdc(WorldWindow janela, PointD pontoMundo, NdcScenario cenario) {
        double fracaoX = (pontoMundo.getX() - janela.getXMin()) / janela.getWidth();
        double fracaoY = (pontoMundo.getY() - janela.getYMin()) / janela.getHeight();

        if (cenario == NdcScenario.ZERO_A_UM) {
            return new PointD(fracaoX, fracaoY);
        }

        double ndcX = 2 * fracaoX - 1;
        double ndcY = 2 * fracaoY - 1;
        return new PointD(ndcX, ndcY);
    }

    /**
     * Transforma um ponto de coordenadas NDC de volta para coordenadas de
     * mundo (ndc_to_user). E a operação inversa de {@link #userToNdc}.
     *
     * @param janela a janela de mundo definida pelo usuario
     * @param pontoNdc o ponto em coordenadas NDC
     * @param cenario o intervalo de NDC em que pontoNdc esta expresso
     * @return o ponto correspondente em coordenadas de mundo
     */
    public static PointD ndcToUser(WorldWindow janela, PointD pontoNdc, NdcScenario cenario) {
        double fracaoX;
        double fracaoY;

        if (cenario == NdcScenario.ZERO_A_UM) {
            fracaoX = pontoNdc.getX();
            fracaoY = pontoNdc.getY();
        } else {
            fracaoX = (pontoNdc.getX() + 1) / 2.0;
            fracaoY = (pontoNdc.getY() + 1) / 2.0;
        }

        double x = janela.getXMin() + fracaoX * janela.getWidth();
        double y = janela.getYMin() + fracaoY * janela.getHeight();
        return new PointD(x, y);
    }

    /**
     * Transforma um ponto de coordenadas NDC para coordenadas de
     * dispositivo (ndc_to_dc), arredondando para o pixel inteiro mais
     * proximo.
     *
     * @param pontoNdc o ponto em coordenadas NDC
     * @param ndh numero de pixels horizontais do dispositivo (resolucao horizontal)
     * @param ndv numero de pixels verticais do dispositivo (resolucao vertical)
     * @param cenario o intervalo de NDC em que pontoNdc esta expresso
     * @return o ponto em coordenadas de dispositivo (indices de pixel inteiros)
     */
    public static DevicePoint ndcToDc(PointD pontoNdc, int ndh, int ndv, NdcScenario cenario) {
        double fracaoX;
        double fracaoY;

        if (cenario == NdcScenario.ZERO_A_UM) {
            fracaoX = pontoNdc.getX();
            fracaoY = pontoNdc.getY();
        } else {
            fracaoX = (pontoNdc.getX() + 1) / 2.0;
            fracaoY = (pontoNdc.getY() + 1) / 2.0;
        }

        int dcx = (int) Math.round((ndh - 1) * fracaoX);
        int dcy = (int) Math.round((ndv - 1) * fracaoY);
        return new DevicePoint(dcx, dcy);
    }

    /**
     * Transforma um ponto de coordenadas de dispositivo (entrada) para
     * coordenadas NDC (inp_to_ndc). E a operação inversa de
     * {@link #ndcToDc}, sem a etapa de arredondamento (que não é
     * reversivel).
     *
     * @param pontoDispositivo o ponto em coordenadas de dispositivo
     * @param ndh numero de pixels horizontais do dispositivo
     * @param ndv numero de pixels verticais do dispositivo
     * @param cenario o intervalo de NDC desejado na saida
     * @return o ponto correspondente em coordenadas NDC
     */
    public static PointD inpToNdc(DevicePoint pontoDispositivo, int ndh, int ndv, NdcScenario cenario) {
        double fracaoX = (double) pontoDispositivo.getDcx() / (ndh - 1);
        double fracaoY = (double) pontoDispositivo.getDcy() / (ndv - 1);

        if (cenario == NdcScenario.ZERO_A_UM) {
            return new PointD(fracaoX, fracaoY);
        }

        double ndcX = 2 * fracaoX - 1;
        double ndcY = 2 * fracaoY - 1;
        return new PointD(ndcX, ndcY);
    }
}