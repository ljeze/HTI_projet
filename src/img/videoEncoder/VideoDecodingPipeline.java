package img.videoEncoder;

import static img.videoEncoder.VideoEncoder.inverseTransformBlockMovementMap;
import static img.videoEncoder.VideoEncoder.inverseTransformErrors;
import static img.videoEncoder.VideoEncoder.reconstruct;

import java.util.function.Function;

import img.math.Vector2D;
import img.videoEncoder.EncodedFrame.FrameType;

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
		// Trame Intra.
		if (frame.getType() == FrameType.I)
		{
			prevFrameRec = inverseTransformErrors(frame.getTransformedErrors(), parameters.getDctBlockSize());
			return prevFrameRec;
		}
		
		// La trame est une matrice d'erreurs de prédiction.
		int[][] errors = inverseTransformErrors(frame.getTransformedErrors(), parameters.getDctBlockSize());
		// La carte de compensation de mouvement.
		Vector2D[][] blockMovementMap = inverseTransformBlockMovementMap(frame.getTransformedBlockMovementMap());
		
		// On calcul la trame actuelle reconstruite.
		int[][] frameRec = reconstruct(prevFrameRec, errors, blockMovementMap, parameters.getMovementBlockSize(), parameters.getMovementBlockSize());
		
		prevFrameRec = frameRec;
		return frameRec;
	}
}