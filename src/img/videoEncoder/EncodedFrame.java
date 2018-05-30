package img.videoEncoder;

import img.math.Vector2D;

/**
 * Trame encodée.
 */
public class EncodedFrame
{
	/**
	 * Différents type de trames encodées.
	 */
	public static enum FrameType
	{
		I,	// Intra
		P	// Prédite
	};
	
	/**
	 * Matrice des coefficients de la DCT des erreurs de prédiction.
	 */
	private final double[][] predictionErrorCoeffs;
	/**
	 * Vecteurs de déplacement des blocs utilisés pour la compensation de
	 * mouvements.
	 */
	private final Vector2D[][] blockMovementMap;
	/**
	 * Type de trame.
	 */
	private final FrameType type;
	
	private EncodedFrame(final FrameType type, final double[][] predictionErrorCoeffs, final Vector2D[][] blockMovementMap)
	{
		this.type = type;
		this.predictionErrorCoeffs = predictionErrorCoeffs;
		this.blockMovementMap =	blockMovementMap;
	}
	
	/**
	 * Obtenir une trame encodée intra.
	 * 
	 * @param imageDctCoeffs
	 *            matrice des coefficients DCT de l'image intra.
	 * @return trame encodée intra.
	 */
	public static EncodedFrame intraFrame(final double[][] imageDctCoeffs)
	{
		return new EncodedFrame(FrameType.I, imageDctCoeffs, null);
	}
	
	/**
	 * Obtenir une trame encodée prédite.
	 * 
	 * @param predictionErrorCoeffs
	 *            matrice des coefficients de la DCT des erreurs de prédiction.
	 * @param blockMovementMap
	 *            Vecteurs de déplacement des blocs utilisés pour la
	 *            compensation de mouvements.
	 * @return trame encodée prédite.
	 */
	public static EncodedFrame predictedFrame(final double[][] predictionErrorCoeffs, final Vector2D[][] blockMovementMap)
	{
		return new EncodedFrame(FrameType.P, predictionErrorCoeffs, blockMovementMap);
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
	
	/**
	 * Obtenir le type de cette trame.
	 * @return I ou P.
	 */
	public FrameType getType()
	{
		return type;
	}
}
