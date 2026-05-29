package com.project2;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import com.project2.bdd.Neo4jDAO;
import com.project2.modelo.Restaurante;
import com.project2.modelo.Usuario;
import com.project2.servicio.RecomendacionService;

public class MainApp {
    public static void main(String[] args) {
        // Se configuran las credenciales de Neo4j local
        String uri = System.getenv().getOrDefault("NEO4J_URI", "neo4j+s://e99223bc.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USERNAME", "e99223bc");
        String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "NNRon_9Zwh_pmzmlYWgJUiTBsuXlixh4Pkai0HTkQRc");

        // Se inicializa el driver de Neo4j
        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))) {
            
            // Se agregan dependencias a los componentes
            Neo4jDAO dao = new Neo4jDAO(driver);
            RecomendacionService recomendacionService = new RecomendacionService(driver);

            System.out.println("1. Se insertan los nodos y aristas anonimizadas");

            // Aquí va la base de datos
            // Aqui se agregan los usuarios
            Usuario anon = new Usuario(1, "Diego", "Zona 15", 150.0);
            Usuario anon1 = new Usuario(2, "David", "San Cristobal", 120.0);
            Usuario anon2 = new Usuario(3, "Wilfred", "Zona 11", 250.0);
           
            dao.agregarUsuario(anon);
            dao.agregarUsuario(anon1);
            dao.agregarUsuario(anon2);

            // Aqui se agregan los restaurantes 
            Restaurante cafeGitane = new Restaurante(101, "Café Gitane UVG", "Campus UVG", 4.8, "$$");
            Restaurante hamburguesas = new Restaurante(102, "Hamburguesas El Capi", "Cerca UVG", 4.5, "$");
            Restaurante Mcdonalds = new Restaurante(103, "McDonalds", "Cayala", 4.3, "$");

            dao.agregarRestaurante(cafeGitane);
            dao.agregarRestaurante(hamburguesas);
            dao.agregarRestaurante(Mcdonalds);

            // Aquí van las relaciones iniciales

            // Usuario 1
            dao.agregarRelacionGusta(1, "Café");
            dao.agregarRelacionGusta(1, "Hamburguesas");
            // Usuario 2 
            dao.agregarRelacionGusta(2, "Italiana");
            dao.agregarRelacionGusta(2, "Hamburguesas");
            // Usuario 3
            dao.agregarRelacionGusta(3, "Hamburguesas");
            dao.agregarRelacionGusta(3, "Italiana");

            // Interacciones o calificaciones de los usuarios a los restaurantes
            // Importante: el algoritmo solo toma visitas con calificación mayor a 4.

            // Diego visitó McDonalds, pero con baja calificación.
            // Así el sistema todavía le puede recomendar Café Gitane UVG y Hamburguesas El Capi.
            dao.registrarVisita(1, 103, 3);

            // David visitó Café Gitane UVG.
            // No visitó los restaurantes de Hamburguesas, para que se le puedan recomendar.
            dao.registrarVisita(2, 101, 5);

            // Wilfred visitó los restaurantes que coinciden con los gustos de Diego y David.
            // Estas visitas sirven como "prueba social" para recomendar.
            dao.registrarVisita(3, 101, 5);
            dao.registrarVisita(3, 102, 5);
            dao.registrarVisita(3, 103, 5);

            // Categorías 
            dao.agregarCategoriaRestaurante(101, "Café");
            dao.agregarCategoriaRestaurante(101, "Italiana");
            dao.agregarCategoriaRestaurante(102, "Hamburguesas");
            dao.agregarCategoriaRestaurante(103, "Hamburguesas");
            // cualidades de los restaurantes
            dao.agregarCualidadRestaurante(101, "Tranquilo");
            dao.agregarCualidadRestaurante(101, "Ambiente estudiantil");
            dao.agregarCualidadRestaurante(102, "Servicio rápido");
            dao.agregarCualidadRestaurante(103, "Servicio rápido");

            System.out.println("\n2. Ejecutando Algoritmo de Recomendación Híbrido");
            // Generar sugerencias para el usuario (con ID1)
            List<Map<String, Object>> sugerencias = recomendacionService.generarRecomendacionHibrida(1);

            if (sugerencias.isEmpty()) {
                System.out.println("En este momento no hay recomendaciones que cumplan los filtros deseados.");
            } else {
                System.out.println("Top " + sugerencias.size() + " Recomendaciones Personalizadas:");
                int idx = 1;
                for (Map<String, Object> sug : sugerencias) {
                    System.out.println(idx + ". " + sug.get("restaurante") + " | Rating: " + sug.get("rating") + " | Precio: " + sug.get("precio"));
                    System.out.println("   Razón: Recomendado por " + sug.get("coincidenciaSocial") + " estudiantes de tu entorno con gustos similares.");
                    List<?> etiquetas = (List<?>) sug.get("etiquetas");
                    System.out.println("    Ambiente: " + (etiquetas.isEmpty() ? "No especificado" : String.join(", ", (List<String>)etiquetas)));
                    idx++;
                }
            }

            System.out.println("\n3. Limpieza de Información");
            // Demostración del funcionamiento para eliminar datos
            //dao.eliminarUsuario(1);
            //dao.eliminarUsuario(2);
            //dao.eliminarUsuario(3);
        } catch (Exception e) {
            System.err.println("\nHubo un error en la conexión o ejecución de la base de datos: " + e.getMessage());
            System.err.println("Por favor, asegúrate de que Neo4j esté corriendo y que los datos de acceso sean válidos.");
        }
    }
}