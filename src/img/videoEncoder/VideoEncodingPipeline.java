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
		// Si l'on est sur la première trame.
		if (prevFrameRec == null)
		{
			// On calcul les coefficients DCT de ces erreurs (l'image) et on applique la quantification.
			double[][] predErrorCoeff = getPredictionErrorCoeff(frame, parameters.getDctBlockSize(),
					parameters.getQuantificationWeights(), parameters.getQuantificationScale(), true);
			prevFrameRec = getPredictionErrorMap(predErrorCoeff, parameters.getDctBlockSize());
			// L'envoyer sans prédiction.
			return EncodedFrame.intraFrame(predErrorCoeff);
		}
		
		// On calcul la carte de compensation de mouvement des blocs.
		Vector2D[][] blockMovementMap = getBlockMovementMap(prevFrameRec, frame, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		// On calcul les erreurs de prédiction entre la trame actuelle initiale et la trame précédente reconstruite.
		int[][] predError = predict(prevFrameRec, frame, blockMovementMap, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		// On calcul les coefficients DCT de ces erreurs et on applique la quantification.
		double[][] predErrorCoeff = getPredictionErrorCoeff(predError, parameters.getDctBlockSize(),
				parameters.getQuantificationWeights(), parameters.getQuantificationScale(), false);
		
		// On calcul la trame actuelle reconstruite.
		int[][] frameRec = reconstruct(prevFrameRec, getPredictionErrorMap(predErrorCoeff, parameters.getDctBlockSize()), blockMovementMap, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		prevFrameRec = frameRec;
		return EncodedFrame.predictedFrame(predErrorCoeff, blockMovementMap);
	}
}