package com.project2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import com.project2.bdd.Neo4jDAO;
import com.project2.modelo.Restaurante;
import com.project2.modelo.Usuario;
import com.project2.servicio.RecomendacionService;

public class GestionApp extends JFrame {
    private Driver driver;
    private Neo4jDAO dao;
    private RecomendacionService recomendacionService;

    private DefaultTableModel modeloUsuarios;
    private DefaultTableModel modeloRestaurantes;

    // --- PALETA DE COLORES GASTRONÓMICA ---
    private final Color COLOR_FONDO_CLARO = new Color(252, 248, 243);  // Crema/Hueso cálido
    private final Color COLOR_PANEL_INTERNO = new Color(255, 255, 255); // Blanco puro
    private final Color COLOR_NARANJA_RESTAURANTE = new Color(230, 126, 34); // Naranja apetito
    private final Color COLOR_ROJO_TOMATE = new Color(211, 47, 47);      // Rojo cálido para eliminar
    private final Color COLOR_TEXTO_OSCURO = new Color(44, 62, 80);      // Gris carbón para lectura
    
    private final Font FUENTE_UI = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 14);

    public GestionApp() {
        conectarBD();
        
        // Configuración inicial de la ventana principal (oculta al inicio)
        setTitle("Soulmate Food Analytics - Panel Gourmet");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO_CLARO);

        // Mostrar primero la pantalla de bienvenida
        mostrarPantallaBienvenida();
    }

    private void conectarBD() {
        String uri = System.getenv().getOrDefault("NEO4J_URI", "neo4j+s://e99223bc.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USERNAME", "e99223bc");
        String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "NNRon_9Zwh_pmzmlYWgJUiTBsuXlixh4Pkai0HTkQRc");
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        dao = new Neo4jDAO(driver);
        recomendacionService = new RecomendacionService(driver);
    }

    // --- PANTALLA DE BIENVENIDA (SPLASH SCREEN) ---
    private void mostrarPantallaBienvenida() {
        JDialog splash = new JDialog(this, "Bienvenido a Soulmate Food", true);
        splash.setSize(500, 550);
        splash.setLocationRelativeTo(null);
        splash.setLayout(new BorderLayout());
        splash.getContentPane().setBackground(COLOR_FONDO_CLARO);

        // Contenedor del contenido
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setOpaque(false);
        panelContenido.setBorder(new EmptyBorder(30, 30, 30, 30));

// 1. Logo del Programa
        JLabel lblLogo = new JLabel();
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {

            java.net.URL imgURL = getClass().getResource("/assets/logo.png");
            
            if (imgURL == null) {
                // Si por alguna razón no encuentra el recurso, muestra el cuadro temporal
                lblLogo.setText("<html><center><div style='background-color:#E67E22; color:white; padding:40px; border-radius:150px;'>🍳 LOGO AQUÍ<br>(300x300)</div></center></html>");
                lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
            } else {
                ImageIcon icon = new ImageIcon(imgURL);
                // Escalar la imagen de forma suave
                Image imgEscala = icon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(imgEscala));
            }
        } catch (Exception e) {
            lblLogo.setText("🎨 Soulmate Food");
            System.err.println("Error al cargar el logo: " + e.getMessage());
        }

        // 2. Títulos
        JLabel lblTitulo = new JLabel("Soulmate Food App");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(COLOR_NARANJA_RESTAURANTE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Encuentra tu match gastronómico perfecto");
        lblSub.setFont(FUENTE_UI);
        lblSub.setForeground(COLOR_TEXTO_OSCURO);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Botón de Entrada
        JButton btnEntrar = new JButton("Presiona ENTER o Clic Aquí para Entrar ➔");
        btnEntrar.setFont(FUENTE_TITULO);
        btnEntrar.setBackground(COLOR_NARANJA_RESTAURANTE);
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.setPreferredSize(new Dimension(350, 45));
        btnEntrar.setMaximumSize(new Dimension(350, 45));

        // Acción al hacer clic en el botón
        btnEntrar.addActionListener(e -> {
            splash.dispose();
            inicializarPanelPrincipal();
        });

        // LÓGICA CLAVE: Capturar la tecla ENTER en cualquier parte de la pantalla de bienvenida
        KeyAdapter mapeoEnter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    splash.dispose();
                    inicializarPanelPrincipal();
                }
            }
        };
        
        btnEntrar.addKeyListener(mapeoEnter);
        splash.addKeyListener(mapeoEnter);
        panelContenido.addKeyListener(mapeoEnter);

        // Organizar elementos en la pantalla de bienvenida
        panelContenido.add(lblLogo);
        panelContenido.add(Box.createVerticalStrut(25));
        panelContenido.add(lblTitulo);
        panelContenido.add(Box.createVerticalStrut(8));
        panelContenido.add(lblSub);
        panelContenido.add(Box.createVerticalStrut(35));
        panelContenido.add(btnEntrar);

        splash.add(panelContenido, BorderLayout.CENTER);
        
        // Hacemos que el botón tenga el foco inicial para que el Enter funcione directamente
        SwingUtilities.invokeLater(btnEntrar::requestFocusInWindow);
        
        splash.setVisible(true);
    }

    // --- CONSTRUCCIÓN DEL PANEL PRINCIPAL (POST-LOGIN) ---
    private void inicializarPanelPrincipal() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(new EmptyBorder(15, 15, 15, 15));
        contenedor.setBackground(COLOR_FONDO_CLARO);

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(FUENTE_TITULO);
        pestañas.setBackground(COLOR_PANEL_INTERNO);
        pestañas.setForeground(COLOR_TEXTO_OSCURO);

        // Paneles con estilo culinario
        pestañas.addTab("👤 Miembros / Usuarios", crearPanelUsuarios());
        pestañas.addTab("🍔 Menú de Restaurantes", crearPanelRestaurantes());
        pestañas.addTab("🍕 Red de Gustos", crearPanelVinculos());
        pestañas.addTab("🍳 Recomendaciones del Chef", crearPanelRecomendaciones());

        contenedor.add(pestañas, BorderLayout.CENTER);
        add(contenedor);

        // Forzar carga de datos desde Neo4j
        actualizarTablaUsuarios();
        actualizarTablaRestaurantes();

        // Mostrar ventana principal
        setVisible(true);
    }

    private JPanel crearPanelUsuarios() {
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2, 20, 0));
        panelPrincipal.setBackground(COLOR_FONDO_CLARO);

        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));
        panelForm.setBackground(COLOR_PANEL_INTERNO);
        panelForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true), "Registrar Comensal", 0, 0, FUENTE_TITULO, COLOR_NARANJA_RESTAURANTE));

        JTextField txtId = crearTextFieldCulinario("ID Único");
        JTextField txtNombre = crearTextFieldCulinario("Nombre Completo");
        JTextField txtUbi = crearTextFieldCulinario("Ubicación (Zona)");
        JTextField txtPresu = crearTextFieldCulinario("Presupuesto Disponible (Q)");

        panelForm.add(Box.createVerticalStrut(15)); panelForm.add(txtId); 
        panelForm.add(Box.createVerticalStrut(10)); panelForm.add(txtNombre); 
        panelForm.add(Box.createVerticalStrut(10)); panelForm.add(txtUbi); 
        panelForm.add(Box.createVerticalStrut(10)); panelForm.add(txtPresu); 
        panelForm.add(Box.createVerticalStrut(20));

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 12, 0));
        panelBotones.setOpaque(false);
        JButton btnGuardar = new JButton("Añadir a la Red");
        JButton btnEliminar = new JButton("Dar de Baja");
        estilarBoton(btnGuardar, COLOR_NARANJA_RESTAURANTE);
        estilarBoton(btnEliminar, COLOR_ROJO_TOMATE);
        panelBotones.add(btnGuardar); panelBotones.add(btnEliminar);
        panelForm.add(panelBotones);

        JPanel panelTabla = new JPanel(new BorderLayout(0, 10));
        panelTabla.setBackground(COLOR_PANEL_INTERNO);
        panelTabla.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true), "Comensales en la Red", 0, 0, FUENTE_TITULO, COLOR_TEXTO_OSCURO));

        String[] cols = {"ID", "Nombre", "Zona", "Presupuesto"};
        modeloUsuarios = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable tabla = new JTable(modeloUsuarios);
        configurarEstiloTabla(tabla);

        JButton btnRefresh = new JButton("🔄 Recargar Comensales");
        estilarBoton(btnRefresh, COLOR_TEXTO_OSCURO);

        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panelTabla.add(btnRefresh, BorderLayout.SOUTH);

        // Acciones
        btnGuardar.addActionListener(e -> {
            try {
                Usuario u = new Usuario(Integer.parseInt(txtId.getText()), txtNombre.getText(), txtUbi.getText(), Double.parseDouble(txtPresu.getText()));
                dao.agregarUsuario(u);
                actualizarTablaUsuarios();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Por favor, completa los campos correctamente."); }
        });

        btnEliminar.addActionListener(e -> {
            try {
                dao.eliminarUsuario(Integer.parseInt(txtId.getText()));
                actualizarTablaUsuarios();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Ingresa un ID válido."); }
        });

        btnRefresh.addActionListener(e -> actualizarTablaUsuarios());

        panelPrincipal.add(panelForm);
        panelPrincipal.add(panelTabla);
        return panelPrincipal;
    }

    private JPanel crearPanelRestaurantes() {
        JPanel panelPrincipal = new JPanel(new GridLayout(1, 2, 20, 0));
        panelPrincipal.setBackground(COLOR_FONDO_CLARO);

        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));
        panelForm.setBackground(COLOR_PANEL_INTERNO);
        panelForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true), "Nuevo Establecimiento", 0, 0, FUENTE_TITULO, COLOR_NARANJA_RESTAURANTE));

        JTextField txtId = crearTextFieldCulinario("ID de Registro");
        JTextField txtNombre = crearTextFieldCulinario("Nombre Comercial");
        JTextField txtDir = crearTextFieldCulinario("Dirección Física");
        JTextField txtRat = crearTextFieldCulinario("Puntuación de Críticos (0-5)");
        JTextField txtPrecio = crearTextFieldCulinario("Costo Promedio ($, $$, $$$)");

        panelForm.add(Box.createVerticalStrut(15)); panelForm.add(txtId); 
        panelForm.add(Box.createVerticalStrut(10)); panelForm.add(txtNombre); 
        panelForm.add(Box.createVerticalStrut(10)); panelForm.add(txtDir); 
        panelForm.add(Box.createVerticalStrut(10)); panelForm.add(txtRat); 
        panelForm.add(Box.createVerticalStrut(10)); panelForm.add(txtPrecio); 
        panelForm.add(Box.createVerticalStrut(20));

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 12, 0));
        panelBotones.setOpaque(false);
        JButton btnGuardar = new JButton("Publicar Lugar");
        JButton btnEliminar = new JButton("Remover del Mapa");
        estilarBoton(btnGuardar, COLOR_NARANJA_RESTAURANTE);
        estilarBoton(btnEliminar, COLOR_ROJO_TOMATE);
        panelBotones.add(btnGuardar); panelBotones.add(btnEliminar);
        panelForm.add(panelBotones);

        JPanel panelTabla = new JPanel(new BorderLayout(0, 10));
        panelTabla.setBackground(COLOR_PANEL_INTERNO);
        panelTabla.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true), "Mapa de Restaurantes Activos", 0, 0, FUENTE_TITULO, COLOR_TEXTO_OSCURO));

        String[] cols = {"ID", "Establecimiento", "Dirección", "Crítica", "Rango"};
        modeloRestaurantes = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable tabla = new JTable(modeloRestaurantes);
        configurarEstiloTabla(tabla);

        JButton btnRefresh = new JButton("🔄 Recargar Mapa");
        estilarBoton(btnRefresh, COLOR_TEXTO_OSCURO);

        panelTabla.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panelTabla.add(btnRefresh, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> {
            try {
                Restaurante r = new Restaurante(Integer.parseInt(txtId.getText()), txtNombre.getText(), txtDir.getText(), Double.parseDouble(txtRat.getText()), txtPrecio.getText());
                dao.agregarRestaurante(r);
                actualizarTablaRestaurantes();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Revisa los campos numéricos."); }
        });

        btnEliminar.addActionListener(e -> {
            try {
                dao.eliminarRestaurante(Integer.parseInt(txtId.getText()));
                actualizarTablaRestaurantes();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "ID no encontrado."); }
        });

        btnRefresh.addActionListener(e -> actualizarTablaRestaurantes());

        panelPrincipal.add(panelForm);
        panelPrincipal.add(panelTabla);
        return panelPrincipal;
    }

