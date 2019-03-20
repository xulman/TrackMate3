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
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.Link;
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
import net.imglib2.util.Util;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

@Plugin( type = ShifterPlugin.class )
public class ShifterPlugin extends AbstractContextual implements MastodonPlugin
{
	//"IDs" of all plug-ins wrapped in this class
	private static final String SP_Z_SHIFT01  = "SP-zshift01";
	private static final String SP_Z_SHIFT02  = "SP-zshift02";
	private static final String SP_Z_SMOOTH = "SP-zsmooth";
	private static final String SP_XY_SHIFT  = "SP-xyshift";
	private static final String SP_XY_SMOOTH = "SP-xysmooth";
	//------------------------------------------------------------------------

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		//this places the plug-in's menu items into the menu,
		//the titles of the items are defined right below
		return Arrays.asList(
				menu( "Plugins",
								item( SP_Z_SHIFT01 ), item( SP_Z_SHIFT02 ), item( SP_Z_SMOOTH ),
								item( SP_XY_SHIFT ), item( SP_XY_SMOOTH ) ) );
	}

	/** titles of this plug-in's menu items */
	private static Map< String, String > menuTexts = new HashMap<>();
	static
	{
		menuTexts.put( SP_Z_SHIFT01,  "HARDCODED Z SHIFTS for SEQ 01");
		menuTexts.put( SP_Z_SHIFT02,  "HARDCODED Z SHIFTS for SEQ 02");
		menuTexts.put( SP_Z_SMOOTH, "Shift points along z by z-coord smoothing");
		menuTexts.put( SP_XY_SHIFT,  "Shift points along xy-gradients a little");
		menuTexts.put( SP_XY_SMOOTH, "Shift points along xy by xy-coord smoothing");
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}
	//------------------------------------------------------------------------

	private final AbstractNamedAction actionZShift01,actionZShift02;
	private final AbstractNamedAction actionZSmooth;
	private final AbstractNamedAction actionXYShift;
	private final AbstractNamedAction actionXYSmooth;

	/** default c'tor: creates Actions available from this plug-in */
	public ShifterPlugin()
	{
		actionZShift01  = new RunnableAction( SP_Z_SHIFT01,  this::pointsZShifter01 );
		actionZShift02  = new RunnableAction( SP_Z_SHIFT02,  this::pointsZShifter02 );
		actionZSmooth = new RunnableAction( SP_Z_SMOOTH, this::pointsZSmoother );
		actionXYShift  = new RunnableAction( SP_XY_SHIFT,  this::pointsXYShifter);
		actionXYSmooth = new RunnableAction( SP_XY_SMOOTH, this::pointsXYSmoother);
		updateEnabledActions();
	}

	/** register the actions to the application (with no shortcut keys) */
	@Override
	public void installGlobalActions( final Actions actions )
	{
		final String[] noShortCut = new String[] {};
		actions.namedAction( actionZShift01,  noShortCut );
		actions.namedAction( actionZShift02,  noShortCut );
		actions.namedAction( actionZSmooth, noShortCut );
		actions.namedAction( actionXYShift,  noShortCut );
		actions.namedAction( actionXYSmooth, noShortCut );
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
		actionZShift01.setEnabled(  appModel != null );
		actionZShift02.setEnabled(  appModel != null );
		actionZSmooth.setEnabled( appModel != null );
		actionXYShift.setEnabled(  appModel != null );
		actionXYSmooth.setEnabled( appModel != null );
	}
	//------------------------------------------------------------------------

	private void pointsZShifter01() { pointsZShifter(1); }
	private void pointsZShifter02() { pointsZShifter(2); }

	private void pointsZShifter(final int seq)
	{
		final Corrections corrector = new Corrections(seq);

		final SpatioTemporalIndex< Spot > spots = pluginAppModel.getAppModel().getModel().getSpatioTemporalIndex();

		final double[] coords = new double[3];

		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ startUpdate(); }};

		for (Integer t : corrector.listTimePoints())
		{
			AffineTransform3D transform = new AffineTransform3D();
			pluginAppModel.getAppModel().getSharedBdvData().getSources().get(0).getSpimSource().getSourceTransform(t,0, transform);
			transform = transform.inverse();

			for (final Spot s : spots.getSpatialIndex(t))
			{
				//convert spot's coordinate into underlying image coordinate system
				s.localize(coords);
				transform.apply(coords,coords);

				final int newz = corrector.suggestZ( (int)t,
					(int)Math.round(coords[0]),
					(int)Math.round(coords[1]) );

				if (newz > -1 && (double)newz != coords[2])
				{
					System.out.println(String.format("%03d: ",t)
					  +"should shift from "+Util.printCoordinates(s)+" to newz="+newz);

					coords[2] = newz;
					transform.applyInverse(coords,coords);

					s.setLabel( s.getLabel() + " Z" );
					s.setPosition(coords[2],2);
				}
			}
		}
		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ finishImport(); }};

		this.context().getService(LogService.class).log().info("done z max crawler.");
	}

	private void pointsZSmoother()
	{
		final SpatioTemporalIndex< Spot > spots = pluginAppModel.getAppModel().getModel().getSpatioTemporalIndex();
		final int timeF = pluginAppModel.getAppModel().getMinTimepoint();
		final int timeT = pluginAppModel.getAppModel().getMaxTimepoint();

		final ModelGraph modelGraph = pluginAppModel.getAppModel().getModel().getGraph();
		final Link lRef = modelGraph.edgeRef();
		final Spot[] ss = new Spot[2];
		ss[0] = modelGraph.vertices().createRef();
		ss[1] = modelGraph.vertices().createRef();

		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ startUpdate(); }};

		for (int t = timeF; t <= timeT; ++t)
		{
			for (final Spot s : spots.getSpatialIndex(t))
			if ( (s.incomingEdges().size() + s.outgoingEdges().size()) == 2 )
			{
				int c=0;
				for (int n=0; n < s.incomingEdges().size(); ++n)
					s.incomingEdges().get(n, lRef).getSource( ss[c++] );

				for (int n=0; n < s.outgoingEdges().size(); ++n)
					s.outgoingEdges().get(n, lRef).getTarget( ss[c++] );

				final float avgZ = s.getFloatPosition(2) + ss[0].getFloatPosition(2) + ss[1].getFloatPosition(2);
				s.setPosition( avgZ / 3.f, 2 );
			}
		}
		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ finishImport(); }};

		modelGraph.releaseRef(lRef);
		modelGraph.vertices().releaseRef(ss[0]);
		modelGraph.vertices().releaseRef(ss[1]);

		this.context().getService(LogService.class).log().info("done z smoother.");
	}
	//------------------------------------------------------------------------

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

				float val;
				int iters = 0;
				do
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
					val = (float)Math.sqrt( DX*DX + DY*DY ) / 2.f;

					f.write(s.getLabel()+" "+iters+" :\t"+val+"\t"+DX+"\t"+DY+"\t\t"+pxCoords[0]+"\t"+pxCoords[1]+"\t"+pxCoords[2]);
					f.newLine();

					if (val > 20)
					{
						//we adjust
						double azimuth = Math.atan2(DY,DX);
						pxCoords[0] += Math.round( Math.cos(azimuth) );
						pxCoords[1] += Math.round( Math.sin(azimuth) );
						p.setPosition(pxCoords);
						++iters;
					}
				} while (val > 20 && iters < 7);

				s.setLabel( s.getLabel() + String.format(" %dXY", iters) );
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

		this.context().getService(LogService.class).log().info("done xy grad crawler.");
	}

	private void pointsXYSmoother()
	{
		final SpatioTemporalIndex< Spot > spots = pluginAppModel.getAppModel().getModel().getSpatioTemporalIndex();
		final int timeF = pluginAppModel.getAppModel().getMinTimepoint();
		final int timeT = pluginAppModel.getAppModel().getMaxTimepoint();

		final ModelGraph modelGraph = pluginAppModel.getAppModel().getModel().getGraph();
		final Link lRef = modelGraph.edgeRef();
		final Spot[] ss = new Spot[2];
		ss[0] = modelGraph.vertices().createRef();
		ss[1] = modelGraph.vertices().createRef();

		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ startUpdate(); }};

		for (int t = timeF; t <= timeT; ++t)
		{
			for (final Spot s : spots.getSpatialIndex(t))
			if ( (s.incomingEdges().size() + s.outgoingEdges().size()) == 2 )
			{
				int c=0;
				for (int n=0; n < s.incomingEdges().size(); ++n)
					s.incomingEdges().get(n, lRef).getSource( ss[c++] );

				for (int n=0; n < s.outgoingEdges().size(); ++n)
					s.outgoingEdges().get(n, lRef).getTarget( ss[c++] );

				float avgC = s.getFloatPosition(0) + ss[0].getFloatPosition(0) + ss[1].getFloatPosition(0);
				s.setPosition( avgC / 3.f, 0 );

				avgC = s.getFloatPosition(1) + ss[0].getFloatPosition(1) + ss[1].getFloatPosition(1);
				s.setPosition( avgC / 3.f, 1 );
			}
		}
		new AbstractModelImporter< Model >(pluginAppModel.getAppModel().getModel()) {{ finishImport(); }};

		modelGraph.releaseRef(lRef);
		modelGraph.vertices().releaseRef(ss[0]);
		modelGraph.vertices().releaseRef(ss[1]);

		this.context().getService(LogService.class).log().info("done xy smoother.");
	}
}
