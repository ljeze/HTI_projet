package img;

import img.math.Vector2D;

/**
 * Trame encodée.
 */
public class EncodedFrame
{
	/**
	 * Matrice des coefficients de la DCT des erreurs de prédiction.
	 */
	private final double[][] predictionErrorCoeffs;
	/**
	 * Vecteurs de déplacement des blocs utilisés pour la compensation de
	 * mouvements.
	 */
	private final Vector2D[][] blockMovementMap;
	
	public EncodedFrame(final double[][] predictionErrorCoeffs, final Vector2D[][] blockMovementMap)
	{
		this.predictionErrorCoeffs = predictionErrorCoeffs;
		this.blockMovementMap =	blockMovementMap;
	}
	
	public EncodedFrame(final double[][] predictionErrors)
	{
		this(predictionErrors, null);
	}
	
	/**
	 * Obtenir la matrice des coefficients de la DCT des erreurs de prédiction.
	 * @return matrice des coefficients de la DCT des erreurs de prédiction.
	 */
	public double[][] getPredictionErrorCoeffs()
	{
		return predictionErrorCoeffs;
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
