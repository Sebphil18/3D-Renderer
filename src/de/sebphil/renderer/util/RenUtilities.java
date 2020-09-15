package de.sebphil.renderer.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class RenUtilities {

	/**
	 * Diese Funktion kann verwendet werden, um zu überprüfen, ob eine Zeichenkette als Zahl dargestellt werden kann.
	 * 
	 * @param arg 	String, welcher geprüft werden soll
	 * @param hasNoPoint wenn aktiviert (true) ist ein Punkt erlaubt; wenn deaktiviert (false) ist ein Punkt nicht erlaubt
	 * @param e 	wenn aktiviert (true) sind e-Ausdrücke erlaubt; wenn deaktiviert (false) ist dies nicht erlaubt
	 * @return returns Wahr (true), wenn Eingabe-String als Zahl dargestellt werden kann, Falsch (false) wenn nicht.
	 */
	public static boolean isNumeric(String arg, boolean hasNoPoint, boolean e) {

		// Die Zeichenkette ist leer oder es wurde das Vorzeichen falsch gesetzt.
		if (arg.isEmpty())
			return false;
		if ((arg.contains("+") && !arg.startsWith("+")) || (arg.contains("-") && !arg.startsWith("-"))) {
			return false;
		}

		char[] chars = arg.toUpperCase().toCharArray();
		
		// Wenn die Zeichenkette nur aus einem Buchstabe besteht, kann die Methode Character.isDigit verwendet werden.
		if (chars.length == 1) {
			if (!Character.isDigit(chars[0]))
				return false;
		}
		
		for (char c : chars) {
			
			// Wenn der aktuelle Buchstabe keinen Zahl oder Vorzeichen ist
			if (!Character.isDigit(c) && c != '-' && c != '+') {
				
					// Der aktuelle Buchstabe stellt einen Punkt dar.
				if (hasNoPoint && c == '.') {
					hasNoPoint = false;
					continue;
					
					// Es wird ein valider E-Ausdruck verwendet
				} else if (e && c == 'E' && !arg.endsWith("E")) {
					e = false;
					continue;
				}
				
				// Die zuvor geprüften Kriterien können nicht erfüllt werden. -> Zeichenkette ist ungültig.
				return false;
			}
		}

		return true;
	}

	/**
	 * Multipliziert die Werte eines Vektors mit den Werten eines anderen.
	 * 
	 * @param vec1 Vektor1 (bzw. Punkt1)
	 * @param vec2 Vektor2 (bzw. Punkt2)
	 * @return returns Gibt das Ergebnis dieser Multiplikation als 
	 * neuen Vektor (bzw. Punkt) zurück.
	 */
	public static Point3D multVecVec(Point3D vec1, Point3D vec2) {
		return new Point3D(vec1.getX() * vec2.getX(), vec1.getY() * vec2.getY(), vec1.getZ() * vec2.getZ());
	}

	/**
	 * Multipliziert einen Vektor mit einer 4x4 Matrix und teilt das Ergbeniss mit der w-Komponente.
	 * Die w-Koordiante des Vektors wird als 1 angenommen.
	 * 
	 * @param matrix 4x4 Matrix
	 * @param vector Vektor
	 * @return returns Gibt das Ergebnis als neuen Vektor (bzw. Punkt) zurück.
	 */
	public static Point3D multMatVec(double[][] matrix, Point3D vector) {

		double[] result = new double[3];

		result[0] = vector.getX() * matrix[0][0] + vector.getY() * matrix[1][0] + vector.getZ() * matrix[2][0]
				+ matrix[3][0];
		result[1] = vector.getX() * matrix[0][1] + vector.getY() * matrix[1][1] + vector.getZ() * matrix[2][1]
				+ matrix[3][1];
		result[2] = vector.getX() * matrix[0][2] + vector.getY() * matrix[1][2] + vector.getZ() * matrix[2][2]
				+ matrix[3][2];

		double w = vector.getX() * matrix[0][3] + vector.getY() * matrix[1][3] + vector.getZ() * matrix[2][3]
				+ matrix[3][3];
		
		if (w != 0.0f) {
			result[0] /= w;
			result[1] /= w;
			result[2] /= w;
		}

		return new Point3D(result[0], result[1], result[2]);
	}

	/**
	 * Multipliziert zwei Matrizen.
	 * 
	 * @param m1 Matrix1
	 * @param m2 Matrix2
	 * @return Gibt das Ergebnis von m1 * m2 als neue Matrix zurück.
	 */
	public static double[][] multMatMat(double[][] m1, double[][] m2) {

		double[][] result = new double[m1.length][m2[0].length];

		for (int i = 0; i < result[0].length; i++) {
			for (int j = 0; j < result.length; j++) {

				double s = 0;

				for (int k = 0; k < m2.length; k++)
					s += m1[j][k] * m2[k][i];

				result[j][i] = s;
			}
		}

		return result;
	}

	/**
	 * Invertiert die lookAt-Matrix.
	 * 
	 * @param mat lookAt-Matrix
	 * @return Gibt die neue, invertierte lookAt-Matrix zurück.
	 */
	public static double[][] invertLookAtMat(double[][] mat) {

		double[][] result = new double[4][4];
		result[0][0] = mat[0][0];
		result[0][1] = mat[1][0];
		result[0][2] = mat[2][0];
		result[0][3] = 0;
		result[1][0] = mat[0][1];
		result[1][1] = mat[1][1];
		result[1][2] = mat[2][1];
		result[1][3] = 0;
		result[2][0] = mat[0][2];
		result[2][1] = mat[1][2];
		result[2][2] = mat[2][2];
		result[2][3] = 0;
		result[3][0] = -(mat[3][0] * result[0][0] + mat[3][1] * result[1][0] + mat[3][2] * result[2][0]);
		result[3][1] = -(mat[3][0] * result[0][1] + mat[3][1] * result[1][1] + mat[3][2] * result[2][1]);
		result[3][2] = -(mat[3][0] * result[0][2] + mat[3][1] * result[1][2] + mat[3][2] * result[2][2]);
		result[3][3] = 1;
		return result;
	}

	/**
	 * Zeigt einen Fehler bei dem beschriebenen Etikett an.
	 * 
	 * @param errorMsg 	Fehler-Nachricht
	 * @param error 	Etikett, welches den Fehler anzeigen soll
	 * @param color 	Farbe der angezeigten Schrift
	 */
	public static void showErrorMessage(String errorMsg, Label error, Color color) {

		error.setText(errorMsg);
		error.setTextFill(color);

		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), new KeyValue(error.opacityProperty(), 1)));
		timeline.getKeyFrames()
				.add(new KeyFrame(Duration.millis(errorMsg.length() * 200), new KeyValue(error.opacityProperty(), 0)));

		timeline.play();

	}
	
	/**
	 * Verschiebt ein Gitter, welches als einfaches Array 
	 * beschrieben wurde um eine Einheite nach "unten".
	 * (Gitter kann auch als Tabelle beschrieben werden.)
	 * 
	 * @param array		Array, welches verschoben werden soll
	 * @param width		Breite des Gitters
	 * @param height	Höhe des Gitters
	 */
	public static void shiftArrDown(double[] array, int width, int height) {

		for (int y = 0; y < height - 1; y++) {

			for (int x = 0; x < width; x++) {
				
				array[y * width + x] = array[(y + 1) * width + x];
				
			}

		}

	}
	
	/**
	 * Verschiebt ein Gitter, welches als einfaches Array 
	 * beschrieben wurde um eine Einheite nach "oben".
	 * (Gitter kann auch als Tabelle beschrieben werden.)
	 * 
	 * @param array		Array, welches verschoben werden soll
	 * @param width		Breite des Gitters
	 * @param height	Höhe des Gitters
	 */
	public static void shiftArrUp(double[] array, int width, int height) {

		for (int y = height - 1; y > 0; --y) {

			for (int x = 0; x < width; x++) {

				array[y * width + x] = array[(y - 1) * width + x];

			}

		}

	}
	
	/**
	 * Gibt eine Matrix in Textform aus.
	 * (Es wird der standardmäßige Outputstream verwendet.)
	 * 
	 * @param mat	Matrix, welche ausgegeben werden soll
	 */
	public static void printMat(double[][] mat) {
		for(int i=0;i<mat.length;i++) {
			for(int j=0;j<mat[0].length;j++) {
				System.out.print(mat[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}

}
