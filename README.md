# Calculadora Remota TCP Multithread

Este repositorio contiene el código fuente de una calculadora remota implementada en Java. La aplicación consta de un servidor multithread que utiliza TCP para la comunicación con los clientes y un cliente para interactuar con el servidor.

## Funcionalidades
Realiza operaciones aritméticas simples (suma, resta, multiplicación, división, módulo y factorial).
Soporte para comunicación a través de TCP.
Gestión multithread para manejar múltiples conexiones simultáneamente.

## Estructura del Repositorio

- **`tcpmpser.java`**: Código fuente del cliente de la calculadora remota TCP.
- **`tcpmtser.java`**: Código fuente del servidor multithread de la calculadora remota TCP.

## Instrucciones de Uso

### Servidor

Para ejecutar el servidor, utiliza el siguiente comando en la terminal:

```bash
java tcpmtser <puerto>
```

### Cliente

Para ejecutar el cliente, proporciona la dirección IP del servidor y el puerto como argumentos:

```bash
java tcpmtcli <ip_servidor> <puerto>
```
#### Comandos del Cliente

El cliente acepta operaciones aritméticas en el siguiente formato:

```bash
<operando1> <operador> <operando2>
```
Por ejemplo:

```bash
5 + 3
10 / 2
-7 * 4
5 !
```

Envía "QUIT" para salir del cliente.

## Notas
Este código proporciona una base para construir una calculadora remota con capacidad de manejar múltiples conexiones a través de TCP.
Puedes extender sus funcionalidades o adaptarlo según tus necesidades.
