package de.sebphil.renderer.util;

import java.util.Random;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class NoiseGenerator2D {

	private double frequency, amplitude, freqMult, amplMult;
	private int tableLength, octaves;
	private int[] permTable;
	private Point2D[] grad;
	private Random ran;

	/**
	 * Constructor für einen NoiseGenerator2D.
	 * 
	 * Diese Klasse stellt einen Noise-Generator dar, welcher geeignete Werte mithilfe
	 * von einer zweidimensionalen PerlinNoise Funktion erzeugt.
	 * Alle nötigen Parameter werden mit gültigen werden belegt.
	 * 
	 * @param seed Seed der Noise-Funktion
	 */
	public NoiseGenerator2D(long seed) {

		frequency = 0.2;
		amplitude = 1;
		freqMult = 1;
		amplMult = 1;
		octaves = 1;
		tableLength = 256;
		permTable = new int[tableLength * 2];
		grad = new Point2D[tableLength];

		ran = new Random(seed);

		for (int i = 0; i < tableLength; i++) {
			permTable[i] = i;
			grad[i] = new Point2D(ran.nextDouble() * 2 - 1, ran.nextDouble() * 2 - 1);
		}

		for (int i = 0; i < tableLength; ++i) {
			permTable[i] = permTable[ran.nextInt(255) % (tableLength - 1)];
			permTable[i + tableLength] = permTable[i];
		}

	}
	
	/**
	 * Errechnet den summierten Wert der Noise-Funktion an der 
	 * ensprechenden Stelle.
	 * 
	 * @param x x-Koordinate
	 * @param y y-Koordiante
	 * @return Gibt den Wer Noise-Funktion an der angegebenen Stelle zurück
	 */
	public double realNoise(double x, double y) {

		if (frequency < 0)
			return 0;

		int layers = this.octaves;

		double frequency = this.frequency;
		double amplitude = this.amplitude;
		double freqMult = this.freqMult;
		double amplMult = this.amplMult;

		double sum = 0;

		for (int i = 0; i < layers; i++) {

			sum += noise(x * frequency, y * frequency) * amplitude;
			frequency *= freqMult;
			amplitude *= amplMult;

		} 
		
		return sum;
	}

	/**
	 * Errechnet ein Grayscale-Wert für die gegebene Summe.
	 * 
	 * @param sum Summe von dieser Noise-Funktion
	 * @return returns Grayscale-Wert für eine Summe dieser Noise-Funktion
	 */
	public double getNoiseGray(double sum) {
		
		double grayscale = map(-1, 1, sum/octaves, 0, 1);
		
		if(grayscale < 0) grayscale = 0;
		else if (grayscale > 1) grayscale = 1;
		
		return grayscale;
	}

	/**
	 * Errechnet Farbe (RGB) für die gegebene Summe.
	 * 
	 * @param sum Summe von dieser Noise-Funktion
	 * @return returns Farbe des gegebenen Noise-Wertes (sum)
	 */
	public Color getNoiseRGB(double sum) {

		Color color = Color.DARKBLUE;

		double value = getLevel(sum);
		
		if (value <= 0.3) {
			color = Color.web("#0a0075").interpolate(Color.web("#00b4eb"), value / 0.5);
		} else if (value <= 0.5) {
			color = Color.web("#26bf00").interpolate(Color.web("#b2ed02"), value / 0.1);
		} else if (value <= 1) {
			color = Color.web("#b2ed02").interpolate(Color.web("#e1eb8d"), value / 1);
		} else if (value <= 1.4){
			color = Color.web("#e1eb8d").interpolate(Color.web("#cf7800"), value / 1);
		}else {
			color = Color.web("cf7800").interpolate(Color.web("#ffffff"), value / 0.2);
		}
		
		return color;
	}

	public double getLevel(double sum) {
		return map(-1 * amplitude * octaves, amplitude * octaves, sum, -0.2, 1);
	}
	
	/**
	 * Berechnet den Wer der Noise-Funktion an der gegebenen Stelle.
	 * 
	 * @param x x-Koordinate
	 * @param y y-Koordinate
	 * @return returns Noise-Wert an den entsprechenden Koordinaten
	 */
	private double noise(double x, double y) {

		int fx1 = (int) (Math.floor(x) % tableLength);
		int fy1 = (int) (Math.floor(y) % tableLength);
		int fx2 = (fx1 + 1) % tableLength;
		int fy2 = (fy1 + 1) % tableLength;
		
		double tx = x - Math.floor(x);
		double ty = y - Math.floor(y);

		// Smoothfunction
		double sx = Math.pow(tx, 2) * (3 - 2 * tx);
		double sy = Math.pow(ty, 2) * (3 - 2 * ty);

		Point2D g1 = grad[getInd(fy1, fx1)];
		Point2D g2 = grad[getInd(fy1, fx2)];
		Point2D g3 = grad[getInd(fy2, fx1)];
		Point2D g4 = grad[getInd(fy2, fx2)];

		double x1 = tx;
		double y1 = ty;
		double x2 = tx - 1;
		double y2 = ty - 1;

		Point2D v1 = new Point2D(x1, y1);
		Point2D v2 = new Point2D(x2, y2);
		Point2D v3 = new Point2D(x1, y2);
		Point2D v4 = new Point2D(x2, y1);

		double lerpX1 = lerp(g1.dotProduct(v1), g2.dotProduct(v4), sx);
		double lerpX2 = lerp(g3.dotProduct(v3), g4.dotProduct(v2), sx);

		return lerp(lerpX1, lerpX2, sy);
	}
	
	/**
	 * Diese Funktion gibt einen Index zu einem Element für die Gradienten
	 * zurück.
	 * 
	 * @param x	x-Koordiante
	 * @param y	y-Koordiante
	 * @return Index für ein Element des Arrays, welches die Gradienten enthält
	 */
	private int getInd(int x, int y) {
		return permTable[permTable[y] + x];
	}

	/**
	 * Diese Funktion führt eine lineare Interpolation zwischen den Zahlen
	 * a und b aus.
	 * 
	 * @param a Wert A
	 * @param b Wert B
	 * @param c "Grad" der Interpolation (sollte zwischen 0.0 oder 1.0 liegen)
	 * @return interpolierter Wert
	 */
	private double lerp(double a, double b, double c) {
		return a + c * (b - a);
	}
	
	private static double map(double a, double b, double x, double c, double d) {
		return (x - a) / (b - a) * (d - c) + c;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public double getAmplitude() {
		return amplitude;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}

	public double getFreqMult() {
		return freqMult;
	}

	public void setFreqMult(double freqMult) {
		this.freqMult = freqMult;
	}

	public double getAmplMult() {
		return amplMult;
	}

	public void setAmplMult(double amplMult) {
		this.amplMult = amplMult;
	}

	public int getTableLength() {
		return tableLength;
	}

	public void setTableLength(int tableLength) {
		this.tableLength = tableLength;
	}

	public int getOctaves() {
		return octaves;
	}

	public void setOctaves(int layers) {
		this.octaves = layers;
	}
	
	public void setSeed(long seed) {
		ran.setSeed(seed);
		
		for (int i = 0; i < tableLength; i++) {
			permTable[i] = i;
			grad[i] = new Point2D(ran.nextDouble() * 2 - 1, ran.nextDouble() * 2 - 1);
		}

		for (int i = 0; i < tableLength; ++i) {
			permTable[i] = permTable[ran.nextInt(255) % (tableLength - 1)];
			permTable[i + tableLength] = permTable[i];
		}
		
	}

}
