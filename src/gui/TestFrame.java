package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

import gui.custom.VectorMapView;
import gui.observable.Observables;

/**
 * Frame principal pour l'interface de test.
 */
public class TestFrame extends JFrame
{
	/**
	 *  UID par défaut pour la sérialisation.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Controlleur de l'interface.
	 */
	private final TestController controller;
	
	public TestFrame()
	{
		controller = new TestController(this);
		/*
		try
		{
			// Prendre le style du sytème.
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}*/
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		setTitle("Prédiction temporelle avec compensation de mouvement");
		
		buildFrame();
		
		pack();
		setSize(500, 500);
	}
	
	/**
	 * Construit l'interface complète.
	 */
	private void buildFrame()
	{
		final JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, buildViewPanel(), buildParameterPanel());
		
		centerPane.setResizeWeight(1);
		Observables.notNull(controller.getSequencePathPrefix()).addListener(b->centerPane.resetToPreferredSizes());
		
		add(buildToolBar(), BorderLayout.NORTH);
		add(centerPane, BorderLayout.CENTER);
	}
	
	/**
	 * Construit la barre d'outil supérieure.
	 * @return barre d'outil supérieure.
	 */
	private JToolBar buildToolBar()
	{
		final JToolBar toolBar = new JToolBar();
		final JButton openBtn  = new JButton(UIManager.getIcon("Tree.openIcon")),
					  startBtn = new JButton("Encodage");
		
		toolBar.setFloatable(false);

		startBtn.setEnabled(false);
		Observables.notNull(controller.getSequencePathPrefix()).addListener(startBtn::setEnabled);
		
		openBtn.addActionListener(controller::handleOpenFile);
		startBtn.addActionListener(controller::handleStart);
		
		toolBar.add(openBtn);
		toolBar.addSeparator();
		toolBar.add(startBtn);
		
		return toolBar;
	}
	
	/**
	 * Construit le panneau des paramètres de l'encodage.
	 * @return panneau des paramètres de l'encodage.
	 */
	private JPanel buildParameterPanel()
	{
		final JPanel parameterPanel = new JPanel();
		final JPanel formPanel = new JPanel(new GridBagLayout())
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public Dimension getMaximumSize()
			{
				return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
			}
		 };
		
		final JTextField dctSizeField 	   = new JTextField(),
						 movementSizeField = new JTextField(),
						 quantifScaleField = new JTextField();
		
		parameterPanel.setLayout(new BoxLayout(parameterPanel, BoxLayout.Y_AXIS));
		parameterPanel.setVisible(false);
		Observables.notNull(controller.getSequencePathPrefix()).addListener(parameterPanel::setVisible);
		// Marges du panneau.
		parameterPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20),
																	BorderFactory.createTitledBorder("Paramètres de l'encodage")));
		
		Observables.bind(movementSizeField, controller.movementBlockSizeProperty(), Integer::parseInt);
		Observables.bind(dctSizeField, controller.dctBlockSizeProperty(), Integer::parseInt);
		Observables.bind(quantifScaleField, controller.quantifScaleProperty(), Integer::parseInt);
		
		final JComponent[][] formComponents = {
			{new JLabel("Taille des blocs prédiction de mouvement"), movementSizeField},
			{new JLabel("Taille des blocs DCT"), dctSizeField},
			{new JLabel("Echelle de quantification"), quantifScaleField},
		};
		
		populateForm(formPanel, new Insets(5, 5, 5, 5), formComponents);
		parameterPanel.add(formPanel);
		return parameterPanel;
	}
	
	/**
	 * Construit le panneau de visualisation.
	 * @return panneau de visualisation.
	 */
	private JPanel buildViewPanel()
	{
		final JPanel viewPanel = new JPanel(new BorderLayout());
		final JPanel resultPanel = new JPanel(new GridLayout(2, 2, 20, 20));
		
		final JLabel originalImg = new JLabel("", JLabel.CENTER),	// Image originale.
					 reconstImg  = new JLabel("", JLabel.CENTER),	// Image reconstruite.
					 errorsImg   = new JLabel("", JLabel.CENTER);	// Carte des erreurs.
		
		final VectorMapView movementMap = new VectorMapView();	// Carte des vecteurs de mouvement.
		
		final JSlider progressSlider = new JSlider(0, 0);
		progressSlider.setPaintTicks(true);
		progressSlider.setSnapToTicks(true);
		progressSlider.setMinorTickSpacing(1);
		progressSlider.setMajorTickSpacing(1);
		progressSlider.setPaintLabels(true);
		
		viewPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		originalImg.setBorder(BorderFactory.createTitledBorder("Trame originale"));
		movementMap.setBorder(BorderFactory.createTitledBorder("Vecteurs de mouvement"));
		reconstImg .setBorder(BorderFactory.createTitledBorder("Image reconstruite"));
		errorsImg  .setBorder(BorderFactory.createTitledBorder("Erreurs de prédiction"));
		
		movementMap.setBackground(Color.WHITE);
		
		// Coordonne les résultats et les labels d'image.
		controller.getCodingResults().originalImg.addListener(img->setLabelImage(originalImg, img));
		controller.getCodingResults().reconstImg.addListener(img->setLabelImage(reconstImg, img));
		controller.getCodingResults().errorsImg.addListener(img->setLabelImage(errorsImg, img));
		
		controller.getCodingResults().movementMap.addListener(map->SwingUtilities.invokeLater(()->
			movementMap.setVectorMap(map, controller.movementBlockSizeProperty().get())));
		
		progressSlider.setVisible(false);
		controller.videoResultsProperty().addListener(results->
		{
			if (results != null && results.size() > 0)
			{
				progressSlider.setMaximum(results.size()-1);
				for (final ChangeListener listener : progressSlider.getChangeListeners())
				{
					progressSlider.removeChangeListener(listener);
				}
				
				progressSlider.addChangeListener(e->
				{
					controller.setVideoResult(progressSlider.getValue());
				});
				controller.setVideoResult(0);
				progressSlider.setValue(0);
				progressSlider.setVisible(true);
			}
			else
			{
				progressSlider.setVisible(false);
			}
		});
		
		resultPanel.add(originalImg);
		resultPanel.add(movementMap);
		resultPanel.add(reconstImg);
		resultPanel.add(errorsImg);
		
		viewPanel.add(resultPanel, BorderLayout.CENTER);
		viewPanel.add(progressSlider, BorderLayout.SOUTH);
		
		return viewPanel;
	}
	
	/**
	 * Définir l'image d'un label.
	 * 
	 * @param label
	 *            label sur lequel on modifie l'image.
	 * @param img
	 *            image du label.
	 */
	private void setLabelImage(final JLabel label, final BufferedImage img)
	{
		SwingUtilities.invokeLater(()->
		{
			label.setIcon(img != null ? 
							new ImageIcon(img) 
						: 
							null);
		});
	}
	
	/**
	 * Rempli un panneau formulaire avec les composants souhaités.
	 * 
	 * @param formPanel
	 *            panneau formulaire.
	 * @param insets
	 *            marges entres les composants.
	 * @param formComponents
	 *            composants à ajouter au panneau sur chaque ligne, doit être de
	 *            dimension (nbr_ligne, 2).
	 * @throws IllegalArgumentException
	 *             si le tableau des composants est malformé.
	 */
	private void populateForm(final JPanel formPanel, final Insets insets, final JComponent[][] formComponents) throws IllegalArgumentException
	{
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = insets;
		
		for (int row = 0; row < formComponents.length; ++row)
		{
			final JComponent[] componentRow = formComponents[row];
			
			if (componentRow.length != 2)
			{
				throw new IllegalArgumentException("Ligne du formulaire malformée.");
			}
			
			/**/c.gridx = 0; c.gridy = row; c.weightx = 0;/**/
			formPanel.add(componentRow[0], c);
			
			/**/c.gridx = 1; c.gridy = row; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;/**/
			formPanel.add(componentRow[1], c);
		}
	}
	
	/**
	 * Point d'entrée interface de test.
	 * 
	 * @param args
	 *            arguments en entrée.
	 */
	public static void main(final String[] args)
	{
		new TestFrame().setVisible(true);
	}

}
