package img.math;

/**
 * Classe utilitaire pour les transformées en cosinus discret (DCT).
 */
public class DCT
{
	/**
	 * Faire une DCT d'une matrice 2D.
	 * 
	 * @param matrix
	 *            matrice 2D à transformer.
	 * @return Transformée en cos discret de la matrice 2D.
	 */
	public static double[][] transform2D(final double[][] matrix)
	{
		final int h = matrix.length,
				  w = matrix[0].length;
		
		final double[][] matrixDCT = new double[h][w];
		
		// Calculer la matrice rowDCT de la transformée sur les lignes.
		final double[][] rowDCT = new double[h][];
		
		for (int y = 0; y < h; ++y)
		{
			// Calculer la DCT de la ligne.
			rowDCT[y] = transform(matrix[y]);
		}
		
		// Calculer la matrice de la transformée sur les colonnes de la matrice
		// rowDCT.
		
		// Pour chaque colonne...
		double[] column = new double[h];
		
		for (int x = 0; x < w; ++x)
		{
			for (int y = 0; y < h; ++y)
			{
				column[y] = rowDCT[y][x];
			}
			
			// Calculer la DCT de la colonne de rowDCT.
			double[] columnDCT = transform(column);
			for (int y = 0; y < h; ++y)
			{
				matrixDCT[y][x] = columnDCT[y];
			}
		}
		
		return matrixDCT;
	}

	/**
	 * TODO : Finir transformée DCT bloc.
	 * 
	public static double[][] blockTransform(final double[][] matrix, final int blockW, final int blockH)
	{
		final int h = matrix.length,
				  w = matrix[0].length;
		
		//final double[][] blockDCT = new double[];
		for (int by = 0; by < blockH; ++by)
		{
			for (int bx = 0; bx < blockW; ++bx)
			{
				
			}
		}
	}*/

	/**
	 * Obtenir l'extension symétrique du vecteur spécifié. L'extension
	 * symétrique d'un vecteur (u0, u1, ..., un) sera 
	 * (un, un-1, ..., u1, u0, u0, u1, ..., un).
	 * 
	 * @param vector
	 *            vecteur à partir duquel l'extension symétrique sera effectuée.
	 * @return extension symétrique du vecteur spécifié.
	 */
	private static double[] symetricExtension(final double[] vector)
	{
		final double[] symExtension = new double[vector.length*2];
		
		// Copier la partie à droite avec les fonctions système.
		System.arraycopy(vector, 0, symExtension, vector.length, vector.length);
		// Faire la partie à gauche "à la main".
		for (int x = 0; x < vector.length; ++x)
		{
			symExtension[x] = vector[vector.length-1-x];
		}
		
		return symExtension;
	}
	
	/**
	 * Faire une DCT d'un vecteur 1D.
	 * 
	 * @param vector
	 *            vecteur à transformer.
	 * @return Transformée en cos discret du vecteur 1D.
	 */
	public static double[] transform(final double[] vector)
	{
		final double[] symExt = symetricExtension(vector);
		// Fft de l'extension symétrique.
		// TODO : intégrer la FFT.
		final double[] symExtFFT = /*FFT.transform(*/symExt/*)*/;
		
		// On prend un terme sur deux.
		final double[] vectorDCT = new double[vector.length];
		for (int i = 0; i < vectorDCT.length; ++i)
		{
			vectorDCT[i] = symExtFFT[i*2];
		}
		
		return vectorDCT;
	}
}
