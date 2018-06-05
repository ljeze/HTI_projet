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
		
		/*BufferedImage tmp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		tmp.setRGB(0, 0, tmp.getWidth(), tmp.getHeight(), img.getRGB(0, 0, tmp.getWidth(), tmp.getHeight(), null, 0, tmp.getWidth()), 0, tmp.getWidth());*/
		/*Graphics2D g = tmp.createGraphics();
		
		g.drawImage(img, 0, 0, null);
		g.fillRect(0, 0, 50, 50);
		g.dispose();
		*/
		JLabel imgLbl = new JLabel(new ImageIcon(img));
		frame.add(imgLbl);
		
		frame.setVisible(true);
		frame.pack();
	}
	
}
