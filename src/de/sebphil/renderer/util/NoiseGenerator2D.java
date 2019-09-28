package de.sebphil.renderer.util;

import java.util.Random;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class NoiseGenerator2D {

	private double frequency, amplitude, freqMult, amplMult;
	private int tableLength, layers;
	private int[] permTable;
	private Point2D gard[];
	private Random ran;

	public NoiseGenerator2D(long seed) {

		frequency = 1;
		amplitude = 1;
		freqMult = 1;
		amplMult = 1;
		layers = 1;
		tableLength = 256;
		permTable = new int[tableLength * 2];
		gard = new Point2D[tableLength];

		ran = new Random(seed);

		for (int i = 0; i < tableLength; i++) {
			permTable[i] = i;
			gard[i] = new Point2D(ran.nextDouble() * 2 - 1, ran.nextDouble() * 2 - 1);
		}

		for (int i = 0; i < tableLength; ++i) {
			permTable[i] = permTable[ran.nextInt(255) % (tableLength - 1)];
			permTable[i + tableLength] = permTable[i];
		}

	}

	public double realNoise(double x, double y) {

		if (frequency < 0)
			return 0;

		int layers = this.layers;

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

	public double getNoiseGray(double sum) {
		return Math.abs(map(-1, 1, sum / layers, 0, 1) % 1);
	}

	public Color getNoiseRGB(double sum) {

		Color color = Color.DARKBLUE;

		double value = map(-1 * amplitude * layers, 1 * amplitude * layers, sum, -0.2, 1);

		if (value <= 0.1) {
			color = Color.web("#0a0075").interpolate(Color.web("#00b4eb"), value / 0.1);
		} else if (value <= 0.2) {
			color = Color.web("#00b4eb").interpolate(Color.web("#26bf00"), value / 0.2);
		} else if (value <= 0.4) {
			color = Color.web("#26bf00").interpolate(Color.web("#b2ed02"), value / 0.4);
		} else if (value <= 0.7) {
			color = Color.web("#b2ed02").interpolate(Color.web("#e1eb8d"), value / 0.7);
		} else {
			color = Color.web("#e1eb8d").interpolate(Color.web("#cf7800"), value / 1.1);
		}

		return color;
	}

	private double noise(double x, double y) {

		int fx1 = (int) (Math.floor(x) % tableLength);
		int fy1 = (int) (Math.floor(y) % tableLength);
		int fx2 = (fx1 + 1) % tableLength;
		int fy2 = (fy1 + 1) % tableLength;

		double tx = x - Math.floor(x);
		double ty = y - Math.floor(y);

		double sx = Math.pow(tx, 2) * (3 - 2 * tx);
		double sy = Math.pow(ty, 2) * (3 - 2 * ty);

		Point2D g1 = gard[getInd(fy1, fx1)];
		Point2D g2 = gard[getInd(fy1, fx2)];
		Point2D g3 = gard[getInd(fy2, fx1)];
		Point2D g4 = gard[getInd(fy2, fx2)];

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

	private int getInd(int x, int y) {
		return permTable[permTable[y] + x];
	}

	private double lerp(double a, double b, double c) {
		return a + c * (b - a);
	}

	private double map(double a, double b, double x, double c, double d) {
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

	public int getLayers() {
		return layers;
	}

	public void setLayers(int layers) {
		this.layers = layers;
	}

	public void setSeed(long seed) {
		ran.setSeed(seed);
		for (int i = 0; i < tableLength; i++) {
			gard[i] = new Point2D(ran.nextDouble() * 2 - 1, ran.nextDouble() * 2 - 1);
		}
	}

}
