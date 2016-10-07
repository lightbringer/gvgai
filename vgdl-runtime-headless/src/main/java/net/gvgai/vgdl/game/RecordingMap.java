package net.gvgai.vgdl.game;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

public class RecordingMap<T> {
    private final static int iConstant = 37;

    private static Integer hash( int x, int y ) {
        int iTotal = 17;
        iTotal = iTotal * iConstant + x;
        iTotal = iTotal * iConstant + y;
        return iTotal;
    }

    private RecordingMap<T> parent;

    private final MultiValuedMap<Integer, T> map;

    private final Set<Integer> isCurrent;

    private int width;
    private int height;

    public RecordingMap( int width, int height ) {
        this( null );

        this.width = width;
        this.height = height;
    }

    private RecordingMap( RecordingMap parent ) {
        map = new HashSetValuedHashMap<>();
        this.parent = parent;
        isCurrent = new HashSet<>();

        if (parent != null) {
            width = parent.width;
            height = parent.height;

        }

    }

    public RecordingMap advanceFrame() {
        return new RecordingMap( this );
    }

    public void flatten() {
        if (parent != null) {
            parent.collect( map );
            parent = null;
            isCurrent.addAll( map.keySet() );
        }

    }

    public Collection<T> get( int x, int y ) {
        final Integer hash = hash( x, y );
        return getEntriesAt( hash );
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isEmpty( int x, int y ) {
        final Integer hash = hash( x, y );
        return isEmpty( hash );
    }

    public void remove( int x, int y, T s ) {
        final Integer hash = hash( x, y );
        makeCurrent( hash );
        map.removeMapping( hash, s );
    }

    public void set( int x, int y, T s ) {
        assert s != null;

        final Integer hash = hash( x, y );
        makeCurrent( hash );
        map.put( hash, s );
    }

    public Stream<T> values() {
        final Stream<Map.Entry<Integer, T>> ret = getEntriesInternal();

        return ret.map( e -> e.getValue() );

    }

    private void collect( MultiValuedMap<Integer, T> m ) {
        map.asMap().entrySet().stream().filter( e -> !m.containsKey( e.getKey() ) ).forEach( e -> m.putAll( e.getKey(), e.getValue() ) );
        if (parent != null) {
            parent.collect( m );
        }
    }

    private Collection<T> getEntriesAt( Integer hash ) {
        if (map.containsKey( hash )) {
            return Collections.unmodifiableCollection( map.get( hash ) );
        }
        else if (isCurrent.contains( hash )) {
            return Collections.EMPTY_SET;
        }
        else if (parent != null) {
            return parent.getEntriesAt( hash );
        }
        else {
            return Collections.EMPTY_SET;
        }
    }

    private Stream<Map.Entry<Integer, T>> getEntriesInternal() {
        Stream<Map.Entry<Integer, T>> ret = map.entries().stream();
        if (parent != null) {
            ret = Stream.concat( ret, parent.getEntriesInternal().filter( e -> !isCurrent.contains( e.getKey() ) ) );
        }
        return ret;
    }

    private boolean isEmpty( Integer hash ) {
        if (map.containsKey( hash )) {
            return false;
        }
        else if (parent != null) {
            return parent.isEmpty( hash );
        }
        else {
            return true;
        }
    }

    private void makeCurrent( Integer hash ) {
        if (!isCurrent.contains( hash )) {
            isCurrent.add( hash );
            if (parent != null) {
                map.putAll( hash, parent.getEntriesAt( hash ) );
            }
        }

    }
}
