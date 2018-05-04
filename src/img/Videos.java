package img;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Classe utilitaire de lecture et d'écriture de séquence d'images.
 */
public class Videos
{
	/**
	 * Lire une séquence d'images en niveau de gris.
	 * Pour le nom de la séquence préciser son dossier avec le préfixe de nom.<br>
	 * Par exemple, pour lire une séquence d'images "seq1.jpg", "seq2.jpg",
	 * "seq3.jpg"... présents dans le dossier "C://img/", on utilisera le chemin
	 * "C://img/seq".
	 * 
	 * @param sequencePath
	 *            Emplacement de la séquence avec le prefixe de nom.
	 * @return Flux de trames de la séquence.
	 * @throws FileNotFoundException
	 */
	public static Iterator<int[][]> readGray(final Path sequencePath) throws FileNotFoundException
	{
		final PathMatcher frameMatcher = FileSystems.getDefault().getPathMatcher("glob:" + sequencePath.getFileName() + "*.{jpg,jpeg,png,bmp}");
		
		try
		{
			return Files.list(sequencePath.getParent())
					 	.filter(frameMatcher::matches)
					 	.map(asUncheckedFunction(Images::readGray))
					 	.iterator();
		} catch (IOException e)
		{
			throw new FileNotFoundException("La séquence est inexistante.");
		}
	}
	
	// L'interface et la méthode suivante sont utilisées pour régler les
	// problèmes de lambda fonction avec des exceptions.
	//_________________________________________________________________________________________________________________
	private static interface ExceptionFunction<T, R, E extends Exception>
	{
		R apply(T t) throws E;
	}

	private static <T, R, E extends Exception> Function<T, R> asUncheckedFunction(ExceptionFunction<T, R, E> func)
	{
		return e -> {
			try {
				return func.apply(e);
			} catch (Exception ex) { throw new RuntimeException(ex); }
		};
	}
	//_________________________________________________________________________________________________________________
}
