package img;

import img.math.Vector2D;

/**
 * Trame encodée.
 */
public class EncodedFrame
{
	/**
	 * Matrice des erreurs de prédiction.
	 */
	private final int[][] predictionErrors;
	/**
	 * Vecteurs de déplacement des blocs utilisés pour la compensation de
	 * mouvements.
	 */
	private final Vector2D[][] blockMovementMap;
	
	public EncodedFrame(final int[][] predictionErrors, final Vector2D[][] blockMovementMap)
	{
		this.predictionErrors = predictionErrors;
		this.blockMovementMap =	blockMovementMap;
	}
	
	public EncodedFrame(final int[][] predictionErrors)
	{
		this(predictionErrors, null);
	}
	
	/**
	 * Obtenir la matrice des erreurs de prédiction.
	 * @return matrice des erreurs de prédiction.
	 */
	public int[][] getPredictionErrors()
	{
		return predictionErrors;
	}
	
	/**
	 * Obtenir la carte de compensation de mouvement des blocs.
	 * @return carte de compensation de mouvement des blocs.
	 */
	public Vector2D[][] getBlockMovementMap()
	{
		return blockMovementMap;
	}
}
