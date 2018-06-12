package img.prediction;

import img.math.Vector2D;

/**
 * Classe utilitaire pour la DPCM.
 */
public class DPCM
{
	/**
	 * Encoder une matrice par un codage DPCM avec le pas de quantification
	 * spécifié.
	 * 
	 * @param matrix
	 *            matrice à encoder.
	 * @param step
	 *            pas de quantification.
	 * @return matrice des erreurs de prediction.
	 */
	public static double[][] encode(final double[][] matrix, final int step)
	{
		final int h = matrix.length,
				  w = matrix[0].length;
		
		final double[][] reconstructedMatrix = new double[h][w];
		final double[][] error = new double[h][w];
		
		for (int x = 0; x < w; ++x)
		{
			reconstructedMatrix[0][x] = matrix[0][x];
			error[0][x] = matrix[0][x];
		}
		
		for (int y = 0; y < h; ++y)
		{
			reconstructedMatrix[y][0] = matrix[y][0];
			error[y][0] = matrix[y][0];
		}
		
	    for (int l = 1; l < h; ++l)
	    {
	        for (int c = 1; c < w; ++c)
	        {
	        	final double predictedElement = reconstructedMatrix[l-1][c] * 0.5 + reconstructedMatrix[l][c-1] * 0.5;
	        	error[l][c] = quantize(matrix[l][c] - predictedElement, step);
	        	reconstructedMatrix[l][c] = predictedElement + error[l][c];
	        }
	    }
	    
	    return error;
	}
	
	/**
	 * Décoder une matrice d'erreurs obtenues par DPCM.
	 * 
	 * @param error
	 *            matrice d'erreurs de la DPCM.
	 * @return matrice originale à des erreurs de quantification près.
	 */
	public static double[][] decode(final double[][] error)
	{
		final int h = error.length,
				  w = error[0].length;
		
		final double[][] reconstructedMatrix = new double[h][w];
		
		for (int x = 0; x < w; ++x)
		{
			reconstructedMatrix[0][x] = error[0][x];
		}
		
		for (int y = 0; y < h; ++y)
		{
			reconstructedMatrix[y][0] = error[y][0];
		}
		
	    for (int l = 1; l < h; ++l)
	    {
	        for (int c = 1; c < w; ++c)
	        {
	        	final double predictedElement = reconstructedMatrix[l-1][c] * 0.5 + reconstructedMatrix[l][c-1] * 0.5;
	        	reconstructedMatrix[l][c] = predictedElement + error[l][c];
	        }
	    }
	    
	    return reconstructedMatrix;
	}
	
	/**
	 * Quantifier une valeur donnée avec le pas de quantification spécifié.
	 * 
	 * @param x
	 *            valeur à quantifier.
	 * @param step
	 *            pas de quantification.
	 * @return valeur quantifiée.
	 */
	public static double quantize(final double x, final double step)
	{
		return x;
		//return step * Math.round(x / step);
	}
	
	/**
	 * Encoder une matrice de vecteurs par un codage DPCM avec le pas de
	 * quantification spécifié.
	 * 
	 * @param matrix
	 *            matrice de vecteurs à encoder.
	 * @param step
	 *            pas de quantification.
	 * @return matrice des erreurs de prediction.
	 */
	public static Vector2D[][] encode(final Vector2D[][] matrix, final int step)
	{
		final int h = matrix.length,
				  w = matrix[0].length;
		
		final Vector2D[][] reconstructedMatrix = new Vector2D[h][w];
		final Vector2D[][] error = new Vector2D[h][w];
		
		for (int x = 0; x < w; ++x)
		{
			reconstructedMatrix[0][x] = matrix[0][x];
			error[0][x] = matrix[0][x];
		}
		
		for (int y = 0; y < h; ++y)
		{
			reconstructedMatrix[y][0] = matrix[y][0];
			error[y][0] = matrix[y][0];
		}
		
	    for (int l = 1; l < h; ++l)
	    {
	        for (int c = 1; c < w; ++c)
	        {
	        	final Vector2D predictedElement = reconstructedMatrix[l-1][c].times(0.5).plus(reconstructedMatrix[l][c-1].times(0.5));
	        	error[l][c] = quantize(matrix[l][c].minus(predictedElement), step);
	        	reconstructedMatrix[l][c] = predictedElement.plus(error[l][c]);
	        }
	    }
	    
	    return error;
	}
	
	/**
	 * Décoder une matrice d'erreurs obtenues par DPCM d'une carte de vecteurs.
	 * 
	 * @param error
	 *            matrice d'erreurs de la DPCM d'une carte de vecteurs.
	 * @return matrice originale à des erreurs de quantification près.
	 */
	public static Vector2D[][] decode(final Vector2D[][] error)
	{
		final int h = error.length,
				  w = error[0].length;
		
		final Vector2D[][] reconstructedMatrix = new Vector2D[h][w];
		
		for (int x = 0; x < w; ++x)
		{
			reconstructedMatrix[0][x] = error[0][x];
		}
		
		for (int y = 0; y < h; ++y)
		{
			reconstructedMatrix[y][0] = error[y][0];
		}
		
	    for (int l = 1; l < h; ++l)
	    {
	        for (int c = 1; c < w; ++c)
	        {
	        	final Vector2D predictedElement = reconstructedMatrix[l-1][c].times(0.5).plus(reconstructedMatrix[l][c-1].times(0.5));
	        	reconstructedMatrix[l][c] = predictedElement.plus(error[l][c]);
	        }
	    }
	    
	    return reconstructedMatrix;
	}
	
	/**
	 * Quantifier un vecteur donnée avec le pas de quantification spécifié.
	 * 
	 * @param vector
	 *            vecteur à quantifier.
	 * @param step
	 *            pas de quantification.
	 * @return vecteur quantifiée.
	 */
	public static Vector2D quantize(final Vector2D vector, final int step)
	{
		return vector;
//		return new Vector2D(step * (int)Math.round((double)vector.x() / step), step * (int)Math.round((double)vector.y() / step)) ;
	}
	
}
