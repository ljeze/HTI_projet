package img.math.transforms;

import img.math.Complex;
import img.math.Matrices;

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
		
		// Calculer la matrice matrixDCT de la transformée sur les lignes.
		for (int y = 0; y < h; ++y)
		{
			// Calculer la DCT de la ligne.
			matrixDCT[y] = transform(matrix[y]);
		}
		
		// Calculer la matrice de la transformée sur les colonnes de la matrice
		// rowDCT.
		
		// Pour chaque colonne...
		double[] column = new double[h];
		
		for (int x = 0; x < w; ++x)
		{
			for (int y = 0; y < h; ++y)
			{
				column[y] = matrixDCT[y][x];
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
	 * Faire une transformée DCT inverse d'une matrice 2D.
	 * 
	 * @param matrixDCT
	 *            matrice 2D à transformer inversement.
	 * @return transformée DCT inverse de la matrice 2D.
	 */
	public static double[][] inverseTransform2D(final double[][] matrixDCT)
	{
		final int h = matrixDCT.length,
				  w = matrixDCT[0].length;
		
		final double[][] matrix = new double[h][w];
		
		// Pour chaque colonne...
		double[] columnDCT = new double[h];
		
		for (int x = 0; x < w; ++x)
		{
			for (int y = 0; y < h; ++y)
			{
				columnDCT[y] = matrixDCT[y][x];
			}
			
			// Calculer la DCT inverse de la colonne.
			double[] column = inverseTransform(columnDCT);
			for (int y = 0; y < h; ++y)
			{
				matrix[y][x] = column[y];
			}
		}
		
		// Calculer la transformée inverse sur chaque ligne de l'image obtenue.
		for (int y = 0; y < h; ++y)
		{
			// Calculer la DCT inverse de la ligne.
			matrix[y] = inverseTransform(matrix[y]);
		}
		
		return matrix;
	}
	
	/**
	 * Faire une transformée DCT en blocs d'une matrice 2D.
	 * 
	 * @param matrix
	 *            matrice à transformer.
	 * @param blockW
	 *            largeur des blocs.
	 * @param blockH
	 *            hauteur des blocs.
	 * @return transformée DCT en blocs de la matrice 2D.
	 * @throws IllegalArgumentException
	 *             si la taille de la matrice n'est pas un multiple de la taille du
	 *             bloc.
	 */
	public static double[][] blockTransform(final double[][] matrix, final int blockW, final int blockH)
			throws IllegalArgumentException
	{
		final int h = matrix.length,
				  w = matrix[0].length;
		
		if (w%blockW != 0 || h%blockH != 0)
		{
			throw new IllegalArgumentException("La taille de la matrice n'est pas un multiple de la taille du bloc.");
		}
		
		final double[][] matrixDCT = new double[h][w];
		final double[][] block = new double[blockH][blockH];
		
		for (int y = 0; y < h; y+=blockH)
		{
			for (int x = 0; x < w; x+=blockW)
			{
				Matrices.blockCopy(matrix, x, y, block, 0, 0, blockW, blockH);
				Matrices.blockCopy(DCT.transform2D(block), 0, 0, matrixDCT, x, y, blockW, blockH);
			}
		}
		
		return matrixDCT;
	}

	/**
	 * Faire une transformée DCT en blocs inverse d'une matrice 2D.
	 * 
	 * @param matrix
	 *            matrice à transformer inversement.
	 * @param blockW
	 *            largeur des blocs.
	 * @param blockH
	 *            hauteur des blocs.
	 * @return transformée DCT en blocs inverse de la matrice 2D.
	 * @throws IllegalArgumentException
	 *             si la taille de la matrice n'est pas un multiple de la taille
	 *             du bloc.
	 */
	public static double[][] inverseBlockTransform(final double[][] matrixDCT, final int blockW, final int blockH)
	{
		final int h = matrixDCT.length,
				  w = matrixDCT[0].length;
		
		if (w%blockW != 0 || h%blockH != 0)
		{
			throw new IllegalArgumentException("La taille de la matrice n'est pas un multiple de la taille du bloc.");
		}
		
		final double[][] matrix = new double[h][w];
		final double[][] blockDCT = new double[blockH][blockH];
		
		for (int y = 0; y < h; y+=blockH)
		{
			for (int x = 0; x < w; x+=blockW)
			{
				Matrices.blockCopy(matrixDCT, x, y, blockDCT, 0, 0, blockW, blockH);
				Matrices.blockCopy(DCT.inverseTransform2D(blockDCT), 0, 0, matrix, x, y, blockW, blockH);
			}
		}
		
		return matrix;
	}
	
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
	 * Voir <a href="https://ieeexplore.ieee.org/document/1163351/">A fast cosine transform in one and two dimensions</a>
	 * et <a href="http://fourier.eng.hmc.edu/e161/lectures/dct/node2.html">Fast DCT algorithm</a>
	 * @param vector
	 *            vecteur à transformer.
	 * @return Transformée en cos discret du vecteur 1D.
	 */
	public static double[] transform(final double[] vector)
	{
		final int n = vector.length;
		
		final double[] symExt = symetricExtension(vector);
		// Fft de l'extension symétrique.
		final Complex[] symExtFFT = FFT.transform(symExt);
		
		// On obtient alors [vectorDCT[0], vectorDCT[1], ..., vectorDCT[n-1], 0, vectorDCT[n-1]*, ..., vectorDCT[1]*].
		// On prend la première moitiée translatée.
		final double[] vectorDCT = new double[n];
		for (int i = 0; i < n; ++i)
		{
			vectorDCT[i] = symExtFFT[i].mult(Complex.exp(-Math.PI*i/(2*n))).realPart();
		}
		
		return vectorDCT;
	}
	
	/**
	 * Obtenir la transformée DCT inverse du vecteur 1D spécifié.
	 * 
	 * @param vectorDCT
	 *            vecteur 1D à transformer inversement.
	 * @return Transformée inverse en cas discret du vecteur 1D.
	 */
	public static double[] inverseTransform(final double[] vectorDCT)
	{
		final int n = vectorDCT.length;
		
		// On reconstitue le vecteur [vectorDCT[0], vectorDCT[1], ..., vectorDCT[n-1], 0, vectorDCT[n-1]*, ..., vectorDCT[1]*].
		final Complex[] symExtFFT = new Complex[2*n];
		
		symExtFFT[0] = Complex.real(vectorDCT[0]);
		symExtFFT[n] = Complex.real(0);
		for (int i = 1; i < n; ++i)
		{
			symExtFFT[i] = Complex.real(vectorDCT[i]).mult(Complex.exp(Math.PI*i/(2*n)));
			symExtFFT[2*n - i] = symExtFFT[i].conjugate();
		}
		
		// On applique une FFT inverse.
		final Complex[] symExtComplex = FFT.inverseTransform(symExtFFT);
		final double[] vector = new double[n];
		
		// On ne récupère que les n derniers échantillons.
		for (int i = 0; i < n; ++i)
		{
			vector[i] = symExtComplex[n + i].realPart();
		}
		
		return vector;
	}
}
