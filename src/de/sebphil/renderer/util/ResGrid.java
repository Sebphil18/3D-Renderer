package de.sebphil.renderer.util;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ResGrid {

	private SimpleDoubleProperty sizeX, sizeY, width;
	private SimpleBooleanProperty showGrid;
	private int amountX, amountY;
	private double[] grid;

	public ResGrid(double sizeX, double sizeY, GraphicsContext gc) {

		this.sizeX = new SimpleDoubleProperty(sizeX);
		this.sizeY = new SimpleDoubleProperty(sizeY);
		this.width = new SimpleDoubleProperty(10);
		this.showGrid = new SimpleBooleanProperty(false);

		this.grid = generateGrid();

		setUpListener(gc);

	}

	public ResGrid(double sizeX, double sizeY) {

		this.sizeX = new SimpleDoubleProperty(sizeX);
		this.sizeY = new SimpleDoubleProperty(sizeY);
		this.width = new SimpleDoubleProperty(10);
		this.showGrid = new SimpleBooleanProperty(false);
		this.grid = generateGrid();

	}

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

	private double[] generateGrid() {

		amountX = (int) (this.sizeX.getValue() / width.getValue());
		amountY = (int) (this.sizeY.getValue() / width.getValue());

		double[] grid = new double[amountX * amountY];

		return grid;

	}

	public void fillCell(int x, int y, GraphicsContext gc) {
		if (showGrid.getValue()) {
			gc.fillRect(x * width.getValue() + 1, y * width.getValue() + 1, width.getValue() - 2, width.getValue() - 2);
		} else {
			gc.fillRect(x * width.getValue(), y * width.getValue(), width.getValue(), width.getValue());
		}
	}

	public double getVal(int x, int y) {
		return grid[y * amountX + x];
	}

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
