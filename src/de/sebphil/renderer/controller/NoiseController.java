package de.sebphil.renderer.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import de.sebphil.renderer.objects.RenNoise;
import de.sebphil.renderer.util.NoiseGenerator2D;
import de.sebphil.renderer.util.RenUtilities;
import de.sebphil.renderer.util.ResGrid;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Diese Klasse wird für die Steuerung der Benutzeroberfläche verwendet.
 * Sie steuert das Fenster, welches das Editieren eines Noise-Objektes ermöglicht
 * Sie wird durch de/sebphil/renderer/fxml/NoiseWindow.fxml aufgerufen (bzw. ausgeführt).
 */
public class NoiseController implements Initializable {

	@FXML
	private AnchorPane rootPane;

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
	private CheckBox showGridField;

	@FXML
	private TextField seedField;

	@FXML
	private CheckBox colorCheck;

	@FXML
	private Slider maxHeightSlider;

	@FXML
	private TextField maxHeightMinField;

	@FXML
	private TextField maxHeightMaxField;

	@FXML
	private TextField maxHeightValField;

	@FXML
	private Slider minHeightSlider;

	@FXML
	private TextField minHeightMinField;

	@FXML
	private TextField minHeightMaxField;

	@FXML
	private TextField minHeightValField;

	@FXML
	private Slider pencilStrengthSlider;

	@FXML
	private Slider pencilRadiusSlider;

	@FXML
	private Slider scaleSlider;

	@FXML
	private TextField scaleMinField;

	@FXML
	private TextField scaleMaxField;

	@FXML
	private TextField scaleValField;

	@FXML
	private CheckBox maskCheck;
	
	@FXML
    private Button editMaskButton;
	
	private double brushRadius, brushStrength;
	private long seed;
	private RenNoise noiseShape;
	private ResGrid grid;
	private NoiseGenerator2D noise, maskNoise;
	private Circle prevCircle;
	private GraphicsContext gc;

	/**
	 * Initialisiert diesen Kontroller.
	 * Dabei werden die Listener für die Steuerelemente (Textfelder, Knöpfe, etc.) initilisiert.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		Canvas canvas = new Canvas(canvasPane.getPrefWidth(), canvasPane.getPrefHeight());
		gc = canvas.getGraphicsContext2D();

		seed = new Random().nextLong();
		grid = new ResGrid(canvas.getWidth(), canvas.getHeight());

		if (noiseShape == null) {
			/*
			 * Wenn kein Noise-Objekt ausgewählt wurde, wird automatisch ein neues erzeugt,
			 * welches anschließend verwendet werden kann und mit Standardwerten belegt ist.
			 * Es wird zu der aktuell vom MainController ausgewählten Szene hinzugefügt.
			 */
			
			noiseShape = new RenNoise("noise");
			noiseShape.setGrid(grid);
			noiseShape.setSeed(seed);
			noiseShape.getGrid().setUpListener(gc);

			noise = noiseShape.getNoise();
			maskNoise = noiseShape.getMaskNoise();