private JPanel crearPanelVinculos() {
    // 4 filas: gustos, categorías, cualidades y visitas. Así el algoritmo puede usar
    // contenido (:Le_Gusta / :Pertenece_A / :Ofrece) y visitas (:Visito).
    JPanel panel = new JPanel(new GridLayout(4, 1, 0, 15));
    panel.setBackground(COLOR_FONDO_CLARO);
    panel.setBorder(new EmptyBorder(15, 15, 15, 15));

    JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
    p1.setBackground(COLOR_PANEL_INTERNO);
    p1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true), "Preferencias: ¿Qué tipo de cocina prefiere el comensal? (:Le_Gusta)", 0, 0, FUENTE_TITULO, COLOR_NARANJA_RESTAURANTE));
    
    JTextField idU = crearTextFieldCulinario("ID Usuario"); 
    idU.setPreferredSize(new Dimension(120, 45));
    JTextField gusto = crearTextFieldCulinario("Especialidad (Ej: italiana, asados)"); 
    gusto.setPreferredSize(new Dimension(250, 45));
    
    JPanel panelBotonesGusto = new JPanel(new GridLayout(1, 2, 12, 0));
    panelBotonesGusto.setOpaque(false);
    panelBotonesGusto.setPreferredSize(new Dimension(320, 45));
    
    JButton btnG = new JButton("Asignar Gusto 🍳");
    JButton btnRemoverGusto = new JButton("Remover Gusto ✕");
    estilarBoton(btnG, COLOR_NARANJA_RESTAURANTE);
    estilarBoton(btnRemoverGusto, COLOR_ROJO_TOMATE);
    
    panelBotonesGusto.add(btnG); 
    panelBotonesGusto.add(btnRemoverGusto);
    p1.add(idU); p1.add(gusto); p1.add(panelBotonesGusto);

    btnG.addActionListener(e -> {
        try {
            dao.agregarRelacionGusta(Integer.parseInt(idU.getText()), gusto.getText());
            JOptionPane.showMessageDialog(this, "Preferencia enlazada de forma exitosa.");
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Verifica el ID del usuario y el gusto."); 
        }
    });

    btnRemoverGusto.addActionListener(e -> {
        try {
            dao.eliminarRelacionGusta(Integer.parseInt(idU.getText()), gusto.getText());
            JOptionPane.showMessageDialog(this, "Preferencia removida de la red.");
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Ingresa datos válidos para remover la preferencia."); 
        }
    });

    JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
    p2.setBackground(COLOR_PANEL_INTERNO);
    p2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true), "Clasificación: Especialidad de la Casa (:Pertenece_A)", 0, 0, FUENTE_TITULO, COLOR_NARANJA_RESTAURANTE));
    
    JTextField idR = crearTextFieldCulinario("ID Restaurante"); 
    idR.setPreferredSize(new Dimension(120, 45));
    JTextField cat = crearTextFieldCulinario("Categoría Base"); 
    cat.setPreferredSize(new Dimension(250, 45));
    
    JPanel panelBotonesCat = new JPanel(new GridLayout(1, 2, 12, 0));
    panelBotonesCat.setOpaque(false);
    panelBotonesCat.setPreferredSize(new Dimension(340, 45));
    
    JButton btnC = new JButton("Asignar Categoría 🍔");
    JButton btnRemoverCat = new JButton("Remover Categoría ✕");
    estilarBoton(btnC, COLOR_NARANJA_RESTAURANTE);
    estilarBoton(btnRemoverCat, COLOR_ROJO_TOMATE);
    
    panelBotonesCat.add(btnC); 
    panelBotonesCat.add(btnRemoverCat);
    p2.add(idR); p2.add(cat); p2.add(panelBotonesCat);

    btnC.addActionListener(e -> {
        try {
            dao.agregarCategoriaRestaurante(Integer.parseInt(idR.getText()), cat.getText());
            JOptionPane.showMessageDialog(this, "Categoría vinculada al restaurante.");
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Verifica el ID del restaurante y la categoría."); 
        }
    });

    btnRemoverCat.addActionListener(e -> {
        try {
            dao.eliminarCategoriaRestaurante(Integer.parseInt(idR.getText()), cat.getText());
            JOptionPane.showMessageDialog(this, "Categoría desvinculada del restaurante.");
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Ingresa datos válidos para remover la clasificación."); 
        }
    });

    JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
    p3.setBackground(COLOR_PANEL_INTERNO);
    p3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true), "Cualidades del Restaurante (:Ofrece)", 0, 0, FUENTE_TITULO, COLOR_NARANJA_RESTAURANTE));

    JTextField idRCualidad = crearTextFieldCulinario("ID Restaurante");
    idRCualidad.setPreferredSize(new Dimension(120, 45));
    JTextField cualidad = crearTextFieldCulinario("Cualidad (Ej: familiar, romántico, terraza)");
    cualidad.setPreferredSize(new Dimension(280, 45));

    JPanel panelBotonesCualidad = new JPanel(new GridLayout(1, 2, 12, 0));
    panelBotonesCualidad.setOpaque(false);
    panelBotonesCualidad.setPreferredSize(new Dimension(340, 45));

    JButton btnAgregarCualidad = new JButton("Agregar Cualidad ✨");
    JButton btnEliminarCualidad = new JButton("Eliminar Cualidad ✕");
    estilarBoton(btnAgregarCualidad, COLOR_NARANJA_RESTAURANTE);
    estilarBoton(btnEliminarCualidad, COLOR_ROJO_TOMATE);

    panelBotonesCualidad.add(btnAgregarCualidad);
    panelBotonesCualidad.add(btnEliminarCualidad);
    p3.add(idRCualidad); p3.add(cualidad); p3.add(panelBotonesCualidad);

    btnAgregarCualidad.addActionListener(e -> {
        try {
            dao.agregarCualidadRestaurante(Integer.parseInt(idRCualidad.getText()), cualidad.getText());
            JOptionPane.showMessageDialog(this, "Cualidad agregada al restaurante.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Verifica el ID del restaurante y la cualidad.");
        }
    });

    btnEliminarCualidad.addActionListener(e -> {
        try {
            dao.eliminarCualidadRestaurante(Integer.parseInt(idRCualidad.getText()), cualidad.getText());
            JOptionPane.showMessageDialog(this, "Cualidad eliminada del restaurante.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ingresa datos válidos para eliminar la cualidad.");
        }
    });

    JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
    p4.setBackground(COLOR_PANEL_INTERNO);
    p4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true), "Historial: Marcar Restaurante Visitado (:Visito)", 0, 0, FUENTE_TITULO, COLOR_NARANJA_RESTAURANTE));

    JTextField idUsuarioVisita = crearTextFieldCulinario("ID Usuario");
    idUsuarioVisita.setPreferredSize(new Dimension(120, 45));
    JTextField idRestauranteVisita = crearTextFieldCulinario("ID Restaurante");
    idRestauranteVisita.setPreferredSize(new Dimension(140, 45));
    JTextField calificacionVisita = crearTextFieldCulinario("Calificación 1-5");
    calificacionVisita.setPreferredSize(new Dimension(140, 45));

    JButton btnRegistrarVisita = new JButton("Marcar Visitado ✅");
    estilarBoton(btnRegistrarVisita, COLOR_NARANJA_RESTAURANTE);
    btnRegistrarVisita.setPreferredSize(new Dimension(210, 45));

    p4.add(idUsuarioVisita); p4.add(idRestauranteVisita); p4.add(calificacionVisita); p4.add(btnRegistrarVisita);

    btnRegistrarVisita.addActionListener(e -> {
        try {
            int calificacion = Integer.parseInt(calificacionVisita.getText());
            if (calificacion < 1 || calificacion > 5) {
                JOptionPane.showMessageDialog(this, "La calificación debe estar entre 1 y 5.");
                return;
            }
            dao.registrarVisita(
                Integer.parseInt(idUsuarioVisita.getText()),
                Integer.parseInt(idRestauranteVisita.getText()),
                calificacion
            );
            JOptionPane.showMessageDialog(this, "Visita registrada. Ese restaurante ya no aparecerá como recomendación para ese usuario.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Verifica ID de usuario, ID de restaurante y calificación.");
        }
    });

    panel.add(p1);
    panel.add(p2);
    panel.add(p3);
    panel.add(p4);
    return panel;
}

    private JPanel crearPanelRecomendaciones() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(COLOR_FONDO_CLARO);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        top.setBackground(COLOR_PANEL_INTERNO);
        top.setBorder(BorderFactory.createLineBorder(COLOR_NARANJA_RESTAURANTE, 1, true));
        
        JTextField txtIdBusqueda = crearTextFieldCulinario("ID del Comensal Objetivo");
        txtIdBusqueda.setPreferredSize(new Dimension(200, 45));
        
        JButton btnVer = new JButton("Generar Menú Recomendado ✨");
        estilarBoton(btnVer, COLOR_NARANJA_RESTAURANTE);
        top.add(txtIdBusqueda); top.add(btnVer);

        String[] cols = {"Sugerencia Gastronómica", "Reputación", "Escala de Costo", "Aceptación Social", "Categorías/Cualidades", "Score"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable tabla = new JTable(model);
        configurarEstiloTabla(tabla);

        btnVer.addActionListener(e -> {
            try {
                model.setRowCount(0);
                List<Map<String, Object>> sugs = recomendacionService.generarRecomendacionHibrida(Integer.parseInt(txtIdBusqueda.getText()));
                if (sugs.isEmpty()) JOptionPane.showMessageDialog(this, "No hay sugerencias culinarias que coincidan con sus gustos actuales.");
                for (Map<String, Object> s : sugs) {
                    List<?> categorias = (List<?>) s.get("categorias");
                    List<?> etiquetas = (List<?>) s.get("etiquetas");
                    String detalle = "Cat: " + (categorias == null || categorias.isEmpty() ? "sin coincidencia directa" : String.join(", ", categorias.stream().map(Object::toString).toList()))
                            + " | Cual: " + (etiquetas == null || etiquetas.isEmpty() ? "no registradas" : String.join(", ", etiquetas.stream().map(Object::toString).toList()));
                    model.addRow(new Object[]{
                        "  " + s.get("restaurante"),
                        s.get("rating") + " ⭐",
                        s.get("precio"),
                        s.get("coincidenciaSocial") + " usuarios similares",
                        detalle,
                        String.format("%.2f", (Double) s.get("score"))
                    });
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "ID no válido."); }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // --- RELLENO DE DATOS DESDE DAO ---
    private void actualizarTablaUsuarios() {
        modeloUsuarios.setRowCount(0);
        try {
            List<Map<String, Object>> res = dao.obtenerTodosLosUsuarios();
            for (Map<String, Object> u : res) {
                modeloUsuarios.addRow(new Object[]{ "  " + u.get("id"), u.get("nombre"), u.get("ubi"), "Q " + u.get("presu") });
            }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
    }

    private void actualizarTablaRestaurantes() {
        modeloRestaurantes.setRowCount(0);
        try {
            List<Map<String, Object>> res = dao.obtenerTodosLosRestaurantes();
            for (Map<String, Object> r : res) {
                modeloRestaurantes.addRow(new Object[]{ "  " + r.get("id"), r.get("nombre"), r.get("dir"), r.get("rat") + " ⭐", r.get("precio") });
            }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
    }

    // --- MÉTODOS DE ESTILO PERSONALIZADO ---
    private JTextField crearTextFieldCulinario(String tituloBorde) {
        JTextField t = new JTextField();
        t.setFont(FUENTE_UI);
        t.setForeground(COLOR_TEXTO_OSCURO);
        t.setBackground(Color.WHITE);
        t.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1), tituloBorde, 0, 0, new Font("Segoe UI", Font.BOLD, 11), COLOR_NARANJA_RESTAURANTE));
        return t;
    }

    private void estilarBoton(JButton boton, Color fondo) {
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFont(FUENTE_TITULO);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void configurarEstiloTabla(JTable tabla) {
        tabla.setRowHeight(35);
        tabla.setFont(FUENTE_UI);
        tabla.setForeground(COLOR_TEXTO_OSCURO);
        tabla.setSelectionBackground(new Color(253, 235, 208)); // Fondo naranja clarito al seleccionar fila
        tabla.setSelectionForeground(COLOR_TEXTO_OSCURO);
        tabla.getTableHeader().setFont(FUENTE_TITULO);
        tabla.getTableHeader().setBackground(COLOR_NARANJA_RESTAURANTE);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setShowGrid(false);
    }

    public static void main(String[] args) {
        // Ejecutamos directamente el constructor que maneja el flujo de la GUI
        SwingUtilities.invokeLater(() -> new GestionApp());
    }
}