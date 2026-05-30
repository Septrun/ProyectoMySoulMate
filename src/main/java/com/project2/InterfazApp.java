package com.project2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import com.project2.servicio.RecomendacionService;

public class InterfazApp extends JFrame {
    private JComboBox<String> usuarioComboBox;
    private JTable tablaRecomendaciones;
    private DefaultTableModel modeloTabla;
    private RecomendacionService recomendacionService;
    private Driver driver;

    public InterfazApp() {
        // 1. Inicializar la conexión a Neo4j (Usando tus mismas credenciales)
        String uri = System.getenv().getOrDefault("NEO4J_URI", "neo4j+s://e99223bc.databases.neo4j.io");
        String user = System.getenv().getOrDefault("NEO4J_USERNAME", "e99223bc");
        String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "NNRon_9Zwh_pmzmlYWgJUiTBsuXlixh4Pkai0HTkQRc");
        
        try {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
            recomendacionService = new RecomendacionService(driver);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al conectar a Neo4j: " + e.getMessage(), "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        }

        // 2. Configuración de la Ventana Principal
        setTitle("Sistema de Recomendación Híbrido - Soulmate");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 3. Panel Superior: Selección de Usuario
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Configuración de Consulta"));
        
        JLabel lblUsuario = new JLabel("Selecciona un Estudiante / Usuario:");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Mapeamos los IDs a los nombres que insertaste en el Main
        usuarioComboBox = new JComboBox<>(new String[]{"1 - Diego", "2 - David", "3 - Wilfred"});
        
        JButton btnCalcular = new JButton("Obtener Recomendaciones ✨");
        btnCalcular.setBackground(new Color(70, 130, 180));
        btnCalcular.setForeground(Color.WHITE);
        btnCalcular.setFocusPainted(false);

        panelSuperior.add(lblUsuario);
        panelSuperior.add(usuarioComboBox);
        panelSuperior.add(btnCalcular);
        add(panelSuperior, BorderLayout.NORTH);

        // 4. Panel Central: Tabla de Resultados
        String[] columnas = {"Restaurante", "Rating (Neo4j)", "Precio", "Fuerza Social", "Categorías", "Ambiente / Cualidades", "Score"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaRecomendaciones = new JTable(modeloTabla);
        tablaRecomendaciones.setRowHeight(25);
        tablaRecomendaciones.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(tablaRecomendaciones);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resultados Sugeridos"));
        add(scrollPane, BorderLayout.CENTER);

        // 5. Lógica del Botón
        btnCalcular.addActionListener(e -> cargarRecomendaciones());

        // Asegurar el cierre del driver al cerrar la ventana
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (driver != null) driver.close();
            }
        });
    }

    private void cargarRecomendaciones() {
        // Limpiar registros anteriores de la tabla
        modeloTabla.setRowCount(0);

        // Obtener el ID del usuario seleccionado basándonos en el combo
        int index = usuarioComboBox.getSelectedIndex();
        int idUsuario = index + 1; // 0->1 (Diego), 1->2 (David), 2->3 (Wilfred)

        try {
            // Llamamos a tu servicio existente sin modificarle absolutamente nada
            List<Map<String, Object>> sugerencias = recomendacionService.generarRecomendacionHibrida(idUsuario);

            if (sugerencias.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay recomendaciones disponibles para los filtros de este usuario.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Poblamos la tabla con el mapa que retorna tu query de Cypher
            for (Map<String, Object> sug : sugerencias) {
                List<?> etiquetas = (List<?>) sug.get("etiquetas");
                List<?> categorias = (List<?>) sug.get("categorias");
                String ambientes = etiquetas.isEmpty() ? "No especificado" : String.join(", ", etiquetas.stream().map(Object::toString).toList());
                String cats = categorias.isEmpty() ? "Sin coincidencia directa" : String.join(", ", categorias.stream().map(Object::toString).toList());

                Object[] fila = {
                    sug.get("restaurante"),
                    sug.get("rating") + " ⭐",
                    sug.get("precio"),
                    sug.get("coincidenciaSocial") + " coincidencia(s)",
                    cats,
                    ambientes,
                    String.format("%.2f", (Double) sug.get("score"))
                };
                modeloTabla.addRow(fila);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al procesar el algoritmo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Ejecutamos la interfaz en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            new InterfazApp().setVisible(true);
        });
    }
}