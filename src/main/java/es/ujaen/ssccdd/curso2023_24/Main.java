package es.ujaen.ssccdd.curso2023_24;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static es.ujaen.ssccdd.curso2023_24.Constantes.*;

public class Main {
    public static final int NUM_USUARIOS = 20;
    public static final int NUM_TIENDAS = 6;
    public static final int NUM_TRANSPORTISTAS = 10;
    public static final int NUM_PROVEEDORES = 5;
    public static final int MINUTOS_EJECUCION = 3;
    public static final int TIEMPO_ESPERA_RESULTADOS = 1;

    public static void main(String[] args) {

        // Declaración de variables
        int pedidos[];
        LinkedList<Constantes.Articulos> catalogo;
        Semaphore mutexPedidos, mutexArticulos, semProveedor, semTienda, esperaPedidos[], semTransportistas[];
        Usuario usuarios[];
        Proveedor proveedores[];
        Transportista transportistas[];
        Tienda tiendas[];
        ExecutorService executor;
        List<Future<List<Articulos>>> resultados;

        // Inicialización de variables
        pedidos = new int[NUM_USUARIOS];
        catalogo = new LinkedList<>();
        mutexPedidos = new Semaphore(1);
        mutexArticulos = new Semaphore(1);
        semProveedor = new Semaphore(0);
        semTienda = new Semaphore(0);
        esperaPedidos = new Semaphore[NUM_USUARIOS];
        semTransportistas = new Semaphore[NUM_TRANSPORTISTAS];
        usuarios = new Usuario[NUM_USUARIOS];
        proveedores = new Proveedor[NUM_PROVEEDORES];
        transportistas = new Transportista[NUM_TRANSPORTISTAS];
        tiendas = new Tienda[NUM_TIENDAS];
        executor = (ExecutorService) Executors.newCachedThreadPool();
        resultados = new LinkedList<>();

        inicializacion(pedidos, catalogo, mutexPedidos, mutexArticulos, semProveedor, semTienda, esperaPedidos, semTransportistas, usuarios, proveedores, transportistas, tiendas, executor, resultados);

        // Cuerpo de ejecución
        System.out.println("Hilo(Principal) Comienza su ejecución");
        ejecucion(executor, usuarios, proveedores, transportistas, tiendas, resultados);

        // Esperamos un tiempo antes de interrumpir el hilo principal
        try {
            TimeUnit.MINUTES.sleep(MINUTOS_EJECUCION);

            // Finaliza la ejecución de todos los hilos y del marco ejecutor
            finalizar(executor);

            // Esperamos a que todos los hilos finalicen
            executor.awaitTermination(TIEMPO_ESPERA_RESULTADOS, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("Hilo(Principal) Ha sido interrumpido");
        }


        // Presentar resultados
        System.out.println("\n---------- RESULTADOS ----------\n");

        //Muestra los resultados de las listas de compras de los usuarios
        int i = 0;
        for (Usuario u : usuarios) {
            try {
                System.out.printf("\nCompras del USUARIO %d:\n    - Total de artículos: %d\n  - Lista de artículos: %s\n", i, resultados.get(i).get().size(), resultados.get(i).get().toString());
                i++;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        System.out.println("\n----------- FIN RESULTADOS ----------\n");

        // Finalización
        System.out.println("Hilo(Principal) Ha finalizado");
    }

    private static void inicializacion(
            int pedidos[],
            LinkedList<Constantes.Articulos> catalogo,
            Semaphore mutexPedidos,
            Semaphore mutexArticulos,
            Semaphore semProveedor,
            Semaphore semTienda,
            Semaphore esperaPedidos[],
            Semaphore semTransportistas[],
            Usuario usuarios[],
            Proveedor proveedores[],
            Transportista transportistas[],
            Tienda tiendas[],
            ExecutorService executor,
            List<Future<List<Articulos>>> resultados
    ){
        for (int i = 0; i < NUM_USUARIOS; i++) {
            esperaPedidos[i] = new Semaphore(0);
        }

        for (int i = 0; i < NUM_TRANSPORTISTAS; i++) {
            semTransportistas[i] = new Semaphore(0);
        }

        for (int i = 0; i < 50; i++) {
            catalogo.add(Articulos.values()[aleatorio.nextInt(Articulos.values().length)]);
        }

        for (int i = 0; i < NUM_TRANSPORTISTAS; i++) {
            semTransportistas[i] = new Semaphore(0);
        }

        for (int i = 0; i < NUM_USUARIOS; i++) {
            usuarios[i] = new Usuario(i, pedidos, mutexPedidos, esperaPedidos);
        }

        for (int i = 0; i < NUM_PROVEEDORES; i++) {
            proveedores[i] = new Proveedor("Proveedor " + i, catalogo, semTienda, semProveedor);
        }

        for (int i = 0; i < NUM_TRANSPORTISTAS; i++) {
            transportistas[i] = new Transportista(i, usuarios, esperaPedidos, semTransportistas);
        }

        for (int i = 0; i < NUM_TIENDAS; i++) {
            tiendas[i] = new Tienda("Tienda " + i, pedidos, catalogo, mutexPedidos, mutexArticulos, semProveedor, semTienda, semTransportistas, transportistas);
        }
    }

    private static void ejecucion(
            ExecutorService executor,
            Usuario usuarios[],
            Proveedor proveedores[],
            Transportista transportistas[],
            Tienda tiendas[],
            List<Future<List<Articulos>>> resultados
    ){
        for (int i = 0; i < NUM_USUARIOS; i++) {
            resultados.add(executor.submit(usuarios[i]));
        }

        for (int i = 0; i < NUM_TIENDAS; i++) {
            executor.execute(tiendas[i]);
        }

        for (int i = 0; i < NUM_PROVEEDORES; i++) {
            executor.execute(proveedores[i]);
        }

        for (int i = 0; i < NUM_TRANSPORTISTAS; i++) {
            executor.execute(transportistas[i]);
        }
    }

    private static void finalizar(ExecutorService e){
        e.shutdownNow();
    }
}