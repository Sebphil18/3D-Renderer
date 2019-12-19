package de.sebphil.renderer.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import de.sebphil.renderer.objects.RenShape;
import de.sebphil.renderer.util.RenUtilities;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImpObjController implements Initializable {

	@FXML
	private Button browseButton;

	@FXML
	private TextField pathField;

	@FXML
	private Button importButton;

	@FXML
	private Label errorLab;

	private File importFile;

	private Color errCol = Color.web("#ff6f6f");

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

		errorLab.setText("");

		pathField.textProperty().addListener(l -> {

			importFile = new File(pathField.getText());

			if (!importFile.isFile() || !importFile.exists()) {
				RenUtilities.showErrorMessage("given file path can not be used", errorLab, errCol);
				importFile = null;
				return;
			} else if (!importFile.getName().endsWith(".obj")) {
				RenUtilities.showErrorMessage("file is not a Wavefront obj", errorLab, errCol);
				importFile = null;
				return;
			}

			RenUtilities.showErrorMessage("file is valid", errorLab, Color.LIGHTGREEN);

		});

	}

	@FXML
	public void browseFile() {

		FileChooser chooser = new FileChooser();
		chooser.setTitle("importing .obj file");
		chooser.getExtensionFilters().add(new ExtensionFilter("Wavefront (.obj)", "*.obj"));

		importFile = chooser.showOpenDialog(new Stage());

		if (importFile == null)
			return;

		pathField.setText(importFile.getPath());

	}

	@FXML
	public void importFile() {

		if (importFile == null) {
			RenUtilities.showErrorMessage("please choose a file first", errorLab, errCol);
			return;
		}

		RenShape shape = new RenShape(importFile.getName(), Color.WHITE);
		shape.importObj(importFile);
		MainController.mainScene.getShapes().add(shape);

		Stage stage = (Stage) errorLab.getScene().getWindow();
		stage.close();

	}

}
