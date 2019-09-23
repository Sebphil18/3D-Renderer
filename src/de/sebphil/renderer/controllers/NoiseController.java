package de.sebphil.renderer.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import de.sebphil.renderer.objects.RenShape;
import de.sebphil.renderer.objects.RenTriangle;
import de.sebphil.renderer.util.NoiseGenerator2D;
import de.sebphil.renderer.util.RenUtilities;
import de.sebphil.renderer.util.ResGrid;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
	private Button creNoiseButton;

	@FXML
	private TextField increaField;

	@FXML
	private CheckBox showGridField;

	@FXML
	private TextField seedField;

	double increasement = 0.234;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		Canvas canvas = new Canvas(canvasPane.getPrefWidth(), canvasPane.getPrefHeight());
		GraphicsContext gc = canvas.getGraphicsContext2D();

		ResGrid grid = new ResGrid(canvas.getWidth(), canvas.getHeight(), 100, 100, gc);

		long seed = new Random().nextLong();

		NoiseGenerator2D noise = new NoiseGenerator2D(seed);

		List<RenTriangle> tris = new ArrayList<RenTriangle>();

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

		increaField.setText(Double.toString(increasement));

		grid.getShowGrid().addListener(e -> {
			showGridField.setSelected(grid.getShowGrid().getValue());
			fillGrid(noise, grid, gc, increasement);
		});

		grid.getShowGrid().setValue(false);

		showGridField.selectedProperty().addListener(e -> {
			grid.getShowGrid().setValue(showGridField.isSelected());
			fillGrid(noise, grid, gc, increasement);
		});

		canvasPane.heightProperty().addListener(e -> {
			canvas.setHeight(canvasPane.getHeight());
			grid.getSizeY().setValue(canvas.getHeight());
			scaleYSlider.setMax(canvas.getHeight());
			scaleYSlider.setValue(scaleYSlider.getMax() - canvas.getHeight());
			fillGrid(noise, grid, gc, increasement);
		});

		canvasPane.widthProperty().addListener(e -> {
			canvas.setWidth(canvasPane.getWidth());
			grid.getSizeX().setValue(canvas.getWidth());
			scaleXSlider.setMax(canvas.getWidth());
			scaleXSlider.setValue(canvas.getWidth());
			fillGrid(noise, grid, gc, increasement);
		});

		resSlider.setOnMouseReleased(e -> {
			grid.getWidth().setValue(resSlider.getValue());
			fillGrid(noise, grid, gc, increasement);
		});

		scaleXSlider.setOnMouseReleased(e -> {
			grid.getSizeX().setValue(scaleXSlider.getValue());
			fillGrid(noise, grid, gc, increasement);
		});

		scaleYSlider.setOnMouseReleased(e -> {
			grid.getSizeY().setValue(scaleYSlider.getMax() - scaleYSlider.getValue());
			fillGrid(noise, grid, gc, increasement);
		});

		freqSlider.setOnMouseReleased(e -> {
			noise.setFrequency(freqSlider.getValue());
			fillGrid(noise, grid, gc, increasement);
			freqValField.setText(Double.toString(freqSlider.getValue()));
		});

		freqSlider.maxProperty().addListener(e -> {
			freqMaxField.setText(Double.toString(freqSlider.getMax()));
		});

		freqSlider.minProperty().addListener(e -> {
			freqMinField.setText(Double.toString(freqSlider.getMin()));
		});

		amplSlider.setOnMouseReleased(e -> {
			noise.setAmplitude(amplSlider.getValue());
			fillGrid(noise, grid, gc, increasement);
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
				fillGrid(noise, grid, gc, increasement);
			}
		});

		freqMultiField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(freqMultiField.getText(), true, true)) {
				noise.setFreqMult(Double.valueOf(freqMultiField.getText()));
				fillGrid(noise, grid, gc, increasement);
			}
		});

		amplMultiField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(amplMultiField.getText(), true, true)) {
				noise.setAmplMult(Double.valueOf(amplMultiField.getText()));
				fillGrid(noise, grid, gc, increasement);
			}
		});

		increaField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(increaField.getText(), true, true)) {
				increasement = Double.valueOf(increaField.getText());
				fillGrid(noise, grid, gc, increasement);
			}
		});

		seedField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(seedField.getText(), false, true)) {
				noise.setSeed(Long.valueOf(seedField.getText()));
				fillGrid(noise, grid, gc, increasement);
			}
		});

		creNoiseButton.setOnAction(e -> {

			tris.clear();

			double res = grid.getAmountX() / grid.getAmountY();

			for (int x = 0; x < grid.getAmountX() - 1; x++) {

				for (int y = 0; y < grid.getAmountY() - 1; y++) {

					Point3D v1 = new Point3D((x - grid.getAmountX() / 2) / res, grid.getVal(x, y) * 5, y / res);
					Point3D v2 = new Point3D(((x + 1) - grid.getAmountX() / 2) / res, grid.getVal(x + 1, y) * 5,
							y / res);
					Point3D v3 = new Point3D(((x + 1) - grid.getAmountX() / 2) / res, grid.getVal(x + 1, y + 1) * 5,
							(y + 1) / res);
					Point3D v4 = new Point3D((x - grid.getAmountX() / 2) / res, grid.getVal(x, y + 1) * 5,
							(y + 1) / res);

					tris.add(new RenTriangle(v4, v2, v1));
					tris.add(new RenTriangle(v3, v2, v4));

				}

			}

			RenShape noiseShape = new RenShape("noise");
			noiseShape.setSize(new Point3D(1, 1, 1));
			noiseShape.getPolys().addAll(tris);
			MainController.mainScene.getShapes().add(noiseShape);

			Stage stage = (Stage) canvasPane.getScene().getWindow();
			stage.close();

		});

		fillGrid(noise, grid, gc, increasement);

		canvasPane.getChildren().add(canvas);

	}

	private void fillGrid(NoiseGenerator2D noise, ResGrid grid, GraphicsContext gc, double increasement) {

		for (int x = 0; x < grid.getAmountX(); x++) {
			for (int y = 0; y < grid.getAmountY(); y++) {

				double sum = noise.realNoise(x * increasement, y * increasement);

				grid.setVal(x, y, sum);
				gc.setFill(Color.gray(noise.getNoiseGray(sum)));
				grid.fillCell(x, y, gc);

			}
		}

	}

}
