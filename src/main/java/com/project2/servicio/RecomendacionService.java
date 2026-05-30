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

        // Algoritmo híbrido:
        // 1) Contenido: categorías que le gustan al usuario y categorías del restaurante.
        // 2) Colaborativo: usuarios con gustos parecidos que visitaron bien ese restaurante.
        // 3) Calidad general: rating propio del restaurante.
        // 4) Excluye restaurantes que el usuario actual ya visitó.
        //
        // Importante: la base guarda el texto como lo escribió el usuario,
        // pero el algoritmo compara una clave normalizada para evitar duplicados lógicos.
        String query =
            "MATCH (u:Usuario {idUsuario: $idUsuario}) " +
            "MATCH (res:Restaurante) " +
            "WHERE NOT (u)-[:Visito]->(res) " +

            // Coincidencia por contenido entre gustos del usuario y categorías del restaurante
            "OPTIONAL MATCH (u)-[:Le_Gusta]->(catUsuario:Categoria) " +
            "OPTIONAL MATCH (res)-[:Pertenece_A]->(catRestaurante:Categoria) " +
            "WITH u, res, " +
            "count(DISTINCT CASE WHEN coalesce(catUsuario.clave, toLower(trim(catUsuario.tipoComida))) = coalesce(catRestaurante.clave, toLower(trim(catRestaurante.tipoComida))) THEN catRestaurante.tipoComida ELSE null END) AS coincidencias_categoria, " +
            "collect(DISTINCT CASE WHEN coalesce(catUsuario.clave, toLower(trim(catUsuario.tipoComida))) = coalesce(catRestaurante.clave, toLower(trim(catRestaurante.tipoComida))) THEN catRestaurante.tipoComida ELSE null END) AS categorias_coincidentes " +

            // Usuarios similares: comparten gustos con el usuario actual
            "OPTIONAL MATCH (u)-[:Le_Gusta]->(gustoUsuario:Categoria) " +
            "OPTIONAL MATCH (usuarioSimilar:Usuario)-[:Le_Gusta]->(gustoSimilar:Categoria) " +
            "WHERE usuarioSimilar <> u AND coalesce(gustoUsuario.clave, toLower(trim(gustoUsuario.tipoComida))) = coalesce(gustoSimilar.clave, toLower(trim(gustoSimilar.tipoComida))) " +
            "OPTIONAL MATCH (usuarioSimilar)-[visita:Visito]->(res) " +
            "WHERE visita.calificacion >= 4 " +
            "WITH res, coincidencias_categoria, categorias_coincidentes, " +
            "count(DISTINCT CASE WHEN visita IS NULL THEN null ELSE usuarioSimilar END) AS fuerza_social, " +
            "coalesce(avg(visita.calificacion), 0.0) AS promedio_visitas " +

            // Cualidades del restaurante, usadas como explicación de la recomendación
            "OPTIONAL MATCH (res)-[:Ofrece]->(cualidad:Cualidad) " +
            "WITH res, coincidencias_categoria, categorias_coincidentes, fuerza_social, promedio_visitas, " +
            "collect(DISTINCT cualidad.etiqueta) AS etiquetas, " +
            "(coincidencias_categoria * 5.0 + fuerza_social * 2.0 + promedio_visitas + coalesce(res.calificacion, 0.0)) AS score_total " +
            "WHERE score_total > 0 " +
            "ORDER BY score_total DESC, coincidencias_categoria DESC, fuerza_social DESC, res.calificacion DESC " +
            "LIMIT 10 " +
            "RETURN res.nombre AS restaurante, res.calificacion AS rating, res.rangoPrecio AS precio, " +
            "fuerza_social AS score_social, etiquetas AS ambientes, categorias_coincidentes AS categorias, score_total AS score";

        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("idUsuario", idUsuarioActual));
            
            while (result.hasNext()) {
                var record = result.next();
                Map<String, Object> item = Map.of(
                    "restaurante", record.get("restaurante").asString(),
                    "rating", record.get("rating").asDouble(),
                    "precio", record.get("precio").asString(),
                    "coincidenciaSocial", record.get("score_social").asInt(),
                    "etiquetas", record.get("ambientes").asList(org.neo4j.driver.Value::asString),
                    "categorias", record.get("categorias").asList(org.neo4j.driver.Value::asString),
                    "score", record.get("score").asDouble()
                );
                recomendaciones.add(item);
            }
        }
        return recomendaciones;
    }
}
