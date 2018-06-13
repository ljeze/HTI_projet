package gui;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import gui.observable.Observable;
import gui.observable.Observables;
import img.Images;
import img.Videos;
import img.math.Matrices;
import img.videoEncoder.VideoEncoder;
import img.videoEncoder.io.EncodedFrame;
import img.videoEncoder.io.EncodedFrame.FrameType;
import img.videoEncoder.io.EncoderParams;

/**
 * Controlleur pour l'interface de test.
 */
public class TestController
{
	/**
	 * Frame de l'interface de test.
	 */
	private final TestFrame frame;
	
	/**
	 * Chemin de la séquence actuellement ouverte.
	 */
	private final Observable<Path> sequencePathPrefix;
	/**
	 * Paramètres de l'encodage.
	 */
	private final EncoderParams encoderParams;
	/**
	 * Résultats issus du codage.
	 */
	private final CodingResults codingResults;
	/**
	 * Résultats pour une vidéo entière.
	 */
	private final Observable<List<CodingResults>> videoResults;
	/**
	 * Tas des résultats.
	 */
	private final Stack<CodingResults> resultStack;
	/**
	 * Processus de codage / décodage.
	 */
	private Thread encodingThread;
	
	public TestController(final TestFrame frame)
	{
		this.frame = frame;
		
		encoderParams = new EncoderParams();
		
		resultStack = new Stack<>();
		
		videoResults  = new Observable<>();
		codingResults = new CodingResults();
		sequencePathPrefix = new Observable<>();
	}
	
	// ======================================================================================
	//	Fonction appelées par l'interface.
	// ======================================================================================
	
	/**
	 * L'ouverture d'un fichier a été demandé.
	 * @param event
	 */
	public void handleOpenFile(final ActionEvent event)
	{
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File("C:\\Users\\Loic\\Pictures\\test"));
		fc.setDialogTitle("Ouvrir une séquence d'images");
		fc.setFileFilter(new FileNameExtensionFilter("Séquence d'images : *1.jpg, *2.jpg, ..., *n.jpg", "jpg", "jpeg", "bmp"));
		
