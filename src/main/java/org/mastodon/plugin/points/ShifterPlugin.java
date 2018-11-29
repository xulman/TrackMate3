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

@Plugin( type = ShifterPlugin.class )
public class ShifterPlugin extends AbstractContextual implements MastodonPlugin
{
	//"IDs" of all plug-ins wrapped in this class
	private static final String SP_ANALYZE = "SP-analyze";
	private static final String SP_PROCESS	= "SP-process";
	//------------------------------------------------------------------------

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		//this places the plug-in's menu items into the menu,
		//the titles of the items are defined right below
		return Arrays.asList(
				menu( "Plugins",
								item( SP_ANALYZE ), item( SP_PROCESS ) ) );
	}

	/** titles of this plug-in's menu items */
	private static Map< String, String > menuTexts = new HashMap<>();
	static
	{
		menuTexts.put( SP_ANALYZE, "Analyze gradients around points" );
		menuTexts.put( SP_PROCESS, "Shift points along gradients" );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}
	//------------------------------------------------------------------------

	private final AbstractNamedAction actionAnalyze;
	private final AbstractNamedAction actionProcess;

	/** default c'tor: creates Actions available from this plug-in */
	public ShifterPlugin()
	{
		actionAnalyze = new RunnableAction( SP_ANALYZE, this::pointsAnalyzer );
		actionProcess = new RunnableAction( SP_PROCESS, this::pointsShifter  );
		updateEnabledActions();
	}

	/** register the actions to the application (with no shortcut keys) */
	@Override
	public void installGlobalActions( final Actions actions )
	{
		final String[] noShortCut = new String[] {};
		actions.namedAction( actionAnalyze, noShortCut );
		actions.namedAction( actionProcess, noShortCut );
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
		actionAnalyze.setEnabled( appModel != null );
		actionProcess.setEnabled( appModel != null );
	}
	//------------------------------------------------------------------------

	private void pointsAnalyzer()
	{
		System.out.println("analyzer");
	}

	private void pointsShifter()
	{
		System.out.println("shifter");
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
}
