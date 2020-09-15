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

/**
 * Diese Klasse wird für die Steuerung der Benutzeroberfläche verwendet.
 * Sie steuert das Fenster, welches das Importieren von einfachen OBJ-Dateien ermöglicht.
 * Sie wird durch de/sebphil/renderer/fxml/ImpObjWindow.fxml aufgerufen (bzw. ausgeführt).
 */

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

	/**
	 * Initialisiert diesen Kontroller.
	 * Dabei werden die Listener für die Steuerelemente (Textfelder, Knöpfe, etc.)
	 * initilisiert.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

		errorLab.setText("");
		
		// Listener für das Textfeld für den Dateipfad, um zu überprüfen, ob eingegebener Dateipfad valide ist.
		pathField.textProperty().addListener(l -> {

			importFile = new File(pathField.getText());
			
			// Existiert Datei?
			if (!importFile.isFile() || !importFile.exists()) {
				
				// Wenn nicht, gebe eine Fehlermeldung aus und breche die Funktion ab
				RenUtilities.showErrorMessage("given file path can not be used", errorLab, errCol);
				importFile = null;
				return;
				
			} else if (!importFile.getName().endsWith(".obj")) {
				
				// Wenn Datei nicht das richtige Format hat, gebe eine Fehlermelund aus und breche die Funktion ab
				RenUtilities.showErrorMessage("file is not a Wavefront obj", errorLab, errCol);
				importFile = null;
				return;
				
			}
			
			// Datei kann verwendet werden
			RenUtilities.showErrorMessage("file is valid", errorLab, Color.LIGHTGREEN);

		});

	}
	
	/**
	 * Öffnet den Filebrowser, um den Speicherort der zu importierenden Datei zu bestimmen.
	 * Wenn keine Datei ausgewählt wird, bricht die Funktion ab.
	 */
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
	
	/**
	 * Importiert die ausgewählte Datei.
	 * Wenn keine Datei ausgewählt wurde, wird die Funktion abbrechen.
	 * Diese Funktion wird das aktuelle Fenster, welches für das Importieren einer Figur 
	 * zuständig ist schließen
	 */
	@FXML
	public void importFile() {

		if (importFile == null) {
			RenUtilities.showErrorMessage("please choose a file first", errorLab, errCol);
			return;
		}
		
		/*
		 * Importiere die .shp Datei und füge die entstehende Figur 
		 * zu der im MainController ausgewählten Szene hinzu.
		 */
		RenShape shape = new RenShape(importFile.getName(), Color.WHITE);
		shape.importObj(importFile);
		MainController.mainScene.getShapes().add(shape);

		Stage stage = (Stage) errorLab.getScene().getWindow();
		stage.close();

	}

}
