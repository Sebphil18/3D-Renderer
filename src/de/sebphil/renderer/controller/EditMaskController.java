package de.sebphil.renderer.controller;

import java.net.URL;
import java.util.ResourceBundle;

import de.sebphil.renderer.util.NoiseGenerator2D;
import de.sebphil.renderer.util.RenUtilities;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

/**
 * Diese Klasse stellt den Kontroller dar, welcher für die Steuerung des Fenster
 * verantworlich ist, welches das Editieren der Maske im Noise-Menu ermöglicht.
 * Somit kann die Maske eines ausgewählten Noise-Objektes leicht bearbeitet werden.
 * Sie wird durch de/sebphil/renderer/fxml/EditMaskWindow.fxml aufgerufen (bzw. ausgeführt).
 */
public class EditMaskController implements Initializable{

	@FXML
    private TextField freqField;

    @FXML
    private TextField amplField;

    @FXML
    private TextField octavesField;

    @FXML
    private TextField freqMultField;

    @FXML
    private TextField amplMultField;
	
	private NoiseGenerator2D maskNoise;
	private NoiseController noiseController;
	
	/**
	 * Diese Methode initialisiert diesen Kontroller.
	 * Dabei werden die Listener für die einzelnen Steuerelemente (Textfelder, Knöpfe, etc.)
	 * initilisiert.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		freqField.textProperty().addListener(l -> {
			
			if(noiseController == null)
				return;
			
			if(RenUtilities.isNumeric(freqField.getText(), true, true)) {
				
				maskNoise.setFrequency(Double.valueOf(freqField.getText()));
				noiseController.fillGrid();
				noiseController.generatePolyMesh();
				
			}
			
		});
		
		amplField.textProperty().addListener(l -> {
			
			if(noiseController == null)
				return;
			
			if(RenUtilities.isNumeric(amplField.getText(), true, true)) {
				
				maskNoise.setAmplitude(Double.valueOf(amplField.getText()));
				noiseController.fillGrid();
				noiseController.generatePolyMesh();
				
			}
			
		});
		
		octavesField.textProperty().addListener(l -> {
			
			if(noiseController == null)
				return;
			
			if(RenUtilities.isNumeric(octavesField.getText(), false, false)) {
				
				maskNoise.setOctaves(Integer.valueOf(octavesField.getText()));
				noiseController.fillGrid();
				noiseController.generatePolyMesh();
				
			}
			
		});
		
		freqMultField.textProperty().addListener(l -> {
			
			if(noiseController == null)
				return;
			
			if(RenUtilities.isNumeric(freqMultField.getText(), true, true)) {
				
				maskNoise.setFreqMult(Double.valueOf(freqMultField.getText()));
				noiseController.fillGrid();
				noiseController.generatePolyMesh();
				
			}
			
		});
		
		amplMultField.textProperty().addListener(l -> {
			
			if(noiseController == null)
				return;
			
			if(RenUtilities.isNumeric(amplMultField.getText(), true, true)) {
				
				maskNoise.setAmplMult(Double.valueOf(amplMultField.getText()));
				noiseController.fillGrid();
				noiseController.generatePolyMesh();
				
			}
			
		});
		
	}
	
	public NoiseGenerator2D getMaskNoise() {
		return maskNoise;
	}
	
	public void setMaskNoise(NoiseGenerator2D maskNoise) {
		this.maskNoise = maskNoise;
		
		freqField.setText(Double.toString(maskNoise.getFrequency()));
		amplField.setText(Double.toString(maskNoise.getAmplitude()));
		octavesField.setText(Integer.toString(maskNoise.getOctaves()));
		freqMultField.setText(Double.toString(maskNoise.getFreqMult()));
		amplMultField.setText(Double.toString(maskNoise.getAmplMult()));
		
	}

	public NoiseController getNoiseController() {
		return noiseController;
	}

	public void setNoiseController(NoiseController noiseController) {
		this.noiseController = noiseController;
	}
	
}
