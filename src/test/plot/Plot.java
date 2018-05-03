package test.plot;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Classe utilitaire pour l'affichage de fonctions et d'images.
 */
public class Plot
{
	/**
	 * Afficher une image java. Pour afficher une matrice de niveau de gris ou
	 * de couleur, lui appliquer au préalable {@link img.Images#toJavaImg
	 * toJavaImg}.
	 * 
	 * @param img
	 *            image java.
	 */
	public static void showImg(final BufferedImage img)
	{
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JLabel imgLbl = new JLabel(new ImageIcon(img));
		frame.add(imgLbl);
		
		frame.setVisible(true);
		frame.pack();
	}
}
