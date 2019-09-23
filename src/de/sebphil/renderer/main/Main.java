package de.sebphil.renderer.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage arg0) throws Exception {

		BorderPane root = new BorderPane();
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/sebphil/renderer/fxml/MainWindow.fxml"));
		
		root = loader.load();
		
		Scene scene = new Scene(root);
		Stage primaryStage = new Stage();
		primaryStage.setScene(scene);
		primaryStage.setTitle("3D Renderer");
		primaryStage.show();

	}

}
