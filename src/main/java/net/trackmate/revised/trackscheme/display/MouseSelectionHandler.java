package net.trackmate.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeEdge;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

public class MouseSelectionHandler implements MouseListener, MouseMotionListener, OverlayRenderer
{
	private static final double SELECT_DISTANCE_TOLERANCE = 5.0;

	private static final int MOUSE_MASK = InputEvent.BUTTON1_DOWN_MASK;

	private static final int MOUSE_MASK_ADDTOSELECTION = InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;

	private static final int MOUSE_MASK_CLICK = InputEvent.BUTTON1_MASK;

	private static final int MOUSE_MASK_CLICK_ADDTOSELECTION = InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK;

	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeSelection selection;

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * Coordinates where mouse dragging currently is.
	 */
	private int eX, eY;

	private boolean dragStarted = false;

	private final InteractiveDisplayCanvasComponent< ScreenTransform > display;

	private final LineageTreeLayout layout;

	private final TrackSchemeGraph< ?, ? > graph;

	public MouseSelectionHandler( final AbstractTrackSchemeOverlay graphOverlay, final TrackSchemeSelection selection, final InteractiveDisplayCanvasComponent< ScreenTransform > display, final LineageTreeLayout layout, final TrackSchemeGraph< ?, ? > graph )
	{
		this.graphOverlay = graphOverlay;
		this.selection = selection;
		this.display = display;
		this.layout = layout;
		this.graph = graph;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		if ( e.getModifiers() == MOUSE_MASK_CLICK || e.getModifiers() == MOUSE_MASK_CLICK_ADDTOSELECTION )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					final boolean clear = !( e.getModifiers() == MOUSE_MASK_CLICK_ADDTOSELECTION );
					select( e.getX(), e.getY(), clear );
				}
			} );
		}
	}

	private void select( final int x, final int y, final boolean clear )
	{
		final int vertexId = graphOverlay.getVertexIdAt( x, y );
		if ( vertexId < 0 )
		{
			// See if we can select an edge.
			final int edgeId = graphOverlay.getEdgeIdAt( x, y, SELECT_DISTANCE_TOLERANCE );
			if ( edgeId < 0 )
			{
				if ( clear )
					selection.clearSelection();
				return;
			}
			if ( clear )
			{
				selection.clearSelection();
				selection.setEdgeSelected( edgeId, true );
			}
			else
			{
				selection.toggleEdge( edgeId );
			}
		}
		else
		{
			if ( clear )
			{
				selection.clearSelection();
				selection.setVertexSelected( vertexId, true );
			}
			else
			{
				selection.toggleVertex( vertexId );
			}
		}
	}


	@Override
	public void mouseDragged( final MouseEvent e )
	{
		if ( e.getModifiersEx() == MOUSE_MASK || e.getModifiersEx() == MOUSE_MASK_ADDTOSELECTION )
		{
			eX = e.getX();
			eY = e.getY();
			if ( dragStarted == false )
			{
				dragStarted = true;
				oX = e.getX();
				oY = e.getY();
			}
			display.repaint();
		}
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		if ( dragStarted )
		{
			dragStarted = false;

			display.repaint();
			final boolean clear = !( ( e.getModifiersEx() & MOUSE_MASK_ADDTOSELECTION ) != 0 );
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					selectWithin( oX, oY, eX, eY, clear );
				}
			} );
		}
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mouseMoved( final MouseEvent e )
	{}

	/**
	 * Draws the selection box, if there is one.
	 */
	@Override
	public void drawOverlays( final Graphics g )
	{
		if ( !dragStarted )
			return;
		g.setColor( Color.RED );
		final int x = Math.min( oX, eX );
		final int y = Math.min( oY, eY );
		final int width = Math.abs( eX - oX );
		final int height = Math.abs( eY - oY );
		g.drawRect( x, y, width, height );
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	/*
	 * PRIVATE METHODS
	 */

	private void selectWithin( final int x1, final int y1, final int x2, final int y2, final boolean clear )
	{
		final ScreenTransform transform = display.getTransformEventHandler().getTransform();

		if ( clear )
		{
			selection.clearSelection();
		}

		final double lx1 = transform.screenToLayoutX( x1 );
		final double ly1 = transform.screenToLayoutY( y1 );
		final double lx2 = transform.screenToLayoutX( x2 );
		final double ly2 = transform.screenToLayoutY( y2 );

		final RefSet< TrackSchemeVertex > vs = layout.getVerticesWithin( lx1, ly1, lx2, ly2 );
		final TrackSchemeVertex ref = graph.vertexRef();
		for ( final TrackSchemeVertex v : vs )
		{
			selection.setVertexSelected( v.getInternalPoolIndex(), true );
			for ( final TrackSchemeEdge e : v.outgoingEdges() )
			{
				final TrackSchemeVertex t = e.getTarget( ref );
				if ( vs.contains( t ) )
				{
					selection.setEdgeSelected( e.getInternalPoolIndex(), true );
				}
			}
		}
		graph.releaseRef( ref );
	}
}
