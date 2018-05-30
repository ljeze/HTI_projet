package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import gui.observable.Observable;
import gui.observable.Observables;
import img.Images;
import img.Videos;
import img.videoEncoder.EncodedFrame;
import img.videoEncoder.EncoderParams;
import img.videoEncoder.VideoEncoder;
import img.videoEncoder.EncodedFrame.FrameType;

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
	 * Processus de codage / décodage.
	 */
	private Thread encodingThread;
	
	public TestController(final TestFrame frame)
	{
		this.frame = frame;
		
		encoderParams = new EncoderParams();
		
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
					Stream<int[][]> inputSequence = Videos.readGray(sequencePathPrefix.get())
														  .peek(origImg->codingResults.originalImg.set(Images.grayToJavaImg(origImg)));
					
					Stream<EncodedFrame> encodedSequence = VideoEncoder.encode(inputSequence, encoderParams)
																	   .peek(encodedImg->
																	   {
																		   if (encodedImg.getType() != FrameType.I)
																		   {
																			   codingResults.movementImg.set(
																					   Images.vectorMapToJavaImg(encodedImg.getBlockMovementMap(), 
																							   encoderParams.getMovementBlockSize(), 
																							   encoderParams.getMovementBlockSize(),
																							   Color.BLACK, Color.WHITE, 1.5));
																		   }
																	   });
					
					VideoEncoder.decode(encodedSequence, encoderParams)
								.map(Images::grayToJavaImg)
								.peek(reconstImg->codingResults.reconstImg.set(reconstImg))
								.forEach(img->{});
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			});
			
			encodingThread.start();
		}
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
	
	/**
	 * Obtenir les résultats du codage.
	 * @return Résultats du codage.
	 */
	public CodingResults getCodingResults()
	{
		return codingResults;
	}
}
