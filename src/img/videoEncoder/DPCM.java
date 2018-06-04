package img.videoEncoder;

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
		
		double lastRec = 0;
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
		return step * Math.round(x / step);
	}
}
