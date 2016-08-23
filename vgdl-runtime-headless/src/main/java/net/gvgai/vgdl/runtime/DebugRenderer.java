package net.gvgai.vgdl.runtime;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.game.DiscreteGameState;
import net.gvgai.vgdl.game.VGDLSprite;

/**
 *
 * This is just some super hacky class to visualise a discrete game state. There be dragons!
 *
 * @author Tobias Mahlmann
 *
 */
public class DebugRenderer extends PanelUI {
    private final Map<Class, Integer> zLevel;

    private final Headless headless;

    private final Map<Class, Image> images = new HashMap();

    public DebugRenderer( Headless headless ) {
        this.headless = headless;
        zLevel = new HashMap();
    }

    @Override
    public void paint( Graphics g, JComponent c ) {
        super.paint( g, c );

        final DiscreteGameState state = (DiscreteGameState) headless.gameState;
        final VGDLSprite[][][] level = state.getLevel();
        updateZLevel( level );

        final int cellWidth = c.getWidth() / level[0].length;
        final int cellHeight = c.getHeight() / level.length;

        g.clearRect( 0, 0, c.getWidth(), c.getHeight() );

        for (int y = 0; y < level.length; y++) {
            final VGDLSprite[][] row = level[y];
            for (int x = 0; x < row.length; x++) {
                final VGDLSprite[] sprites = row[x].clone();
                if (sprites != null) {
                    Arrays.sort( sprites, ( s1, s2 ) -> Integer.compare( getZLevel( s2 ), getZLevel( s1 ) ) );

                    final VGDLSprite s = sprites[0]; //top most image
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
        final SpriteInfo ann = clazz.getAnnotation( SpriteInfo.class );
        final String[] options = ann.resourceInfo().split( " " );

        for (final String o : options) {
            final String[] e = o.split( "=" );
            if (e[0].equals( "img" )) {
                final URL path = DebugRenderer.class.getResource( "/net/gvgai/vgdl/images/" + e[1] + ".png" );
                assert path != null : "image " + e[1] + " not found";
                return Toolkit.getDefaultToolkit().createImage( path );
            }

        }
        throw new IllegalStateException( "Debug rendering requires SpriteInfo with img set on each SpriteClass" );

    }

    private int getZLevel( VGDLSprite s ) {
        return s != null ? zLevel.get( s.getClass() ) : Integer.MIN_VALUE;
    }

    private void updateZLevel( VGDLSprite[][][] level ) {
        for (final VGDLSprite[][] row : level) {
            for (final VGDLSprite[] column : row) {
                if (column != null) {
                    for (final VGDLSprite s : column) {
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

}
