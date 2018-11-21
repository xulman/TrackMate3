package org.mastodon.plugin.points;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.util.FileChooser;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.revised.ui.util.ExtensionFileFilter;
import org.scijava.AbstractContextual;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import net.imglib2.realtransform.AffineTransform3D;

@Plugin( type = PointsPlugin.class )
public class PointsPlugin extends AbstractContextual implements MastodonPlugin
{
	//"IDs" of all plug-ins wrapped in this class
	private static final String PP_IMPORT   = "PP-import-all";
	private static final String PP_IMPORTFC = "PP-import-allFourColumns";
	private static final String PP_EXPORT   = "PP-export-all";
	private static final String PP_EXPORTPF = "PP-export-allPerFiles";
	//------------------------------------------------------------------------

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		//this places the plug-in's menu items into the menu,
		//the titles of the items are defined right below
		return Arrays.asList(
				menu( "Plugins",
						menu( "Point cloud",
								item( PP_IMPORT ), item( PP_IMPORTFC ),
								item( PP_EXPORT ), item( PP_EXPORTPF ) ) ) );
	}

	/** titles of this plug-in's menu items */
	private static Map< String, String > menuTexts = new HashMap<>();
	static
	{
		menuTexts.put( PP_IMPORT,   "Import from TXT format (3 cols)" );
		menuTexts.put( PP_IMPORTFC, "Import from TXT format (4 cols)" );
		menuTexts.put( PP_EXPORT,   "Export to TXT format (one file)" );
		menuTexts.put( PP_EXPORTPF, "Export to TXT format (many files)" );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}
	//------------------------------------------------------------------------

	private final AbstractNamedAction actionImport;
	private final AbstractNamedAction actionImportfc;
	private final AbstractNamedAction actionExport;
	private final AbstractNamedAction actionExportpf;

	/** default c'tor: creates Actions available from this plug-in */
	public PointsPlugin()
	{
		actionImport   = new RunnableAction( PP_IMPORT,   this::importer );
		actionImportfc = new RunnableAction( PP_IMPORTFC, this::importerFC );
		actionExport   = new RunnableAction( PP_EXPORT,   this::exporter );
		actionExportpf = new RunnableAction( PP_EXPORTPF, this::exporterPerFile );
		updateEnabledActions();
	}

	/** register the actions to the application (with no shortcut keys) */
	@Override
	public void installGlobalActions( final Actions actions )
	{
		final String[] noShortCut = new String[] {};
		actions.namedAction( actionImport,   noShortCut );
		actions.namedAction( actionImportfc, noShortCut );
		actions.namedAction( actionExport,   noShortCut );
		actions.namedAction( actionExportpf, noShortCut );
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
		actionImport.setEnabled(   appModel != null );
		actionImportfc.setEnabled( appModel != null );
		actionExport.setEnabled(   appModel != null );
		actionExportpf.setEnabled( appModel != null );
	}
	//------------------------------------------------------------------------

	private void importer()
	{ importerGeneric(false); }

	private void importerFC()
	{ importerGeneric(true); }

	private void importerGeneric(final boolean fourthColumnIsTime)
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

		//-------------------------------------------------
		//scanning params:
		final String delim = "\t";
		final double spotRadius = 10;
		//-------------------------------------------------

		//define the spot size/radius
		final double[][] cov = new double[3][3];
		cov[0][0] = spotRadius*spotRadius;
		cov[1][1] = spotRadius*spotRadius;
		cov[2][2] = spotRadius*spotRadius;

		final Model model = pluginAppModel.getAppModel().getModel();
		final ModelGraph graph = model.getGraph();
		Spot spot = graph.vertices().createRef();
		final Spot oSpot = graph.vertices().createRef();
		final Link linkRef = graph.edgeRef();

		final AffineTransform3D transform = new AffineTransform3D();
		pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSourceTransform(0,0, transform);

		final int timeF = pluginAppModel.getAppModel().getMinTimepoint();
		final int timeT = pluginAppModel.getAppModel().getMaxTimepoint();

		final double[] coords = new double[3];
		int time;

		new AbstractModelImporter< Model >( model ){{ startImport(); }};

		try {
			s = new Scanner(new BufferedReader(new FileReader(selectedFile.getAbsolutePath())));

			while (s.hasNext())
			{
				//read and prepare the spot spatial coordinate
				s.useDelimiter(delim);
				coords[0] = Float.parseFloat(s.next());
				coords[1] = Float.parseFloat(s.next());
				if (!fourthColumnIsTime) s.reset();
				coords[2] = Float.parseFloat(s.next());
				transform.apply(coords,coords);

				if (fourthColumnIsTime)
				{
					//add to the parsed-out time
					s.reset();
					time = Integer.parseInt(s.next());
					graph.addVertex( spot ).init( time, coords, cov );
				}
				else
				{
					//add to all time points available, connect them with edges
					spot = graph.addVertex( spot ).init( timeF, coords, cov );
					oSpot.refTo(spot);

					for (int t = timeF+1; t <= timeT; ++t)
					{
						spot = graph.addVertex( spot ).init( t, coords, cov );
						graph.addEdge( oSpot, spot, linkRef ).init();
						oSpot.refTo(spot);
					}
				}
			}
		} catch (IOException e) {
			//report the original error message further
			e.printStackTrace();
		} finally {
			if (s != null)
			{
				s.close();
			}
			graph.vertices().releaseRef(spot);
			graph.vertices().releaseRef(oSpot);
			graph.releaseRef(linkRef);
			new AbstractModelImporter< Model >( model ){{ finishImport(); }};
		}

		this.context().getService(LogService.class).log().info("Loaded file: "+selectedFile.getAbsolutePath());
	}


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

		//-------------------------------------------------
		//writing params:
		final String delim = "\t";
		final int timeF = pluginAppModel.getAppModel().getMinTimepoint();
		final int timeT = pluginAppModel.getAppModel().getMaxTimepoint();
		//-------------------------------------------------

		AffineTransform3D transform = new AffineTransform3D();
		pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSourceTransform(0,0, transform);
		transform = transform.inverse();
		final double[] coords = new double[3];

		final SpatioTemporalIndex< Spot > spots = pluginAppModel.getAppModel().getModel().getSpatioTemporalIndex();
		BufferedWriter f;

		try
		{
			f = new BufferedWriter( new FileWriter(selectedFile.getAbsolutePath()) );

			for (int t = timeF; t <= timeT; ++t)
			for (final Spot s : spots.getSpatialIndex(t))
			{
				//convert spot's coordinate into underlying image coordinate system
				s.localize(coords);
				transform.apply(coords,coords);

				f.write(coords[0]+delim
				       +coords[1]+delim
				       +coords[2]+delim
				       +t);
				f.newLine();
			}
			f.close();
		}
		catch (IOException e) {
			//report the original error message further
			e.printStackTrace();
		}

		this.context().getService(LogService.class).log().info("Wrote file: "+selectedFile.getAbsolutePath());
	}


	private void exporterPerFile()
	{
		//open a folder choosing dialog
		File selectedFolder = FileChooser.chooseFile(null, null,
				new ExtensionFileFilter("txt"),
				"Choose folder to write TXT files with point clouds:",
				FileChooser.DialogType.SAVE,
				FileChooser.SelectionMode.DIRECTORIES_ONLY);

		//cancel button ?
		if (selectedFolder == null) return;

		//check we can open the file; and complain if not
		if (selectedFolder.canWrite() == false)
			throw new IllegalArgumentException("Cannot write to the selected folder: "+selectedFolder.getAbsolutePath());

		//-------------------------------------------------
		//writing params:
		final String delim = "\t";
		final int timeF = pluginAppModel.getAppModel().getMinTimepoint();
		final int timeT = pluginAppModel.getAppModel().getMaxTimepoint();
		final String fileNamePattern = "pointCloud_t%03d.txt";
		//-------------------------------------------------

		AffineTransform3D transform = new AffineTransform3D();
		pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSourceTransform(0,0, transform);
		transform = transform.inverse();
		final double[] coords = new double[3];

		final SpatioTemporalIndex< Spot > spots = pluginAppModel.getAppModel().getModel().getSpatioTemporalIndex();
		BufferedWriter f;

		try
		{
			for (int t = timeF; t <= timeT; ++t)
			{
				f = new BufferedWriter( new FileWriter(
					selectedFolder.getAbsolutePath() + File.separator + String.format(fileNamePattern,t)
					) );

				for (final Spot s : spots.getSpatialIndex(t))
				{
					//convert spot's coordinate into underlying image coordinate system
					s.localize(coords);
					transform.apply(coords,coords);

					f.write(coords[0]+delim
					       +coords[1]+delim
					       +coords[2]);
					f.newLine();
				}

				f.close();
			}
		}
		catch (IOException e) {
			//report the original error message further
			e.printStackTrace();
		}

		this.context().getService(LogService.class).log().info("Done exporting.");
	}
}
