Este repositorio contiene mi solución a un proyecto de la asignatura Sistemas Concurrentes y Distribuidos del 2º curso de Ingeniería Informática de la Universidad de Jaén. En esta práctica, será necesario hacer uso de la clase [`Semaphore`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/Semaphore.html) de Java, la factoría [`Executors`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/Executors.html) y la interface [`ExecutorService`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/ExecutorService.html) para la ejecución de las tareas concurrentes que compondrán la solución de la práctica.

## Problema: Sistema de Procesamiento de Pedidos
Vamos a simular un proceso de venta por Internet. Para la simulación deberemos resolver una serie de tareas que deberán ejecutarse de forma concurrente garantizando una máxima eficiencia. 

1.  **RecibirPedido**: Recibe pedidos de los clientes.
2.  **VerificarPago**: Verifica y confirma el pago del pedido.
3.  **ChequearInventario**: Verifica la disponibilidad de los artículos pedidos.
4.  **PrepararPedido**: Empaqueta los artículos una vez que el pago ha sido verificado y los artículos están confirmados como disponibles.
5.  **OrganizarEnvio**: Organiza el envío del paquete al cliente.
6.  **ActualizarInventario**: Actualiza el inventario basado en los artículos enviados.
7.  **NotificarCliente**: Notifica al cliente sobre el estado de su pedido.
8. **EntregarPedido** : El cliente recibe el pedido del transportista y valorará el servicio de envío así como la calidad del producto que ha comprado.

En las diferentes tareas estarán implicados una serie de procesos que deberán utilizar semáforos para garantizar el uso correcto de los datos y la sincronización de las tareas. Los procesos son los siguientes:

 1. proceso **Usuario** : Simula las acciones que deberá seguir el usuario para comprar los articulos que desee en la tienda. Para simplificar el proceso las solicitudes de los artículos se hacen a una tienda que dispone de ellos en su catálogo.
 2. proceso **Tienda** : Tiene un catálogo de productos que desea vender a los clientes.
 3. proceso **Transportista** : Será el encargado de recibir los encargos de transporte de la tienda para enviarlos al cliente correspondiente.
 4. proceso **Proveedor** : Será el encargado de atender las necesidades de inventario de la tienda para completar los productos de su catálogo.

### Para la implementación

Deberán definirse tiempos que simulen las operaciones que tienen que hacer los diferentes procesos implicados en el problema. También deberá definirse un tiempo para la finalización de la prueba. Hay que mostrar información en la consola para seguir la ejecución de los procesos.

El hilo principal deberá crear un número suficiente de procesos para demostrar la concurrencia de los mismo. De todos los procesos deberá haber instancias suficientes.

Además debe implementarse un proceso de finalización que se lanzará pasado un tiempo que estará definido en las constantes. Por tanto todos los procesos presentes en la solución deberán implementar su interrupción como el método de finalización.

