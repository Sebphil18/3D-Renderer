package de.sebphil.renderer.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import de.sebphil.renderer.objects.RenCamera;
import de.sebphil.renderer.objects.RenNoise;
import de.sebphil.renderer.objects.RenObject;
import de.sebphil.renderer.objects.RenScene;
import de.sebphil.renderer.objects.RenShape;
import de.sebphil.renderer.objects.SebRenderer;
import de.sebphil.renderer.uicontrol.CustomTreeCell;
import de.sebphil.renderer.uicontrol.RenObjItem;
import de.sebphil.renderer.util.RenUtilities;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Diser Kontroller ist der Hauptkontroller des Hauptfensters.
 * Er ist für die Steuerung dieses Hauptfensters verantwortlich.
 */

public class MainController implements Initializable {
	
	@FXML
	private BorderPane rootPane;

	@FXML
	private MenuItem imObjItem;

	@FXML
	private MenuItem imSceneItem;

	@FXML
	private MenuItem exSceneItem;

	@FXML
	private MenuItem creNoiseItem;

	@FXML
	private TextField nearField;

	@FXML
	private TextField farField;

	@FXML
	private Label widthLabel;

	@FXML
	private Label heightLabel;

	@FXML
	private Label aspectLabel;

	@FXML
	private TextField fovField;

	@FXML
	private TreeView<RenObjItem> treeView;

	@FXML
	private VBox optionsBox;

	@FXML
	private Pane canvasPane;

	private static double prevMouseX = 0;
	private static double prevMouseY = 0;

	private static SebRenderer mainRenderer;
	private static PixelWriter mainWriter;
	public static RenScene mainScene;
	public static TreeItem<RenObjItem> lightItem;

	/**
	 * Initialisiert diesen Kontroller.
	 * Das heißt es werden alle wichtigen, für die Ausführung und Darstellung wichtigen Komponenten erstellt und geladen.
	 * So wird der Haupt-Renderer erstellt und die Steuerung für die Maus und Tastatur geladen.
	 */
	@Override
	public void initialize(URL url, ResourceBundle bundle) {

		// Erstellen der Hauptzeichenfläche.
		Canvas canvas = new Canvas();
		GraphicsContext gc = canvas.getGraphicsContext2D();

		// Erstellt Eigenschaften der Zeichenfläche sowie für das FieldofView
		SimpleDoubleProperty widthProp = new SimpleDoubleProperty();
		SimpleDoubleProperty heightProp = new SimpleDoubleProperty();
		SimpleDoubleProperty fovProp = new SimpleDoubleProperty();
		SimpleDoubleProperty aspProp = new SimpleDoubleProperty();

		// Erstellt die Haupteinträge der TreeView ("3DObjects", "Shapes, "Lights")
		TreeItem<RenObjItem> rootItem = new TreeItem<RenObjItem>(new RenObjItem("3DObjects"));
		TreeItem<RenObjItem> shapesItem = new TreeItem<RenObjItem>(new RenObjItem("Shapes"));
		lightItem = new TreeItem<RenObjItem>(new RenObjItem("Lights"));

		rootItem.getChildren().add(lightItem);
		rootItem.getChildren().add(shapesItem);

		treeView.setCellFactory(f -> new CustomTreeCell());
		treeView.setRoot(rootItem);

		canvasPane.getChildren().add(canvas);

		/*
		 * Erstellt einen neuen Hauptrenderer sowie eine neue Hauptszene.
		 * Zudem wird die Hauptzeichenfläche festgelegt.
		 */
		mainWriter = gc.getPixelWriter();
		mainRenderer = new SebRenderer(canvas.getWidth(), canvas.getHeight());
		mainScene = new RenScene();
		
		// Fügt beim Start der Applikation ein Licht zu der aktuellen Hauptszene hinzu.
		RenObject light = new RenObject("light");
		light.setPosition(new Point3D(1, 1, -1));
		
		addObject(lightItem, light);
		
		// Definiert Listener für Eigenschaften der Hauptzeichenfläche und des Hauptrenderers
		widthProp.addListener(l -> {

			widthLabel.setText(Double.toString(widthProp.doubleValue()));
			mainRenderer.resize(widthProp.doubleValue(), heightProp.doubleValue());
			canvas.setWidth(widthProp.doubleValue());
			render(mainRenderer, mainScene, mainWriter);

		});

		heightProp.addListener(l -> {

			heightLabel.setText(Double.toString(heightProp.doubleValue()));
			mainRenderer.resize(widthProp.doubleValue(), heightProp.doubleValue());
			canvas.setHeight(heightProp.doubleValue());
			render(mainRenderer, mainScene, mainWriter);

		});

		fovProp.addListener(l -> {

			fovField.setText(Double.toString(fovProp.doubleValue() % 360));
			mainRenderer.setFov(fovProp.doubleValue() % 360);
			render(mainRenderer, mainScene, mainWriter);

		});

		aspProp.addListener(l -> {
			aspectLabel.setText(Double.toString(aspProp.doubleValue()));
		});

		// Erstellt Listener für die Steuerelemente (z.B. Textfelder und Knöpfe)
		nearField.textProperty().addListener(l -> {

			if (RenUtilities.isNumeric(nearField.getText(), true, true)) {

				mainRenderer.setNear(Double.valueOf(nearField.getText()));
				render(mainRenderer, mainScene, mainWriter);

			}

		});

		farField.textProperty().addListener(l -> {

			if (RenUtilities.isNumeric(farField.getText(), true, true)) {

				mainRenderer.setFar(Double.valueOf(farField.getText()));
				render(mainRenderer, mainScene, mainWriter);

			}

		});

		fovField.textProperty().addListener(l -> {

			if (RenUtilities.isNumeric(fovField.getText(), true, true)) {

				double fov = Double.valueOf(fovField.getText()) % 360;

				if (fov >= 180)
					fov = 179;
				else if (fov <= 0)
					fov = 1;

				fovProp.set(fov);

			}

		});

		// Belegt Eigenschaften für die Hauptzeichenfläche und den Hauptrenderer
		widthProp.set(mainRenderer.getWidth());
		heightProp.set(mainRenderer.getHeight());
		fovProp.set(mainRenderer.getFov());
		aspProp.set(mainRenderer.getAspectratio());

		nearField.setText(Double.toString(mainRenderer.getNear()));
		farField.setText(Double.toString(mainRenderer.getFar()));

		RenCamera cam = mainScene.getCamera();

		cam.setPosition(new Point3D(0, 0, -3));

		// (basic) Controls & GUI
		/*
		 * Erstellt die Steuerung der Kamera sowie der Benutzeroberfläche.
		 */
		KeyCombination shiftA = new KeyCodeCombination(KeyCode.A, KeyCombination.SHIFT_DOWN);
		KeyCombination shiftD = new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN);
		
