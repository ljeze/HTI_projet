package gui.custom;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

import img.math.Vector2D;

/**
 * Visualisateur pour les champs de vecteurs.
 */
public class VectorMapView extends JComponent
{
	/**
	 * Champs de vecteurs à visualiser.
	 */
	private Vector2D[][] vectorMap;
	/**
	 * Delta x entre chaque vecteur.
	 */
	private int dx;
	/**
	 * Delta y entre chaque vecteur.
	 */
	private int dy;
	
	/**
	 * Echelle d'affichage.
	 */
	private double scale;
	/**
	 * Offset x.
	 */
	private int xOff;
	/**
	 * Offset y.
	 */
	private int yOff;
	
	/**
	 * Centre automatiquement la vue ou pas.
	 */
	private boolean autoCenter;
	
	/**
	 * UID de sérialisation par défaut.
	 */
	private static final long serialVersionUID = 1L;

	public VectorMapView()
	{
		scale = 1.0;
		
		autoCenter = true;
		
		final VectorMapViewAdapter adapter = new VectorMapViewAdapter();
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
		addMouseWheelListener(adapter);
	}
	
	@Override
	protected void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		
		// Ne pas dessiner si pas de carte de vecteurs.
		if (vectorMap == null)
		{
			return;
		}
		
		final int width  = getWidth()-getInsets().left-getInsets().right, 
				  height = getHeight()-getInsets().top-getInsets().bottom;
		
		final int nW = vectorMap[0].length,
				  nH = vectorMap.length;
		
		final int dxScaled = (int) (dx * scale),
				  dyScaled = (int) (dy * scale);
		
		final int mapWidth  = dxScaled * (nW-1),
				  mapHeight = dyScaled * (nH-1);
		
		// Recentrage automatique de la vue.
		if (autoCenter)
		{
			xOff = width /2 - mapWidth /2;
			yOff = height/2 - mapHeight/2;
		}
		
		final Graphics2D g2 = (Graphics2D) g;
		final Shape clip = g2.getClip();
		final AffineTransform transform = g2.getTransform();
		
		g2.translate(getInsets().left, getInsets().top);
		g2.clipRect(0, 0, width, height);
		//g2.setClip(getInsets().left, getInsets().top, getWidth()-getInsets().left-getInsets().right, getHeight()-getInsets().top-getInsets().bottom);
		
		g2.setColor(getBackground());
		g2.fillRect(0, 0, width, height);
		
		g2.setColor(getBackground().darker().darker());
		g2.drawRect(0, 0, width-1, height-1);
		
		g2.translate(xOff, yOff);
		g2.setColor(getForeground());
		for (int y = 0; y < nH; ++y)
		{
			for (int x = 0; x < nW; ++x)
			{
				/*final int vectX = x*dxScaled,
						  vectY = y*dyScaled,
						  vectTipX = vectX - (int)Math.round(vectorMap[y][x].x() * scale),
						  vectTipY = vectY - (int)Math.round(vectorMap[y][x].y() * scale),
						  vectDx   = vectTipX - vectX,
						  vectDy   = vectTipY - vectY;
				*/
				drawVector(g2, x*dxScaled, y*dyScaled, -(int)Math.round(vectorMap[y][x].x() * scale), -(int)Math.round(vectorMap[y][x].y() * scale));
				//drawVector(g2, vectTipX, vectTipY, vectDx, vectDy);
			}
		}
		g2.translate(-xOff, -yOff);
		
