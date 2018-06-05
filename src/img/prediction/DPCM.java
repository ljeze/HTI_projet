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
		
		final double[][] error = new double[h][w];
		
		double lastPred;
		
	    for (int l = 0; l < h; ++l)
	    {
	    	lastPred = 0;
	    	
	        for (int c = 0; c < w; ++c)
	        {
	        	error[l][c] = quantize(matrix[l][c] - lastPred, step);
	        	lastPred = error[l][c] + lastPred;
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
		
		final double[][] matrix = new double[h][w];
		
		double lastRec;
	    for (int l = 0; l < h; ++l)
	    {
	    	lastRec = 0;
	        for (int c = 0; c < w; ++c)
	        {
	        	matrix[l][c] = error[l][c] + lastRec;
	            lastRec = matrix[l][c];
	        }
	    }
	    
	    return matrix;
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
		
		final Vector2D[][] error = new Vector2D[h][w];
		
		Vector2D lastPred;
		
	    for (int l = 0; l < h; ++l)
	    {
	    	lastPred = new Vector2D(0, 0);
	    	
	        for (int c = 0; c < w; ++c)
	        {
	        	error[l][c] = quantize(matrix[l][c].minus(lastPred), step);
	        	lastPred = error[l][c].plus(lastPred);
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
		
		final Vector2D[][] matrix = new Vector2D[h][w];
		
		Vector2D lastRec;
	    for (int l = 0; l < h; ++l)
	    {
	    	lastRec = new Vector2D(0, 0);
	        for (int c = 0; c < w; ++c)
	        {
	        	matrix[l][c] = error[l][c].plus(lastRec);
	            lastRec = matrix[l][c];
	        }
	    }
	    
	    return matrix;
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
