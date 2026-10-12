import org.iesalandalus.programacion.utilidades.Entrada;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;

public class Main {

    private static final String NOMBRE_FICHERO = "AleatorioDepart.dat";
    private static final int TAM_REGISTRO = 64; // 2 (short) + 30 (15 chars) + 30 (15 chars) + 2 (short)
    private static boolean modoDiagnostico = false;

    public static void main(String[] args) {
        inicializarFicheroYComprobarIntegridad();

        int opcion;
        do {
            mostrarMenu();
            System.out.print("Seleccione una opción: ");
            opcion = leerEnteroSeguro();

            if (modoDiagnostico) {
                switch (opcion) {
                    case 1:
                        inspeccionFisica();
                        break;
                    case 2:
                        estadisticas();
                        break;
                    case 3:
                        integridad();
                        break;
                    case 0:
                        System.out.println("Saliendo de la aplicación...");
                        break;
                    default:
                        System.out.println("Opción no válida. El fichero presenta anomalías y está en modo diagnóstico.");
                }
            } else {
                switch (opcion) {
                    case 1:
                        operacionInsertar();
                        break;
                    case 2:
                        operacionConsultar();
                        break;
                    case 3:
                        operacionActualizarEmpleados();
                        break;
                    case 4:
                        operacionActualizarNombreLocalidad();
                        break;
                    case 5:
                        operacionEliminar();
                        break;
                    case 6:
                        listadoDepartamentos();
                        break;
                    case 7:
                        inspeccionFisica();
                        break;
                    case 8:
                        estadisticas();
                        break;
                    case 9:
                        integridad();
                        break;
                    case 0:
                        System.out.println("Saliendo de la aplicación...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
            System.out.println();
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("==========================================");
        System.out.println("    GESTIÓN DE DEPARTAMENTOS (FICHERO)    ");
        System.out.println("==========================================");
        if (modoDiagnostico) {
            System.out.println(" [ATENCIÓN] MODO DIAGNÓSTICO ACTIVADO (Fichero con anomalías)");
            System.out.println(" 1. Inspección física del fichero");
            System.out.println(" 2. Estadísticas");
            System.out.println(" 3. Comprobar integridad");
            System.out.println(" 0. Salir");
        } else {
            System.out.println(" 1. Insertar departamento");
            System.out.println(" 2. Consultar departamento");
            System.out.println(" 3. Actualizar número de empleados");
            System.out.println(" 4. Actualizar nombre y localidad");
            System.out.println(" 5. Eliminar departamento");
            System.out.println(" 6. Listado de departamentos activos");
            System.out.println(" 7. Inspección física del fichero");
            System.out.println(" 8. Estadísticas");
            System.out.println(" 9. Comprobar integridad");
            System.out.println(" 0. Salir");
        }
        System.out.println("==========================================");
    }

    private static void inicializarFicheroYComprobarIntegridad() {
        File fichero = new File(NOMBRE_FICHERO);
        try {
            if (!fichero.exists()) {
                fichero.createNewFile();
                System.out.println("Fichero " + NOMBRE_FICHERO + " creado vacío correctamente.");
            } else {
                System.out.println("Fichero " + NOMBRE_FICHERO + " existente. Comprobando integridad...");
                if (!comprobarIntegridadSilenciosa()) {
                    modoDiagnostico = true;
                    System.out.println("\n[¡AVISO!] Se han detectado anomalías de integridad en el fichero.");
                    System.out.println("La aplicación ha entrado en MODO DIAGNÓSTICO.\n");
                } else {
                    System.out.println("Integridad del fichero correcta.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error al inicializar el fichero: " + e.getMessage());
        }
    }

    private static boolean comprobarIntegridadSilenciosa() {
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "r")) {
            long longitud = raf.length();
            if (longitud % TAM_REGISTRO != 0) return false;

            long numRegistros = longitud / TAM_REGISTRO;
            for (long i = 0; i < numRegistros; i++) {
                raf.seek(i * TAM_REGISTRO);
                short id = raf.readShort();
                char[] nombreArr = new char[15];
                for (int j = 0; j < 15; j++) nombreArr[j] = raf.readChar();
                char[] locArr = new char[15];
                for (int j = 0; j < 15; j++) locArr[j] = raf.readChar();
                short empleados = raf.readShort();

                String nombre = new String(nombreArr);
                String localidad = new String(locArr);

                if (id == 0) {
                    if (!nombre.equals("---------------") || !localidad.equals("---------------") || empleados != 0)
                        return false;
                } else if (id == -1) {
                    if (!nombre.equals("***************") || !localidad.equals("***************") || empleados != -1)
                        return false;
                } else if (id > 0 && id <= 32767) {
                    if (id != (i + 1)) return false;
                    if (empleados < 0) return false;
                    if (nombre.trim().isEmpty() || localidad.trim().isEmpty()) return false;
                } else {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean existeDepartamento(short numDpto) {
        if (numDpto < 1 || numDpto > 32767) return false;
        long posicion = (numDpto - 1L) * TAM_REGISTRO;
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "r")) {
            if (posicion >= raf.length()) return false;
            raf.seek(posicion);
            short id = raf.readShort();
            return id == numDpto;
        } catch (IOException e) {
            return false;
        }
    }

    public static void consultaDepartamento(short numDpto) {
        if (numDpto < 1 || numDpto > 32767) {
            System.out.println("Número de departamento inválido.");
            return;
        }
        long posicion = (numDpto - 1L) * TAM_REGISTRO;
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "r")) {
            if (posicion >= raf.length()) {
                System.out.println("El departamento número " + numDpto + " no existe (fuera del fichero).");
                return;
            }
            raf.seek(posicion);
            short id = raf.readShort();
            char[] nombreArr = new char[15];
            for (int i = 0; i < 15; i++) nombreArr[i] = raf.readChar();
            char[] locArr = new char[15];
            for (int i = 0; i < 15; i++) locArr[i] = raf.readChar();
            short empleados = raf.readShort();

            if (id == numDpto) {
                System.out.println("--- DATOS DEL DEPARTAMENTO ---");
                System.out.println("Número: " + id);
                System.out.println("Nombre: " + new String(nombreArr).trim());
                System.out.println("Localidad: " + new String(locArr).trim());
                System.out.println("Empleados: " + empleados);
            } else if (id == 0) {
                System.out.println("El departamento número " + numDpto + " nunca ha sido utilizado.");
            } else if (id == -1) {
                System.out.println("El departamento número " + numDpto + " fue eliminado.");
            } else {
                System.out.println("El registro en la posición del departamento " + numDpto + " es anómalo.");
            }
        } catch (IOException e) {
            System.out.println("Error de acceso al fichero: " + e.getMessage());
        }
    }

    public static void actualizaNumeroEmpleados(short numDpto, short nuevoNumEmpleados) {
        if (numDpto < 1 || numDpto > 32767 || nuevoNumEmpleados < 0) {
            System.out.println("Datos inválidos.");
            return;
        }
        long posicion = (numDpto - 1L) * TAM_REGISTRO;
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "rw")) {
            if (posicion >= raf.length()) {
                System.out.println("El departamento no existe.");
                return;
            }
            raf.seek(posicion);
            short id = raf.readShort();
            if (id == numDpto) {
                raf.seek(posicion + 62);
                raf.writeShort(nuevoNumEmpleados);
                System.out.println("Número de empleados actualizado correctamente.");
            } else {
                System.out.println("El departamento número " + numDpto + " no existe como registro activo.");
            }
        } catch (IOException e) {
            System.out.println("Error al actualizar empleados: " + e.getMessage());
        }
    }

    public static void actualizaNombreLocalidad(short numDpto, String nuevoNombre, String nuevaLocalidad) {
        if (numDpto < 1 || numDpto > 32767) {
            System.out.println("Número de departamento inválido.");
            return;
        }
        String nombreValidado = validarYFormatearTexto(nuevoNombre);
        String localidadValidada = validarYFormatearTexto(nuevaLocalidad);

        if (nombreValidado == null || localidadValidada == null) {
            System.out.println("Actualización cancelada. Textos inválidos.");
            return;
        }

        long posicion = (numDpto - 1L) * TAM_REGISTRO;
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "rw")) {
            if (posicion >= raf.length()) {
                System.out.println("El departamento no existe.");
                return;
            }
            raf.seek(posicion);
            short id = raf.readShort();
            if (id == numDpto) {
                raf.seek(posicion + 2);
                for (int i = 0; i < 15; i++) raf.writeChar(nombreValidado.charAt(i));
                for (int i = 0; i < 15; i++) raf.writeChar(localidadValidada.charAt(i));
                System.out.println("Nombre y localidad actualizados correctamente.");
            } else {
                System.out.println("El departamento número " + numDpto + " no existe como activo.");
            }
        } catch (IOException e) {
            System.out.println("Error al actualizar textos: " + e.getMessage());
        }
    }

