package de.sebphil.renderer.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Diese Klasse binhaltet die Hauptfunktion
 * 
 * @author Sebastian Schulz
 *
 */
public class Main extends Application {

	public static void main(String[] args) {
		
		//startet die JavaFX-Laufzeitumgebung & das JavaFX-Programm
		launch(args);
	}

	/**
	 * Funktion, welche beim Start der JavaFX-Anwendung ausgeführt wird.
	 * In diesem Falle erstellt die Funktion das Hauptfenster und lädt die dafür zuständigen Ressourcen:
	 * <p>
	 * <ul>
	 * <li>	de.sebphil.renderer.fxml.MainWindow.fxml
	 * <li>	de.sebphil.renderer.pics/SAvatar.png
	 * </ul>
	 */
	@Override
	public void start(Stage arg0) throws Exception {

		BorderPane root = new BorderPane();
		
		//	Lädt die FXML-Datei MainWindow.fxml, welche den strukturellen Aufbau des Hauptfensters bestimmt.
		//	Zudem ruft diese Datei den Hauptkontroller (MainController) auf.
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/sebphil/renderer/fxml/MainWindow.fxml"));
		root = loader.load();
		
		//Erstellt das eigentliche Fenster mit einer neuen Szene.
		Scene scene = new Scene(root);
		Stage primaryStage = new Stage();
		primaryStage.setScene(scene);
		primaryStage.setTitle("3D Renderer");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/de/sebphil/renderer/pics/SAvatar.png")));
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(e -> {
			Platform.exit();
		});
		
	}

}
