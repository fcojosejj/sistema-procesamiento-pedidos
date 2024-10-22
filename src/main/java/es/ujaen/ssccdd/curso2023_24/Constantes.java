package es.ujaen.ssccdd.curso2023_24;

import java.util.Random;

public interface Constantes {

    enum Articulos {
        TECLADO, RATON, MONITOR, AURICULARES, PORTATIL, IMPRESORA, ALTAVOCES, WEBCAM, MICROFONO, ROUTER
    }

    Random aleatorio = new Random();

    public static final int MIN_ARTICULOS = 4;
    public static final int MAX_ARTICULOS = 6;
    public static final int MIN_VALORACION = 1;
    public static final int MAX_VALORACION = 5;
    public static final int TAM_MIN_CATALOGO = 10;
    public static final int MIN_PROVEEDOR = 20;
    public static final int MAX_PROVEEDOR = 30;
    public static final int ESPERA_RECIBIR_PEDIDO = 1;
}