    public static void insertaDepartamento(short numDpto, String nombre, String localidad, short numEmpleados) {
        if (numDpto < 1 || numDpto > 32767 || numEmpleados < 0) {
            System.out.println("Datos de inserción no válidos.");
            return;
        }

        String nombreVal = validarYFormatearTexto(nombre);
        String localidadVal = validarYFormatearTexto(localidad);
        if (nombreVal == null || localidadVal == null) return;

        long posicionDestino = (numDpto - 1L) * TAM_REGISTRO;

        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "rw")) {
            long longitudActual = raf.length();

            if (posicionDestino < longitudActual) {
                raf.seek(posicionDestino);
                short idActual = raf.readShort();
                if (idActual == numDpto) {
                    System.out.println("Error: Ya existe un departamento activo con el número " + numDpto);
                    return;
                }
            }

            if (posicionDestino > longitudActual) {
                raf.seek(longitudActual);
                for (long p = longitudActual; p < posicionDestino; p += TAM_REGISTRO) {
                    escribirNuncaUtilizada(raf);
                }
            }

            raf.seek(posicionDestino);
            raf.writeShort(numDpto);
            for (int i = 0; i < 15; i++) raf.writeChar(nombreVal.charAt(i));
            for (int i = 0; i < 15; i++) raf.writeChar(localidadVal.charAt(i));
            raf.writeShort(numEmpleados);

            System.out.println("Departamento insertado correctamente en la posición " + numDpto + ".");
        } catch (IOException e) {
            System.out.println("Error durante la inserción: " + e.getMessage());
        }
    }

    public static void eliminaDepartamento(short numDpto) {
        if (numDpto < 1 || numDpto > 32767) return;
        long posicion = (numDpto - 1L) * TAM_REGISTRO;
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "rw")) {
            if (posicion >= raf.length()) {
                System.out.println("El departamento no existe.");
                return;
            }
            raf.seek(posicion);
            short id = raf.readShort();
            if (id == numDpto) {
                raf.seek(posicion);
                raf.writeShort((short) -1);
                for (int i = 0; i < 15; i++) raf.writeChar('*');
                for (int i = 0; i < 15; i++) raf.writeChar('*');
                raf.writeShort((short) -1);
                System.out.println("Departamento eliminado lógicamente con éxito.");
            } else {
                System.out.println("El departamento no existe como registro activo.");
            }
        } catch (IOException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }

    public static void listadoDepartamentos() {
        int contador = 0;
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "r")) {
            long longitud = raf.length();
            if (longitud == 0) {
                System.out.println("El fichero está vacío.");
                return;
            }
            System.out.println("\n--- LISTADO DE DEPARTAMENTOS ACTIVOS ---");
            for (long pos = 0; pos < longitud; pos += TAM_REGISTRO) {
                raf.seek(pos);
                short id = raf.readShort();
                char[] nombreArr = new char[15];
                for (int i = 0; i < 15; i++) nombreArr[i] = raf.readChar();
                char[] locArr = new char[15];
                for (int i = 0; i < 15; i++) locArr[i] = raf.readChar();
                short empleados = raf.readShort();

                if (id > 0 && id <= 32767) {
                    System.out.println("Número: " + id + " | Nombre: " + new String(nombreArr).trim() +
                            " | Localidad: " + new String(locArr).trim() + " | Empleados: " + empleados);
                    contador++;
                }
            }
            System.out.println("----------------------------------------");
            System.out.println("Total activos mostrados: " + contador);
        } catch (IOException e) {
            System.out.println("Error al listar: " + e.getMessage());
        }
    }

    public static void inspeccionFisica() {
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "r")) {
            long longitud = raf.length();
            if (longitud == 0) {
                System.out.println("El fichero está vacío.");
                return;
            }
            System.out.println("\n--- INSPECCIÓN FÍSICA DEL FICHERO ---");
            long numRegistros = longitud / TAM_REGISTRO;
            for (long i = 0; i < numRegistros; i++) {
                long desplazamiento = i * TAM_REGISTRO;
                int idEsperado = (int) i + 1;

                raf.seek(desplazamiento);
                short idAlmacenado = raf.readShort();
                char[] nombreArr = new char[15];
                for (int j = 0; j < 15; j++) nombreArr[j] = raf.readChar();
                char[] locArr = new char[15];
                for (int j = 0; j < 15; j++) locArr[j] = raf.readChar();
                short empleados = raf.readShort();

                String estado = determinarEstadoPosicion(idAlmacenado, new String(nombreArr), new String(locArr), empleados, idEsperado);

                System.out.println("Posición: " + (i + 1) + " | Desplazamiento: " + desplazamiento + " bytes | ID Almacenado: " + idAlmacenado + " | Estado: " + estado);
            }
            System.out.println("--------------------------------------");
        } catch (IOException e) {
            System.out.println("Error en inspección: " + e.getMessage());
        }
    }

    public static void estadisticas() {
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "r")) {
            long longitudBytes = raf.length();
            long numRegistros = longitudBytes / TAM_REGISTRO;
            int activos = 0, eliminadas = 0, nuncaUtilizadas = 0;
            long sumaEmpleados = 0;

            for (long i = 0; i < numRegistros; i++) {
                raf.seek(i * TAM_REGISTRO);
                short id = raf.readShort();
                char[] nombreArr = new char[15];
                for (int j = 0; j < 15; j++) nombreArr[j] = raf.readChar();
                char[] locArr = new char[15];
                for (int j = 0; j < 15; j++) locArr[j] = raf.readChar();
                short empleados = raf.readShort();

                String estado = determinarEstadoPosicion(id, new String(nombreArr), new String(locArr), empleados, (int) i + 1);

                if (estado.equals("Activa")) {
                    activos++;
                    sumaEmpleados += empleados;
                } else if (estado.equals("Eliminada")) eliminadas++;
                else if (estado.equals("Nunca utilizada")) nuncaUtilizadas++;
            }

            double ocupacion = 0.0;
            int divisor = eliminadas + nuncaUtilizadas;
            if (numRegistros > 0 && divisor > 0) ocupacion = ((double) activos / (activos + divisor)) * 100.0;
            else if (activos > 0) ocupacion = 100.0;

            System.out.println("\n--- ESTADÍSTICAS ---");
            System.out.println("Longitud: " + longitudBytes + " bytes");
            System.out.println("Activos: " + activos);
            System.out.println("Eliminados: " + eliminadas);
            System.out.println("Nunca utilizados: " + nuncaUtilizadas);
            System.out.println("Suma de empleados: " + sumaEmpleados);
            System.out.printf("Ocupación: %.2f%%\n", ocupacion);
            System.out.println("--------------------");
        } catch (IOException e) {
            System.out.println("Error en estadísticas: " + e.getMessage());
        }
    }

    public static void integridad() {
        try (RandomAccessFile raf = new RandomAccessFile(NOMBRE_FICHERO, "r")) {
            long longitud = raf.length();
            System.out.println("\n--- INFORME DE INTEGRIDAD ---");
            boolean integro = true;

            if (longitud % TAM_REGISTRO != 0) {
                System.out.println("[ANOMALÍA] Longitud (" + longitud + " bytes) no es múltiplo de 64.");
                integro = false;
            }

            long numRegistros = longitud / TAM_REGISTRO;
            for (long i = 0; i < numRegistros; i++) {
                long posByte = i * TAM_REGISTRO;
                raf.seek(posByte);
                short id = raf.readShort();
                char[] nombreArr = new char[15];
                for (int j = 0; j < 15; j++) nombreArr[j] = raf.readChar();
                char[] locArr = new char[15];
                for (int j = 0; j < 15; j++) locArr[j] = raf.readChar();
                short empleados = raf.readShort();

                String nombre = new String(nombreArr);
                String localidad = new String(locArr);
                int idEsperado = (int) i + 1;

                if (id < -1 || id > 32767) {
                    System.out.println("[ANOMALÍA] Byte " + posByte + ": ID inválido (" + id + ").");
                    integro = false;
                } else if (id == -1 && (!nombre.equals("***************") || !localidad.equals("***************") || empleados != -1)) {
                    System.out.println("[ANOMALÍA] Byte " + posByte + ": Registro eliminado malformado.");
                    integro = false;
                } else if (id == 0 && (!nombre.equals("---------------") || !localidad.equals("---------------") || empleados != 0)) {
                    System.out.println("[ANOMALÍA] Byte " + posByte + ": Posición nunca utilizada malformada.");
                    integro = false;
                } else if (id > 0) {
                    if (id != idEsperado || empleados < 0 || nombre.trim().isEmpty() || localidad.trim().isEmpty()) {
                        System.out.println("[ANOMALÍA] Byte " + posByte + ": Registro activo con datos inconsistentes.");
                        integro = false;
                    }
                }
            }

            if (integro) System.out.println("El fichero es ÍNTEGRO.");
            else System.out.println("Se detectaron fallos de integridad.");
            System.out.println("-----------------------------");
        } catch (IOException e) {
            System.out.println("Error de integridad: " + e.getMessage());
        }
    }

    private static void operacionInsertar() {
        System.out.println("\n--- INSERTAR ---");
        System.out.print("Número de departamento (1-32767): ");
        short num = (short) leerEnteroSeguro();
        System.out.print("Nombre (máx 15 chars): ");
        String nombre = Entrada.cadena();
        System.out.print("Localidad (máx 15 chars): ");
        String localidad = Entrada.cadena();
        System.out.print("Número de empleados: ");
        short emp = (short) leerEnteroSeguro();
        insertaDepartamento(num, nombre, localidad, emp);
    }

    private static void operacionConsultar() {
        System.out.print("Número de departamento a consultar: ");
        consultaDepartamento((short) leerEnteroSeguro());
    }

    private static void operacionActualizarEmpleados() {
        System.out.print("Número de departamento: ");
        short num = (short) leerEnteroSeguro();
        System.out.print("Nuevo número de empleados: ");
        short emp = (short) leerEnteroSeguro();
        actualizaNumeroEmpleados(num, emp);
    }

    private static void operacionActualizarNombreLocalidad() {
        System.out.print("Número de departamento: ");
        short num = (short) leerEnteroSeguro();
        System.out.print("Nuevo nombre: ");
        String nombre = Entrada.cadena();
        System.out.print("Nueva localidad: ");
        String localidad = Entrada.cadena();
        actualizaNombreLocalidad(num, nombre, localidad);
    }

    private static void operacionEliminar() {
        System.out.print("Número de departamento a eliminar: ");
        short num = (short) leerEnteroSeguro();
        if (!existeDepartamento(num)) {
            System.out.println("El departamento no existe.");
            return;
        }
        System.out.print("¿Seguro que desea eliminar el departamento " + num + "? (S/N): ");
        if (Entrada.cadena().trim().equalsIgnoreCase("S")) {
            eliminaDepartamento(num);
        } else {
            System.out.println("Cancelado.");
        }
    }

    private static int leerEnteroSeguro() {
        while (true) {
            try {
                return Entrada.entero();
            } catch (Exception e) {
                System.out.print("Entrada no válida. Introduzca un número: ");
            }
        }
    }

    private static String validarYFormatearTexto(String texto) {
        if (texto == null) return null;
        String limpio = texto.trim();
        if (limpio.isEmpty() || limpio.length() > 15) {
            System.out.println("Error: Texto vacío o superior a 15 caracteres.");
            return null;
        }
        StringBuilder sb = new StringBuilder(limpio);
        while (sb.length() < 15) sb.append(' ');
        return sb.toString();
    }

    private static void escribirNuncaUtilizada(RandomAccessFile raf) throws IOException {
        raf.writeShort((short) 0);
        for (int i = 0; i < 15; i++) raf.writeChar('-');
        for (int i = 0; i < 15; i++) raf.writeChar('-');
        raf.writeShort((short) 0);
    }

    private static String determinarEstadoPosicion(short id, String nombre, String localidad, short empleados, int idEsperado) {
        if (id == 0 && nombre.equals("---------------") && localidad.equals("---------------") && empleados == 0) return "Nunca utilizada";
        if (id == -1 && nombre.equals("***************") && localidad.equals("***************") && empleados == -1) return "Eliminada";
        if (id > 0 && id <= 32767 && id == idEsperado && empleados >= 0 && !nombre.trim().isEmpty() && !localidad.trim().isEmpty()) return "Activa";
        return "Anómala";
    }
}