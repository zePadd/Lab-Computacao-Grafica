/**
 * Representa os dois cenarios de coordenadas normalizadas de dispositivo
 * (NDC) exigidos pelo laboratorio: o intervalo [0,1] x [0,1] e o intervalo
 * [-1,1] x [-1,1].
 *
 * @author Gabriela
 * @date 22/09/2026
 */
public enum NdcScenario {

    /** NDC variando de 0 a 1 em cada eixo. */
    ZERO_A_UM {
        @Override
        public String toString() {
            return "[0,1] x [0,1]";
        }
    },

    /** NDC variando de -1 a 1 em cada eixo. */
    MENOS_UM_A_UM {
        @Override
        public String toString() {
            return "[-1,1] x [-1,1]";
        }
    }
}
