package es.ujaen.ssccdd.curso2023_24;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import es.ujaen.ssccdd.curso2023_24.Constantes.Articulos;

public class Transportista implements Runnable {

    private final int id;
    private int idUsuario;
    private boolean enReparto;
    private List<Articulos> pedido;
    private final Usuario usuarios[];
    private final Semaphore esperaPedido[], semTransportistas[];

    public Transportista(int id, Usuario usuarios[], Semaphore esperaPedido[], Semaphore semTransportistas[]) {
        this.id = id;
        this.usuarios = usuarios;
        this.esperaPedido = esperaPedido;
        this.semTransportistas = semTransportistas;
        this.idUsuario = 0;
        this.enReparto = false;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                semTransportistas[id].acquire();
                enReparto = true;

                // Simulamos tiempo de reparto
                TimeUnit.SECONDS.sleep(pedido.size());
                entregaPedido(idUsuario, pedido);
                System.out.printf("El transportista %d ha entregado el pedido al usuario %d\n", id, idUsuario);
            }
        } catch (InterruptedException e) {
            System.out.printf("El transportista %d ha sido interrumpido\n", id);
        }
    }

    private void entregaPedido(int idUsuario, List<Articulos> pedido) {
        usuarios[idUsuario].getCompras().addAll(pedido);
        enReparto = false;
        esperaPedido[idUsuario].release();
    }

    public void asignarDatos(List<Articulos> pedido, int idUsuario) {
        this.pedido = pedido;
        this.idUsuario = idUsuario;
    }

    public boolean enReparto() {
        return enReparto;
    }
}
