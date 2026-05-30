package com.project2.bdd;

import java.util.Map;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import java.util.ArrayList;

import com.project2.modelo.Restaurante;
import com.project2.modelo.Usuario;

public class Neo4jDAO {
    private final Driver driver;

    public Neo4jDAO(Driver driver) {
        this.driver = driver;
    }

    private String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim();
    }

    private String normalizarClave(String texto) {
        return limpiarTexto(texto).toLowerCase();
    }

    // Se agrega el usuario
    public void agregarUsuario(Usuario usuario) {
        String query = "MERGE (u:Usuario {idUsuario: $idUsuario}) " +
                       "SET u.nombre = $nombre, u.ubicacionActual = $ubicacion, u.presupuestoMax = $presupuesto";
        
        try (Session session = driver.session()) {
            session.run(query, Map.of(
                "idUsuario", usuario.getIdUsuario(),
                "nombre", usuario.getNombre(),
                "ubicacion", usuario.getUbicacionActual(),
                "presupuesto", usuario.getPresupuestoMax()
            ));
            System.out.println("Éxito. Usuario guardado: " + usuario.getNombre());
        }
    }

    // Se agrega el restaurante
    public void agregarRestaurante(Restaurante restaurante) {
        String query = "MERGE (r:Restaurante {idRestaurante: $idRestaurante}) " +
                       "SET r.nombre = $nombre, r.direccion = $direccion, " +
                       "r.calificacion = $calificacion, r.rangoPrecio = $rangoPrecio";
        
        try (Session session = driver.session()) {
            session.run(query, Map.of(
                "idRestaurante", restaurante.getIdRestaurante(),
                "nombre", restaurante.getNombre(),
                "direccion", restaurante.getDireccion(),
                "calificacion", restaurante.getCalificacion(),
                "rangoPrecio", restaurante.getRangoPrecio()
            ));
            System.out.println("Éxito. Restaurante guardado: " + restaurante.getNombre());
        }
    }

    // Se crea la relación de LE_GUSTA para filtrar por contenido
    public void agregarRelacionGusta(int idUsuario, String tipoComida) {
        String query = "MATCH (u:Usuario {idUsuario: $idUsuario}) " +
                       "MERGE (c:Categoria {clave: $clave}) " +
                       "ON CREATE SET c.tipoComida = $tipoComida " +
                       "ON MATCH SET c.tipoComida = coalesce(c.tipoComida, $tipoComida) " +
                       "MERGE (u)-[:Le_Gusta]->(c)";
        
        try (Session session = driver.session()) {
            session.run(query, Map.of(
                "idUsuario", idUsuario,
                "tipoComida", limpiarTexto(tipoComida),
                "clave", normalizarClave(tipoComida)
            ));
            System.out.println("Éxito. Relación Le_Gusta creada para Usuario ID: " + idUsuario);
        }
    }

    // NUEVO MÉTODO: Rompe la relación Le_Gusta entre un Usuario y una Categoría
    public void eliminarRelacionGusta(int idUsuario, String tipoComida) {
        String query = "MATCH (u:Usuario {idUsuario: $idUsuario})-[rel:Le_Gusta]->(c:Categoria) " +
                       "WHERE coalesce(c.clave, toLower(trim(c.tipoComida))) = $clave " +
                       "DELETE rel";
        
        try (Session session = driver.session()) {
            session.run(query, Map.of("idUsuario", idUsuario, "clave", normalizarClave(tipoComida)));
            System.out.println("Éxito. Relación Le_Gusta eliminada para Usuario ID: " + idUsuario);
        }
    }

    // Se registra una visita con calificación por Filtrado Colaborativo
    public void registrarVisita(int idUsuario, int idRestaurante, int calificacionVisita) {
        String query = "MATCH (u:Usuario {idUsuario: $idUsuario}) " +
                       "MATCH (r:Restaurante {idRestaurante: $idRestaurante}) " +
                       "MERGE (u)-[v:Visito]->(r) " +
                       "SET v.calificacion = toInteger($calificacion)";
        
        try (Session session = driver.session()) {
            session.run(query, Map.of(
                "idUsuario", idUsuario,
                "idRestaurante", idRestaurante,
                "calificacion", calificacionVisita
            ));
            System.out.println("Éxito. Visita registrada: Usuario " + idUsuario + " -> Restaurante " + idRestaurante);
        }
    }

    // Se elimina un usuario
    public void eliminarUsuario(int idUsuario) {
        String query = "MATCH (u:Usuario {idUsuario: $idUsuario}) DETACH DELETE u";
        try (Session session = driver.session()) {
            session.run(query, Map.of("idUsuario", idUsuario));
            System.out.println("Éxito. Usuario ID " + idUsuario + " removido.");
        }
    }

    // Se elimina un restaurante
    public void eliminarRestaurante(int idRestaurante) {
        String query = "MATCH (r:Restaurante {idRestaurante: $idRestaurante}) DETACH DELETE r";
        try (Session session = driver.session()) {
            session.run(query, Map.of("idRestaurante", idRestaurante));
            System.out.println("Éxito. Restaurante ID " + idRestaurante + " removido.");
        }
    }

    public void agregarCategoriaRestaurante(int idRestaurante, String tipoComida) {
        String query = "MATCH (r:Restaurante {idRestaurante: $idRestaurante}) " +
                       "MERGE (c:Categoria {clave: $clave}) " +
                       "ON CREATE SET c.tipoComida = $tipoComida " +
                       "ON MATCH SET c.tipoComida = coalesce(c.tipoComida, $tipoComida) " +
                       "MERGE (r)-[:Pertenece_A]->(c)";

        try (Session session = driver.session()) {
            session.run(query, Map.of(
                "idRestaurante", idRestaurante,
                "tipoComida", limpiarTexto(tipoComida),
                "clave", normalizarClave(tipoComida)
            ));
            System.out.println("Éxito. Categoría asignada al restaurante ID: " + idRestaurante);
        }
    }

    // NUEVO MÉTODO: Rompe la relación Pertenece_A entre un Restaurante y una Categoría
    public void eliminarCategoriaRestaurante(int idRestaurante, String tipoComida) {
        String query = "MATCH (r:Restaurante {idRestaurante: $idRestaurante})-[rel:Pertenece_A]->(c:Categoria) " +
                       "WHERE coalesce(c.clave, toLower(trim(c.tipoComida))) = $clave " +
                       "DELETE rel";

        try (Session session = driver.session()) {
            session.run(query, Map.of(
                "idRestaurante", idRestaurante,
                "clave", normalizarClave(tipoComida)
            ));
            System.out.println("Éxito. Categoría desvinculada del restaurante ID: " + idRestaurante);
        }
    }

    public void eliminarCategoria(String tipoComida) {
        String query = "MATCH (c:Categoria) " +
                       "WHERE coalesce(c.clave, toLower(trim(c.tipoComida))) = $clave " +
                       "DETACH DELETE c";
        try (Session session = driver.session()) {
            session.run(query, Map.of("clave", normalizarClave(tipoComida)));
            System.out.println("Éxito. Categoría " + tipoComida + " eliminada.");
        }
    }

    public void agregarCualidadRestaurante(int idRestaurante, String etiqueta) {
        String query = "MATCH (r:Restaurante {idRestaurante: $idRestaurante}) " +
                       "MERGE (q:Cualidad {clave: $clave}) " +
                       "ON CREATE SET q.etiqueta = $etiqueta " +
                       "ON MATCH SET q.etiqueta = coalesce(q.etiqueta, $etiqueta) " +
                       "MERGE (r)-[:Ofrece]->(q)";

        try (Session session = driver.session()) {
            session.run(query, Map.of(
                "idRestaurante", idRestaurante,
                "etiqueta", limpiarTexto(etiqueta),
                "clave", normalizarClave(etiqueta)
            ));
            System.out.println("Éxito. Cualidad asignada al restaurante ID: " + idRestaurante);
        }
    }


    public void eliminarCualidadRestaurante(int idRestaurante, String etiqueta) {
        String query = "MATCH (r:Restaurante {idRestaurante: $idRestaurante})-[rel:Ofrece]->(q:Cualidad) " +
                       "WHERE coalesce(q.clave, toLower(trim(q.etiqueta))) = $clave " +
                       "DELETE rel";

        try (Session session = driver.session()) {
            session.run(query, Map.of(
                "idRestaurante", idRestaurante,
                "clave", normalizarClave(etiqueta)
            ));
            System.out.println("Éxito. Cualidad removida del restaurante ID: " + idRestaurante);
        }
    }

    // Trae todos los usuarios para renderizarlos en la tabla
    public java.util.List<Map<String, Object>> obtenerTodosLosUsuarios() {
        java.util.List<Map<String, Object>> lista = new ArrayList<>();
        String query = "MATCH (u:Usuario) RETURN u.idUsuario AS id, u.nombre AS nombre, u.ubicacionActual AS ubi, u.presupuestoMax AS presu ORDER BY u.idUsuario";
        try (org.neo4j.driver.Session session = driver.session()) {
            org.neo4j.driver.Result result = session.run(query);
            while (result.hasNext()) {
                var r = result.next();
                lista.add(Map.of(
                    "id", r.get("id").asInt(),
                    "nombre", r.get("nombre").asString(),
                    "ubi", r.get("ubi").asString(),
                    "presu", r.get("presu").asDouble()
                ));
            }
        }
        return lista;
    }

    public java.util.List<Map<String, Object>> obtenerTodosLosRestaurantes() {
        java.util.List<Map<String, Object>> lista = new ArrayList<>();
        String query = "MATCH (r:Restaurante) RETURN r.idRestaurante AS id, r.nombre AS nombre, r.direccion AS dir, r.calificacion AS rat, r.rangoPrecio AS precio ORDER BY r.idRestaurante";
        try (org.neo4j.driver.Session session = driver.session()) {
            org.neo4j.driver.Result result = session.run(query);
            while (result.hasNext()) {
                var r = result.next();
                lista.add(Map.of(
                    "id", r.get("id").asInt(),
                    "nombre", r.get("nombre").asString(),
                    "dir", r.get("dir").asString(),
                    "rat", r.get("rat").asDouble(),
                    "precio", r.get("precio").asString()
                ));
            }
        }
        return lista;
    }
}