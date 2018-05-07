package img.math;

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
}
