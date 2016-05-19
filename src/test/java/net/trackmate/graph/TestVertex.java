package net.trackmate.graph;

import static net.trackmate.pool.ByteUtils.INT_SIZE;

import net.trackmate.graph.ref.AbstractVertex;
import net.trackmate.graph.ref.AbstractVertexPool;
import net.trackmate.pool.ByteMappedElement;

public class TestVertex extends AbstractVertex< TestVertex, TestEdge, ByteMappedElement >
{
	protected static final int ID_OFFSET = AbstractVertex.SIZE_IN_BYTES;

	protected static final int SIZE_IN_BYTES = ID_OFFSET + INT_SIZE;

	public final AbstractVertexPool< TestVertex, ?, ByteMappedElement > creatingPool;

	protected TestVertex( final AbstractVertexPool< TestVertex, ?, ByteMappedElement > pool )
	{
		super( pool );
		creatingPool = pool;
	}

	public TestVertex init( final int id )
	{
		setId( id );
		return this;
	}

	public int getId()
	{
		return access.getInt( ID_OFFSET );
	}

	public void setId( final int id )
	{
		access.putInt( id, ID_OFFSET );
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "v(" );
		sb.append( getId() );
		sb.append( ")" );
		return sb.toString();
	}
}
