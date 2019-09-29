package de.sebphil.renderer.controllers;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import de.sebphil.renderer.objects.RenNoise;
import de.sebphil.renderer.util.NoiseGenerator2D;
import de.sebphil.renderer.util.RenUtilities;
import de.sebphil.renderer.util.ResGrid;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class NoiseController implements Initializable {

	@FXML
	private Pane canvasPane;

	@FXML
	private Slider scaleXSlider;

	@FXML
	private Slider scaleYSlider;

	@FXML
	private Slider resSlider;

	@FXML
	private Slider freqSlider;

	@FXML
	private TextField freqMinField;

	@FXML
	private TextField freqMaxField;

	@FXML
	private TextField freqValField;

	@FXML
	private Slider amplSlider;

	@FXML
	private TextField amplMinField;

	@FXML
	private TextField amplMaxField;

	@FXML
	private TextField amplValField;

	@FXML
	private TextField layersField;

	@FXML
	private TextField freqMultiField;

	@FXML
	private TextField amplMultiField;

	@FXML
	private TextField increaField;

	@FXML
	private CheckBox showGridField;

	@FXML
	private TextField seedField;

	@FXML
	private CheckBox colorCheck;

    @FXML
    private CheckBox dynamicCheck;
	
	private double increment;
	private long seed;
	private RenNoise noiseShape;
	private ResGrid grid;
	private NoiseGenerator2D noise;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		Canvas canvas = new Canvas(canvasPane.getPrefWidth(), canvasPane.getPrefHeight());
		GraphicsContext gc = canvas.getGraphicsContext2D();

		increment = 0.234;
		seed = new Random().nextLong();
		noise = new NoiseGenerator2D(seed);
		grid = new ResGrid(canvas.getWidth(), canvas.getHeight());

		if (noiseShape == null) {

			noiseShape = new RenNoise("noise");
			noiseShape.setGrid(grid);
			noiseShape.setNoise(noise);
			noiseShape.setIncrement(increment);
			noiseShape.setSeed(seed);
			noiseShape.getGrid().setUpListener(gc);

			MainController.mainScene.getShapes().add(noiseShape);

		} else {

			noiseShape.setOffsetY(0);
			
			grid = noiseShape.getGrid();
			noise = noiseShape.getNoise();
			seed = noiseShape.getSeed();

			double frequency = noise.getFrequency();
			double amplitude = noise.getAmplitude();

			scaleXSlider.setValue(grid.getSizeX().getValue());
			scaleYSlider.setValue(grid.getSizeY().getValue());

			freqSlider.setValue(frequency);
			freqSlider.setMax(frequency * 2);
			freqSlider.setMin(frequency / 2);

			amplSlider.setValue(amplitude);
			amplSlider.setMax(amplitude * 2);
			amplSlider.setMin(amplitude / 2);
			
			layersField.setText(Integer.toString(noise.getLayers()));
			freqMultiField.setText(Double.toString(noise.getFreqMult()));
			amplMultiField.setText(Double.toString(noise.getAmplMult()));
			increaField.setText(Double.toString(noiseShape.getIncrement()));
			seedField.setText(Long.toString(noiseShape.getSeed()));

		}

		seedField.setText(Long.toString(seed));

		scaleXSlider.setValue(scaleXSlider.getMax());
		scaleYSlider.setValue(scaleYSlider.getMin());
		resSlider.setValue(grid.getWidth().getValue());

		freqMinField.setText(Double.toString(freqSlider.getMin()));
		freqMaxField.setText(Double.toString(freqSlider.getMax()));
		freqValField.setText(Double.toString(noise.getFrequency()));

		amplMinField.setText(Double.toString(amplSlider.getMin()));
		amplMaxField.setText(Double.toString(amplSlider.getMax()));
		amplValField.setText(Double.toString(noise.getAmplitude()));

		increaField.setText(Double.toString(increment));

		grid.getShowGrid().addListener(e -> {
			showGridField.setSelected(grid.getShowGrid().getValue());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
		});

		grid.getShowGrid().setValue(false);

		showGridField.selectedProperty().addListener(e -> {
			grid.getShowGrid().setValue(showGridField.isSelected());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
		});

		canvasPane.heightProperty().addListener(e -> {
			canvas.setHeight(canvasPane.getHeight());
			grid.getSizeY().setValue(canvas.getHeight());
			scaleYSlider.setMax(canvas.getHeight());
			scaleYSlider.setValue(scaleYSlider.getMax() - canvas.getHeight());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
		});

		canvasPane.widthProperty().addListener(e -> {
			canvas.setWidth(canvasPane.getWidth());
			grid.getSizeX().setValue(canvas.getWidth());
			scaleXSlider.setMax(canvas.getWidth());
			scaleXSlider.setValue(canvas.getWidth());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
		});

		resSlider.valueProperty().addListener(e -> {
			grid.getWidth().setValue(resSlider.getValue());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
		});

		scaleXSlider.valueProperty().addListener(e -> {
			grid.getSizeX().setValue(scaleXSlider.getValue());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
		});

		scaleYSlider.valueProperty().addListener(e -> {
			grid.getSizeY().setValue(scaleYSlider.getMax() - scaleYSlider.getValue());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
		});

		freqSlider.valueProperty().addListener(e -> {
			noise.setFrequency(freqSlider.getValue());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
			freqValField.setText(Double.toString(freqSlider.getValue()));
		});

		freqSlider.maxProperty().addListener(e -> {
			freqMaxField.setText(Double.toString(freqSlider.getMax()));
		});

		freqSlider.minProperty().addListener(e -> {
			freqMinField.setText(Double.toString(freqSlider.getMin()));
		});

		amplSlider.valueProperty().addListener(e -> {
			noise.setAmplitude(amplSlider.getValue());
			fillGrid(noise, grid, gc, increment);
			generatePolyMesh(grid);
			amplValField.setText(Double.toString(amplSlider.getValue()));
		});

		amplSlider.maxProperty().addListener(e -> {
			amplMaxField.setText(Double.toString(amplSlider.getMax()));
		});

		amplSlider.minProperty().addListener(e -> {
			amplMinField.setText(Double.toString(amplSlider.getMin()));
		});

		freqMaxField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(freqMaxField.getText(), true, true))
				freqSlider.setMax(Double.valueOf(freqMaxField.getText()));
			else
				freqMaxField.setText(Double.toString(freqSlider.getMax()));
		});

		freqMinField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(freqMinField.getText(), true, true)) {
				double min = Double.valueOf(freqMinField.getText());
				if (min < freqSlider.getMax()) {
					freqSlider.setMin(min);
				}
			} else
				freqMinField.setText(Double.toString(freqSlider.getMin()));
		});

		amplMaxField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(amplMaxField.getText(), true, true))
				amplSlider.setMax(Double.valueOf(amplMaxField.getText()));
			else
				amplMaxField.setText(Double.toString(amplSlider.getValue()));
		});

		amplMinField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(amplMinField.getText(), true, true)) {
				double min = Double.valueOf(amplMinField.getText());
				if (min < amplSlider.getMax()) {
					amplSlider.setMin(min);
				}
			} else
				amplMinField.setText(Double.toString(amplSlider.getMin()));
		});

		layersField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(layersField.getText(), false, false)) {
				noise.setLayers(Integer.valueOf(layersField.getText()));
				fillGrid(noise, grid, gc, increment);
				generatePolyMesh(grid);
			}
		});

		freqMultiField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(freqMultiField.getText(), true, true)) {
				noise.setFreqMult(Double.valueOf(freqMultiField.getText()));
				fillGrid(noise, grid, gc, increment);
				generatePolyMesh(grid);
			}
		});

		amplMultiField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(amplMultiField.getText(), true, true)) {
				noise.setAmplMult(Double.valueOf(amplMultiField.getText()));
				fillGrid(noise, grid, gc, increment);
				generatePolyMesh(grid);
			}
		});

		increaField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(increaField.getText(), true, true)) {
				increment = Double.valueOf(increaField.getText());
				fillGrid(noise, grid, gc, increment);
				generatePolyMesh(grid);
			}
		});

		seedField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(seedField.getText(), false, true)) {
				noise.setSeed(Long.valueOf(seedField.getText()));
				fillGrid(noise, grid, gc, increment);
				generatePolyMesh(grid);
			}
		});

		colorCheck.selectedProperty().addListener(e -> {
			if(!colorCheck.isSelected() && dynamicCheck.isSelected()) {
				colorCheck.setSelected(true);
			}else {
				fillGrid(noise, grid, gc, increment);
				generatePolyMesh(grid);
			}
		});

		freqValField.textProperty().addListener(e -> {
			if(RenUtilities.isNumeric(freqValField.getText(), true, true)) {
				freqSlider.setValue(Double.valueOf(freqValField.getText()));
			}
		});
		
		amplValField.textProperty().addListener(e -> {
			if(RenUtilities.isNumeric(amplValField.getText(), true, true)) {
				amplSlider.setValue(Double.valueOf(amplValField.getText()));
			}
		});
		
		dynamicCheck.selectedProperty().addListener(e-> {
			noiseShape.setDynamic(dynamicCheck.isSelected());
			if(!colorCheck.isSelected() && dynamicCheck.isSelected()) {
				colorCheck.setSelected(true);
			}
		});
		
		dynamicCheck.setSelected(noiseShape.isDynamic());
		
		fillGrid(noise, grid, gc, increment);

		canvasPane.getChildren().add(canvas);

	}

	private void generatePolyMesh(ResGrid grid) {

		noiseShape.generatePolyMesh(colorCheck.isSelected());

		MainController.renderMain();

	}

	private void fillGrid(NoiseGenerator2D noise, ResGrid grid, GraphicsContext gc, double increment) {

		gc.clearRect(0, 0, canvasPane.getWidth(), canvasPane.getHeight());

		for (int x = 0; x < grid.getAmountX(); x++) {
			for (int y = 0; y < grid.getAmountY(); y++) {

				double sum = noise.realNoise(x * increment, y * increment);

				grid.setVal(x, y, sum);

				if (colorCheck.isSelected()) {
					gc.setFill(noise.getNoiseRGB(sum));
				} else {
					gc.setFill(Color.gray(noise.getNoiseGray(sum)));
				}

				grid.fillCell(x, grid.getAmountY() - y, gc);

			}
		}

	}

	public RenNoise getNoiseShape() {
		return noiseShape;
	}

	public void setNoiseShape(RenNoise noiseShape) {
		this.noiseShape = noiseShape;
	}

}
