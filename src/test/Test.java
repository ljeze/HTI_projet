package test;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Iterator;

import img.Images;
import img.VideoEncoder;
import img.Videos;
import test.plot.Plot;

/**
 * Classe contenant tous les tests.
 */
public class Test
{
	/**
	 * Tester la lecture d'images et leur affichage.
	 * @throws FileNotFoundException
	 */
	public void testImageRead() throws FileNotFoundException
	{
		final int[][] img = Images.readGray(Paths.get("C:\\Users\\Loic\\Pictures\\Fujiview.jpg"));
		Plot.showImg(Images.toJavaImg(img));
	}
	
	/**
	 * Tester l'encodage d'une séquence.
	 * @throws FileNotFoundException
	 */
	public void testVideoEncoding() throws FileNotFoundException
	{
		// Exemple : charger une séquence de 5 images nommées sequence1.jpg, sequence2.jpg, ..., sequence5.jpg .
		Iterator<int[][]> encodedSequence = VideoEncoder.encode(
			Videos.readGray(Paths.get("C:\\Users\\Loic\\Pictures\\sequence"))
		);
	}
	
	public static void main(final String[] args)
	{
		// testImageRead();
		// testVideoEncoding();
	}

}
