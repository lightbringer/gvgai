package net.gvgai.vgdl.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.Passive;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class GameMap3D implements GameMap<GameMap3D, Vector3f, Vector3f> {
    private class VGDLSpriteContainer implements Savable {
        VGDLSprite sprite;

        @Override
        public void read( JmeImporter im ) throws IOException {
            throw new UnsupportedOperationException();

        }

        @Override
        public void write( JmeExporter ex ) throws IOException {
            throw new UnsupportedOperationException();

        }

    }

    private static final String VGDL_SPRITE_USER_DATA = "_VGDL_SPRITE_USER_DATA";

    public final static int CUBE_SIZE = 1;

    private static Vector3f reverse( Object v ) {
        return ((Vector3f) v).negate();
    }

    private final BulletAppState bulletAppState;

    private final Node rootNode;

    private final AssetManager assetManager;

    private final GameState3D state;

    public GameMap3D( GameState3D state, Node rootNode, AssetManager assetManager, BulletAppState bulletAppState ) {
        super();
        if (rootNode == null || assetManager == null) {
            throw new IllegalArgumentException( "parameters must not be null" );
        }

        this.state = state;
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.bulletAppState = bulletAppState;
//        bulletAppState.setDebugEnabled( true );
        bulletAppState.getPhysicsSpace().addCollisionListener( event -> {

            final VGDLSpriteContainer a = event.getNodeA().getUserData( VGDL_SPRITE_USER_DATA );
            final VGDLSpriteContainer b = event.getNodeB().getUserData( VGDL_SPRITE_USER_DATA );

            final Set<VGDLSprite> deletedSprites = state.getDeletedSprites();
            //If a physics object produced several collision events, and the first event removed the sprite,
            //we need to ignore the following events
            if (!deletedSprites.contains( a.sprite ) && !deletedSprites.contains( b.sprite )) {
                a.sprite.collide( b.sprite );
                b.sprite.collide( a.sprite );
            }
        } );
    }

    @Override
    public GameMap3D copy() {
        System.out.println( "copy" );
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<VGDLSprite> get( Vector3f p, boolean writable ) {
        System.out.println( "get" );
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHeight() {
        System.out.println( "hei" );
        throw new UnsupportedOperationException();
    }

    public Spatial getNode( VGDLSprite spr ) {
        assert spr != null;
        final Spatial[] ret = new Node[1];
        rootNode.breadthFirstTraversal( s -> {
            final VGDLSpriteContainer o = s.getUserData( VGDL_SPRITE_USER_DATA );
            if (o != null && o.sprite == spr) {
                ret[0] = s;
            }
        } );
        return ret[0];
    }

    public BulletAppState getPhysicsState() {
        return bulletAppState;
    }

    @Override
    public int getWidth() {
        System.out.println( "width" );
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty( Vector3f p ) {
        System.out.println( "isE" );
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean move( VGDLSprite s, Vector3f direction ) {
        rootNode.breadthFirstTraversal( sp -> {
            final VGDLSpriteContainer c = sp.getUserData( VGDL_SPRITE_USER_DATA );
            if (c != null && c.sprite == s) {
                if (!(s instanceof MovingAvatar)) {
                    final RigidBodyControl rg = sp.getControl( RigidBodyControl.class );
                    rg.applyCentralForce( direction );
                }
                else {
                    final BetterCharacterControl bc = sp.getControl( BetterCharacterControl.class );
                    bc.setWalkDirection( direction );
                }
            }
        } );

        //Collision is handled elsewhere
        return false;
    }

    @Override
    public void remove( Vector3f p, VGDLSprite s ) {
        state.getDeletedSprites().add( s );
        rootNode.breadthFirstTraversal( sp -> {
            final VGDLSpriteContainer c = sp.getUserData( VGDL_SPRITE_USER_DATA );
            if (c != null && c.sprite == s) {

                final RigidBodyControl rg = sp.getControl( RigidBodyControl.class );
                bulletAppState.getPhysicsSpace().remove( rg );
                sp.removeFromParent();
            }
        } );

    }

    @Override
    public boolean set( Vector3f p, VGDLSprite s ) {
        final Node n = new Node();
        n.setLocalTranslation( p );
        final VGDLSpriteContainer c = new VGDLSpriteContainer();
        c.sprite = s;
        n.setUserData( VGDL_SPRITE_USER_DATA, c );

        final Box b = new Box( CUBE_SIZE / 2f, CUBE_SIZE / 2f, CUBE_SIZE / 2f ); // create cube shape
        final Geometry geom = new Geometry( "Box", b ); // create cube geometry from the shape
        final Material mat = new Material( assetManager, "Common/MatDefs/Misc/Unshaded.j3md" ); // create a simple material
        mat.setTexture( "ColorMap", assetManager.loadTexture( "/net/gvgai/vgdl/images/" + getImageForClass( s.getClass() ) + ".png" ) );
        geom.setMaterial( mat ); // set the cube's material
        n.attachChild( geom ); // make the cube appear in the scene

        if (!(s instanceof MovingAvatar)) {
            float mass = 0f;

            if (s instanceof Passive) {
                mass = .5f;
            }

            final RigidBodyControl phy = new RigidBodyControl( mass );
            n.addControl( phy );
            if (!(s instanceof Passive)) {
                phy.setKinematic( true );
            }

            //We add some numerical margin, so that sprites next to each other, but not in motion,
            //won't collide
            phy.getCollisionShape().setMargin( -.08f );

            bulletAppState.getPhysicsSpace().add( phy );
        }
        else {
            geom.setLocalTranslation( 0, .5f, 0 );
        }
        rootNode.attachChild( n );

        if (s instanceof Passive) {
            final Passive pa = (Passive) s;
            pa.setReverse( GameMap3D::reverse );

        }
        s.setMap( this );
        s.setState( state );

        //NOT SUPPORTED
        return false;
    }

    @Override
    public Stream<VGDLSprite> values() {
        final List<VGDLSprite> ret = new ArrayList();
        rootNode.breadthFirstTraversal( s -> {
            final VGDLSpriteContainer o = s.getUserData( VGDL_SPRITE_USER_DATA );
            if (o != null) {
                ret.add( o.sprite );
            }
        } );
        return ret.stream();
    }

    private String getImageForClass( Class<? extends VGDLSprite> clazz ) {
        final SpriteInfo ann = clazz.getAnnotation( SpriteInfo.class );
        final String[] options = ann.resourceInfo().split( " " );

        for (final String o : options) {
            final String[] e = o.split( "=" );
            if (e[0].equals( "img" )) {
                return e[1];
            }

        }
        throw new IllegalStateException( "Debug rendering requires SpriteInfo with img set on each SpriteClass" );

    }

}
