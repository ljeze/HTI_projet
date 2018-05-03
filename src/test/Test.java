package test;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

import img.Images;
import test.plot.Plot;

/**
 * Classe contenant tous les tests.
 */
public class Test
{
	
	public static void main(final String[] args)
	{
		try
		{
			final int[][] img = Images.readGray(Paths.get("C:\\Users\\Loic\\Pictures\\Fujiview.jpg"));
			Plot.showImg(Images.toJavaImg(img));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

}