		// Tastatur-Steuerung für die Kamera
		rootPane.setOnKeyPressed(e -> {
			
				// Shift + A = Nach links bewegen
			if (shiftA.match(e)) {

				Point3D right = cam.getNewRight().multiply(0.1);
				cam.setPosition(cam.getPosition().subtract(right));
				render(mainRenderer, mainScene, mainWriter);
				return;
			
				// Shift + D = Nach rechts bewegen
			} else if (shiftD.match(e)) {

				Point3D right = cam.getNewRight().multiply(0.1);
				cam.setPosition(cam.getPosition().add(right));
				render(mainRenderer, mainScene, mainWriter);
				return;

			}
			
				// W = Nach vorn bewegen
			if (e.getCode().equals(KeyCode.W)) {

				Point3D forward = cam.getLookDir().multiply(0.1);
				cam.setPosition(cam.getPosition().add(forward));
				//moveNoisesDown();
				
				// S = Nach hinten bewegen
			} else if (e.getCode().equals(KeyCode.S)) {

				Point3D forward = cam.getLookDir().multiply(-0.1);
				cam.setPosition(cam.getPosition().add(forward));

				// A = Nach links rotieren
			} else if (e.getCode().equals(KeyCode.A)) {
				cam.setYaw(cam.getYaw() + 1);
				
				// D = Nach rechts rotieren
			} else if (e.getCode().equals(KeyCode.D)) {
				cam.setYaw(cam.getYaw() - 1);
			}
			
			render(mainRenderer, mainScene, mainWriter);

		});
		
		// Steuerung mit der Maus (Scrollen)
		rootPane.setOnScroll(e -> {
			
			if (e.isAltDown()) {

				double fov = fovProp.doubleValue();
				
					// ALT + nach "unten" scrollen = Vergrößerung des FieldOfView
				if (e.getDeltaY() > 0) {
					fovProp.set(fov - 1);
					
					// ALT + nach "unten" scrollen = Vergrößerung des FieldOfView
				} else {
					fovProp.set(fov + 1);
				}
				
				// nach "oben" scrollen = Nach unten bewegen
			} else if (e.getDeltaY() > 0) {

				Point3D up = cam.getNewUp().multiply(0.1);
				cam.setPosition(cam.getPosition().subtract(up));
				render(mainRenderer, mainScene, mainWriter);
				
				// nach "unten" scrollen = Nach oben bewegen
			} else if (e.getDeltaY() < 0) {

				Point3D up = cam.getNewUp().multiply(0.1);
				cam.setPosition(cam.getPosition().add(up));
				render(mainRenderer, mainScene, mainWriter);

			}

		});

		// Steuerung mit der Maus (wenn Mauszeiger "gezogen" wird)
		rootPane.setOnMouseDragged(e -> {
			
			double x = prevMouseX - e.getScreenX();
			double y = prevMouseY - e.getScreenY();
			
			/*
			 * Rotiere die Kamera in Abhängigkeit zu der Positionsänderung des Cursors.
			 * Dies ermöglicht das Umschauen mit der Maus.
			 */
			cam.setYaw(cam.getYaw() + x * 0.25);
			cam.setPitch(cam.getPitch() + -(y * 0.25));

			renderMain();

			prevMouseX = e.getScreenX();
			prevMouseY = e.getScreenY();
		});

		rootPane.setOnMousePressed(e -> {
			prevMouseX = e.getScreenX();
			prevMouseY = e.getScreenY();
		});

		canvasPane.setOnMouseClicked(e -> {
			rootPane.requestFocus();
		});

		// Listener für die Figuren der Hauptszene
		mainScene.getShapes().addListener(new ListChangeListener<RenShape>() {

			@Override
			public void onChanged(Change<? extends RenShape> arg0) {

				while (arg0.next()) {

					if (arg0.wasAdded()) {

						for (RenShape shape : arg0.getAddedSubList()) {

							TreeItem<RenObjItem> item = new TreeItem<RenObjItem>(
									new RenObjItem(shape.getName(), shape));
							shapesItem.getChildren().add(item);
							render(mainRenderer, mainScene, mainWriter);

						}

					}

				}

			}

		});

		/* 
		 * Sollte sich die Größe (Breite) der Zeichenfläche ändern, 
		 * werden die Eigenschaften dieser angepasst.
		 */
		canvasPane.widthProperty().addListener(l -> {

			canvas.setWidth(canvasPane.getWidth());
			widthProp.set(canvasPane.getWidth());
			aspProp.set(mainRenderer.getAspectratio());

		});
		
		/* 
		 * Sollte sich die Größe (Höhe) der Zeichenfläche ändern, 
		 * werden die Eigenschaften dieser angepasst.
		 */
		canvasPane.heightProperty().addListener(l -> {

			canvas.setHeight(canvasPane.getHeight());
			heightProp.set(canvasPane.getHeight());
			aspProp.set(mainRenderer.getAspectratio());

		});

