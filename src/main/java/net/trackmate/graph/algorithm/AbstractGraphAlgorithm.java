package net.trackmate.graph.algorithm;

import java.util.Iterator;

import net.trackmate.collection.RefCollections;
import net.trackmate.collection.RefDeque;
import net.trackmate.collection.RefIntMap;
import net.trackmate.collection.RefList;
import net.trackmate.collection.RefObjectMap;
import net.trackmate.collection.RefRefMap;
import net.trackmate.collection.RefSet;
import net.trackmate.collection.RefStack;
import net.trackmate.graph.CollectionUtils;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;

/**
 * Base class for graph algorithms. Provides helper functions aimed at
 * minimizing the pain of dealing with graphs that may or may not be ref based.
 *
 * @param <V>
 *            the {@link Vertex} type of the {@link Graph}.
 * @param <E>
 *            the {@link Edge} type of the {@link Graph}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class AbstractGraphAlgorithm< V extends Vertex< E >, E extends Edge< V > >
{
	protected final ReadOnlyGraph< V, E > graph;

	private final Assigner< V > vertexAssigner;

	private final Assigner< E > edgeAssigner;

	public AbstractGraphAlgorithm( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		final V v = graph.vertexRef();
		vertexAssigner = Assigner.getFor( v );
		graph.releaseRef( v );
		final E e = graph.edgeRef();
		edgeAssigner = Assigner.getFor( e );
		graph.releaseRef( e );
	}

	protected V assign( final V value, final V target )
	{
		return vertexAssigner.assign( value, target );
	}

	protected E assign( final E value, final E target )
	{
		return edgeAssigner.assign( value, target );
	}

	protected V vertexRef()
	{
		return graph.vertexRef();
	}

	protected E edgeRef()
	{
		return graph.edgeRef();
	}

	protected void releaseRef( final V ref )
	{
		graph.releaseRef( ref );
	}

	protected void releaseRef( final E ref )
	{
		graph.releaseRef( ref );
	}

	protected RefSet< V > createVertexSet()
	{
		return CollectionUtils.createVertexSet( graph );
	}

	protected RefSet< V > createVertexSet( final int initialCapacity )
	{
		return CollectionUtils.createVertexSet( graph, initialCapacity );
	}

	protected RefSet< E > createEdgeSet()
	{
		return CollectionUtils.createEdgeSet( graph );
	}

	protected RefSet< E > createEdgeSet( final int initialCapacity )
	{
		return CollectionUtils.createEdgeSet( graph, initialCapacity );
	}

	protected RefList< V > createVertexList()
	{
		return CollectionUtils.createVertexList( graph );
	}

	protected RefList< V > createVertexList( final int initialCapacity )
	{
		return CollectionUtils.createVertexList( graph, initialCapacity );
	}

	protected RefList< E > createEdgeList()
	{
		return CollectionUtils.createEdgeList( graph );
	}

	protected RefList< E > createEdgeList( final int initialCapacity )
	{
		return CollectionUtils.createEdgeList( graph, initialCapacity );
	}

	protected RefDeque< V > createVertexDeque()
	{
		return CollectionUtils.createVertexDeque( graph );
	}

	protected RefDeque< V > createVertexDeque( final int initialCapacity )
	{
		return CollectionUtils.createVertexDeque( graph, initialCapacity );
	}

	protected RefDeque< E > createEdgeDeque()
	{
		return CollectionUtils.createEdgeDeque( graph );
	}

	protected RefDeque< E > createEdgeDeque( final int initialCapacity )
	{
		return CollectionUtils.createEdgeDeque( graph, initialCapacity );
	}

	protected RefStack< V > createVertexStack()
	{
		return CollectionUtils.createVertexStack( graph );
	}

	protected RefStack< V > createVertexStack( final int initialCapacity )
	{
		return CollectionUtils.createVertexStack( graph, initialCapacity );
	}

	protected RefStack< E > createEdgeStack()
	{
		return CollectionUtils.createEdgeStack( graph );
	}

	protected RefStack< E > createEdgeStack( final int initialCapacity )
	{
		return CollectionUtils.createEdgeStack( graph, initialCapacity );
	}

	protected < O > RefObjectMap< V, O > createVertexObjectMap()
	{
		return CollectionUtils.createVertexObjectMap( graph);
	}

	protected < O > RefObjectMap< E, O > createEdgeObjectMap()
	{
		return CollectionUtils.createEdgeObjectMap( graph );
	}

	protected RefRefMap< V, E > createVertexEdgeMap()
	{
		return CollectionUtils.createVertexEdgeMap( graph );
	}

	protected RefRefMap< V, E > createVertexEdgeMap( final int initialCapacity )
	{
		return CollectionUtils.createVertexEdgeMap( graph, initialCapacity );
	}

	protected RefRefMap< E, V > createEdgeVertexMap()
	{
		return CollectionUtils.createEdgeVertexMap( graph );
	}

	protected RefRefMap< E, V > createEdgeVertexMap( final int initialCapacity )
	{
		return CollectionUtils.createEdgeVertexMap( graph, initialCapacity );
	}

	protected RefRefMap< V, V > createVertexVertexMap()
	{
		return CollectionUtils.createVertexVertexMap( graph );
	}

	protected RefRefMap< V, V > createVertexVertexMap( final int initialCapacity )
	{
		return CollectionUtils.createVertexVertexMap( graph, initialCapacity );
	}

	protected RefRefMap< E, E > createEdgeEdgeMap()
	{
		return CollectionUtils.createEdgeEdgeMap( graph );
	}

	protected RefRefMap< E, E > createEdgeEdgeMap( final int initialCapacity )
	{
		return CollectionUtils.createEdgeEdgeMap( graph, initialCapacity );
	}

	protected RefIntMap< V > createVertexIntMap( final int noEntryValue )
	{
		return CollectionUtils.createVertexIntMap( graph, noEntryValue );
	}

	protected RefIntMap< V > createVertexIntMap( final int noEntryValue, final int initialCapacity )
	{
		return CollectionUtils.createVertexIntMap( graph, noEntryValue, initialCapacity );
	}

	protected RefIntMap< E > createEdgeIntMap( final int noEntryValue )
	{
		return CollectionUtils.createEdgeIntMap( graph, noEntryValue );
	}

	protected RefIntMap< E > createEdgeIntMap( final int noEntryValue, final int initialCapacity )
	{
		return CollectionUtils.createEdgeIntMap( graph, noEntryValue, initialCapacity );
	}

	protected Iterator< V > safeVertexIterator( final Iterator< V > iterator )
	{
		return RefCollections.safeIterator( iterator, graph.vertices() );
	}

	protected Iterator< E > safeEdgeIterator( final Iterator< E > iterator )
	{
		return RefCollections.safeIterator( iterator, graph.edges() );
	}
}
