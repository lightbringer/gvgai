package net.gvgai.vgdl.runtime;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

import net.gvgai.vgdl.SpriteInfo;
import net.gvgai.vgdl.game.DiscreteGameState;
import net.gvgai.vgdl.game.VGDLSprite;
import net.gvgai.vgdl.game.Wall;

/**
 *
 * This is just some super hacky class to visualise a discrete game state. There be dragons!
 *
 * @author Tobias Mahlmann
 *
 */
public class DebugRenderer extends PanelUI {

    private static Image getImageForClass( Class<? extends VGDLSprite> clazz ) {
        final SpriteInfo ann = clazz.getAnnotation( SpriteInfo.class );
        final String[] options = ann.resourceInfo().split( " " );
        for (final String o : options) {
            final String[] e = o.split( "=" );
            if (e[0].equals( "img" )) {
                final URL path = DebugRenderer.class.getResource( "/net/gvgai/vgdl/images/" + e[1] + ".png" );
                return Toolkit.getDefaultToolkit().createImage( path );
            }

        }
        throw new IllegalStateException( "Debug rendering requires SpriteInfo with img set on each SpriteClass" );
    }

    private final Headless headless;
    private final Map<Class, Image> images = new HashMap();

    public DebugRenderer( Headless headless ) {
        this.headless = headless;
    }

    @Override
    public void paint( Graphics g, JComponent c ) {
        super.paint( g, c );

        final DiscreteGameState state = (DiscreteGameState) headless.gameState;
        final VGDLSprite[][][] level = state.getLevel();

        final int cellWidth = c.getWidth() / level[0].length;
        final int cellHeight = c.getHeight() / level.length;

        g.clearRect( 0, 0, c.getWidth(), c.getHeight() );

        for (int y = 0; y < level.length; y++) {
            for (int x = 0; x < level[0].length; x++) {
                final VGDLSprite[] sprites = level[y][x];
                if (sprites != null) {
                    for (final VGDLSprite s : sprites) {
                        if (s != null) {
                            final int px = x * cellWidth;
                            final int py = /*c.getHeight() - cellHeight -*/ y * cellHeight;
                            if (s instanceof Wall) {
                                g.setColor( Color.BLACK );
                                g.fillRect( px, py, cellWidth, cellHeight );
                            }
                            else {
                                g.setColor( Color.WHITE );
                                Image img = images.get( s.getClass() );
                                if (img == null) {
                                    img = getImageForClass( s.getClass() );
                                    images.put( s.getClass(), img );
                                }
                                g.drawImage( img, px, py, px + cellWidth, py + cellHeight, 0, 0, img.getWidth( null ), img.getHeight( null ), null );
                            }

                            break;
                        }
                    }
                }
            }
        }
    }

}
