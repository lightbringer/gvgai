package net.gvgai.vgdl.runtime;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.game.GameState2D;
import net.gvgai.vgdl.sprites.VGDLSprite;

/**
 *
 * This is just some super hacky class to visualise a discrete game state. There be dragons!
 *
 * @author Tobias Mahlmann
 *
 */
public class DebugRenderer extends PanelUI {
    private final Map<Class, Integer> zLevel;

    private final RuntimeSwing runtime;

    private final Map<Class, Image> images = new HashMap();

    public DebugRenderer( RuntimeSwing headless ) {
        runtime = headless;
        zLevel = new HashMap();
    }

    @Override
    public void paint( Graphics g, JComponent c ) {
        super.paint( g, c );

        final GameState2D level = (GameState2D) runtime.getGame().getGameState();
        synchronized (level) {

            updateZLevel( level );

            final int cellWidth = c.getWidth() / level.getWidth();
            final int cellHeight = c.getHeight() / level.getHeight();

            g.clearRect( 0, 0, c.getWidth(), c.getHeight() );

            for (int x = 0; x < level.getWidth(); x++) {
                for (int y = 0; y < level.getHeight(); y++) {
                    final int[] p = new int[] { x, y };
                    if (level.isEmpty( p )) {
                        continue;
                    }
                    final Object[] sprites = level.get( p ).toArray();
                    Arrays.sort( sprites, ( s1, s2 ) -> Integer.compare( getZLevel( (VGDLSprite) s2 ), getZLevel( (VGDLSprite) s1 ) ) );

                    final VGDLSprite s = (VGDLSprite) sprites[0]; //top most image
                    if (s != null) {
                        final int px = x * cellWidth;
                        final int py = y * cellHeight;

                        g.setColor( Color.WHITE );
                        Image img = images.get( s.getClass() );

                        if (img == null) {
                            img = getImageForClass( s.getClass() );
                            images.put( s.getClass(), img );
                        }
                        g.drawImage( img, px, py, px + cellWidth, py + cellHeight, 0, 0, img.getWidth( null ), img.getHeight( null ), null );

                    }

                }
            }
        }
    }

    private Image getImageForClass( Class<? extends VGDLSprite> clazz ) {
        while (clazz != VGDLSprite.class) {
            System.out.println( "Querying SpriteInfo on " + clazz );
            final SpriteInfo ann = clazz.getAnnotation( SpriteInfo.class );
            if (ann == null) {
                break;
            }
            final String[] options = ann.resourceInfo().split( " " );

            for (final String o : options) {
                final String[] e = o.split( "=" );
                if (e[0].equals( "img" )) {
                    final URL path = DebugRenderer.class.getResource( "/net/gvgai/vgdl/images/" + e[1] + ".png" );
                    assert path != null : "image " + e[1] + " not found";
                    return Toolkit.getDefaultToolkit().createImage( path );
                }

            }
            clazz = (Class<? extends VGDLSprite>) clazz.getSuperclass();
        }
        throw new IllegalStateException( "Debug rendering requires SpriteInfo with img set on each SpriteClass. No image for " + clazz );

    }

    private int getZLevel( VGDLSprite s ) {
        return s != null ? zLevel.get( s.getClass() ) : Integer.MIN_VALUE;
    }

    private void updateZLevel( GameMap level ) {
        for (int x = 0; x < level.getWidth(); x++) {
            for (int y = 0; y < level.getHeight(); y++) {
                final int[] p = new int[] { x, y };
                final Collection<VGDLSprite> sprites = level.get( p );
                for (final VGDLSprite s : sprites) {
                    if (s != null && !zLevel.containsKey( s.getClass() )) {
                        final SpriteInfo ann = s.getClass().getAnnotation( SpriteInfo.class );
                        final String[] options = ann.resourceInfo().split( " " );

                        for (final String o : options) {
                            final String[] e = o.split( "=" );
                            if (e[0].equals( "zLevel" )) {
                                assert e[1] != null;
                                zLevel.put( s.getClass(), Integer.valueOf( e[1] ) );
                            }
                        }
                        assert zLevel.containsKey( s.getClass() );
                    }
                }

            }
        }

    }

}
