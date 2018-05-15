package img.math;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Classe utilitaire pour les matrices.
 */
public class Matrices
{
	/**
	 * Copier un bloc d'une matrice vers une autre.
	 * 
	 * @param src
	 *            matrice source à partie de laquelle le bloc sera copié.
	 * @param srcX
	 *            position x du bloc dans l'image source.
	 * @param srcY
	 *            position y du bloc dans l'image source.
	 * @param dest
	 *            matrice destination dans laquelle le bloc sera collé.
	 * @param destX
	 *            position x du bloc une fois collé dans l'image destination.
	 * @param destY
	 *            position y du bloc une fois collé dans l'image destination.
	 * @param blockW
	 *            largeur du bloc.
	 * @param blockH
	 *            hauteur du bloc.
	 */
	public static void blockCopy(final double[][] src, final int srcX, final int srcY, final double[][] dest,
			final int destX, final int destY, final int blockW, final int blockH)
	{
		for (int y = 0; y < blockH; ++y)
		{
			System.arraycopy(src[y+srcY]  , srcX, 
							 dest[y+destY], destX, blockW);
		}
	}
	
	/**
	 * Copie la colonne spécifiée issue de la matrice source donnée dans le
	 * vecteur destination.
	 * 
	 * @param src
	 *            matrice source dont la colonne est copiée.
	 * @param column
	 *            index de la colonne à copier.
	 * @param dest
	 *            vecteur destination.
	 */
	public static void columnCopy(final double[][] src, final int column, final double[] dest)
	{
		for (int y = 0; y < dest.length; ++y)
		{
			dest[y] = src[y][column];
		}
	}
	
	/**
	 * Copie le vecteur source donnée dans la colonne spécifiée de la matrice
	 * destination.
	 * 
	 * @param src
	 *            vecteur source.
	 * @param dest
	 *            matrice destination dans laquelle on va insérer le vecteur
	 *            source.
	 * @param column
	 *            colonne dans laquelle on insert le vecteur source.
	 */
	public static void columnCopy(final double[] src, final double[][] dest, final int column)
	{
		for (int y = 0; y < dest.length; ++y)
		{
			dest[y][column] = src[y];
		}
	}
	
	/**
	 * Obtenir une matrice de doubles à partir d'une matrice d'entiers.
	 * 
	 * @param matrix
	 *            matrice d'entiers.
	 * @return matrice de doubles contenant les mêmes valeurs que la matrice
	 *         d'entiers.
	 */
	public static double[][] toDouble(final int[][] matrix)
	{
		final int h = matrix.length,
				  w = matrix[0].length;
		
		final double[][] doubleMatrix = new double[h][w];
		
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				doubleMatrix[y][x] = matrix[y][x];
			}
		}
		
		return doubleMatrix;
	}
	
	/**
	 * Log(2) constant.
	 */
	private final static double LOG2 = Math.log(2);
	
	/**
	 * Calcule l'entropie d'une matrice représentant une source de symboles
	 * doubles.
	 * 
	 * @param matrix
	 *            matrice, source de symboles doubles.
	 * @return entropie de cette matrice.
	 */
	public static double computeEntropy(final double[][] matrix)
	{
		final int h = matrix.length,
				  w = matrix[0].length;
		
		final Map<Double, Integer> histogram = new HashMap<>();
		
		// Compter chaque occurence de double présent dans la matrice.
		for (int y = 0; y < h; ++y)
		{
			for (int x = 0; x < w; ++x)
			{
				Integer counter = histogram.get(matrix[y][x]);
				histogram.put(matrix[y][x], counter == null ? 1 : (counter+1));
			}
		}
		
		double entropy = 0;
		for (int symbolCounter : histogram.values())
		{
			// Probabilité d'apparition de ce symbole.
			final double symbolP = (double) symbolCounter / (w*h);
			entropy -= symbolP * (Math.log(symbolP)/LOG2);
		}
		
		return entropy;
	}
}
