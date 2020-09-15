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
 * Diese Klasse wird f�r die Steuerung der Benutzeroberfl�che verwendet.
 * Sie steuert das Fenster, welches das Importieren von einfachen OBJ-Dateien erm�glicht.
 * Sie wird durch de/sebphil/renderer/fxml/ImpObjWindow.fxml aufgerufen (bzw. ausgef�hrt).
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
	 * Dabei werden die Listener f�r die Steuerelemente (Textfelder, Kn�pfe, etc.)
	 * initilisiert.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub

		errorLab.setText("");
		
		// Listener f�r das Textfeld f�r den Dateipfad, um zu �berpr�fen, ob eingegebener Dateipfad valide ist.
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
	 * �ffnet den Filebrowser, um den Speicherort der zu importierenden Datei zu bestimmen.
	 * Wenn keine Datei ausgew�hlt wird, bricht die Funktion ab.
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
	 * Importiert die ausgew�hlte Datei.
	 * Wenn keine Datei ausgew�hlt wurde, wird die Funktion abbrechen.
	 * Diese Funktion wird das aktuelle Fenster, welches f�r das Importieren einer Figur 
	 * zust�ndig ist schlie�en
	 */
	@FXML
	public void importFile() {

		if (importFile == null) {
			RenUtilities.showErrorMessage("please choose a file first", errorLab, errCol);
			return;
		}
		
		/*
		 * Importiere die .shp Datei und f�ge die entstehende Figur 
		 * zu der im MainController ausgew�hlten Szene hinzu.
		 */
		RenShape shape = new RenShape(importFile.getName(), Color.WHITE);
		shape.importObj(importFile);
		MainController.mainScene.getShapes().add(shape);

		Stage stage = (Stage) errorLab.getScene().getWindow();
		stage.close();

	}

}
