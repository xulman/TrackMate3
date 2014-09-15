package net.trackmate.model.abstractmodel;

import static net.trackmate.util.mempool.ByteUtils.INDEX_SIZE;
import static net.trackmate.util.mempool.ByteUtils.INT_SIZE;
import net.trackmate.util.mempool.MappedElement;

/**
 * TODO: javadoc
 *
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class AbstractVertex< T extends MappedElement, E extends AbstractEdge< ?, ? > > extends PoolObject< T >
{
	protected static final int ID_OFFSET = 0;
	protected static final int FIRST_IN_EDGE_INDEX_OFFSET = ID_OFFSET + INT_SIZE;
	protected static final int FIRST_OUT_EDGE_INDEX_OFFSET = FIRST_IN_EDGE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int SIZE_IN_BYTES = FIRST_OUT_EDGE_INDEX_OFFSET + INDEX_SIZE;

	protected AbstractVertex( final AbstractVertexPool< ?, T, ? > pool )
	{
		super( pool.getMemPool() );
	}

	protected int getId()
	{
		return access.getInt( ID_OFFSET );
	}

	protected void setId( final int id )
	{
		access.putInt( id, ID_OFFSET );
	}

	protected int getFirstInEdgeIndex()
	{
		return access.getIndex( FIRST_IN_EDGE_INDEX_OFFSET );
	}

	protected void setFirstInEdgeIndex( final int index )
	{
		access.putIndex( index, FIRST_IN_EDGE_INDEX_OFFSET );
	}

	protected int getFirstOutEdgeIndex()
	{
		return access.getIndex( FIRST_OUT_EDGE_INDEX_OFFSET );
	}

	protected void setFirstOutEdgeIndex( final int index )
	{
		access.putIndex( index, FIRST_OUT_EDGE_INDEX_OFFSET );
	}

	@Override
	protected void setToUninitializedState()
	{
		setFirstInEdgeIndex( -1 );
		setFirstOutEdgeIndex( -1 );
	}

	private IncomingEdges< E > incomingEdges;

	private OutgoingEdges< E > outgoingEdges;

	private AllEdges< E > edges;

	protected IncomingEdges< E > incomingEdges()
	{
		return incomingEdges;
	}

	protected OutgoingEdges< E > outgoingEdges()
	{
		return outgoingEdges;
	}

	protected AllEdges< E > edges()
	{
		return edges;
	}

	void linkEdgePool( final AbstractEdgePool< E, ?, ? > edgePool )
	{
		incomingEdges = new IncomingEdges< E >( this, edgePool );
		outgoingEdges = new OutgoingEdges< E >( this, edgePool );
		edges = new AllEdges< E >( this, edgePool );
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof AbstractVertex< ?, ? > &&
				access.equals( ( ( AbstractVertex< ?, ? > ) obj ).access );
	}

	@Override
	public int hashCode()
	{
		return access.hashCode();
	}

	public static interface Factory< S extends AbstractVertex< T, ? >, T extends MappedElement >
	{
		public int getSizeInBytes();

		public S createEmptyRef();
	}
}