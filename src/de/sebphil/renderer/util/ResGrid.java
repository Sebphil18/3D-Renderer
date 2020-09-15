package de.sebphil.renderer.util;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ResGrid {

	//width 	Breite eiener einzelnen Zelle
	//sizeX/Y 	Größe des Gitters in Pixel
	private SimpleDoubleProperty sizeX, sizeY, width;
	private SimpleBooleanProperty showGrid;
	
	// Anzahl der Spalten und Zeilen
	private int amountX, amountY;
	private double[] grid;

	/**
	 * Constructor für ein ResGrid.
	 * 
	 * Diese Klasse repräsentiert eine Gitterstruktur, in welcher jeder Zelle ein Wert
	 * zugeteilt werden kann und gezeichnet werden kann. Dieser Constructor sollte
	 * verwendet werden, wenn das Gitter gezeichnet werden soll.
	 * 
	 * @param sizeX Anzahl der Spalten
	 * @param sizeY Anzahl der Reihen
	 * @param gc GraphicContext, um das Gitter zu zeichnen
	 */
	public ResGrid(double sizeX, double sizeY, GraphicsContext gc) {

		this.sizeX = new SimpleDoubleProperty(sizeX);
		this.sizeY = new SimpleDoubleProperty(sizeY);
		this.width = new SimpleDoubleProperty(10);
		this.showGrid = new SimpleBooleanProperty(false);

		this.grid = generateGrid();

		setUpListener(gc);

	}

	/**
	 * Constructor für ein ResGrid.
	 * 
	 * Diese Klasse repräsentiert eine Gitterstruktur, in welcher jeder Zelle ein Wert
	 * zugeteilt werden kann und gezeichnet werden kann. Dieser Constructor sollte verwendet
	 * werden, wenn das Gitter nicht gezeichnet werden soll.
	 * 
	 * @param sizeX Anzahl der Spalten
	 * @param sizeY Anzahl der Reihen
	 */
	public ResGrid(double sizeX, double sizeY) {

		this.sizeX = new SimpleDoubleProperty(sizeX);
		this.sizeY = new SimpleDoubleProperty(sizeY);
		this.width = new SimpleDoubleProperty(10);
		this.showGrid = new SimpleBooleanProperty(false);
		this.grid = generateGrid();

	}

	/**
	 * Aktiviert Listener für das GraphicsContext
	 * @param gc GraphicsContext
	 */
	public void setUpListener(GraphicsContext gc) {

		this.sizeX.addListener(e -> {
			grid = generateGrid();
			drawGrid(gc);
		});

		this.sizeY.addListener(e -> {
			grid = generateGrid();
			drawGrid(gc);
		});

		this.width.addListener(e -> {
			grid = generateGrid();
			drawGrid(gc);
		});

		showGrid.addListener(e -> {
			drawGrid(gc);
		});

	}

	/**
	 * Zeichnet die einzelnen Linien des Gitters auf eine Zeichenfläche.
	 * 
	 * @param gc GraphicsContext
	 */
	private void drawGrid(GraphicsContext gc) {

		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

		if (showGrid.getValue()) {

			gc.setStroke(Color.BLACK);

			for (int i = 0; i < amountX; i++) {
				for (int j = 0; j < amountY; j++) {

					gc.strokeRect(i * width.getValue(), j * width.getValue(), width.getValue(), width.getValue());

				}
			}

		}

	}

	/**
	 * Generiert das Gitter.
	 * @return Array, welches das Gitter repräsentiert
	 */
	private double[] generateGrid() {

		amountX = (int) (this.sizeX.getValue() / width.getValue());
		amountY = (int) (this.sizeY.getValue() / width.getValue());

		double[] grid = new double[amountX * amountY];

		return grid;

	}

	/**
	 * Zeichnet einzelne Zelle
	 * @param x x-Koordiante der Zelle
	 * @param y y-Koordiante der Zelle
	 * @param gc GraphicsContext, auf welchem die Zelle gezeichnet werden soll
	 */
	public void fillCell(int x, int y, GraphicsContext gc) {
		
		if (showGrid.getValue()) {
			gc.fillRect(x * width.getValue() + 1, y * width.getValue() + 1, width.getValue() - 2, width.getValue() - 2);
		} else {
			gc.fillRect(x * width.getValue(), y * width.getValue(), width.getValue(), width.getValue());
		}
		
	}
	
	/**
	 * 
	 * @param x x-Koordinate
	 * @param y y-Koordinate
	 * @return gibt Wert der Zelle bei (x|y) zurück
	 */
	public double getVal(int x, int y) {
		return grid[y * amountX + x];
	}

	/**
	 * setzt Wert für Zelle bei (x|y)
	 * @param x x-Koordiante
	 * @param y y-Koordiante
	 * @param val Wert, welcher in die Zelle eingetragen werden soll
	 */
	public void setVal(int x, int y, double val) {
		grid[y * amountX + x] = val;
	}

	public SimpleDoubleProperty getSizeX() {
		return sizeX;
	}

	public void setSizeX(SimpleDoubleProperty sizeX) {
		this.sizeX = sizeX;
	}

	public SimpleDoubleProperty getSizeY() {
		return sizeY;
	}

	public void setSizeY(SimpleDoubleProperty sizeY) {
		this.sizeY = sizeY;
	}

	public SimpleDoubleProperty getWidth() {
		return width;
	}

	public void setWidth(SimpleDoubleProperty width) {
		this.width = width;
	}

	public SimpleBooleanProperty getShowGrid() {
		return showGrid;
	}

	public void setShowGrid(SimpleBooleanProperty showGrid) {
		this.showGrid = showGrid;
	}

	public int getAmountX() {
		return amountX;
	}

	public void setAmountX(int amountX) {
		this.amountX = amountX;
	}

	public int getAmountY() {
		return amountY;
	}

	public void setAmountY(int amountY) {
		this.amountY = amountY;
	}

	public double[] getGrid() {
		return grid;
	}

	public void setGrid(double[] grid) {
		this.grid = grid;
	}

}
