package de.sebphil.renderer.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.InputMismatchException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.sebphil.renderer.objects.RenObject;
import de.sebphil.renderer.objects.RenScene;
import de.sebphil.renderer.objects.RenShape;
import de.sebphil.renderer.objects.RenTriangle;
import de.sebphil.renderer.uicontrol.RenObjItem;
import de.sebphil.renderer.util.RenUtilities;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImpSceneController implements Initializable {

	@FXML
	private TextField pathField;

	@FXML
	private Button browseButton;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Button importButton;

	@FXML
	private Label errorLab;

	private File file;
	private Color errCol = Color.web("#ff6f6f");

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		errorLab.setText("");
		progressBar.setProgress(0);

		pathField.textProperty().addListener(e -> {

			file = new File(pathField.getText());

			if (!file.exists() || !file.isFile() || !file.getPath().endsWith(".zip")) {
				file = null;
				RenUtilities.showErrorMessage("path is not valid", errorLab, errCol);
				return;
			}

			RenUtilities.showErrorMessage("path is valid", errorLab, Color.LIGHTGREEN);

		});

	}

	@FXML
	public void browse() {

		FileChooser chooser = new FileChooser();

		chooser.getExtensionFilters().add(new ExtensionFilter("ZIP-Archive (.zip)", "*.zip"));

		file = chooser.showOpenDialog(new Stage());

		if (file == null) {
			RenUtilities.showErrorMessage("please choose a file first", errorLab, errCol);
			return;
		}

		pathField.setText(file.getPath());

	}

	@FXML
	public void importScene() {

		RenScene mainScene = MainController.mainScene;

		if (file != null) {

			List<RenObject> objects = getObjects(file);

			for (RenObject renObj : objects) {

				if (renObj instanceof RenShape)
					mainScene.getShapes().add((RenShape) renObj);
				else {
					mainScene.getLights().add(renObj.getPosition());
					MainController.lightItem.getChildren()
							.add(new TreeItem<RenObjItem>(new RenObjItem(renObj.getName(), renObj)));
				}

			}

		}

		Stage stage = (Stage) errorLab.getScene().getWindow();
		stage.close();

	}

	public List<RenObject> getObjects(File file) {

		progressBar.setProgress(-1);

		List<RenObject> shapes = new ArrayList<RenObject>();

		try {

			ZipFile zipFile = new ZipFile(file);

			final Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {

				ZipEntry entry = entries.nextElement();

				Scanner scanner = new Scanner(zipFile.getInputStream(entry));

				if (entry.getName().endsWith(".shp")) {

					RenShape shape = new RenShape(entry.getName());

					Color color = null;

					try {

						shape.setPosition(
								new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble()));
						shape.setTranslation(
								new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble()));
						shape.setSize(new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble()));

						shape.setAngleX(scanner.nextDouble());
						shape.setAngleY(scanner.nextDouble());
						shape.setAngleZ(scanner.nextDouble());

						color = Color.valueOf(scanner.next());

					} catch (InputMismatchException e) {
						RenUtilities.showErrorMessage(entry.getName() + " is corrupted or has an error", errorLab,
								errCol);
						e.printStackTrace();
						continue;
					}

					double maxX;
					double maxY;
					double maxZ;

					double minX;
					double minY;
					double minZ;

					while (scanner.hasNextDouble()) {

						maxX = shape.getMaxX();
						maxY = shape.getMaxY();
						maxZ = shape.getMaxZ();

						minX = shape.getMinX();
						minY = shape.getMinY();
						minZ = shape.getMinZ();

						try {

							Point3D v1 = new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
							Point3D v2 = new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
							Point3D v3 = new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());

							shape.getPolys().add(new RenTriangle(v1, v2, v3));

							for (Point3D v : shape.getPolys().get(shape.getPolys().size() - 1).getVert()) {

								if (v.getX() > maxX)
									shape.setMaxX(v.getX());
								else if (v.getX() < minX)
									shape.setMinX(v.getX());

								if (v.getY() > maxY)
									shape.setMaxY(v.getY());
								else if (v.getY() < minY)
									shape.setMinY(v.getY());

								if (v.getZ() > maxZ)
									shape.setMaxZ(v.getZ());
								else if (v.getZ() < minZ)
									shape.setMinZ(v.getZ());

							}

						} catch (InputMismatchException e) {
							RenUtilities.showErrorMessage(entry.getName() + " is corrupted or has an error", errorLab,
									errCol);
							e.printStackTrace();
							continue;
						}

					}

					shape.setColor(color);

					shapes.add(shape);

				} else if (entry.getName().endsWith(".ligh")) {

					while (scanner.hasNextDouble()) {

						RenObject renObj = new RenObject(entry.getName());
						renObj.setName("light");

						try {

							renObj.setPosition(
									new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble()));

						} catch (InputMismatchException e) {
							RenUtilities.showErrorMessage(entry.getName() + " is corrupted or has an error", errorLab,
									errCol);
							e.printStackTrace();
							continue;
						}

						shapes.add(renObj);

					}

				}

				scanner.close();

			}

			zipFile.close();

		} catch (IOException e) {
			e.printStackTrace();
			RenUtilities.showErrorMessage("could not read from zip-archive", errorLab, errCol);
			progressBar.setProgress(0);
		}

		progressBar.setProgress(0);

		return shapes;

	}

}
