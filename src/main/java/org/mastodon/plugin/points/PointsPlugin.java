package org.mastodon.plugin.points;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.plugin.MastodonPlugin;
import org.mastodon.plugin.MastodonPluginAppModel;
import org.mastodon.revised.mamut.MamutAppModel;
import org.mastodon.revised.model.AbstractModelImporter;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.util.FileChooser;
import org.mastodon.revised.ui.util.ExtensionFileFilter;
import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import net.imglib2.realtransform.AffineTransform3D;

@Plugin( type = PointsPlugin.class )
public class PointsPlugin extends AbstractContextual implements MastodonPlugin
{
	//"IDs" of all plug-ins wrapped in this class
	private static final String PP_IMPORT = "PP-import-all";
	private static final String PP_EXPORT = "PP-export-all";
	//------------------------------------------------------------------------

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		//this places the plug-in's menu items into the menu,
		//the titles of the items are defined right below
		return Arrays.asList(
				menu( "Plugins",
						menu( "Point cloud",
								item( PP_IMPORT ), item ( PP_EXPORT) ) ) );
	}

	/** titles of this plug-in's menu items */
	private static Map< String, String > menuTexts = new HashMap<>();
	static
	{
		menuTexts.put( PP_IMPORT, "Import from TXT format" );
		menuTexts.put( PP_EXPORT, "Export to TXT format" );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}
	//------------------------------------------------------------------------

	private final AbstractNamedAction actionImport;
	private final AbstractNamedAction actionExport;

	/** default c'tor: creates Actions available from this plug-in */
	public PointsPlugin()
	{
		actionImport = new RunnableAction( PP_IMPORT, this::importer );
		actionExport = new RunnableAction( PP_EXPORT, this::exporter );
		updateEnabledActions();
	}

	/** register the actions to the application (with no shortcut keys) */
	@Override
	public void installGlobalActions( final Actions actions )
	{
		final String[] noShortCut = new String[] {};
		actions.namedAction( actionImport, noShortCut );
		actions.namedAction( actionExport, noShortCut );
	}

	/** reference to the currently available project in Mastodon */
	private MastodonPluginAppModel pluginAppModel;

	/** learn about the current project's params */
	@Override
	public void setAppModel( final MastodonPluginAppModel model )
	{
		//the application reports back to us if some project is available
		this.pluginAppModel = model;
		updateEnabledActions();
	}

	/** enables/disables menu items based on the availability of some project */
	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ? null : pluginAppModel.getAppModel();
		actionImport.setEnabled( appModel != null );
		actionExport.setEnabled( appModel != null );
	}
	//------------------------------------------------------------------------

	/** opens the import dialog to find the tracks.txt file,
	    and runs the import on the currently viewed images
	    provided params were harvested successfully */
	private void importer()
	{
		//open a folder choosing dialog
		File selectedFile = FileChooser.chooseFile(null, null,
				new ExtensionFileFilter("txt"),
				"Choose TXT file to write point cloud from:",
				FileChooser.DialogType.LOAD,
				FileChooser.SelectionMode.FILES_ONLY);

		//cancel button ?
		if (selectedFile == null) return;

		//check we can open the file; and complain if not
		if (selectedFile.canRead() == false)
			throw new IllegalArgumentException("Cannot read the selected file: "+selectedFile.getAbsolutePath());

		Scanner s = null;

		//define the spot size/radius
		final double radius = 10;
		final double[][] cov = new double[3][3];
		cov[0][0] = radius*radius;
		cov[1][1] = radius*radius;
		cov[2][2] = radius*radius;

		final Model model = pluginAppModel.getAppModel().getModel();
		final ModelGraph graph = model.getGraph();
		final Spot spot = graph.vertices().createRef();

		final AffineTransform3D transform = new AffineTransform3D();
		pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSourceTransform(0,0, transform);
		final double[] coords = new double[3];

		new AbstractModelImporter< Model >( model ){{ startImport(); }};

		try {
			s = new Scanner(new BufferedReader(new FileReader(selectedFile.getAbsolutePath())));

			while (s.hasNext())
			{
				coords[0] = Float.parseFloat(s.next());
				coords[1] = Float.parseFloat(s.next());
				coords[2] = Float.parseFloat(s.next());

				//create the spot
				transform.apply(coords,coords);
				graph.addVertex( spot ).init( 0, coords, cov );
			}
		} catch (IOException e) {
			//anyway, send the original error message further
			e.printStackTrace();
		} finally {
			if (s != null)
			{
				s.close();
			}
			graph.vertices().releaseRef(spot);
			new AbstractModelImporter< Model >( model ){{ finishImport(); }};
		}

		this.context().getService("LogService.class").log().info("Loaded file: "+selectedFile.getAbsolutePath());
	}

	/** opens the export dialog, and runs the export
	    provided params were harvested successfully */
	private void exporter()
	{
		//open a folder choosing dialog
		File selectedFile = FileChooser.chooseFile(null, null,
				new ExtensionFileFilter("txt"),
				"Choose TXT file to write point cloud to:",
				FileChooser.DialogType.SAVE,
				FileChooser.SelectionMode.FILES_ONLY);

		//cancel button ?
		if (selectedFile == null) return;

		//check we can open the file; and complain if not
		if (selectedFile.canWrite() == false)
			throw new IllegalArgumentException("Cannot write the selected file: "+selectedFile.getAbsolutePath());

		System.out.println("writer");
	}
}
