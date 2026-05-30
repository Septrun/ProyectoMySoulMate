package com.project2;

import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        // Inicializa la interfaz gráfica con el Splash Screen y los colores culinarios
        SwingUtilities.invokeLater(() -> new GestionApp());
    }
}