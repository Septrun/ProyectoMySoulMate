package com.project2.servicio;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecomendacionService {
    private final Driver driver;

    public RecomendacionService(Driver driver) {
        this.driver = driver;
    }

    public List<Map<String, Object>> generarRecomendacionHibrida(int idUsuarioActual) {
        List<Map<String, Object>> recomendaciones = new ArrayList<>();

        // Query de recomendación
        String query = 
            "MATCH (usuario_actual:Usuario {idUsuario: $idUsuario})-[:Le_Gusta]->(cat:Categoria) " + 
            // 1. Se obtienen las categorías
            "MATCH (res:Restaurante)-[:Pertenece_A]->(cat) " +                                      
            // 2. Se buscan los restaurantes
            "MATCH (usuarios_similares:Usuario)-[v:Visito]->(res) " +                               
            // 3. Se identifican los usuarios similares
            "WHERE usuarios_similares <> usuario_actual " +
            "AND v.calificacion > 4 " +                                                   // 4. Se filtra el lugar por > 4 estrellas y que no se haya visitado antes
            "AND NOT (usuario_actual)-[:Visito]->(res) " +
            "OPTIONAL MATCH (res)-[:Ofrece]->(cualidad:Cualidad) " +
            "WITH res, count(DISTINCT usuarios_similares) AS fuerza_social, collect(DISTINCT cualidad.etiqueta) AS etiquetas " +
            "ORDER BY fuerza_social DESC, res.calificacion DESC " +                                 
            // 5. Ordenar
            "LIMIT 5 " +                                                                            
            // 6. Se muestran las top 5 sugerencias
            "RETURN res.nombre AS restaurante, res.calificacion AS rating, res.rangoPrecio AS precio, " +
            "fuerza_social AS score_social, etiquetas AS ambientes";

        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("idUsuario", idUsuarioActual));
            
            while (result.hasNext()) {
                var record = result.next();
                Map<String, Object> item = Map.of(
                    "restaurante", record.get("restaurante").asString(),
                    "rating", record.get("rating").asDouble(),
                    "precio", record.get("precio").asString(),
                    "coincidenciaSocial", record.get("score_social").asInt(),
                    "etiquetas", record.get("ambientes").asList(org.neo4j.driver.Value::asString)
                );
                recomendaciones.add(item);
            }
        }
        return recomendaciones;
    }
}