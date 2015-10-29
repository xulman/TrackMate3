package net.trackmate.revised.model.mamut;

import net.trackmate.revised.bdv.overlay.OverlayGraph;
import net.trackmate.revised.bdv.overlay.wrap.OverlayProperties;

/**
 * Provides spot {@link OverlayProperties properties} for BDV {@link OverlayGraph}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ModelOverlayProperties implements OverlayProperties< Spot >
{
	@Override
	public void localize( final Spot v, final float[] position )
	{
		v.localize( position );
	}

	@Override
	public void localize( final Spot v, final double[] position )
	{
		v.localize( position );
	}

	@Override
	public float getFloatPosition( final Spot v, final int d )
	{
		return v.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final Spot v, final int d )
	{
		return v.getDoublePosition( d );
	}

	@Override
	public int numDimensions( final Spot v )
	{
		return v.numDimensions();
	}

	@Override
	public void getCovariance( final Spot v, final double[][] mat )
	{
		v.getCovariance( mat );
	}

	@Override
	public int getTimepoint( final Spot v )
	{
		return v.getTimepoint();
	}
}