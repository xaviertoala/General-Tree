package ec.edu.espoch.controller;

import ec.edu.espoch.modelo.ArbolGeneral;
import ec.edu.espoch.modelo.Nodo;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class TreeController {

    @FXML
    private TextField txtValor;

    @FXML
    private Canvas canvas;

    @FXML
    private StackPane container;

    @FXML
    private ScrollPane scrollPane;

    private ArbolGeneral<Integer> arbol;
    private final double radius = 22;
    private final double levelHeight = 100;

    private Map<Nodo<Integer>, Integer> leafCountMap = new HashMap<>();

    // Variables para el zoom
    private double zoomFactor = 1.0;
    private static final double MIN_ZOOM = 0.3;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_DELTA = 0.1;

    // Tamaño mínimo del viewport
    private static final double MIN_WIDTH = 800;
    private static final double MIN_HEIGHT = 600;

    @FXML
    public void initialize() {
        arbol = new ArbolGeneral<>();

        // Configurar evento de zoom con Ctrl + Scroll
        canvas.setOnScroll(this::handleZoom);

        // Listener para ajustar el canvas cuando cambia el tamaño del ScrollPane
        scrollPane.widthProperty().addListener((obs, oldVal, newVal) -> ajustarCanvas());
        scrollPane.heightProperty().addListener((obs, oldVal, newVal) -> ajustarCanvas());

        ajustarCanvas();
        limpiarCanvas();
    }

    private void ajustarCanvas() {
        if (arbol.getRaiz() == null) {
            // Si no hay árbol, usar el tamaño del viewport
            double viewportWidth = Math.max(scrollPane.getWidth(), MIN_WIDTH);
            double viewportHeight = Math.max(scrollPane.getHeight(), MIN_HEIGHT);

            canvas.setWidth(viewportWidth);
            canvas.setHeight(viewportHeight);
            container.setMinWidth(viewportWidth);
            container.setMinHeight(viewportHeight);
        } else {
            // Calcular el tamaño necesario basado en el árbol
            calcularYAjustarTamano();
        }
    }

    private void calcularYAjustarTamano() {
        if (arbol.getRaiz() == null)
            return;

        // Calcular profundidad del árbol
        int profundidad = calcularProfundidad(arbol.getRaiz());

        // Calcular número de hojas (ancho necesario)
        leafCountMap.clear();
        int numHojas = contarHojas(arbol.getRaiz());

        // Calcular dimensiones necesarias
        double anchoNecesario = Math.max(numHojas * 80, MIN_WIDTH); // 80px por hoja
        double altoNecesario = Math.max(profundidad * levelHeight + 200, MIN_HEIGHT); // altura por nivel + margen

        // Aplicar zoom
        double anchoFinal = anchoNecesario * zoomFactor;
        double altoFinal = altoNecesario * zoomFactor;

        // Asegurar que sea al menos del tamaño del viewport
        double viewportWidth = scrollPane.getWidth();
        double viewportHeight = scrollPane.getHeight();

        anchoFinal = Math.max(anchoFinal, viewportWidth);
        altoFinal = Math.max(altoFinal, viewportHeight);

        canvas.setWidth(anchoFinal);
        canvas.setHeight(altoFinal);
        container.setMinWidth(anchoFinal);
        container.setMinHeight(altoFinal);
    }

    private int calcularProfundidad(Nodo<Integer> nodo) {
        if (nodo == null)
            return 0;
        if (nodo.hijos.isEmpty())
            return 1;

        int maxProfundidad = 0;
        for (Nodo<Integer> hijo : nodo.hijos) {
            maxProfundidad = Math.max(maxProfundidad, calcularProfundidad(hijo));
        }
        return maxProfundidad + 1;
    }

    private void handleZoom(ScrollEvent event) {
        if (event.isControlDown()) {
            event.consume();

            double delta = event.getDeltaY() > 0 ? ZOOM_DELTA : -ZOOM_DELTA;
            double newZoom = zoomFactor + delta;

            if (newZoom >= MIN_ZOOM && newZoom <= MAX_ZOOM) {
                zoomFactor = newZoom;
                calcularYAjustarTamano();
                dibujarArbol();
            }
        }
    }

    @FXML
    private void onAgregar() {
        try {
            int valor = Integer.parseInt(txtValor.getText());
            arbol.insertar(valor);
            txtValor.clear();
            calcularYAjustarTamano();
            dibujarArbol();
        } catch (NumberFormatException e) {
            mostrarError("Formato inválido", "Por favor ingrese un número entero.");
        }
    }

    @FXML
    private void onEliminar() {
        try {
            int valor = Integer.parseInt(txtValor.getText());
            arbol.eliminar(valor);
            txtValor.clear();
            calcularYAjustarTamano();
            dibujarArbol();
        } catch (NumberFormatException e) {
            mostrarError("Formato inválido", "Por favor ingrese un número entero.");
        }
    }

    @FXML
    private void onLimpiar() {
        arbol = new ArbolGeneral<>();
        zoomFactor = 1.0;
        ajustarCanvas();
        dibujarArbol();
    }

    private void dibujarArbol() {
        limpiarCanvas();
        if (arbol.getRaiz() != null) {
            leafCountMap.clear();
            contarHojas(arbol.getRaiz());

            double width = canvas.getWidth();
            double height = canvas.getHeight();

            if (width > 0 && height > 0) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                dibujarNodo(gc, arbol.getRaiz(), width / 2, 60 * zoomFactor, width * 0.8);
            }
        }
    }

    private int contarHojas(Nodo<Integer> nodo) {
        if (nodo == null)
            return 0;
        if (nodo.hijos.isEmpty()) {
            leafCountMap.put(nodo, 1);
            return 1;
        }
        int sum = 0;
        for (Nodo<Integer> hijo : nodo.hijos) {
            sum += contarHojas(hijo);
        }
        leafCountMap.put(nodo, sum);
        return sum;
    }

    private void dibujarNodo(GraphicsContext gc, Nodo<Integer> nodo, double x, double y, double availableWidth) {
        if (nodo == null)
            return;

        int totalLeaves = leafCountMap.getOrDefault(nodo, 1);
        int numHijos = nodo.hijos.size();

        if (numHijos > 0) {
            double currentX = x - (availableWidth / 2.0);

            for (Nodo<Integer> hijo : nodo.hijos) {
                int childLeaves = leafCountMap.getOrDefault(hijo, 1);
                double childWidth = (availableWidth * childLeaves) / totalLeaves;

                double childX = currentX + (childWidth / 2.0);
                double childY = y + levelHeight * zoomFactor;

                gc.setStroke(Color.rgb(44, 62, 80));
                gc.setLineWidth(2.5 * zoomFactor);
                gc.strokeLine(x, y, childX, childY);

                dibujarNodo(gc, hijo, childX, childY, childWidth);

                currentX += childWidth;
            }
        }

        dibujarCirculoNodo(gc, x, y, String.valueOf(nodo.valor));
    }

    private void dibujarCirculoNodo(GraphicsContext gc, double x, double y, String text) {
        double scaledRadius = radius * zoomFactor;

        Stop[] stops = new Stop[] { new Stop(0, Color.web("#3498db")), new Stop(1, Color.web("#2980b9")) };
        LinearGradient lg = new LinearGradient(x - scaledRadius, y - scaledRadius, x + scaledRadius, y + scaledRadius,
                false, CycleMethod.NO_CYCLE, stops);

        gc.setFill(lg);
        gc.fillOval(x - scaledRadius, y - scaledRadius, scaledRadius * 2, scaledRadius * 2);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2 * zoomFactor);
        gc.strokeOval(x - scaledRadius, y - scaledRadius, scaledRadius * 2, scaledRadius * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System Bold", 14 * zoomFactor));

        Text t = new Text(text);
        t.setFont(gc.getFont());
        double tw = t.getLayoutBounds().getWidth();
        double th = t.getLayoutBounds().getHeight();

        gc.fillText(text, x - tw / 2, y + th / 4);
    }

    private void limpiarCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        if (w <= 0 || h <= 0)
            return;

        gc.setFill(Color.web("#f8f9fa"));
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.rgb(200, 200, 200, 0.15));
        gc.setLineWidth(1);
        double gridSize = 40 * zoomFactor;
        for (int i = 0; i < w; i += (int) gridSize)
            gc.strokeLine(i, 0, i, h);
        for (int i = 0; i < h; i += (int) gridSize)
            gc.strokeLine(0, i, w, i);
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