## Análisis y diseño
### Análisis
El problema consiste en sincronizar correctamente los 4 procesos que aparecen en el enunciado del problema. Para ello, voy a hacer uso de las estructuras de datos, variables y semáforos que estime oportunos para el correcto desarrollo de la práctica.
#### Datos
Los tipos de datos que serán necesarios para la solución de la práctica son:
* **TDA** `Articulo`: Es un Enumerado de posibles artículos que tenga la tienda, que en este caso será una tienda de informática.
#### Variables compartidas
* `pedidos: Integer[]`: Array de pedidos que realizan los procesos `Usuario` y que recogen los procesos `Tienda`. Los usuarios indican a la tienda el número de artículos que quieren. Será un array de enteros inicializado a NUM_USUARIOS.
* `catalogo<Articulo>`: Lista de artículos compartida entre los procesos `Tienda` y los procesos `Proveedor`. Cuando queden pocos artículos en las tiendas, se llamará a un proveedor para que añada los suficientes artículos.
* `mutexPedidos: Semaphore`: Semáforo para controlar el acceso en exclusión mutua al array de peticiones. Se inicializará con valor 1.
* `mutexArticulos: Semaphore`: Semáforo para controlar el acceso en exclusión mutua a la lista del inventario de las tiendas. Se inicializará con valor 1.
* `esperaPedido: Semaphore[]` Array de semáforos que mantendrá bloqueados a aquellos procesos `Usuario` que hayan realizado un pedido a la espera de recibirlo y sean desbloqueados por un proceso `Transportista`, es decir, un usuario no podrá hacer un pedido hasta que reciba el anterior. Tendrá tamaño NUM_USUARIOS y cada semáforo se inicializará con valor 0.
* `semProveedor: Semaphore`: Semáforo que mantendrá a los procesos `Proveedor` bloqueados a la espera de una llamada de un proceso `Tienda` para rellenar el stock del inventario. Se inicializará con valor 0.
* `semTienda: Semaphore` Semáforo que mantendrá al proceso `Tienda` que llame al proceso `Proveedor` bloqueado mientras éste repone el catálogo de productos. Se inicializará con valor 0.
* `semTransportistas: Semaphore[]`: Array de semáforos que mantendrá bloqueados a los procesos `Transportista` a la espera de ser liberados por un proceso `Tienda` una vez un pedido este listo para ser puesto en reparto. Se usa un array para identificar fácilmente qué transportista hemos desbloqueado. Tendrá tamaño NUM_TRANSPORTISTAS y cada semáforo se inicializará con valor 0.
#### Constantes
Las constantes más destacables necesarias para la solución de la práctica son:
* `NUM_USUARIOS`: Número de procesos `Usuario` que van a ejecutarse.
* `NUM_TIENDAS`: Número de procesos `Tienda` que van a ejecutarse.
* `NUM_PROVEEDORES`: Número de procesos `Proveedor` que van a ejecutarse.
* `NUM_TRANSPORTISTAS`: Número de procesos `Transportista` que van a ejecutarse.
* `TAM_MINIMO`: Tamaño mínimo que tendrá `catalogo`. Si el tamaño de la lista es menor que su tamaño mínimo, se llamará a un proveedor para que rellene el stock.
* `NUM_ARTICULOS_INICIO:` Número de artículos que tendrá el catálogo de inicio.
* `TIEMPO_EJECUCION`: Tiempo de ejecución del programa.
---
### Diseño
A continuación, se van a presentar las variables locales, cabecera del constructor  y ejecución de cada uno de los cuatros procesos que intervienen en el problema. Solo se incluyen aquellas variables pertenecientes al apartado anterior, el resto se omiten por ahora.
#### Usuario
* Variables locales
	* `id: Integer`: Un identificador numérico para cada usuario que permite diferenciarlo de los demás.
	* `compras<Articulo>: List` Lista con todos los artículos que el usuario ha adquirido a lo largo de la ejecución del programa. Es propia de cada usuario.
* Constructor
```
Usuario (id: Integer, pedidos[]: Integer, mutexPedidos: Semaphore, esperaPedido: Semaphore[])
```
* Ejecución
```
while (Hasta ser interrumpido) {
	mutexPedidos.wait()
	crearPedido()
	mutexPedidos.signal()
	
	esperaPedido[id].wait() // Espera a que el transportista le traiga el pedido y le desbloquee
	valorarServicio()
}
```
* `crearPedido()`: Método que creará un pedido y lo añadirá a `pedidos[id]`.
* `valorarServicio()`: Método que valorará el servicio del transportista y de los productos recibidos.
#### Tienda
* Variables locales
	* `id: String` Un identificador para diferencia a una tienda de las demás.
	* `idUsuario: Integer`: Variable para almacenar el identificador del usuario del cuál se esté preparando el pedido.
	* `numArticulos: Integer`: Variable para almacenar el número de artículos que quiere el usuario del cuál se esté preparando el pedido