		// Reinitialiser les transformations du context graphique.
		g2.setTransform(transform);
		g2.setClip(clip);
	}
	
	/**
	 * Définir le champs de vecteurs avec le delta x et y entre chaque vecteur.
	 * 
	 * @param vectorMap
	 *            champs de vecteur.
	 * @param dx
	 *            delta x.
	 * @param dy
	 *            delta y.
	 */
	public void setVectorMap(final Vector2D[][] vectorMap, final int dx, final int dy)
	{
		this.vectorMap = vectorMap;
		
		this.dx = dx;
		this.dy = dy;
	}
	
	/**
	 * Définir le champs de vecteurs avec le delta s entre chaque vecteur.
	 * 
	 * @param vectorMap
	 *            champs de vecteur.
	 * @param ds
	 *            delta s.
	 */
	public void setVectorMap(final Vector2D[][] vectorMap, final int ds)
	{
		setVectorMap(vectorMap, ds, ds);
		repaint();
	}
	
	/**
	 * Dessiner un vecteur partant d'une origine (x, y) et de composantes (dx, dy).
	 * 
	 * @param g
	 *            environnement graphique.
	 * @param x
	 *            origine x.
	 * @param y
	 *            origine y.
	 * @param dx
	 *            composante x.
	 * @param dy
	 *            composante y.
	 */
	private static void drawVector(final Graphics g,  int x,  int y,  int dx,  int dy)
	{
		/*int oldX = x,
			oldY = y;
		*/
		x += dx;
		y += dy;
		
		dx *= -1;
		dy *= -1;
		
		final double arrowAngle  = Math.PI/8,
					 arrowLength = 4;
		final double vectorAngle = Math.atan2(dy, dx);
		
		final int tipX = x+dx,
				  tipY = y+dy;
		
		g.drawLine(x, y, tipX, tipY);
		
		if (dx != 0 || dy != 0)
		{
			g.drawLine(tipX, tipY, 
			     (int)(tipX + arrowLength*Math.cos(vectorAngle + (Math.PI-arrowAngle))),
				 (int)(tipY + arrowLength*Math.sin(vectorAngle + (Math.PI-arrowAngle))));
			g.drawLine(tipX, tipY, 
			     (int)(tipX + arrowLength*Math.cos(vectorAngle - (Math.PI-arrowAngle))),
				 (int)(tipY + arrowLength*Math.sin(vectorAngle - (Math.PI-arrowAngle))));
		}
	}
	
	/**
	 * Définir la valeur du flag autoCenter qui indique si la vue est
	 * automatiquement centrée ou pas.
	 * 
	 * @param autoCenter
	 *            indique si la vue est automatiquement centrée ou pas.
	 */
	public void setAutoCenter(final boolean autoCenter)
	{
		this.autoCenter = autoCenter;
	}
	
	
	/**
	 * Controleur de souris pour le visualisateur de champs de vecteurs.
	 */
	private class VectorMapViewAdapter extends MouseAdapter
	{
		/**
		 * Emplacement du dernier point où la souris a été pressée.
		 */
		private Point lastPressedPoint;
		/**
		 * Valeur du offset au moment où la souris a été pressée.
		 */
		private Point lastOffset;
		
		public VectorMapViewAdapter()
		{
			lastOffset = new Point(0, 0);
		}
		
		@Override
		public void mousePressed(final MouseEvent e)
		{
			lastPressedPoint = e.getPoint();
			//lastPressedPoint = new Point(e.getX(), e.getY());
			lastOffset.setLocation(xOff, yOff);
		}
		
		@Override
		public void mouseWheelMoved(final MouseWheelEvent e)
		{
			/*final Point2D lastMousePos;
			final Point2D mousePos;
			
			lastMousePos= new Point2D.Double(e.getX() / scale, e.getY() / scale);*/
			scale -= e.getPreciseWheelRotation()/10;
			/*mousePos = new Point2D.Double(lastMousePos.getX()*scale, lastMousePos.getY()*scale);
			
			xOff -= mousePos.getX() - e.getX();
			yOff -= mousePos.getY() - e.getY();
			*/
			repaint();
		}
		
		@Override
		public void mouseDragged(final MouseEvent e)
		{
			if (lastPressedPoint == null || lastOffset == null)
			{
				return;
			}
			
			autoCenter = false;
			
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			xOff = (int) (lastOffset.getX() + e.getX() - lastPressedPoint.getX());
			yOff = (int) (lastOffset.getY() + e.getY() - lastPressedPoint.getY());
			
			repaint();
		}
		
		@Override
		public void mouseReleased(final MouseEvent e)
		{
			setCursor(Cursor.getDefaultCursor());
		}
	}
}
