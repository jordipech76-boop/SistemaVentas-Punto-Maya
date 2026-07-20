# PuntoMaya — Sistema de Ventas (Java + Maven + JavaFX + MySQL)

Proyecto generado siguiendo la arquitectura MVC en capas:

```
Controller → Service → DAO → MySQL
```

## Estructura

```
com.puntomaya
├── model        → Cliente, Producto, Usuario, Venta, DetalleVenta, Proveedor, Inventario, CorteCaja
├── dao          → ClienteDAO, ProductoDAO, UsuarioDAO, VentaDAO, ProveedorDAO, InventarioDAO, CorteCajaDAO
├── service      → ClienteService, ProductoService, VentaService, InventarioService, UsuarioService, CorteCajaService
├── controller   → LoginController, MenuController, VentaController, ClienteController,
│                  ProductoController, InventarioController
├── util         → Conexion, Validaciones, Alertas, Utilidades, SesionActual
└── Main.java

resources/com/puntomaya/view  → Login.fxml, Menu.fxml, Ventas.fxml, Clientes.fxml,
                                 Productos.fxml, Inventario.fxml, estilos.css

database/puntomaya.sql        → script para crear la base y las tablas en MySQL
```

## 1. Requisitos

- **JDK 17** o superior instalado.
- **MySQL** corriendo en tu computadora (Workbench, XAMPP o instalación estándar — cualquiera funciona,
  la app se conecta por JDBC a `localhost:3306`).
- **IntelliJ IDEA** (Community o Ultimate) con soporte de Maven (ya viene incluido).

## 2. Crear la base de datos

1. Abre **MySQL Workbench** (o phpMyAdmin si usas XAMPP).
2. Abre el archivo `database/puntomaya.sql` de este proyecto.
3. Ejecuta todo el script completo (⚡ Execute). Esto crea la base `puntomaya`, todas las
   tablas, y agrega usuarios y productos de ejemplo para probar.

Usuarios de prueba que quedan creados:

| Usuario  | Contraseña   | Rol            |
|----------|--------------|----------------|
| rosa     | admin123     | ADMINISTRADOR  |
| lupita   | cajero123    | CAJERO         |
| beto     | almacen123   | ALMACENISTA    |

## 3. Configurar la conexión a tu MySQL

Abre el archivo:

```
src/main/java/com/puntomaya/util/Conexion.java
```

Y ajusta estas líneas con tus datos reales:

```java
private static final String USUARIO = "root";
private static final String PASSWORD = "TU_PASSWORD_AQUI";
```

- Si usas **XAMPP**: normalmente `USUARIO = "root"` y `PASSWORD = ""` (vacío).
- Si usas **MySQL Workbench** con instalación estándar: usa el usuario/contraseña
  que configuraste al instalar MySQL.

## 4. Abrir el proyecto en IntelliJ IDEA

1. Descomprime este `.zip`.
2. Abre IntelliJ IDEA → **File → Open...** → selecciona la carpeta `SistemadeVentas`
   (la que contiene el archivo `pom.xml`).
3. IntelliJ detecta que es un proyecto Maven y descarga automáticamente las dependencias
   (JavaFX y el conector de MySQL) la primera vez — necesitas internet solo para este paso único.
4. Espera a que termine de indexar/descargar (barra de progreso abajo a la derecha).

## 5. Ejecutar la aplicación

**Opción A — desde Maven (recomendada):**
En la pestaña "Maven" de IntelliJ (lado derecho), expande `SistemadeVentas → Plugins → javafx`
y da doble clic en `javafx:run`.

**Opción B — desde terminal integrada de IntelliJ:**
```
mvn clean javafx:run
```

**Opción C — ejecutar Main.java directo:**
Puede marcar error de módulos JavaFX si lo corres solo con el botón ▶ de `Main.java`,
porque JavaFX necesita sus módulos en el *classpath*. Si te pasa esto, usa la Opción A o B.

## 6. Primer inicio de sesión

Usa cualquiera de los usuarios de prueba, por ejemplo:
- Usuario: `rosa`
- Contraseña: `admin123`

Vas a entrar como Administrador y vas a ver las 4 opciones (Vender, Clientes, Productos, Inventario).
Si entras como `lupita` (Cajero) o `beto` (Almacenista), el menú oculta las opciones
que no les corresponden a su rol.

## 7. Notas importantes

- **Funciona sin internet**: MySQL corre en `localhost`, en la misma computadora, así que
  la app nunca depende de internet para vender — solo de que el servicio de MySQL esté encendido.
- **Cada DAO abre y cierra su propia conexión** (try-with-resources) en cada operación,
  para no dejar conexiones abiertas innecesariamente.
- **VentaDAO.guardar()** guarda la venta y todos sus detalles en una sola transacción:
  si algo falla, no se guarda nada a medias.
- El proyecto queda listo para que sigas agregando pantallas (por ejemplo, Reportes o
  Corte de Caja) usando exactamente el mismo patrón: FXML → Controller → Service → DAO.

## 8. Siguientes pasos sugeridos

- Agregar pantalla de **Corte de Caja** (`CorteCajaController` + `CorteCaja.fxml`), usando
  el `CorteCajaService` que ya viene incluido.
- Agregar impresión de ticket con una impresora térmica (protocolo ESC/POS).
- Agregar un `ComboBox<Proveedor>` real en la pantalla de Inventario (por ahora usa un
  proveedor fijo de ejemplo en `registrarEntrada`).
- Hashear las contraseñas de `usuario` en vez de guardarlas en texto plano (por ejemplo con
  BCrypt) antes de usar el sistema en producción.