* Constructor
```
Tienda (id: String, pedidos: Integer[], catalogo: LinkedList<Articulo>, mutexPedidos: Semaphore, mutexArticulos: Semaphore, semTienda: Semaphore, semProveedor: Semaphore, semTransportista: Semaphore[])
```
* Ejecución
```
while (Hasta ser interrumpido) {
	mutexPedidos.wait()
	recibirPedido()
	mutexPedidos.signal()
	verificarPago()

	mutexArticulos.wait()
	List<Articulo> pedido = prepararPedido(numArticulos)
	organizarEnvio(pedido, idUsuario)
	notificarUsuario(idUsuario)
	actualizarInventario()
	mutexArticulos.signal()	
}
```
A continuación se va a mostrar algunos de los métodos más relevantes del proceso `Tienda`.
```
function recibirPedido(){
	idUsuario, numArticulos = 0
	while(idUsuario ó numArticulos == 0){
		// Busco un usuario que haya hecho un pedido, y obtengo su id y el número de artículos que quiere
		int i = aleatorio.nextInt(NUM_USUARIOS)
		if(pedidos[i] != 0){
			idUsuario = i
			numArticulos = pedidos[i]
		}
	}
}
```

```
function prepararPedido(numArticulos){
	// Creamos una lista auxiliar y sacamos tantos artículos del catálogo como quiera el usuario en cuestión
	List<Articulo> pedido
	for(i = 0, i < numArticulos, i++){
		pedido.add(catalogo.poll())
	}
	return pedido
}
```

```
function organizarEnvio(pedido, idUsuario){
	// Buscamos un transportista libre, le asignamos el pedido y lo liberamos
	for(i = 0, i < transportistas.size, i++){
		if (transportistas[i] no ocupado){
			transportistas[i].asignarDatos(pedido, idUsuario)
			semTransportistas[i].signal()
		}
	}
}
```

```
function actualizarInventario(){
	// Si quedan pocos artículos, desbloqueo a un proveedor y me bloqueo yo. El resto de tiendas están bloqueadas en el mutexArticulos
	if(catalogo.size < TAM_MINIMO)
		semProveedor.signal()
		semTienda.wait()
}
```
#### Proveedor
* Variables locales
	* `id: String`: Identificador único para diferenciar a los proveedores.
* Constructor
```
Proveedor (id: String, catalogo: LinkedList<Articulo>, semTienda: Semaphore, semProveedor: Semaphore)
```
* Ejecución
```
while (Hasta ser interrumpido){
	// Espera a ser desbloqueado por una tienda, cuando esto pase añade un número aleatorio de articulos al catálogo y desbloquea a la tienda
	semProveedor.wait()
	for(i = 0, i < entero_random, i++){
		catalogo.add(artículo_aleatorio)
	}
	semTienda.signal()
}
```
#### Transportista
* Variables locales
	* `id: Integer`: Identificador único para diferenciar a los transportistas.
	* `idUsuario: Integer`: Variable para almacenar el identificador del usuario al que se debe entregar el pedido.
	* `enReparto: booleano`: Variable para saber si el transportista está o no reparto para poder asignarlo un pedido o no. Inicialmente estará a false para todos los transportistas.
	* `pedido: List<Articulo>`: Lista de artículo que representa el pedido que se lleva a un usuario.
* Constructor
```
Transportista (id: String, esperaPedido: Semaphore[], semTransportistas: Semaphore[])
```
* Ejecución
```
while (Hasta ser interrumpido){
	// Se bloquean esperando a recibir un pedido por parte de la tienda. Una vez son liberados, entregan el pedido al usuario y lo liberan para que pueda realizar pedidos de nuevo
	semTransportistas[id].wait()
	// Simulo tiempo de reparto
	entregarPedido(idUsuario, pedido)
	esperaPedido[idUsuario].signal()
}
```
* `entregarPedido(idUsuario, pedido)`: Método que añade los artículos del pedido a la lista `compras` del usuario con identificador idUsuario.
#### Hilo principal
```
function main{
	inicializarVariables()
	ejecutarProcesos()
	// Esperamos un tiempo
	interrumpirEjecucion()
	mostrarResultados()
}
```