package de.sebphil.renderer.controller;

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

/**
 * Diese Klasse wird f�r die Steuerung der Benutzeroberfl�che verwendet.
 * Sie steuert das Fenster, welches das Importieren von Szenen erm�glicht.
 * Sie wird durch de/sebphil/renderer/fxml/ImpSceneWindow.fxml aufgerufen (bzw. ausgef�hrt).
 */

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

	/**
	 * Initialisiert diesen Kontroller.
	 * Dabei werden die Listener f�r die Steuerelemente (Textfelder, Kn�pfe, etc.)
	 * initilisiert.
	 */
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
	
	/**
	 * �ffnet den Filebrowser, um den Speicherort der zu importierenden Datei zu bestimmen.
	 * Sollte kein Speicherort ausgew�hlt werden, wird die Funktion abbrechen.
	 */
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

	/**
	 * Importiert die ausgew�hlte Szene.
	 * Es wird dabei keine neue Szene erstellt, sondern der aktuellen
	 * Szene, welche im MainController ausgew�hlt ist, angef�gt.
	 * Diese Funktion schlie�t zudem das aktuelle Fenster, welches f�r das Importieren
	 * einer Szene zust�ndig ist.
	 */
	@FXML
	public void importScene() {

		RenScene mainScene = MainController.mainScene;

		if (file != null) {
			
			// Liste der in der Szene enthaltenen Objekte
			List<RenObject> objects = getObjects(file);

			// F�ge die einzelnen Objekte der Szene hinzu.
			for (RenObject renObj : objects) {
				
				// Figuren werden in die Liste 'shapes' hinzugef�gt.
				if (renObj instanceof RenShape)
					mainScene.getShapes().add((RenShape) renObj);
				else {
					// Lichter werden in die Liste 'lights' hinzugef�gt.
					mainScene.getLights().add(renObj.getPosition());
					MainController.lightItem.getChildren()
							.add(new TreeItem<RenObjItem>(new RenObjItem(renObj.getName(), renObj)));
				}

			}

		}

		Stage stage = (Stage) errorLab.getScene().getWindow();
		stage.close();

	}

	/**
	 * Diese Funktion soll die einzelnen Objekte einer Szene auslesen und zur�ckgeben.
	 * 
	 * @param file Dateipfad zu der Datei, welche als Szene importiert werden soll (Datei muss ein ZIP-Archiv sein)
	 * @return Gibt eine Liste aller Objekte der ausgew�hlten Szene zur�ck
	 */
	public List<RenObject> getObjects(File file) {

		progressBar.setProgress(-1);
		
		// Erstelle eine Liste f�r alle Objekte, welche in der ausgew�hlten Szene enthalten sind.
		List<RenObject> objects = new ArrayList<RenObject>();

		try {
			
			ZipFile zipFile = new ZipFile(file);

			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			
			// Gehe durch alle Eintr�ge des ZIP-Archivs
			while (entries.hasMoreElements()) {

				ZipEntry entry = entries.nextElement();

				Scanner scanner = new Scanner(zipFile.getInputStream(entry));
				
				// Wenn es sich bei dem aktuellen Eintrag, um eine Datei f�r eine Figur handelt
				if (entry.getName().endsWith(".shp")) {

					RenShape shape = new RenShape(entry.getName());

					Color color = null;
					
					/*
					 * Ermitteln der Eigenschaften der aktuellen Figur
					 * (Position, Gr��e, Rotation und Farbe)
					 */
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
					
					/*
					 * Ermittle die Dreiecke, welche in der Datei enthalten sind 
					 * und f�ge diese der aktuellen Figur hinzu.
					 */
					while (scanner.hasNextDouble()) {

						maxX = shape.getMaxX();
						maxY = shape.getMaxY();
						maxZ = shape.getMaxZ();

						minX = shape.getMinX();
						minY = shape.getMinY();
						minZ = shape.getMinZ();

						try {
							
							// Ermittle die Koordianten der Eckpunkte des aktuellen Dreiecks.
							Point3D v1 = new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
							Point3D v2 = new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
							Point3D v3 = new Point3D(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
							
							// F�ge das aktuelle Dreieck zu der aktuellen Figur hinzu.
							shape.getPolys().add(new RenTriangle(v1, v2, v3));
							
							// Passe die Grenzwerte f�r die Figur geg. Falls an.
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
					
					// aktuelle Figur zu der Liste f�r die Objekte hinzuf�gen.
					objects.add(shape);

				} else if (entry.getName().endsWith(".ligh")) {
					// Wenn es sich bei dem aktuellen Eintrag um eine Lichtquelle handelt.
					
					// Ermittle alle Lichtquellen und trage diese in die Liste f�r alle Objekte ein.
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

						objects.add(renObj);

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

		return objects;

	}

}
