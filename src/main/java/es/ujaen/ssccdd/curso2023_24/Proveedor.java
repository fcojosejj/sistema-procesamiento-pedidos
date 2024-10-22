package es.ujaen.ssccdd.curso2023_24;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import static es.ujaen.ssccdd.curso2023_24.Constantes.*;

public class Proveedor implements Runnable {

    private final String idProveedor;
    private final LinkedList<Articulos> catalogo;
    private final Semaphore semTienda, semProveedor;

    public Proveedor(String idProveedor, LinkedList<Articulos> catalogo, Semaphore semTienda, Semaphore semProveedor) {
        this.idProveedor = idProveedor;
        this.catalogo = catalogo;
        this.semTienda = semTienda;
        this.semProveedor = semProveedor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                semProveedor.acquire();

                int n = aleatorio.nextInt(MIN_PROVEEDOR, MAX_PROVEEDOR);
                for (int i = 0; i < n; i++) {
                    catalogo.add(Articulos.values()[aleatorio.nextInt(Articulos.values().length)]);
                }

                semTienda.release();
            }
        } catch (InterruptedException e) {
            System.out.printf("El proveedor %s ha sido interrumpido\n", idProveedor);
        }
    }
}
