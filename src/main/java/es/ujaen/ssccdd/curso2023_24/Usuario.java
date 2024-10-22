package es.ujaen.ssccdd.curso2023_24;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import static  es.ujaen.ssccdd.curso2023_24.Constantes.*;

public class Usuario implements Callable<List<Articulos>> {

    private final int id;
    private final int pedidos[];
    private final Semaphore mutexPedidos;
    private final Semaphore esperaPedido[];
    private final List<Articulos> compras;

    public Usuario(int id, int pedidos[], Semaphore mutexPedidos, Semaphore[] esperaPedido) {
        this.id = id;
        this.pedidos = pedidos;
        this.mutexPedidos = mutexPedidos;
        this.esperaPedido = esperaPedido;
        this.compras = new ArrayList<>();
    }

    @Override
    public List<Articulos> call() {
            try{
                while(!Thread.currentThread().isInterrupted()) {
                    mutexPedidos.acquire();
                    crearPedido();
                    mutexPedidos.release();

                    esperaPedido[id].acquire();
                    valorarServicio();
                }
            } catch (InterruptedException e) {
                System.out.printf("El usuario %d ha sido interrumpido\n", id);
            }
        return compras;
    }

    private void crearPedido() {
        pedidos[id] = aleatorio.nextInt(MIN_ARTICULOS, MAX_ARTICULOS);
    }

    private void valorarServicio() {
        System.out.printf("El usuario %d ha recibido su pedido. Valoración del servicio: %d estrellas sobre 5\n", id, aleatorio.nextInt(MIN_VALORACION, MAX_VALORACION));
        pedidos[id] = 0;
    }

    public List<Articulos> getCompras() {
        return compras;
    }

}