			MainController.mainScene.getShapes().add(noiseShape);

		} else {

			/*
			 * Wurde ein Noise-Objekt ausgewählt, werden dessen Eigenschaften geladen
			 * und die Steuerelemente des Fensters an diese Werte entsprechend angepasst.
			 */
			
			noiseShape.setOffsetY(0);

			grid = noiseShape.getGrid();
			noise = noiseShape.getNoise();
			seed = noiseShape.getSeed();
			maskNoise = noiseShape.getMaskNoise();

			double frequency = noise.getFrequency();
			double amplitude = noise.getAmplitude();
			double maxHeight = noiseShape.getMaxHeight();
			double minHeight = noiseShape.getMinHeight();

			scaleXSlider.setValue(grid.getSizeX().getValue());
			scaleYSlider.setValue(grid.getSizeY().getValue());

			freqSlider.setValue(frequency);
			freqSlider.setMax(frequency * 2);
			freqSlider.setMin(frequency / 2);

			amplSlider.setValue(amplitude);
			amplSlider.setMax(amplitude * 2);
			amplSlider.setMin(amplitude / 2);

			maxHeightSlider.setValue(maxHeight);
			maxHeightSlider.setMax(maxHeight * 2);
			maxHeightSlider.setMin(maxHeight / 2);

			minHeightSlider.setValue(minHeight);
			minHeightSlider.setMax(minHeight * 2);
			minHeightSlider.setMin(minHeight / 2);

			layersField.setText(Integer.toString(noise.getOctaves()));
			freqMultiField.setText(Double.toString(noise.getFreqMult()));
			amplMultiField.setText(Double.toString(noise.getAmplMult()));
			seedField.setText(Long.toString(noiseShape.getSeed()));
			
			maskCheck.setSelected(noiseShape.isMask());
			
		}
		
		// Weitere Anpassung von Steuerelemente an das aktuelle Noise-Objekt
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

		maxHeightMinField.setText(Double.toString(maxHeightSlider.getMin()));
		maxHeightMaxField.setText(Double.toString(maxHeightSlider.getMax()));
		maxHeightValField.setText(Double.toString(maxHeightSlider.getValue()));

		minHeightMinField.setText(Double.toString(minHeightSlider.getMin()));
		minHeightMaxField.setText(Double.toString(minHeightSlider.getMax()));
		minHeightValField.setText(Double.toString(minHeightSlider.getValue()));

		scaleMinField.setText(Double.toString(scaleSlider.getMin()));
		scaleMaxField.setText(Double.toString(scaleSlider.getMax()));
		scaleValField.setText(Double.toString(scaleSlider.getValue()));

		// Grid listener
		grid.getShowGrid().addListener(e -> {
			showGridField.setSelected(grid.getShowGrid().getValue());
			fillGridRaw(noise, grid, gc, noiseShape);
		});

		grid.getShowGrid().setValue(false);

		showGridField.selectedProperty().addListener(e -> {
			grid.getShowGrid().setValue(showGridField.isSelected());
		});

		// Canvas listener
		canvasPane.heightProperty().addListener(e -> {
			canvas.setHeight(canvasPane.getHeight());
			grid.getSizeY().setValue(canvas.getHeight());
			scaleYSlider.setMax(canvas.getHeight());
			scaleYSlider.setValue(scaleYSlider.getMax() - canvas.getHeight());
			fillGrid();
			generatePolyMesh();
		});

		canvasPane.widthProperty().addListener(e -> {
			canvas.setWidth(canvasPane.getWidth());
			grid.getSizeX().setValue(canvas.getWidth());
			scaleXSlider.setMax(canvas.getWidth());
			scaleXSlider.setValue(canvas.getWidth());
			fillGrid();
			generatePolyMesh();
		});

		// resolution-slider listener
		resSlider.valueProperty().addListener(e -> {
			grid.getWidth().setValue(resSlider.getValue());
			fillGrid();
			generatePolyMesh();
		});

		// scale-sliders listener
		scaleXSlider.valueProperty().addListener(e -> {
			grid.getSizeX().setValue(scaleXSlider.getValue());
			fillGrid();
			generatePolyMesh();
		});

		scaleYSlider.valueProperty().addListener(e -> {
			grid.getSizeY().setValue(scaleYSlider.getMax() - scaleYSlider.getValue());
			fillGrid();
			generatePolyMesh();
		});

		// freq.-slider listener
		freqSlider.valueProperty().addListener(e -> {
			noise.setFrequency(freqSlider.getValue());
			fillGrid();
			generatePolyMesh();
			freqValField.setText(Double.toString(freqSlider.getValue()));
		});

		freqSlider.maxProperty().addListener(e -> {
			freqMaxField.setText(Double.toString(freqSlider.getMax()));
		});

		freqSlider.minProperty().addListener(e -> {
			freqMinField.setText(Double.toString(freqSlider.getMin()));
		});

		// ampl.-slider listener
		amplSlider.valueProperty().addListener(e -> {
			noise.setAmplitude(amplSlider.getValue());
			fillGrid();
			generatePolyMesh();
			amplValField.setText(Double.toString(amplSlider.getValue()));
		});

		amplSlider.maxProperty().addListener(e -> {
			amplMaxField.setText(Double.toString(amplSlider.getMax()));
		});

		amplSlider.minProperty().addListener(e -> {
			amplMinField.setText(Double.toString(amplSlider.getMin()));
		});

		// scale-slider listener
		scaleSlider.valueProperty().addListener(e -> {
			noiseShape.setScale(scaleSlider.getValue());
			scaleValField.setText(Double.toString(noiseShape.getScale()));
			generatePolyMesh();
		});

		scaleSlider.maxProperty().addListener(e -> {
			scaleMaxField.setText(Double.toString(scaleSlider.getMax()));
		});

		scaleSlider.minProperty().addListener(e -> {
			scaleMinField.setText(Double.toString(scaleSlider.getMin()));
		});

		// maxHeight-slider listener [max/min]
		maxHeightSlider.valueProperty().addListener(e -> {
			noiseShape.setMaxHeight(maxHeightSlider.getValue());
			fillGrid();
			generatePolyMesh();
			maxHeightValField.setText(Double.toString(maxHeightSlider.getValue()));
		});

		maxHeightSlider.maxProperty().addListener(e -> {
			maxHeightMaxField.setText(Double.toString(maxHeightSlider.getMax()));
		});

		maxHeightSlider.minProperty().addListener(e -> {
			maxHeightMinField.setText(Double.toString(maxHeightSlider.getMin()));
		});

		// minHeight-slider listener [max/min]
		minHeightSlider.valueProperty().addListener(e -> {
			noiseShape.setMinHeight(minHeightSlider.getValue());
			fillGrid();
			generatePolyMesh();
			minHeightValField.setText(Double.toString(minHeightSlider.getValue()));
		});

		minHeightSlider.maxProperty().addListener(e -> {
			minHeightMaxField.setText(Double.toString(minHeightSlider.getMax()));
		});

		minHeightSlider.minProperty().addListener(e -> {
			minHeightMinField.setText(Double.toString(minHeightSlider.getMin()));
		});

		// freq.-fields listener [max/min]
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

		// ampl.-fields listener [max/min]
		amplMaxField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(amplMaxField.getText(), true, true))
				amplSlider.setMax(Double.valueOf(amplMaxField.getText()));
			else
				amplMaxField.setText(Double.toString(amplSlider.getMax()));
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

		// maxHeight-fields listener [max/min]
		maxHeightMaxField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(maxHeightMaxField.getText(), true, true))
				maxHeightSlider.setMax(Double.valueOf(maxHeightMaxField.getText()));
			else
				maxHeightMaxField.setText(Double.toString(maxHeightSlider.getMax()));
		});

		maxHeightMinField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(maxHeightMinField.getText(), true, true)) {
				double min = Double.valueOf(maxHeightMinField.getText());
				if (min < maxHeightSlider.getMax())
					maxHeightSlider.setMin(min);
			} else
				maxHeightMinField.setText(Double.toString(maxHeightSlider.getMin()));
		});

		// minHeight-fields listener [max/min]
		minHeightMaxField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(minHeightMaxField.getText(), true, true))
				minHeightSlider.setMax(Double.valueOf(minHeightMaxField.getText()));
			else
				minHeightMaxField.setText(Double.toString(minHeightSlider.getMax()));
		});

		minHeightMinField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(minHeightMinField.getText(), true, true)) {
				double min = Double.valueOf(minHeightMinField.getText());
				if (min < minHeightSlider.getMax())
					minHeightSlider.setMin(min);
			} else
				minHeightMinField.setText(Double.toString(minHeightSlider.getMin()));
		});

		// scale-fields listener [max/min]

		scaleMaxField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(scaleMaxField.getText(), true, true))
				scaleSlider.setMax(Double.valueOf(scaleMaxField.getText()));
			else
				scaleMaxField.setText(Double.toString(scaleSlider.getMax()));
		});

		scaleMinField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(scaleMinField.getText(), true, true)) {
				double min = Double.valueOf(scaleMinField.getText());
				if (min < scaleSlider.getMin())
					scaleSlider.setMin(min);
			} else
				scaleMinField.setText(Double.toString(scaleSlider.getMin()));
		});

		// settings-fields listener
		layersField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(layersField.getText(), false, false)) {
				noise.setOctaves(Integer.valueOf(layersField.getText()));
				fillGrid();
				generatePolyMesh();
			}
		});
		
		// freqMultField listener
		freqMultiField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(freqMultiField.getText(), true, true)) {
				noise.setFreqMult(Double.valueOf(freqMultiField.getText()));
				fillGrid();
				generatePolyMesh();
			}
		});
		
		// amplMultField listener
		amplMultiField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(amplMultiField.getText(), true, true)) {
				noise.setAmplMult(Double.valueOf(amplMultiField.getText()));
				fillGrid();
				generatePolyMesh();
			}
		});
		
		// seedField listener
		seedField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(seedField.getText(), false, false)) {
				noise.setSeed(Long.valueOf(seedField.getText()));
				maskNoise.setSeed(Long.valueOf(seedField.getText()));
				fillGrid();
				generatePolyMesh();
			}
		});
		
		// colorCheck listener
		colorCheck.selectedProperty().addListener(e -> {
			fillGrid();
			generatePolyMesh();
		});

		// value-fields listener
		freqValField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(freqValField.getText(), true, true))
				freqSlider.setValue(Double.valueOf(freqValField.getText()));
		});

		amplValField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(amplValField.getText(), true, true))
				amplSlider.setValue(Double.valueOf(amplValField.getText()));
		});

		maxHeightValField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(maxHeightValField.getText(), true, true))
				maxHeightSlider.setValue(Double.valueOf(maxHeightValField.getText()));
		});

		minHeightValField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(minHeightValField.getText(), true, true))
				minHeightSlider.setValue(Double.valueOf(minHeightValField.getText()));
		});

		scaleValField.textProperty().addListener(e -> {
			if (RenUtilities.isNumeric(scaleValField.getText(), true, true))
				scaleSlider.setValue(Double.valueOf(scaleValField.getText()));
		});
		
		// maskCheck listener
		maskCheck.selectedProperty().addListener(e -> {
			
			// Wenn dieses Prüffeld ausgewählt wurde, soll eine Maske verwendet werden
			noiseShape.setMask(maskCheck.isSelected());
			fillGrid();
			generatePolyMesh();
			
		});
		
		// editMaskButton listener
		editMaskButton.setOnAction(e -> {
			
			/*
			 * Dieser Listener lädt und öffnet das Fenster, welches verwendet wird, um die Eigenschaften
			 * der Maske zu editieren.
			 */
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/sebphil/renderer/fxml/EditMaskWindow.fxml"));
			
			try {
				AnchorPane root = loader.load();
				EditMaskController controller = loader.getController();
				controller.setMaskNoise(maskNoise);
				controller.setNoiseController(this);
				
				Scene scene = new Scene(root);
				
				Stage stage = new Stage();
				stage.setScene(scene);
				stage.setResizable(false);
				stage.setTitle("Mask-Noise");
				stage.show();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		});
		
		// Steuerung für den "Pinsel", mit den die HeightMap per Hand editiert werden kann
		brushRadius = 25;
		brushStrength = 0.5;
		
		prevCircle = new Circle(brushRadius);
		prevCircle.setVisible(false);
		prevCircle.setFill(Color.TRANSPARENT);
		prevCircle.setStroke(Color.MAGENTA);
		
		canvasPane.getChildren().add(prevCircle);

		rootPane.setOnMouseMoved(e -> {
			
			// Prüfung, ob Mauszeiger sich auf der Zeichenfläche befindet
			boolean inBounds = canvasPane.getBoundsInParent().contains(new Point2D(e.getX(), e.getY()));

			if (inBounds && (e.getTarget() instanceof Canvas || e.getTarget() instanceof Circle)) {
				
				// Lässt einen Kreis erscheinen, in welchem Bereich die Werte der Heightmap geändert werden
				if (!prevCircle.isVisible())
					prevCircle.setVisible(true);

				// Steuerung des Vorschau-Kreises
				double x = e.getX() - canvasPane.getLayoutX();
				double y = e.getY() - canvasPane.getLayoutY();

				if (x >= 0 && x <= canvas.getWidth())
					prevCircle.setCenterX(x);

				if (y >= 0 && y <= canvas.getHeight())
					prevCircle.setCenterY(y);

				prevCircle.toFront();

			} else if (prevCircle.isVisible())
				prevCircle.setVisible(false);

		});
		
		canvasPane.setOnMouseClicked(e -> {
			
			// Steuerung für die Manipulation der Werte in der Heightmap
			double x = e.getX();
			double y = e.getY();

			double width = grid.getWidth().doubleValue();

			double r = brushRadius / width;

			int ix = (int) (x / width);
			int iy = (int) (y / width);
			
			/*
			 * Berechnen des Bereiches, welcher maximal untersucht werden muss.
			 * Dazu wird ein Quadrat angenommen, welches den Kreis genau einschließt
			 * (d.h. die Seitenlängen gleich dem Durchmesser des Kreises sind), damit nicht jede
			 * Zelle des Gitters untersucht werden muss, sondern nur Zellen, welche sich in diesem
			 * Quadrat befinden.
			 */
			int minX = (int) ((x - brushRadius) / width);
			int minY = (int) ((y - brushRadius) / width);

			int maxX = (int) ((x + brushRadius) / width);
			int maxY = (int) ((y + brushRadius) / width);
			
			// Grenzfälle, wenn Teil des Quadrats über Grenzen des Gitters hinaus gehen würde
			if (minX < 0)
				minX = 0;

			if (minY < 0)
				minY = 0;

			if (maxX > grid.getAmountX() - 1)
				maxX = grid.getAmountX() - 1;

			if (maxY > grid.getAmountY() - 1)
				maxY = grid.getAmountY() - 1;

			// Untersuche jede Zelle des Quadrates
			for (int sx = minX; sx <= maxX; sx++) {

				for (int sy = minY; sy <= maxY; sy++) {

					double dx = sx - ix;
					double dy = sy - iy;

					// Überprüfe, ob Zelle in Kreis liegt.
					if (dx * dx + dy * dy <= r * r) {
						
						// Wenn ja, berechne einen Wert, für die Stelle des Gitters
						double val = (dist(sx, sy, ix, iy) * brushStrength) / r;

						y = grid.getAmountY() - sy - 1;
						
						if (e.getButton().equals(MouseButton.PRIMARY)) {
							
							/*
							 * Wenn die linke Maustaste betätigt wurde, wird der berechnete Wert zu dem 
							 * bestehenden Wert im Gitter (an dieser Stelle) addiert.
							 */
							grid.setVal(sx, (int) y, grid.getVal(sx, (int) y) + 0.5 * (brushStrength - val));

						} else
							/*
							 * Wenn eine andere Maustaste betätigt wurde, wird der berechnete Wert von dem
							 * bestehenden Wert im Gitter (an dieser Stelle) subtrahiert.
							 */
							grid.setVal(sx, (int) y, grid.getVal(sx, (int) y) - 0.5 * (brushStrength - val));

					}

				}

			}

			fillGridRaw(noise, grid, gc, noiseShape);
			generatePolyMesh();

		});

		pencilStrengthSlider.valueProperty().addListener(l -> {
			brushStrength = pencilStrengthSlider.getValue();
		});

		pencilRadiusSlider.valueProperty().addListener(l -> {
			brushRadius = pencilRadiusSlider.getValue();
			prevCircle.setRadius(brushRadius);
		});

		// -------------------------------------------------------------------------------------------------------------------------

		fillGridRaw(noise, grid, gc, noiseShape);

		canvasPane.getChildren().add(canvas);

	}

	/**
	 * Diese Funktion dient dem Generieren des Gitters aus Dreiecken 
	 * für dieses Noise-Objekt. Zudem wird die von dem MainController aktuell
	 * ausgewählte Szene neu gerendert.
	 * Diese Funktion wird dazu verwendet, wenn z.B. sich die Werte des Gitters
	 * oder der Noise-Funktionen ändern und diese Änderungen sofort dargestellt
	 * werden sollen.
	 */
	protected void generatePolyMesh() {

		noiseShape.generatePolyMesh(colorCheck.isSelected());
		MainController.renderMain();

	}
	
	/**
	 * Diese Funktion dient zur Berechnung der einzelnen Werte für die
	 * einzelnen Zellen des Gitters (z.B. für die Heightmap). Zudem
	 * werden wird das Gitter (Heightmap) neu gezeichnet.
	 */
	protected void fillGrid() {
		
		/*
		 * Aktuelle Zeichnungen auf der Zeichenfläche im Bereich der zu zeichnenden Fläche löschen.
		 * Es können sonst Artefakte der vorherigen Zeichnungen bestehen bleiben.
		 */
		gc.clearRect(0, 0, canvasPane.getWidth(), canvasPane.getHeight());

		/*
		 * Gehe durch jede Zelle des Gitters, um diese
		 * anschließend mit einem Wert zu füllen. 
		 */
		for (int x = 0; x < grid.getAmountX(); x++) {
			for (int y = 0; y < grid.getAmountY(); y++) {
				
				// sum - Wert, welcher in die aktuelle Zelle eingetragen wird
				double sum = 0;

				// Muss Maske berücksichtigt werden?
				if (maskCheck.isSelected()) {
					
					// Wenn ja, muss der Wert von der Maske berücksichtigt werden
					sum = maskNoise.realNoise(x, y);

					
					if(sum > 0)
						/*
						 * Wird eine Maske verwendet, sind nur positive Werte der "normalen" Noise-Funktion
						 * erwünscht.
						 */
						sum *= Math.sqrt(noise.realNoise(x, y) * noise.realNoise(x, y));
					
				} else
					sum = noise.realNoise(x, y);

				// Wert an die Grenzwerte anpassen
				if (sum > noiseShape.getMaxHeight())
					sum = noiseShape.getMaxHeight()
							+ noise.realNoise(x, y) / (noise.getAmplitude() * noise.getOctaves() * 5);

				if (sum < noiseShape.getMinHeight())
					sum = noiseShape.getMinHeight()
							- -noise.realNoise(x, y) / (noise.getAmplitude() * noise.getOctaves() * 5);
				
				// Wert in das Gitter eintragen
				grid.setVal(x, y, sum);

				// Zelle auf die Zeichenfläche zeichnen
				if (colorCheck.isSelected()) {
					gc.setFill(noise.getNoiseRGB(sum));
				} else {
					gc.setFill(Color.gray(noise.getNoiseGray(sum)));
				}

				grid.fillCell(x, grid.getAmountY() - y, gc);

			}
		}

	}

	/**
	 * Diese Funktion zeichnet das Gitter dieses Noise-Objektes neu.
	 * Die Werte des Gitters bleiben dabei unverändert.
	 * 
	 * @param noise			NoiseFunktion
	 * @param grid			Gitter
	 * @param gc			GraphicsContext von der Zeichenfläche
	 * @param noiseShape	Noise-Objekt (RenNoise)
	 */
	private void fillGridRaw(NoiseGenerator2D noise, ResGrid grid, GraphicsContext gc, RenNoise noiseShape) {

		/*
		 * Aktuelle Zeichnungen auf der Zeichenfläche im Bereich der zu zeichnenden Fläche löschen.
		 * Es können sonst Artefakte der vorherigen Zeichnungen bestehen bleiben.
		 */
		gc.clearRect(0, 0, canvasPane.getWidth(), canvasPane.getHeight());

		/*
		 * Gehe durch jede Zelle des Gitters, um diese
		 * zu zeichnen.
		 */
		for (int x = 0; x < grid.getAmountX(); x++) {
			for (int y = 0; y < grid.getAmountY(); y++) {

				double val = grid.getVal(x, y);

				if (val > noiseShape.getMaxHeight())
					val = noiseShape.getMaxHeight();

				if (val < noiseShape.getMinHeight())
					val = noiseShape.getMinHeight();

				if (colorCheck.isSelected()) {
					gc.setFill(noise.getNoiseRGB(val));
				} else {
					gc.setFill(Color.gray(noise.getNoiseGray(val)));
				}

				grid.fillCell(x, grid.getAmountY() - y, gc);

			}
		}

	}

	/**
	 * Funktion, welche die Distanz zwischen zwei, 2D Punkten berechnet
	 * 
	 * @param x1	x-Koordinate von Punkt1
	 * @param y1	y-Koordinate von Punkt1
	 * @param x2	x-Koordiante von Punkt2
	 * @param y2	y-Koordiante von Punkt2
	 * @return Gibt die Distanz von zwei Punkten zu einander zurück.
	 */
	private double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public RenNoise getNoiseShape() {
		return noiseShape;
	}

	public void setNoiseShape(RenNoise noiseShape) {
		this.noiseShape = noiseShape;
	}

}