		// Un fichier a bien été sélectionné.
		if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			sequencePathPrefix.set(getSequenceFilePrefix(fc.getSelectedFile().toPath()));
		}
	}
	
	/**
	 * Le démarrage de l'encodage a été demandé.
	 * @param event
	 */
	public void handleStart(final ActionEvent event)
	{
		if (encodingThread == null || !encodingThread.isAlive())
		{
			encodingThread = new Thread(()->
			{
				try
				{
					videoResults.set(null);
					resultStack.clear();
					
					// Flux de trames originales.
					Stream<int[][]> inputSequence = Videos.readGray(sequencePathPrefix.get())
														  .peek(this::handleNewFrame);
					
					// Flux de trames encodées.
					Stream<EncodedFrame> encodedSequence = VideoEncoder.encode(inputSequence, encoderParams)
																	   .peek(this::handleNewEncodedFrame);
					
					// Flux de trames décodées.
					VideoEncoder.decode(encodedSequence, encoderParams)
								.peek(this::handleNewReconstructedFrame)
								.forEach(img->{});
					
					videoResults.set(resultStack.stream().collect(Collectors.toList()));
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			});
			
			encodingThread.start();
		}
	}
	
	
	/**
	 * Une nouvelle trame originale est arrivée.
	 * 
	 * @param frame
	 *            trame originale.
	 */
	private void handleNewFrame(final int[][] frame)
	{
		final CodingResults newResults = new CodingResults();
		final BufferedImage originalImg = Images.grayToJavaImg(frame);
		
		codingResults.originalImg.set(originalImg);
		newResults.originalImg.set(originalImg);
		newResults.originalEntropy.set(Matrices.computeEntropy(frame));
		
		resultStack.push(newResults);
	}
	
	/**
	 * Une nouvelle trame encodée est arrivée.
	 * 
	 * @param encodedFrame
	 *            trame encodée.
	 */
	private void handleNewEncodedFrame(final EncodedFrame encodedFrame)
	{
		final BufferedImage errorsImg = Images
				.grayToJavaImg(Matrices.map(VideoEncoder.inverseTransformErrors(encodedFrame.getTransformedErrors(),
						encoderParams.getDctBlockSize(), encoderParams.getQuantificationWeights(), encoderParams.getQuantificationScale(), encodedFrame.getType()), -255, 255, 0, 255)
		);
		
		codingResults.errorsImg.set(errorsImg);
		
		resultStack.peek().errorsImg.set(errorsImg);
		resultStack.peek().errorsEntropy.set(Matrices.computeEntropy(encodedFrame.getTransformedErrors()));
		
		if (encodedFrame.getType() != FrameType.I)
		{
			codingResults.movementMap.set(VideoEncoder.inverseTransformBlockMovementMap(encodedFrame.getTransformedBlockMovementMap()));
			
			resultStack.peek().movementMap.set(VideoEncoder.inverseTransformBlockMovementMap(encodedFrame.getTransformedBlockMovementMap()));
			resultStack.peek().movementMapEntropy.set(Matrices.computeEntropy(encodedFrame.getTransformedBlockMovementMap()));
		}
	}
	
	/**
	 * Une nouvelle trame reconstruite est arrivée.
	 * 
	 * @param reconstructedFrame
	 *            trame reconstruite.
	 */
	private void handleNewReconstructedFrame(final int[][] reconstructedFrame)
	{
		final BufferedImage reconstImg = Images.grayToJavaImg(reconstructedFrame);
		codingResults.reconstImg.set(reconstImg);
		resultStack.peek().reconstImg.set(reconstImg);
	}
	
	// ======================================================================================
	//	Fonction annexes.
	// ======================================================================================
	
	/**
	 * Motif regex pour l'extraction du prefix d'une séquence.
	 */
	private static final Pattern SEQUENCE_PREFIX_PATTERN = Pattern.compile("^(.*?)\\d+\\.\\w+$");
	
	/**
	 * Obtenir le prefix du nom d'une image constituant une séquence.
	 * 
	 * @param path
	 *            chemin d'une des trames de la séquence.
	 * @return prefix de la séquence.
	 * @throws IllegalArgumentException
	 *             si le nom est malformé.
	 */
	private Path getSequenceFilePrefix(final Path path) throws IllegalArgumentException
	{
		final Matcher matcher = SEQUENCE_PREFIX_PATTERN.matcher(path.getFileName().toString());
		if (matcher.find())
		{
			return path.getParent().resolve(matcher.group(1));
		}
		else
		{
			throw new IllegalArgumentException("Nom de séquence malformé.");
		}
	}
	
	/**
	 * Définir le résultat de la frameIndex ème trame.
	 * 
	 * @param frameIndex
	 *            index de la trame à définir.
	 */
	public void setVideoResult(final int frameIndex)
	{
		codingResults.errorsImg.set(videoResults.get().get(frameIndex).errorsImg.get());
		codingResults.movementMap.set(videoResults.get().get(frameIndex).movementMap.get());
		codingResults.originalImg.set(videoResults.get().get(frameIndex).originalImg.get());
		codingResults.reconstImg.set(videoResults.get().get(frameIndex).reconstImg.get());
		
		codingResults.originalEntropy.set(videoResults.get().get(frameIndex).originalEntropy.get());
		codingResults.errorsEntropy.set(videoResults.get().get(frameIndex).errorsEntropy.get());
		codingResults.movementMapEntropy.set(videoResults.get().get(frameIndex).movementMapEntropy.get());
	}
	
	// ======================================================================================
	//	Getters / setters.
	// ======================================================================================
	/**
	 * Obtenir le chemin de la séquence actuellement ouverte.
	 * @return Chemin de la séquence actuellement ouverte.
	 */
	public Observable<Path> getSequencePathPrefix()
	{
		return sequencePathPrefix;
	}
	
	/**
	 * Obtenir la taille des blocs pour la DCT.
	 * @return Taille des blocs pour la DCT.
	 */
	public Observable<Integer> dctBlockSizeProperty()
	{
		return Observables.observable(encoderParams::getDctBlockSize, encoderParams::dctBlockSize);
	}
	
	/**
	 * Obtenir la taille des blocs pour la compensation de mouvement.
	 * @return Taille des blocs pour la compensation de mouvement.
	 */
	public Observable<Integer> movementBlockSizeProperty()
	{
		return Observables.observable(encoderParams::getMovementBlockSize, encoderParams::movementBlockSize);
	}
	
	public Observable<Integer> quantifScaleProperty()
	{
		return Observables.observable(encoderParams::getQuantificationScale, encoderParams::quantifierScale);
	}
	
	public Observable<List<CodingResults>> videoResultsProperty()
	{
		return videoResults;
	}
	
	/**
	 * Obtenir les résultats du codage.
	 * @return Résultats du codage.
	 */
	public CodingResults getCodingResults()
	{
		return codingResults;
	}
}
