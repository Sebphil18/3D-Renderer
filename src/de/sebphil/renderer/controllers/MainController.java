package de.sebphil.renderer.controllers;

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
import de.sebphil.renderer.objects.RenTriangle;
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

	private static SebRenderer mainRenderer;
	private static PixelWriter mainWriter;
	public static RenScene mainScene;
	public static TreeItem<RenObjItem> lightItem;

	@Override
	public void initialize(URL url, ResourceBundle bundle) {

		Canvas canvas = new Canvas();
		GraphicsContext gc = canvas.getGraphicsContext2D();

		SimpleDoubleProperty widthProp = new SimpleDoubleProperty();
		SimpleDoubleProperty heightProp = new SimpleDoubleProperty();
		SimpleDoubleProperty fovProp = new SimpleDoubleProperty();
		SimpleDoubleProperty aspProp = new SimpleDoubleProperty();

		TreeItem<RenObjItem> rootItem = new TreeItem<RenObjItem>(new RenObjItem("3DObjects"));
		TreeItem<RenObjItem> shapesItem = new TreeItem<RenObjItem>(new RenObjItem("Shapes"));
		lightItem = new TreeItem<RenObjItem>(new RenObjItem("Lights"));

		rootItem.getChildren().add(lightItem);
		rootItem.getChildren().add(shapesItem);

		treeView.setCellFactory(f -> new CustomTreeCell());
		treeView.setRoot(rootItem);

		canvasPane.getChildren().add(canvas);

		mainWriter = gc.getPixelWriter();
		mainRenderer = new SebRenderer(canvas.getWidth(), canvas.getHeight());
		mainScene = new RenScene();

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

		nearField.textProperty().addListener(l -> {

			if (RenUtilities.isNumeric(nearField.getText(), true, true)) {

				mainRenderer.setNear(Double.valueOf(nearField.getText()));
				render(mainRenderer, mainScene, mainWriter);

			} else
				nearField.setText(Double.toString(mainRenderer.getNear()));

		});

		farField.textProperty().addListener(l -> {

			if (RenUtilities.isNumeric(farField.getText(), true, true)) {

				mainRenderer.setFar(Double.valueOf(farField.getText()));
				render(mainRenderer, mainScene, mainWriter);

			} else
				farField.setText(Double.toString(mainRenderer.getFar()));

		});

		fovField.textProperty().addListener(l -> {

			if (RenUtilities.isNumeric(fovField.getText(), true, true)) {

				double fov = Double.valueOf(fovField.getText()) % 360;

				if (fov >= 180)
					fov = 179;
				else if (fov <= 0)
					fov = 1;

				fovProp.set(fov);

			} else
				fovField.setText(Double.toString(fovProp.doubleValue()));

		});

		widthProp.set(mainRenderer.getWidth());
		heightProp.set(mainRenderer.getHeight());
		fovProp.set(mainRenderer.getFov());
		aspProp.set(mainRenderer.getAspectratio());

		nearField.setText(Double.toString(mainRenderer.getNear()));
		farField.setText(Double.toString(mainRenderer.getFar()));

		RenCamera cam = mainScene.getCamera();

		cam.setPosition(new Point3D(0, 0, -3));

		// CONTROLS & GUI
		KeyCombination shiftA = new KeyCodeCombination(KeyCode.A, KeyCombination.SHIFT_DOWN);
		KeyCombination shiftD = new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN);

		rootPane.setOnKeyPressed(e -> {

			if (shiftA.match(e)) {

				Point3D right = cam.getNewRight().multiply(0.1);
				cam.setPosition(cam.getPosition().subtract(right));
				render(mainRenderer, mainScene, mainWriter);
				return;

			} else if (shiftD.match(e)) {

				Point3D right = cam.getNewRight().multiply(0.1);
				cam.setPosition(cam.getPosition().add(right));
				render(mainRenderer, mainScene, mainWriter);
				return;

			}

			if (e.getCode().equals(KeyCode.W)) {

				Point3D forward = cam.getLookDir().multiply(0.1);
				cam.setPosition(cam.getPosition().add(forward));

			} else if (e.getCode().equals(KeyCode.S)) {

				Point3D forward = cam.getLookDir().multiply(-0.1);
				cam.setPosition(cam.getPosition().add(forward));

			} else if (e.getCode().equals(KeyCode.A)) {
				cam.setYaw(cam.getYaw() + 1);
			} else if (e.getCode().equals(KeyCode.D)) {
				cam.setYaw(cam.getYaw() - 1);
			}

			render(mainRenderer, mainScene, mainWriter);

		});

		rootPane.setOnScroll(e -> {

			if (e.isAltDown()) {

				double fov = fovProp.doubleValue();

				if (e.getDeltaY() > 0) {
					fovProp.set(fov - 1);
				} else {
					fovProp.set(fov + 1);
				}

			} else if (e.getDeltaY() > 0) {

				Point3D up = cam.getNewUp().multiply(0.1);
				cam.setPosition(cam.getPosition().subtract(up));
				render(mainRenderer, mainScene, mainWriter);

			} else if (e.getDeltaY() < 0) {

				Point3D up = cam.getNewUp().multiply(0.1);
				cam.setPosition(cam.getPosition().add(up));
				render(mainRenderer, mainScene, mainWriter);

			}

		});

		canvasPane.setOnMouseClicked(e -> {
			rootPane.requestFocus();
		});

		// Listener for shapes
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

		// (RESIZE RENDERER)
		canvasPane.widthProperty().addListener(l -> {

			canvas.setWidth(canvasPane.getWidth());
			widthProp.set(canvasPane.getWidth());
			aspProp.set(mainRenderer.getAspectratio());

		});

		canvasPane.heightProperty().addListener(l -> {

			canvas.setHeight(canvasPane.getHeight());
			heightProp.set(canvasPane.getHeight());
			aspProp.set(mainRenderer.getAspectratio());

		});

		// (Treeview)
		treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<RenObjItem>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<RenObjItem>> arg0, TreeItem<RenObjItem> arg1,
					TreeItem<RenObjItem> arg2) {

				optionsBox.getChildren().clear();

				if (arg2 == null)
					return;
				if (arg2.getValue().getRenObj() == null)
					return;

				RenObject selectedObj = arg2.getValue().getRenObj();

				if (arg2.getParent() != null) {
					if (arg2.getParent().equals(shapesItem)) {

						if (selectedObj instanceof RenNoise) {
							loadNoiseOptions((RenNoise) selectedObj, optionsBox);
						} else {
							loadShapeOptions((RenShape) selectedObj, optionsBox, mainWriter);
						}

					} else if (arg2.getParent().equals(lightItem)) {
						loadLightOptions(selectedObj, optionsBox);
					}
				}

			}

		});

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

	@FXML
	public void importObj() {

		openImpObjWin().show();

	}

	@FXML
	public void exportScene() {

		openExpSceneWin().show();

	}

	@FXML
	public void importScene() {

		openImpSceneWin().show();

	}

	@FXML
	public void createNoise() {

		openNoiseWin().show();

	}

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

		generatePositionListener(0, posFields, shape, writer);

		posFields[0].textProperty().addListener(generatePositionListener(0, posFields, shape, writer));
		posFields[1].textProperty().addListener(generatePositionListener(1, posFields, shape, writer));
		posFields[2].textProperty().addListener(generatePositionListener(2, posFields, shape, writer));

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

		transFields[0].textProperty().addListener(generateTransListener(0, transFields, shape, writer));
		transFields[1].textProperty().addListener(generateTransListener(1, transFields, shape, writer));
		transFields[2].textProperty().addListener(generateTransListener(2, transFields, shape, writer));

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

		rotFields[0].textProperty().addListener(generateRotListener(0, rotFields, shape, writer));
		rotFields[1].textProperty().addListener(generateRotListener(1, rotFields, shape, writer));
		rotFields[2].textProperty().addListener(generateRotListener(2, rotFields, shape, writer));

		// Size
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

		sizeFields[0].textProperty().addListener(generateSizeListener(0, sizeFields, shape, writer));
		sizeFields[1].textProperty().addListener(generateSizeListener(1, sizeFields, shape, writer));
		sizeFields[2].textProperty().addListener(generateSizeListener(2, sizeFields, shape, writer));

		// Color
		Label title5 = new Label("Color");
		ColorPicker colPicker = new ColorPicker(shape.getColor());

		title5.setId("text1");
		title5.setMaxWidth(150);

		// Preview
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

		preCam.setPosition(new Point3D(0, 0, -dZ));
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
			for(RenTriangle tri : shape.getPolys()) {
				tri.setColor(colPicker.getValue());
			}
			render(preRenderer, preScene, preWriter);
			render(mainRenderer, mainScene, writer);
		});

		// Controls & GUI - Preview

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

	private void loadNoiseOptions(RenNoise noise, VBox optionsBox) {

		loadShapeOptions(noise, optionsBox, mainWriter);

		VBox contentBox = (VBox) optionsBox.getChildren().get(0);

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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Scene scene = new Scene(root);

			stage.setScene(scene);
			stage.setTitle("Noise-Generator");

			stage.show();

		});

		contentBox.getChildren().add(editNoiseButton);

	}

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

		fields[0].textProperty().addListener(generatePositionListener(0, fields, renObj, mainWriter));
		fields[1].textProperty().addListener(generatePositionListener(1, fields, renObj, mainWriter));
		fields[2].textProperty().addListener(generatePositionListener(2, fields, renObj, mainWriter));

		lightBox.setPadding(new Insets(5, 6, 5, 5));
		lightBox.setMinWidth(200);
		lightBox.setSpacing(5);
		lightBox.setAlignment(Pos.CENTER_LEFT);

		lightBox.getChildren().addAll(namePane, seps[0], title1, posPane);
		optionsBox.getChildren().add(lightBox);

	}

	private void openMenu(double x, double y, TreeItem<RenObjItem> item, TreeItem<RenObjItem> objectsItem) {

		ContextMenu menu = new ContextMenu();

		menu.setX(x);
		menu.setY(y);

		if (item == null || item.getParent() == null) {

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

		} else {

			TreeItem<RenObjItem> parent = item.getParent();

			if (parent.equals(objectsItem) || item.equals(objectsItem)) {

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

			} else if (parent.equals(lightItem) || item.equals(lightItem)) {

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

	private ChangeListener<String> generatePositionListener(int index, TextField[] fields, RenObject renObj,
			PixelWriter writer) {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// arg2 = new

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

				} else {

					if (index == 0)
						fields[index].setText(renObj.getPosition().getX() + "");
					if (index == 1)
						fields[index].setText(renObj.getPosition().getY() + "");
					if (index == 2)
						fields[index].setText(renObj.getPosition().getZ() + "");
				}
			}
		};
	}

	private ChangeListener<String> generateTransListener(int index, TextField[] fields, RenShape shape,
			PixelWriter writer) {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// arg2 = new

				if (RenUtilities.isNumeric(arg2, true, true)) {
					shape.setTranslation(new Point3D(Double.valueOf(fields[0].getText()),
							Double.valueOf(fields[1].getText()), Double.valueOf(fields[2].getText())));
					render(mainRenderer, mainScene, writer);
				} else {
					if (index == 0)
						fields[index].setText(shape.getTranslation().getX() + "");
					if (index == 1)
						fields[index].setText(shape.getTranslation().getY() + "");
					if (index == 2)
						fields[index].setText(shape.getTranslation().getZ() + "");
				}
			}
		};
	}

	private ChangeListener<String> generateRotListener(int index, TextField[] fields, RenObject renObj,
			PixelWriter writer) {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// arg2 = new

				if (RenUtilities.isNumeric(arg2, true, true)) {
					renObj.setAngleX(Double.valueOf(fields[0].getText()));
					renObj.setAngleY(Double.valueOf(fields[1].getText()));
					renObj.setAngleZ(Double.valueOf(fields[2].getText()));
					render(mainRenderer, mainScene, writer);
				} else {
					if (index == 0)
						fields[index].setText(renObj.getAngleX() + "");
					if (index == 1)
						fields[index].setText(renObj.getAngleY() + "");
					if (index == 2)
						fields[index].setText(renObj.getAngleZ() + "");
				}
			}
		};
	}

	private ChangeListener<String> generateSizeListener(int index, TextField[] fields, RenShape shape,
			PixelWriter writer) {
		return new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				// arg2 = new

				if (RenUtilities.isNumeric(arg2, true, true)) {
					shape.setSize(new Point3D(Double.valueOf(fields[0].getText()), Double.valueOf(fields[1].getText()),
							Double.valueOf(fields[2].getText())));
					render(mainRenderer, mainScene, writer);
				} else {
					if (index == 0)
						fields[index].setText(shape.getSize().getX() + "");
					if (index == 1)
						fields[index].setText(shape.getSize().getY() + "");
					if (index == 2)
						fields[index].setText(shape.getSize().getZ() + "");
				}
			}
		};
	}

	private void alignmentGridPane(GridPane gridpane) {

		for (Node node : gridpane.getChildren())
			GridPane.setHalignment(node, HPos.CENTER);

	}

	private void fillValues(Label[] labs, TextField[] fields, GridPane valuePane) {

		for (int i = 0; i < fields.length; i++) {
			valuePane.add(labs[i], i, 0);
			valuePane.add(fields[i], i, 1);
		}

		alignmentGridPane(valuePane);

	}

	private Label[] generateInfoLabs(String[] args) {

		Label[] labs = new Label[args.length];

		for (int i = 0; i < labs.length; i++) {
			labs[i] = new Label(args[i]);
			labs[i].setId("text3");
		}

		return labs;
	}

	private TextField[] generateNumFields(String[] args) {

		TextField[] fields = new TextField[args.length];

		for (int i = 0; i < fields.length; i++) {
			fields[i] = new TextField(args[i]);
			fields[i].setPromptText("num");
			fields[i].setId("numField");
		}

		return fields;
	}

	private Separator[] generateSeperators(int num) {

		Separator[] seps = new Separator[num];

		for (int i = 0; i < num; i++) {
			seps[i] = new Separator();
			seps[i].setPrefHeight(15);
		}

		return seps;
	}

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

	private double render(SebRenderer renderer, RenScene scene, PixelWriter writer) {

		long start = System.currentTimeMillis();
		renderer.update(scene);
		renderer.draw(writer);
		long stop = System.currentTimeMillis();

		if ((stop - start) / 1000 == 0) {
			return Double.POSITIVE_INFINITY;
		}

		return 1 / ((double) (stop - start) / 1000);
	}

	public static void renderMain() {
		mainRenderer.update(mainScene);
		mainRenderer.draw(mainWriter);
	}

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

		stage.setOnCloseRequest(e -> {
			render(mainRenderer, mainScene, mainWriter);
		});

		return stage;

	}

}
