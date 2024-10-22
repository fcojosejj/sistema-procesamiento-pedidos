package es.ujaen.ssccdd.curso2023_24;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static es.ujaen.ssccdd.curso2023_24.Constantes.*;
import static es.ujaen.ssccdd.curso2023_24.Main.NUM_USUARIOS;


public class Tienda implements Runnable {

    private final String id;
    private final int pedidos[];
    private final LinkedList<Constantes.Articulos> catalogo;
    private final Semaphore mutexPedidos, mutexArticulos;
    private final Semaphore semProveedor, semTienda;
    private final Semaphore semTransportistas[];
    private int idUsuario;
    private int numArticulos;
    private final Transportista transportistas[];

    public Tienda(String id, int pedidos[], LinkedList<Constantes.Articulos> catalogo, Semaphore mutexPedidos, Semaphore mutexArticulos, Semaphore semProveedor, Semaphore semTienda, Semaphore[] semTransportistas, Transportista[] transportistas) {
        this.id = id;
        this.pedidos = pedidos;
        this.catalogo = catalogo;
        this.mutexPedidos = mutexPedidos;
        this.mutexArticulos = mutexArticulos;
        this.semProveedor = semProveedor;
        this.semTienda = semTienda;
        this.semTransportistas = semTransportistas;
        this.transportistas = transportistas;
        this.idUsuario = 0;
        this.numArticulos = 0;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                mutexPedidos.acquire();
                recibirPedido();
                TimeUnit.SECONDS.sleep(ESPERA_RECIBIR_PEDIDO);

                mutexPedidos.release();
                verificarPago();
                TimeUnit.SECONDS.sleep(numArticulos);

                mutexArticulos.acquire();
                List<Articulos> pedido = prepararPedido(numArticulos);
                organizarPedido(pedido, idUsuario);

                // Actualizamos el catálogo
                if (catalogo.size() < TAM_MIN_CATALOGO) {
                    System.out.printf("La tienda %s necesita más productos. Llamando a un proveedor...\n", id);
                    semProveedor.release();
                    semTienda.acquire();
                    System.out.printf("La tienda %s ha actualizado su inventario\n", id);
                }

                mutexArticulos.release();
            }

        } catch (InterruptedException e) {
            System.out.printf("La tienda %s ha sido interrumpida\n", id);
        }
    }

    private void recibirPedido() {
        idUsuario = numArticulos = 0;
        while (numArticulos == 0) {
            int i = aleatorio.nextInt(NUM_USUARIOS);
            if (pedidos[i] != 0) {
                idUsuario = i;
                numArticulos = pedidos[i];
                pedidos[i] = 0;
            }
        }
        System.out.printf("La tienda %s ha recibido un pedido de %d articulos del usuario %d\n", id, numArticulos, idUsuario);
    }

    private void verificarPago() {
        System.out.printf("La tienda %s está verificando el pago del usuario %d ...\n", id, idUsuario);
    }

    private List<Articulos> prepararPedido(int numArticulos) {
        System.out.printf("La tienda %s está preparando el pedido del usuario %d\n", id, idUsuario);
        List<Articulos> pedido = new LinkedList<>();
        for (int i = 0; i < numArticulos; i++) {
            pedido.add(catalogo.removeFirst());
        }
        return pedido;
    }

    private void organizarPedido(List<Articulos> pedido, int idUsuario) {
        System.out.printf("La tienda %s está organizando el pedido del usuario %d\n", id, idUsuario);
        for (int i = 0; i < transportistas.length; i++) {
            if (!transportistas[i].enReparto()) {
                transportistas[i].asignarDatos(pedido, idUsuario);
                System.out.printf("La tienda %s ha asignado el pedido del usuario %d al transportista %d\n", id, idUsuario, i);
                semTransportistas[i].release();
                break;
            }
        }
    }
}
