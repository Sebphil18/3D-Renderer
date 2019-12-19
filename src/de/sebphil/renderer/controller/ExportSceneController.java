package de.sebphil.renderer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.sebphil.renderer.objects.RenScene;
import de.sebphil.renderer.objects.RenShape;
import de.sebphil.renderer.objects.RenTriangle;
import de.sebphil.renderer.util.RenUtilities;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ExportSceneController implements Initializable {

	@FXML
	private TextField pathField;

	@FXML
	private Button browseButton;

	@FXML
	private Button exportButton;

	@FXML
	private Label errorLab;

	@FXML
	private TextField nameField;

	@FXML
	private ProgressBar progressBar;

	private String name;
	private File dir;
	private Color errCol = Color.web("#ff6f6f");

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		name = "scene";

		errorLab.setText("");
		progressBar.setProgress(0);

		pathField.textProperty().addListener(l -> {

			dir = new File(pathField.getText());

			if (!dir.isDirectory() || !dir.exists()) {
				dir = null;
				RenUtilities.showErrorMessage("please select a directory", errorLab, errCol);
				return;
			}

			RenUtilities.showErrorMessage("directory is valid", errorLab, Color.LIGHTGREEN);

		});

		nameField.textProperty().addListener(l -> {

			for (char c : nameField.getText().toCharArray()) {

				if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
					RenUtilities.showErrorMessage("special letters are not allowed", errorLab, errCol);
					return;
				}

				name = nameField.getText();

			}

		});

	}

	@FXML
	public void browseFile() {

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("importing .obj file");

		dir = chooser.showDialog(errorLab.getScene().getWindow());

		if (dir == null)
			return;

		pathField.setText(dir.getPath());

	}

	@FXML
	public void exportFile() {

		if (dir != null) {

			String path = dir.getPath() + "\\" + name + ".zip";

			List<File> files = createFileScene(dir.getPath() + "\\");

			zipFiles(files, path);

			RenUtilities.showErrorMessage("exported scene", errorLab, Color.LIGHTGREEN);

			Stage stage = (Stage) errorLab.getScene().getWindow();
			stage.close();

		}

	}

	private List<File> createFileScene(String path) {

		RenScene scene = MainController.mainScene;

		List<File> files = new ArrayList<File>(scene.getShapes().size() + 1);

		double progObj = 1.0 / (scene.getShapes().size() + scene.getLights().size());

		for (RenShape shape : scene.getShapes()) {

			Point3D pos = shape.getPosition();
			Point3D trans = shape.getTranslation();
			Point3D size = shape.getSize();
			double rotX = shape.getAngleX();
			double rotY = shape.getAngleY();
			double rotZ = shape.getAngleZ();
			String color = shape.getColor().toString();

			StringBuilder properties = new StringBuilder();
			properties.append(String.format("%.4f ", pos.getX()));
			properties.append(String.format("%.4f ", pos.getY()));
			properties.append(String.format("%.4f ", pos.getZ()));

			properties.append(String.format("%.4f ", trans.getX()));
			properties.append(String.format("%.4f ", trans.getY()));
			properties.append(String.format("%.4f ", trans.getZ()));

			properties.append(String.format("%.4f ", size.getX()));
			properties.append(String.format("%.4f ", size.getY()));
			properties.append(String.format("%.4f ", size.getZ()));

			properties.append(String.format("%.4f ", rotX));
			properties.append(String.format("%.4f ", rotY));
			properties.append(String.format("%.4f ", rotZ));

			properties.append(color.toString());

			File file = new File(path + shape.getName() + ".shp");

			if (file.exists()) {

				cleanUp(files);

				RenUtilities.showErrorMessage("file " + file.getName() + " already exists", errorLab, errCol);

				break;
			}

			try {
				PrintWriter writer = new PrintWriter(file);
				writer.println(properties.toString());

				double progShape = progObj / shape.getPolys().size();

				for (RenTriangle tri : shape.getPolys()) {

					Point3D[] verts = tri.getVert();

					StringBuilder vertProp = new StringBuilder();
					for (int i = 0; i < 3; i++) {

						vertProp.append(String.format("%.4f ", verts[i].getX()));
						vertProp.append(String.format("%.4f ", verts[i].getY()));
						vertProp.append(String.format("%.4f ", verts[i].getZ()));

					}
					writer.println(vertProp.toString());

					progressBar.setProgress(progressBar.getProgress() + progShape);
				}

				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				cleanUp(files);
				RenUtilities.showErrorMessage("error while writing to " + file.getName(), errorLab, errCol);
			}

			files.add(file);

		}

		File file = new File(path + "lights.ligh");

		try {

			PrintWriter writer = new PrintWriter(file);

			for (Point3D light : scene.getLights())
				writer.println(String.format("%.4f %.4f %.4f", light.getX(), light.getY(), light.getZ()));

			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			cleanUp(files);
			RenUtilities.showErrorMessage("error while writing to " + file.getName(), errorLab, errCol);
		}

		files.add(file);

		progressBar.setProgress(1);

		return files;

	}

	private void zipFiles(List<File> files, String path) {

		progressBar.setProgress(-1);

		File zipFile = new File(path);

		byte[] buffer = new byte[1024];
		int length;

		try {

			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));

			for (File file : files) {

				FileInputStream fileIn = new FileInputStream(file);

				zipOut.putNextEntry(new ZipEntry(file.getName()));

				while ((length = fileIn.read(buffer)) != -1)
					zipOut.write(buffer, 0, length);

				zipOut.closeEntry();
				fileIn.close();
				file.delete();

			}

			zipOut.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (zipFile.exists())
				zipFile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (zipFile.exists())
				zipFile.delete();
		}

		progressBar.setProgress(0);

	}

	private void cleanUp(List<File> files) {

		progressBar.setProgress(-1);

		for (File file : files) {

			if (file.exists())
				file.delete();

		}

		progressBar.setProgress(0);

	}

}
