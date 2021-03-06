package img.videoEncoder;

import static img.videoEncoder.VideoEncoder.*;

import java.util.Arrays;
import java.util.function.Function;

import img.Images;
import img.math.Vector2D;
import img.videoEncoder.io.EncodedFrame;
import img.videoEncoder.io.EncoderParams;
import img.videoEncoder.io.EncodedFrame.FrameType;
import test.plot.Plot;

/**
 * Pipeline de décodage vidéo.
 */
public class VideoDecodingPipeline implements Function<EncodedFrame, int[][]>
{
	/**
	 * Paramètres d'encodage.
	 */
	private final EncoderParams parameters;
	/**
	 * Trame précédente reconstruite.
	 */
	private int[][] prevFrameRec;
	
	public VideoDecodingPipeline(final EncoderParams parameters)
	{
		this.parameters = parameters;
	}
	
	/**
	 * Décode une trame.
	 * 
	 * @param frame
	 *            trame encodée.
	 * @return trame décodée.
	 */
	@Override
	public int[][] apply(final EncodedFrame frame)
	{
		/**
		 * Erreurs de prédiction.
		 */
		final int[][] errors;
		
		// Trame Intra.
		if (frame.getType() == FrameType.I)
		{
			errors = inverseTransformErrors(frame.getTransformedErrors(), parameters.getDctBlockSize(), parameters.getQuantificationWeights(), parameters.getQuantificationScale(), FrameType.I);
			prevFrameRec = reconstructI(errors);
			
			return prevFrameRec;
		}
		
		// On récupère les erreurs de prédiction.
		errors = inverseTransformErrors(frame.getTransformedErrors(), parameters.getDctBlockSize(), parameters.getQuantificationWeights(), parameters.getQuantificationScale(), FrameType.P);
		// La carte de compensation de mouvement.
		final Vector2D[][] blockMovementMap = inverseTransformBlockMovementMap(frame.getTransformedBlockMovementMap());
		
		// On calcul la trame actuelle reconstruite.
		final int[][] frameRec = reconstructP(prevFrameRec, errors, blockMovementMap, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		prevFrameRec = frameRec;
		return frameRec;
	}
}