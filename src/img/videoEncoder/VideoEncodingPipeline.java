package img.videoEncoder;

import java.util.function.Function;

import img.math.Vector2D;
import static img.videoEncoder.VideoEncoder.*;

/**
 * Pipeline d'encodage vidéo.
 */
public class VideoEncodingPipeline implements Function<int[][], EncodedFrame>
{
	/**
	 * Paramètres d'encodage.
	 */
	private final EncoderParams parameters;
	/**
	 * Trame précédente reconstruite.
	 */
	private int[][] prevFrameRec;
	
	public VideoEncodingPipeline(final EncoderParams parameters)
	{
		this.parameters = parameters;
	}
	
	/**
	 * Encode une trame.
	 * 
	 * @param frame
	 *            trame actuelle initiale (non reconstruite).
	 * @return trame encodée.
	 */
	@Override
	public EncodedFrame apply(final int[][] frame)
	{
		/**
		 * Matrice des erreurs de prédiction.
		 */
		final int[][] errors;
		/**
		 * Matrice des erreurs transformée.
		 */
		final double[][] transformedErrors;
		
		// Si l'on est sur la première trame.
		if (prevFrameRec == null)
		{
			// Les erreurs sont l'image.
			errors = frame;
			
			// On calcul les coefficients DCT de ces erreurs (l'image) et on applique la quantification.
			transformedErrors = transformErrors(errors, parameters.getDctBlockSize(),
					parameters.getQuantificationWeights(), parameters.getQuantificationScale(), true);
			
			// On reconstruit la trame.
			prevFrameRec = inverseTransformErrors(transformedErrors, parameters.getDctBlockSize());
			
			// L'envoyer sans prédiction.
			return EncodedFrame.intraFrame(transformedErrors);
		}
		
		final Vector2D[][] transformedBlockMovementMap;
		
		// On calcul la carte de compensation de mouvement des blocs.
		final Vector2D[][] blockMovementMap = computeBlockMovementMap(prevFrameRec, frame, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		// On calcul les erreurs de prédiction entre la trame actuelle initiale et la trame précédente reconstruite.
		errors = computeErrors(prevFrameRec, frame, blockMovementMap, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		// On calcul la carte de compensation de mouvement transformée.
		transformedBlockMovementMap = transformBlockMovementMap(blockMovementMap);
		// On calcul les coefficients DCT de ces erreurs et on applique la quantification puis prédiction DPCM.
		transformedErrors = transformErrors(errors, parameters.getDctBlockSize(),
				parameters.getQuantificationWeights(), parameters.getQuantificationScale(), false);
		
		// On calcul la trame actuelle reconstruite.
		int[][] frameRec = reconstruct(prevFrameRec, inverseTransformErrors(transformedErrors, parameters.getDctBlockSize()), 
													 inverseTransformBlockMovementMap(transformedBlockMovementMap), 
									   parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		prevFrameRec = frameRec;
		return EncodedFrame.predictedFrame(transformedErrors, transformedBlockMovementMap);
	}
}