		// Listener für das Selektieren eines Eintrages der TreeView
		treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<RenObjItem>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<RenObjItem>> arg0, TreeItem<RenObjItem> arg1,
					TreeItem<RenObjItem> arg2) {
				
				/*
				 *  Leere die Optionen, da sonst falsche Optionen für ein nicht ausgewähltes 
				 *  Objekt geladen sein könnten.
				 */
				
				optionsBox.getChildren().clear();

				// Wenn das nun der auszuwählende Eintrag null entspricht tue nichts.
				if (arg2 == null)
					return;
				if (arg2.getValue().getRenObj() == null)
					return;
				
				// Referenziere jenes RenObject, welches in dem auszuwählenden Eintrag enthalten ist.
				RenObject selectedObj = arg2.getValue().getRenObj();
				
				// Sollte es einen Übergeordneten Eintrag geben
				if (arg2.getParent() != null) {
					
					// Wenn es sich dabei um den Haupteintrag für Figuren handelt
					if (arg2.getParent().equals(shapesItem)) {
						
						// Unterscheidung, ob das aktuelle Objekt ein Noise-Objekt ist oder nicht
						if (selectedObj instanceof RenNoise) {
							
							// laden Optionen für dieses Noise-Objekt
							loadNoiseOptions((RenNoise) selectedObj, optionsBox);
						} else {
							
							// laden Optionen für diese Figur
							loadShapeOptions((RenShape) selectedObj, optionsBox, mainWriter);
						}
						
						// Wenn es sich dabei um den Haupteintrag für Lichtquellen handelt
					} else if (arg2.getParent().equals(lightItem)) {
						
						// Lade die Optionen für die aktuelle Lichtquelle
						loadLightOptions(selectedObj, optionsBox);
					}
				}

			}

		});
		
		// Steuerung für Kontextmenu für die einzelnen Einträge der TreeView
		treeView.setOnMouseClicked(e -> {
			MouseButton button = e.getButton();
			if (button.equals(MouseButton.SECONDARY)) {

				Node pickedNode = e.getPickResult().getIntersectedNode();

				if (pickedNode instanceof CustomTreeCell) {
					CustomTreeCell cell = (CustomTreeCell) pickedNode;
					TreeItem<RenObjItem> item = cell.getTreeItem();
					openMenu(e.getScreenX(), e.getScreenY(), item, shapesItem);
				}

			}
		});

	}

	/**
	 * Diese Funktion ruft ein Fenster auf, welches für das Importieren einer
	 * .obj Datei benötigt wird.
	 * Diese Funktion verfügt über eine FXML Annotation. Sie kann zu jedem 
	 * Zeipunkt über ein FXML Dokument aufgerufen werden.
	 */
	@FXML
	public void importObj() {

		openImpObjWin().show();

	}
	
	/**
	 * Diese Funktion ruft ein Fenster auf, welches für das Exportieren der aktuell
	 * ausgewählten Hauptszene verantwortlich ist.
	 * Diese Funktion verfügt über eine FXML Annotation. Sie kann zu jedem 
	 * Zeipunkt über ein FXML Dokument aufgerufen werden.
	 */
	@FXML
	public void exportScene() {

		openExpSceneWin().show();

	}

	/**
	 * Diese Funktion ruft ein Fenster auf, welches für das Importieren einer Szene
	 * verantwortlich ist.
	 * Diese Funktion verfügt über eine FXML Annotation. Sie kann zu jedem 
	 * Zeipunkt über ein FXML Dokument aufgerufen werden.
	 */
	@FXML
	public void importScene() {

		openImpSceneWin().show();

	}

	/**
	 * Diese Funktion ruft ein Fenster auf, welches für das Editieren eines
	 * Noise-Objektes verantwortlich ist.
	 * Diese Funktion verfügt über eine FXML Annotation. Sie kann zu jedem 
	 * Zeipunkt über ein FXML Dokument aufgerufen werden.
	 */
	@FXML
	public void createNoise() {

		openNoiseWin().show();

	}

	/**
	 * Lädt alle verfügbaren Optionen, welche eine bestimmte Figur haben kann.
	 * 
	 * @param shape 		3D Figur
	 * @param optionsBox 	Behälter für die Optionsfelder
	 * @param writer 		PixelWriter für die Vorschau der Figur
	 */
	private void loadShapeOptions(RenShape shape, VBox optionsBox, PixelWriter writer) {
		optionsBox.getChildren().clear();

		String[] xyz = new String[] { "X", "Y", "Z" };
		Separator[] seps = generateSeperators(6);
		VBox shapeBox = new VBox();

		// Name
		Label title7 = new Label("name: ");
		GridPane namePane = new GridPane();
		List<TreeItem<RenObjItem>> items = getLeafes(treeView.getRoot().getChildren().get(1));

		TextField nameField = new TextField(shape.getName());

		title7.setId("text2");
		title7.setMinWidth(50);

		namePane.setHgap(10);

		namePane.add(title7, 0, 0);
		namePane.add(nameField, 1, 0);

		alignmentGridPane(namePane);

		nameField.textProperty().addListener(l -> {

			if (nameField.getText().length() >= 14) {
				nameField.setText(shape.getName());
				return;
			}

			for (TreeItem<RenObjItem> item : items) {

				String testName = item.getValue().getName();

				if (testName.equals(shape.getName())) {

					shape.setName(nameField.getText());
					item.getValue().setName(shape.getName());
					treeView.refresh();

				}

			}

		});

		// Position
		Point3D pos = shape.getPosition();
		Label title1 = new Label("Position");
		GridPane posPane = new GridPane();

		Label[] posLabs = generateInfoLabs(xyz);
		TextField[] posFields = generateNumFields(new String[] { Double.toString(pos.getX()),
				Double.toString(pos.getY()), Double.toString(pos.getZ()), });

		title1.setId("text1");
		title1.setMaxWidth(150);

		fillValues(posLabs, posFields, posPane);

		posPane.setHgap(10);
		alignmentGridPane(posPane);

		generatePositionListener(posFields, shape, writer);

		posFields[0].textProperty().addListener(generatePositionListener(posFields, shape, writer));
		posFields[1].textProperty().addListener(generatePositionListener(posFields, shape, writer));
		posFields[2].textProperty().addListener(generatePositionListener(posFields, shape, writer));

		// Translation
		Point3D trans = shape.getTranslation();
		Label title2 = new Label("Translation");
		GridPane transPane = new GridPane();

		Label[] transLabs = generateInfoLabs(xyz);
		TextField[] transFields = generateNumFields(new String[] { Double.toString(trans.getX()),
				Double.toString(trans.getY()), Double.toString(trans.getZ()) });

		title2.setId("text1");
		title2.setMaxWidth(150);

		fillValues(transLabs, transFields, transPane);

		transPane.setHgap(10);

		transFields[0].textProperty().addListener(generateTransListener(transFields, shape, writer));
		transFields[1].textProperty().addListener(generateTransListener(transFields, shape, writer));
		transFields[2].textProperty().addListener(generateTransListener(transFields, shape, writer));

		// Rotation
		Label title3 = new Label("Rotation");
		GridPane rotPane = new GridPane();

		Label[] rotLabs = generateInfoLabs(xyz);
		TextField[] rotFields = generateNumFields(new String[] { Double.toString(shape.getAngleX()),
				Double.toString(shape.getAngleY()), Double.toString(shape.getAngleZ()) });

		title3.setId("text1");
		title3.setMaxWidth(150);

		fillValues(rotLabs, rotFields, rotPane);

		rotPane.setHgap(10);

		rotFields[0].textProperty().addListener(generateRotListener(rotFields, shape, writer));
		rotFields[1].textProperty().addListener(generateRotListener(rotFields, shape, writer));
		rotFields[2].textProperty().addListener(generateRotListener(rotFields, shape, writer));

		// Größe
		Point3D size = shape.getSize();
		Label title4 = new Label("Size");
		GridPane sizePane = new GridPane();

		Label[] sizeLabs = generateInfoLabs(xyz);
		TextField[] sizeFields = generateNumFields(new String[] { Double.toString(size.getX()),
				Double.toString(size.getY()), Double.toString(size.getZ()) });

		title4.setId("text1");
		title4.setMaxWidth(150);

		fillValues(sizeLabs, sizeFields, sizePane);

		sizePane.setHgap(10);

		sizeFields[0].textProperty().addListener(generateSizeListener(sizeFields, shape, writer));
		sizeFields[1].textProperty().addListener(generateSizeListener(sizeFields, shape, writer));
		sizeFields[2].textProperty().addListener(generateSizeListener(sizeFields, shape, writer));

		// Farbe
		Label title5 = new Label("Color");
		ColorPicker colPicker = new ColorPicker(shape.getColor());

		title5.setId("text1");
		title5.setMaxWidth(150);

		// Vorschau
		Label title6 = new Label("Preview");
		BorderPane prePane = new BorderPane();
		double preWidth = 190;
		double preHeight = 190;
		Canvas preCanvas = new Canvas(preWidth, preHeight);
		PixelWriter preWriter = preCanvas.getGraphicsContext2D().getPixelWriter();

		title6.setMinWidth(180);
		title6.setId("text1");

		SebRenderer preRenderer = new SebRenderer(preWidth, preHeight);
		RenScene preScene = new RenScene();
		RenCamera preCam = new RenCamera("preCam");
		RenShape copyShape = shape.copy();

		double dZ = copyShape.getMaxZ() - copyShape.getMinZ();

		copyShape.setPosition(new Point3D(0, 0, 0));
		copyShape.setTranslation(new Point3D(0, 0, 0));
		copyShape.setSize(new Point3D(1, 1, 1));
		copyShape.setAngleX(0);
		copyShape.setAngleY(0);
		copyShape.setAngleZ(0);

		preCam.setPosition(new Point3D(0, 0, -dZ*3));
		preScene.getShapes().add(copyShape);
		preScene.getLights().add(new Point3D(0, 1, -1));
		preScene.setCamera(preCam);
		render(preRenderer, preScene, preWriter);

		prePane.setId("disBox");
		prePane.setCenter(preCanvas);

		shapeBox.setPadding(new Insets(5, 6, 5, 5));
		shapeBox.setMinWidth(200);
		shapeBox.setSpacing(5);
		shapeBox.setAlignment(Pos.CENTER_LEFT);
		shapeBox.getChildren().addAll(namePane, seps[5], title1, posPane, seps[0], title2, transPane, seps[1], title3,
				rotPane, seps[2], title4, sizePane, seps[3], title5, colPicker, seps[4], title6, prePane);

		colPicker.setOnAction(e -> {
			copyShape.setColor(colPicker.getValue());
			shape.setColor(colPicker.getValue());
			render(preRenderer, preScene, preWriter);
			render(mainRenderer, mainScene, writer);
		});

		// Steuerung für die Vorschau

		preCanvas.setOnMouseClicked(e -> {
			if (!prePane.isFocused())
				prePane.requestFocus();
			else
				rootPane.requestFocus();
		});

		prePane.setOnKeyPressed(e -> {

			if (e.getCode().equals(KeyCode.I)) {
				copyShape.setAngleX(copyShape.getAngleX() + 1);
				render(preRenderer, preScene, preWriter);
			} else if (e.getCode().equals(KeyCode.K)) {
				copyShape.setAngleX(copyShape.getAngleX() - 1);
				render(preRenderer, preScene, preWriter);
			} else if (e.getCode().equals(KeyCode.J)) {
				copyShape.setAngleZ(copyShape.getAngleZ() + 1);
				render(preRenderer, preScene, preWriter);
			} else if (e.getCode().equals(KeyCode.L)) {
				copyShape.setAngleZ(copyShape.getAngleZ() - 1);
				render(preRenderer, preScene, preWriter);
			}

		});

		optionsBox.getChildren().add(shapeBox);

	}

	/**
	 * Lädt das Fenster für die Optionen eines Noise-Objektes.
	 * Diese Funktion ruft zudem die Funktion
	 * @see de.sebphil.renderer.controller.MainController#loadShapeOptions(RenShape, VBox, PixelWriter)
	 * auf.
	 * 
	 * @param noise 		Noise-Objekt
	 * @param optionsBox 	behälter für Optionen
	 */
	private void loadNoiseOptions(RenNoise noise, VBox optionsBox) {

		loadShapeOptions(noise, optionsBox, mainWriter);

		VBox contentBox = (VBox) optionsBox.getChildren().get(0);
		
		// Knopf, um das Fenster aufzurufen, welches das Editieren dieses Noise-Objektes ermöglicht
		Button editNoiseButton = new Button("edit noise");

		editNoiseButton.setOnAction(e -> {

			Stage stage = new Stage();

			AnchorPane root = new AnchorPane();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/sebphil/renderer/fxml/NoiseWindow.fxml"));

			NoiseController controller = new NoiseController();
			controller.setNoiseShape(noise);

			loader.setController(controller);

			try {
				root = loader.load();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			Scene scene = new Scene(root);

			stage.setScene(scene);
			stage.setTitle("Noise-Generator");

			stage.show();

		});

		contentBox.getChildren().add(editNoiseButton);

	}

	/**
	 * Lädt die Optionen für eine Lichtquelle.
	 * 
	 * @param renObj 		Lichtquelle
	 * @param optionsBox 	Behälter für die Optionen
	 */
	private void loadLightOptions(RenObject renObj, VBox optionsBox) {
		optionsBox.getChildren().clear();

		String[] xyz = new String[] { "X", "Y", "Z" };
		VBox lightBox = new VBox();
		Separator[] seps = generateSeperators(1);

		// Name

		Label title2 = new Label("name: ");
		GridPane namePane = new GridPane();
		List<TreeItem<RenObjItem>> items = getLeafes(treeView.getRoot().getChildren().get(0));

		TextField nameField = new TextField(renObj.getName());

		title2.setId("text2");
		title2.setMinWidth(50);

		namePane.setHgap(10);

		namePane.add(title2, 0, 0);
		namePane.add(nameField, 1, 0);

		alignmentGridPane(namePane);

		nameField.textProperty().addListener(l -> {

			if (nameField.getText().length() >= 14) {
				nameField.setText(renObj.getName());
				return;
			}

			for (TreeItem<RenObjItem> item : items) {

				String testName = item.getValue().getName();

				if (testName.equals(renObj.getName())) {

					renObj.setName(nameField.getText());
					item.getValue().setName(renObj.getName());
					treeView.refresh();

				}

			}

		});

		// Position

		Point3D pos = renObj.getPosition();
		GridPane posPane = new GridPane();

		Label title1 = new Label("Position");

		Label[] labs = generateInfoLabs(xyz);
		TextField[] fields = generateNumFields(
				new String[] { Double.toString(pos.getX()), Double.toString(pos.getY()), Double.toString(pos.getZ()) });

		title1.setId("text1");
		title1.setMaxWidth(150);

		fillValues(labs, fields, posPane);
		alignmentGridPane(posPane);

		posPane.setHgap(10);

		fields[0].textProperty().addListener(generatePositionListener(fields, renObj, mainWriter));
		fields[1].textProperty().addListener(generatePositionListener(fields, renObj, mainWriter));
		fields[2].textProperty().addListener(generatePositionListener(fields, renObj, mainWriter));

		lightBox.setPadding(new Insets(5, 6, 5, 5));
		lightBox.setMinWidth(200);
		lightBox.setSpacing(5);
		lightBox.setAlignment(Pos.CENTER_LEFT);

		lightBox.getChildren().addAll(namePane, seps[0], title1, posPane);
		optionsBox.getChildren().add(lightBox);

	}

	/**
	 * Öffnet das Kontextmenü für die TreeView.
	 * 
	 * @param x 			x-Koordiante des Kontextmenü
	 * @param y 			y-Koordiante des Kontextmenü
	 * @param item 			TreeItem, welches von diesem Kontextmenü beeinflusst werden soll
	 * @param objectsItem  	Übergrordnetes TreeItem, welches das beeinflusste TreeItem beinhaltet
	 */
	private void openMenu(double x, double y, TreeItem<RenObjItem> item, TreeItem<RenObjItem> objectsItem) {

		ContextMenu menu = new ContextMenu();

		menu.setX(x);
		menu.setY(y);

			/*
			 *  Wenn eine leere Fläche in der TreeView angesteuert wird oder
			 *  das angesteuerte Element das Hauptelement "rootItem" ist.
			 */
		if (item == null || item.getParent() == null) {
			
			/*
			 * Lade die Einträge für das importieren einer .obj Datei sowie
			 * das Hinzufügen einer Lichtquelle.
			 */
			MenuItem item1 = new MenuItem("import obj");
			MenuItem item2 = new MenuItem("add light");

			item1.setId("menuclickable");
			item2.setId("menuclickable");

			menu.getItems().add(item1);
			menu.getItems().add(item2);

			item1.setOnAction(e -> {
				openImpObjWin().show();
			});

			item2.setOnAction(e -> {
				addObject(lightItem, new RenObject("light"));
			});
			
			// Sollte es sich um einen validen Eintrag handeln (Eintrag hat ein Übergeordneten Eintrag)
		} else {
			
			TreeItem<RenObjItem> parent = item.getParent();
			
				// Sollte es sich um den Haupteintrag für Figuren handeln
			if (parent.equals(objectsItem) || item.equals(objectsItem)) {
				
				/*
				 * Lade die Einträge, um ein Objekt zu importieren, das angesteuerte Objekt zu löschen
				 * oder ein neues Noise-Objekt hinzu zu fügen.
				 */
				MenuItem item1 = new MenuItem("import obj");
				MenuItem item2 = new MenuItem("remove shape");
				MenuItem item3 = new MenuItem("add noise");

				item1.setId("menuclickable");
				item2.setId("menuclickable");
				item3.setId("menuclickable");

				menu.getItems().add(item1);
				menu.getItems().add(item3);
				menu.getItems().add(item2);

				item1.setOnAction(e -> {
					openImpObjWin().show();
				});

				item2.setOnAction(e -> {
					if (parent.equals(objectsItem))
						removeObject(item.getValue().getRenObj().getUuid(), objectsItem);
				});

				item3.setOnAction(e -> {
					openNoiseWin().show();
				});
				
				// Sollte es sich um den Haupteintrag für Lichtquellen handeln
			} else if (parent.equals(lightItem) || item.equals(lightItem)) {
				
				/*
				 * Lade die Einträge für das Hinzufügen oder Löschen der
				 * angesteuerten Lichtquelle.
				 */
				MenuItem item1 = new MenuItem("add light");
				MenuItem item2 = new MenuItem("remove light");

				item1.setId("menuclickable");
				item2.setId("menuclickable");

				menu.getItems().add(item1);
				menu.getItems().add(item2);

				item1.setOnAction(e -> {
					addObject(lightItem, new RenObject("light"));
				});

				item2.setOnAction(e -> {
					if (parent.equals(lightItem))
						removeObject(item.getValue().getRenObj().getUuid(), lightItem);
				});

			}

		}

		menu.show(rootPane.getScene().getWindow());
	}
	
	/**
	 * Generiert die Steuerung für die Positionsfelder der Optionen.
	 * 
	 * @param fields Textfelder
	 * @param renObj Objekt, von welchem die Position angezeigt und beeinflusst werden soll
	 * @param writer PixelWriter des Hauptrenderers
	 * @return Gibt einen ChangeListener zurück, welcher auf die Position des bestimmten Objektes reagiert, wenn dieser aufgerufen wird
	 */
	private ChangeListener<String> generatePositionListener(TextField[] fields, RenObject renObj, PixelWriter writer) {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// arg0 - beobachtbarer Wert für den veränderten Wert
				// arg1 - alter Wert
				// arg2 - neuer Wert

				if (RenUtilities.isNumeric(arg2, true, true)) {

					if (!(renObj instanceof RenShape)) {
						mainScene.getLights().remove(renObj.getPosition());
					}

					renObj.setPosition(new Point3D(Double.valueOf(fields[0].getText()),
							Double.valueOf(fields[1].getText()), Double.valueOf(fields[2].getText())));

					if (!(renObj instanceof RenShape)) {
						mainScene.getLights().add(renObj.getPosition());
					}

					render(mainRenderer, mainScene, writer);

				}
			}
		};
	}
	
	/**
	 * Generiert die Steuerung für die Translationfelder der Optionen.
	 * 
	 * @param fields 	Textfelder
	 * @param shape 	Objekt, von welchem die Translation angezeigt und beeinflusst werden soll
	 * @param writer 	PixelWriter des Hauptrenderers
	 * @return Gibt einen ChangeListener zurück, welcher auf die Position des bestimmten Objektes reagiert, wenn dieser aufgerufen wird
	 */
	private ChangeListener<String> generateTransListener(TextField[] fields, RenShape shape, PixelWriter writer) {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// arg0 - beobachtbarer Wert für den veränderten Wert
				// arg1 - alter Wert
				// arg2 - neuer Wert

				if (RenUtilities.isNumeric(arg2, true, true)) {
					shape.setTranslation(new Point3D(Double.valueOf(fields[0].getText()),
							Double.valueOf(fields[1].getText()), Double.valueOf(fields[2].getText())));
					render(mainRenderer, mainScene, writer);
				}
			}
		};
	}
	
	/**
	 * Generiert die Steuerung für die Rotationsfelder der Optionen.
	 * 
	 * @param fields Textfelder
	 * @param renObj Objekt, von welchem die Position angezeigt und beeinflusst werden soll
	 * @param writer PixelWriter des Hauptrenderers
	 * @return Gibt einen ChangeListener zurück, welcher auf die Rotation des bestimmten Objektes reagiert, wenn dieser aufgerufen wird
	 */
	private ChangeListener<String> generateRotListener(TextField[] fields, RenObject renObj, PixelWriter writer) {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// arg0 - beobachtbarer Wert für den veränderten Wert
				// arg1 - alter Wert
				// arg2 - neuer Wert

				if (RenUtilities.isNumeric(arg2, true, true)) {
					renObj.setAngleX(Double.valueOf(fields[0].getText()));
					renObj.setAngleY(Double.valueOf(fields[1].getText()));
					renObj.setAngleZ(Double.valueOf(fields[2].getText()));
					render(mainRenderer, mainScene, writer);
				}
			}
		};
	}

	/**
	 * Generiert die Steuerung für die Größenfelder der Optionen.
	 * 
	 * @param fields 	Textfelder
	 * @param shape 	Objekt, von welchem die Größe angezeigt und beeinflusst werden soll
	 * @param writer 	PixelWriter des Hauptrenderers
	 * @return Gibt einen ChangeListener zurück, welcher auf die Position des bestimmten Objektes reagiert, wenn dieser aufgerufen wird
	 */
	private ChangeListener<String> generateSizeListener(TextField[] fields, RenShape shape, PixelWriter writer) {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// arg0 - beobachtbarer Wert für den veränderten Wert
				// arg1 - alter Wert
				// arg2 - neuer Wert

				if (RenUtilities.isNumeric(arg2, true, true)) {
					shape.setSize(new Point3D(Double.valueOf(fields[0].getText()), Double.valueOf(fields[1].getText()),
							Double.valueOf(fields[2].getText())));
					render(mainRenderer, mainScene, writer);
				}
			}
		};
	}

	/**
	 * Diese Funktion fügt einer GridPane die richtige Ausrichtung für Elemente hinzu.
	 * 
	 * @param gridpane GridPane Container
	 */
	private void alignmentGridPane(GridPane gridpane) {

		for (Node node : gridpane.getChildren())
			GridPane.setHalignment(node, HPos.CENTER);

	}
	
	/**
	 * Diese Funktion fügt Textfelder und Etikette zu einer GridPane hinzu.
	 * 
	 * @param labs		Etikette
	 * @param fields	Textfelder
	 * @param valuePane	GridPane Container
	 */
	private void fillValues(Label[] labs, TextField[] fields, GridPane valuePane) {

		for (int i = 0; i < fields.length; i++) {
			valuePane.add(labs[i], i, 0);
			valuePane.add(fields[i], i, 1);
		}

		alignmentGridPane(valuePane);

	}

	/**
	 * Diese Funktion generiert ein Array von Etikette mit einer
	 * Inschrift.
	 * 
	 * @param args	Array von Zeichenketten. Die Länge dieses Arrays ist 
	 * gleich der Anzahl der generierten Erikette.
	 * 
	 * @return Array, welches die erzeugten Etikette enthält.
	 */
	private Label[] generateInfoLabs(String[] args) {

		Label[] labs = new Label[args.length];

		for (int i = 0; i < labs.length; i++) {
			labs[i] = new Label(args[i]);
			labs[i].setId("text3");
		}

		return labs;
	}

	/**
	 * Diese Funktion generiert ein Array von Textfelder mit einer
	 * Inschrift. Diese Textfelder sollten dazu verwendet werden, um darin
	 * Zahlen zu schreiben.
	 * 
	 * @param args	Array von Zeichenketten. Die Länge dieses Arrays ist 
	 * gleich der Anzahl der generierten Textfelder.
	 * 
	 * @return Array, welches die erzeugten Textfelder enthält.
	 */
	private TextField[] generateNumFields(String[] args) {

		TextField[] fields = new TextField[args.length];

		for (int i = 0; i < fields.length; i++) {
			fields[i] = new TextField(args[i]);
			fields[i].setPromptText("num");
			fields[i].setId("numField");
		}

		return fields;
	}

	/**
	 * Generiert eine Anzahl von Separatoren.
	 * 
	 * @param num	Anzahl der Separatoren
	 * @return Array von Separatoren
	 */
	private Separator[] generateSeperators(int num) {

		Separator[] seps = new Separator[num];

		for (int i = 0; i < num; i++) {
			seps[i] = new Separator();
			seps[i].setPrefHeight(15);
		}

		return seps;
	}

	/**
	 * Fügt ein neues Objekt zu einer Szene und einem TreeItem hinzu.
	 * 
	 * @param objectsItem 	übergeordnetes TreeItem
	 * @param renObj 		Objekt, welches hinzugefügt werden soll
	 */
	private void addObject(TreeItem<RenObjItem> objectsItem, RenObject renObj) {

		if (renObj instanceof RenShape) {
			mainScene.getShapes().add((RenShape) renObj);
		} else if (renObj instanceof RenNoise) {
			mainScene.getShapes().add((RenShape) renObj);
		} else {
			mainScene.getLights().add(renObj.getPosition());
		}

		TreeItem<RenObjItem> item = new TreeItem<RenObjItem>(new RenObjItem(renObj.getName(), renObj));
		objectsItem.getChildren().add(item);

		render(mainRenderer, mainScene, mainWriter);

	}

	/**
	 * Entfernt ein Objekt.
	 * 
	 * @param uuid 		UUID des Objekts
	 * @param branch 	übergeordnetes TreeItem
	 */
	private void removeObject(UUID uuid, TreeItem<RenObjItem> branch) {

		List<TreeItem<RenObjItem>> leafes = getLeafes(branch);

		for (int i = 0; i < leafes.size(); i++) {

			RenObjItem renObjItem = leafes.get(i).getValue();
			RenObject renObj = renObjItem.getRenObj();

			if (renObj.getUuid().compareTo(uuid) == 0) {
				leafes.get(i).getParent().getChildren().remove(leafes.get(i));
				mainScene.getShapes().remove(renObj);
				mainScene.getLights().remove(renObj.getPosition());
				render(mainRenderer, mainScene, mainWriter);
				return;
			}
		}

	}

	private List<TreeItem<RenObjItem>> getLeafes(TreeItem<RenObjItem> branch) {

		List<TreeItem<RenObjItem>> items = new ArrayList<TreeItem<RenObjItem>>();

		for (TreeItem<RenObjItem> item : branch.getChildren()) {

			if (item.isLeaf())
				items.add(item);
			else
				items.addAll(getLeafes(item));

		}

		return items;
	}

	/**
	 * Rendert eine bestimmte Szene mit einem bestimmten Renderer auf eine bestimmte Zeichenfläche.
	 * (Diese Funktion ruft die Funktionen SebRenderer.update und SebRenderer.draw auf.)
	 * 
	 * @param renderer 	Renderer, welcher die Szene rendern soll
	 * @param scene 	Szene, welche gerendert werden soll
	 * @param writer 	PixelWriter der verwendeten Zeichenfläche
	 * @return Frames per Second
	 */
	private double render(SebRenderer renderer, RenScene scene, PixelWriter writer) {

		long start = System.currentTimeMillis();
		renderer.update(scene);
		renderer.draw(writer);
		long stop = System.currentTimeMillis();

		return 1 / ((double) (stop - start) / 1000);
	}

	/**
	 * Diese Funktion rendert die aktuelle Hauptszene mit dem aktuellen Hautprenderer.
	 */
	public static void renderMain() {
		mainRenderer.update(mainScene);
		mainRenderer.draw(mainWriter);
	}
	
	/**
	 * Diese Funktion erzeugt ein Fenster, welches für das Importieren einer .obj Datei
	 * verwendet wird.
	 * 
	 * @return Fenster, welches erzeugt wurde
	 */
	private Stage openImpObjWin() {

		Stage stage = new Stage();

		BorderPane rootPane = new BorderPane();
		try {
			rootPane = FXMLLoader.load(getClass().getResource("/de/sebphil/renderer/fxml/ImpObjWindow.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Scene scene = new Scene(rootPane);

		stage.setScene(scene);

		stage.setResizable(false);
		stage.initStyle(StageStyle.UTILITY);
		stage.setTitle("importing obj");

		return stage;
	}
	
	/**
	 * Diese Funktion erzeugt ein Fenster, welches für das Exportieren der aktuellen 
	 * Hauptszene verwendet wird.
	 * 
	 * @return Fenster, welches erzeugt wurde
	 */
	private Stage openExpSceneWin() {

		Stage stage = new Stage();

		AnchorPane root = new AnchorPane();
		try {
			root = FXMLLoader.load(getClass().getResource("/de/sebphil/renderer/fxml/ExpSceneWindow.fxml"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Scene scene = new Scene(root);

		stage.setScene(scene);

		stage.setResizable(false);
		stage.initStyle(StageStyle.UTILITY);
		stage.setTitle("exporting Scene");

		return stage;

	}
	
	/**
	 * Diese Funktion erzeugt ein Fenster, welches für das Importieren einer Szene
	 * verantworlich ist.
	 * 
	 * @return Fenster, welches erzeugt wurde
	 */
	private Stage openImpSceneWin() {

		Stage stage = new Stage();

		AnchorPane root = new AnchorPane();
		try {
			root = FXMLLoader.load(getClass().getResource("/de/sebphil/renderer/fxml/ImpSceneWindow.fxml"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Scene scene = new Scene(root);

		stage.setScene(scene);

		stage.setResizable(false);
		stage.initStyle(StageStyle.UTILITY);
		stage.setTitle("importing Scene");

		return stage;

	}
	
	/**
	 * Diese Funktion erzeugt ein Fenster, welches für das Editieren eines
	 * Noise-Objektes verwendet werden kann.
	 * 
	 * @return Fenster, welches erzeugt wurde
	 */
	private Stage openNoiseWin() {

		Stage stage = new Stage();

		AnchorPane root = new AnchorPane();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/sebphil/renderer/fxml/NoiseWindow.fxml"));

		NoiseController controller = new NoiseController();
		loader.setController(controller);

		try {
			root = loader.load();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Scene scene = new Scene(root);

		stage.setScene(scene);
		stage.setTitle("Noise-Generator");

		return stage;

	}
	
	/*
	 * "Bewegt" die Noise-Funktion eines NoiseObjektes
	 */
	/*
	private void moveNoisesDown() {

		Point3D pos = mainScene.getCamera().getPosition();
		Point3D lookDir = mainScene.getCamera().getLookDir();

		for (RenShape shape : mainScene.getShapes()) {

			if (shape instanceof RenNoise) {

				RenNoise noiseShape = (RenNoise) shape;
				NoiseGenerator2D noise = noiseShape.getNoise();

				if (noiseShape.isDynamic()) {

					ResGrid grid = noiseShape.getGrid();

					pos = new Point3D(pos.getX(), pos.getY(), pos.getZ() - grid.getAmountY() / 2);
					noiseShape.setPosition(RenUtilities.multVecVec(pos, new Point3D(1, 0, 1)));

					if (lookDir.getZ() > 0) {

						noiseShape.setOffsetY(noiseShape.getOffsetY() + 1);
						RenUtilities.shiftArrDown(grid.getGrid(), grid.getAmountX(), grid.getAmountY());

						double yoff = (grid.getAmountY() - 1) + noiseShape.getOffsetY();

						for (int x = 0; x < grid.getAmountX(); x++) {

							double sum = noiseShape.getNoise().realNoise(x, yoff);
							if (sum > noiseShape.getMaxHeight())
								sum = noiseShape.getMaxHeight()
										+ noise.realNoise(x, yoff) / (noise.getAmplitude() * noise.getOctaves() * 5);

							if (sum < noiseShape.getMinHeight())
								sum = noiseShape.getMinHeight()
										- -noise.realNoise(x, yoff) / (noise.getAmplitude() * noise.getOctaves() * 5);

							grid.setVal(x, grid.getAmountY() - 1, sum);

						}

					} else {

						noiseShape.setOffsetY(noiseShape.getOffsetY() - 1);
						RenUtilities.shiftArrUp(grid.getGrid(), grid.getAmountX(), grid.getAmountY());

						double yoff = (grid.getAmountY() - 1) + noiseShape.getOffsetY();
						double y = yoff - grid.getAmountY();

						for (int x = 0; x < grid.getAmountX(); x++) {

							double sum = noiseShape.getNoise().realNoise(x, y);
							if (sum > noiseShape.getMaxHeight())
								sum = noiseShape.getMaxHeight()
										+ noise.realNoise(x, y) / (noise.getAmplitude() * noise.getOctaves() * 5);

							if (sum < noiseShape.getMinHeight())
								sum = noiseShape.getMinHeight()
										- -noise.realNoise(x, y) / (noise.getAmplitude() * noise.getOctaves() * 5);

							grid.setVal(x, 0, sum);

						}

					}

					noiseShape.generatePolyMesh(true);

				}

			}

		}

	}
	*/

}
