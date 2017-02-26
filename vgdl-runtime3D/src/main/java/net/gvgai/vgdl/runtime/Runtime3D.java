package net.gvgai.vgdl.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;

import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.game.GameMap3D;
import net.gvgai.vgdl.game.GameState3D;
import net.gvgai.vgdl.game.VGDLGame;
import net.gvgai.vgdl.runtime.input.InputListener;
import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class Runtime3D extends SimpleApplication implements VGDLRuntime {

    public static void main( String[] args ) throws ClassNotFoundException {
        final AppSettings settings = new AppSettings( true );
        settings.setWidth( 1024 );
        settings.setHeight( 768 );

        final Runtime3D runtime = new Runtime3D();
        runtime.setShowSettings( false );
        runtime.setSettings( settings );

        runtime.start();
    }

    private VGDLGame game;

    private InputListener keyListener;

    private GameState3D state;

    @Override
    public void loadGame( Class<? extends VGDLGame> gameClass ) {
        try {
            game = gameClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public void loadLevel( InputStream s ) {
        if (game == null) {
            throw new IllegalStateException( "load game frist" );
        }
        try {

            final BufferedReader reader = new BufferedReader( new InputStreamReader( s ) );
            final List<String> lines = new ArrayList<>();
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                lines.add( buffer );
            }

            final BulletAppState bulletappState = new BulletAppState();
            stateManager.attach( bulletappState );

            state = new GameState3D();
            game.setGameState( state );
            final GameMap3D level = new GameMap3D( state, getRootNode(), getAssetManager(), bulletappState );
            state.setLevel( level );

            int lineIndex = 0;
            for (final String line : lines) {
                int offset = 0;
                final int strLen = line.length();

                int colIndex = 0;
                while (offset < strLen) {
                    final int curChar = line.codePointAt( offset );
                    offset += Character.charCount( curChar );
                    // do something with curChar

                    switch (curChar) {
                        case ' ':
                            //empty space
                            break;
                        default:
                            final Class<? extends VGDLSprite>[] spriteClasses = game.getMappedSpriteClass( (char) curChar );

                            int z = 0;
                            for (final Class<? extends VGDLSprite> c : spriteClasses) {
                                final VGDLSprite sprite = c.newInstance();
                                if (sprite instanceof MovingAvatar) {
                                    if (game.getGameState().getAvatar() != null) {
                                        throw new IllegalStateException( "avatar already defined" );
                                    }
                                    game.getGameState().setAvatar( (MovingAvatar) sprite );

                                }
                                sprite.setDirection( Vector3f.UNIT_Z.clone() );

                                final Vector3f p = new Vector3f( colIndex * GameMap3D.CUBE_SIZE, z++ * GameMap3D.CUBE_SIZE, lineIndex * GameMap3D.CUBE_SIZE );
                                level.set( p, sprite );
                            }
                            break;
                    }

                    colIndex++;
                }
                lineIndex++;
            }
        }
        catch (final IOException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public void run() {
        start();

    }

    @Override
    public void simpleInitApp() {

        try {
            final Class<? extends VGDLGame> gameClass = (Class<? extends VGDLGame>) Class.forName( "net.gvgai.game.sokoban.Sokoban" );
            loadGame( gameClass );
            loadLevel( gameClass.getResourceAsStream( "/sokoban_lvl0.txt" ) );

            keyListener = new InputListener( state );
            initKeys();

            //XXX
            cam.setLocation( new Vector3f( 8.496728f, 4.7046204f, 3.48067f ) );
            cam.setRotation( new Quaternion( 0.11778361f, -0.82436746f, 0.1862263f, 0.52140677f ) );

        }
        catch (final ClassNotFoundException e) {
            throw new RuntimeException( e );
        }

    }

    @Override
    public void simpleUpdate( float tpf ) {
        if (!keyListener.checkAndClearKeyPressed()) {
            state.getLevel().getNode( state.getAvatar() ).getControl( BetterCharacterControl.class ).setWalkDirection( Vector3f.ZERO );
        }

        if (game.isGameOver()) {
            keyListener.setEnabled( false );

            final BitmapText hudText = new BitmapText( guiFont, false );
            hudText.setSize( guiFont.getCharSet().getRenderedSize() ); // font size
            hudText.setColor( ColorRGBA.Blue ); // font color
            hudText.setText( "The game ended. Your score: " + state.getScore() ); // the text
            hudText.setLocalTranslation( 300, hudText.getLineHeight(), 0 ); // position
            guiNode.attachChild( hudText );
        }
        else {
            state.preFrame();
            game.update( tpf );
            state.postFrame();
        }
    }

    private void initKeys() {
        // You can map one or several inputs to one named action
        inputManager.addMapping( "Forward", new KeyTrigger( KeyInput.KEY_T ) );
        inputManager.addMapping( "Backward", new KeyTrigger( KeyInput.KEY_G ) );
        inputManager.addMapping( "Left", new KeyTrigger( KeyInput.KEY_F ) );
        inputManager.addMapping( "Right", new KeyTrigger( KeyInput.KEY_H ) );

        inputManager.addListener( keyListener, "Left", "Right", "Forward", "Backward" );

    }

}
