package org.mastodon.plugin.points;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.plugin.MastodonPlugin;
import org.mastodon.plugin.MastodonPluginAppModel;
import org.mastodon.revised.mamut.MamutAppModel;
import org.mastodon.revised.model.AbstractModelImporter;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.scijava.AbstractContextual;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

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
		menuTexts.put( SP_ANALYZE, "Shift points along z to close int. max");
		menuTexts.put( SP_PROCESS, "Shift points along xy-gradients a little");
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
		actionAnalyze = new RunnableAction( SP_ANALYZE, this::pointsZShifter );
		actionProcess = new RunnableAction( SP_PROCESS, this::pointsXYShifter);
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

	private void pointsZShifter()
	{
		final SpatioTemporalIndex< Spot > spots = pluginAppModel.getAppModel().getModel().getSpatioTemporalIndex();
		final int timeF = pluginAppModel.getAppModel().getMinTimepoint();
		final int timeT = pluginAppModel.getAppModel().getMaxTimepoint();

		final double[] coords = new double[3];
		final long[] pxCoords = new long[3];

		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ startUpdate(); }};

		for (int t = timeF; t <= timeT; ++t)
		{
			AffineTransform3D transform = new AffineTransform3D();
			pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSourceTransform(t,0, transform);
			transform = transform.inverse();

			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<? extends RealType<?>> img = (RandomAccessibleInterval<? extends RealType<?>>) pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSource(t,0);
			ExtendedRandomAccessibleInterval<? extends RealType<?>, ?> imgB = Views.extendBorder(img);
			RandomAccess<? extends RealType<?>> p = imgB.randomAccess();

			for (final Spot s : spots.getSpatialIndex(t))
			{
				//convert spot's coordinate into underlying image coordinate system
				s.localize(coords);
				transform.apply(coords,coords);

				pxCoords[0] = Math.round(coords[0]);
				pxCoords[1] = Math.round(coords[1]);
				pxCoords[2] = 0; //Math.round(coords[2]);
				p.setPosition(pxCoords);

				//scan along z and find slice with max intensity
				float val = 0;
				final long zOrig = Math.round(coords[2]);
				long zAtVal = zOrig;
				for (long z = 0; z < 13; ++z)
				{
					p.setPosition(z,2);
					if (p.get().getRealFloat() > val)
					{
						val = p.get().getRealFloat();
						zAtVal = z;
					}
				}

				//don't move if detection would suggest to move too far
				if (Math.abs(zAtVal-zOrig) > 5) zAtVal = zOrig;

				s.setPosition(zAtVal,2); //should go through transform...
			}
		}
		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ finishImport(); }};

		this.context().getService(LogService.class).log().info("done.");
	}

	private void pointsXYShifter()
	{
		final SpatioTemporalIndex< Spot > spots = pluginAppModel.getAppModel().getModel().getSpatioTemporalIndex();
		final int timeF = pluginAppModel.getAppModel().getMinTimepoint();
		final int timeT = pluginAppModel.getAppModel().getMaxTimepoint();

		final double[] coords = new double[3];
		final long[] pxCoords = new long[3];

		try{
		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ startUpdate(); }};
		BufferedWriter f = new BufferedWriter(new FileWriter("/Users/ulman/DATA/CTC2/grads.txt"));

		for (int t = timeF; t <= timeT; ++t)
		{
			AffineTransform3D transform = new AffineTransform3D();
			pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSourceTransform(t,0, transform);
			transform = transform.inverse();

			@SuppressWarnings("unchecked")
			RandomAccessibleInterval<? extends RealType<?>> img = (RandomAccessibleInterval<? extends RealType<?>>) pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSource(t,0);
			ExtendedRandomAccessibleInterval<? extends RealType<?>, ?> imgB = Views.extendBorder(img);
			RandomAccess<? extends RealType<?>> p = imgB.randomAccess();

			for (final Spot s : spots.getSpatialIndex(t))
			{
				//convert spot's coordinate into underlying image coordinate system
				s.localize(coords);
				transform.apply(coords,coords);

				pxCoords[0] = Math.round(coords[0]);
				pxCoords[1] = Math.round(coords[1]);
				pxCoords[2] = Math.round(coords[2]);
				p.setPosition(pxCoords);

				int iters = 0;
				while (iters < 5)
				{
					//check values for xy gradient at this position
					p.fwd(0);
					float DX = p.get().getRealFloat();
					p.move(-2,0);
					DX -= p.get().getRealFloat();
					p.fwd(0);

					p.fwd(1);
					float DY = p.get().getRealFloat();
					p.move(-2,1);
					DY -= p.get().getRealFloat();
					p.fwd(1);

					//sq. of gradient
					float val = (float)Math.sqrt( DX*DX + DY*DY ) / 2.f;

					f.write(s.getLabel()+" "+iters+" :\t"+val+"\t"+DX+"\t"+DY+"\t\t"+pxCoords[0]+"\t"+pxCoords[1]+"\t"+pxCoords[2]);
					f.newLine();

					if (val > 20)
					{
						//we adjust
						double azimuth = Math.atan2(DY,DX);
						pxCoords[0] += Math.round( Math.cos(azimuth) );
						pxCoords[1] += Math.round( Math.sin(azimuth) );
						p.setPosition(pxCoords);
					}
					else
					{
						//no adjustment & stop iterating
						iters = 5;
					}

					++iters;
				}

				s.setPosition(pxCoords[0],0); //should go through transform...
				s.setPosition(pxCoords[1],1); //should go through transform...
			}
		}
		f.close();
		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ finishImport(); }};
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		this.context().getService(LogService.class).log().info("done.");
	}
}
