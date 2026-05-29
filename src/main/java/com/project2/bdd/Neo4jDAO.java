package com.project2.bdd;

import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

import com.project2.modelo.Restaurante;
import com.project2.modelo.Usuario;

public class Neo4jDAO {
    private final Driver driver;

    public Neo4jDAO(Driver driver) {
        this.driver = driver;
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
                       "MERGE (c:Categoria {tipoComida: $tipoComida}) " +
                       "MERGE (u)-[:Le_Gusta]->(c)";
        
        try (Session session = driver.session()) {
            session.run(query, Map.of("idUsuario", idUsuario, "tipoComida", tipoComida));
            System.out.println("Éxito. Relación Le_Gusta creada para Usuario ID: " + idUsuario);
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
                   "MERGE (c:Categoria {tipoComida: $tipoComida}) " +
                   "MERGE (r)-[:Pertenece_A]->(c)";

    try (Session session = driver.session()) {
        session.run(query, Map.of(
            "idRestaurante", idRestaurante,
            "tipoComida", tipoComida
        ));
        System.out.println("Éxito. Categoría asignada al restaurante ID: " + idRestaurante);
    }
}

    public void agregarCualidadRestaurante(int idRestaurante, String etiqueta) {
    String query = "MATCH (r:Restaurante {idRestaurante: $idRestaurante}) " +
                   "MERGE (q:Cualidad {etiqueta: $etiqueta}) " +
                   "MERGE (r)-[:Ofrece]->(q)";

    try (Session session = driver.session()) {
        session.run(query, Map.of(
            "idRestaurante", idRestaurante,
            "etiqueta", etiqueta
        ));
        System.out.println("Éxito. Cualidad asignada al restaurante ID: " + idRestaurante);
    }
}
}