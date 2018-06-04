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
	 * Matrice des coefficients de la DCT des erreurs de prédiction encodée.
	 */
	private final double[][] transformedErrors;
	/**
	 * Vecteurs de déplacement des blocs utilisés pour la compensation de
	 * mouvements encodés.
	 */
	private final Vector2D[][] transformedBlockMovementMap;
	/**
	 * Type de trame.
	 */
	private final FrameType type;
	
	private EncodedFrame(final FrameType type, final double[][] transformedErrors, final Vector2D[][] transformedBlockMovementMap)
	{
		this.type = type;
		this.transformedErrors = transformedErrors;
		this.transformedBlockMovementMap = transformedBlockMovementMap;
	}
	
	/**
	 * Obtenir une trame encodée intra.
	 * 
	 * @param transformedImage
	 *            matrice des coefficients DCT de l'image intra.
	 * @return trame encodée intra.
	 */
	public static EncodedFrame intraFrame(final double[][] transformedImage)
	{
		return new EncodedFrame(FrameType.I, transformedImage, null);
	}
	
	/**
	 * Obtenir une trame encodée prédite.
	 * 
	 * @param transformedErrors
	 *            matrice des coefficients de la DCT des erreurs de prédiction.
	 * @param transformedBlockMovementMap
	 *            Vecteurs de déplacement des blocs utilisés pour la
	 *            compensation de mouvements.
	 * @return trame encodée prédite.
	 */
	public static EncodedFrame predictedFrame(final double[][] transformedErrors, final Vector2D[][] transformedBlockMovementMap)
	{
		return new EncodedFrame(FrameType.P, transformedErrors, transformedBlockMovementMap);
	}
	
	/**
	 * Obtenir la matrice des coefficients de la DCT des erreurs de prédiction.
	 * @return matrice des coefficients de la DCT des erreurs de prédiction transformée.
	 */
	public double[][] getTransformedErrors()
	{
		return transformedErrors;
	}
	
	/**
	 * Obtenir la carte de compensation de mouvement des blocs.
	 * @return carte de compensation de mouvement des blocs transformée.
	 */
	public Vector2D[][] getTransformedBlockMovementMap()
	{
		return transformedBlockMovementMap;
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
