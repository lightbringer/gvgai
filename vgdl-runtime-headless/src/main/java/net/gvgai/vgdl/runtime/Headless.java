package net.gvgai.vgdl.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.controllers.singleplayer.sampleMCTS.Agent;
import net.gvgai.vgdl.game.GameState2D;
import net.gvgai.vgdl.game.MovingAvatar;
import net.gvgai.vgdl.game.VGDLGame;
import net.gvgai.vgdl.game.VGDLSprite;
import net.gvgai.vgdl.input.Action;
import net.gvgai.vgdl.input.Controller;

public class Headless implements VGDLRuntime {
    public static void main( String[] args ) throws IOException, ClassNotFoundException {
//        final Class<? extends VGDLGame> gameClass = VGDL2Java.loadIntoMemory( "Sokoban", VGDL2Java.class.getResource( "sokoban.txt" ).openStream() );

        final Class<? extends VGDLGame> gameClass = (Class<? extends VGDLGame>) Class.forName( "net.gvgai.game.sokoban.Sokoban" );
        final ServiceLoader<VGDLRuntime> loader = ServiceLoader.load( VGDLRuntime.class );
        final VGDLRuntime runtime = loader.iterator().next();
        runtime.loadGame( gameClass );
        runtime.loadLevel( gameClass.getResourceAsStream( "/sokoban_lvl0.txt" ) );
        runtime.run();
    }

    private VGDLGame game;

    private Controller controller;

    private double updateFrequency;

    public VGDLGame getGame() {
        return game;
    }

    @Override
    public void loadGame( Class<? extends VGDLGame> gameClass ) {

        try {
            game = gameClass.newInstance();

            final Feature[] modes = game.getRequiredFeatures();
            final Feature mode = Arrays.asList( modes ).stream().filter( f -> f == Feature.DISCRETE_GAME ).findAny().orElseThrow( IllegalStateException::new );
            switch (mode) {
                case DISCRETE_GAME:
                    if (controller == null) {
//REMOVE
                        updateFrequency = 0.05; //50ms
                    }
                    else {
                        System.out.println( "FIXME: set agent" );
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
//            XXX
            controller = new Agent( 1000L );

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

            final GameState2D level = new GameState2D( lines.get( 0 ).length(), lines.size() );
            game.setGameState( level );

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

                            for (final Class<? extends VGDLSprite> c : spriteClasses) {
                                final VGDLSprite sprite = c.newInstance();
                                if (sprite instanceof MovingAvatar) {
                                    if (game.getGameState().getAvatar() != null) {
                                        throw new IllegalStateException( "avatar already defined" );
                                    }
                                    game.getGameState().setAvatar( (MovingAvatar) sprite );
                                }
                                sprite.setDirection( Action.ACTION_UP );
                                final int[] p = new int[] { colIndex, lineIndex };
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
        if (game == null || game.getGameState() == null || !game.getGameState().isReady()) {
            throw new IllegalStateException( "load game and level first" );
        }
        long time = System.currentTimeMillis();
        long controllerTime = System.currentTimeMillis();

        while (!game.isGameOver()) {

            final double delta = (System.currentTimeMillis() - time) / 1000.0;
            final double controllerDelta = (System.currentTimeMillis() - controllerTime) / 1000.0;
            Action a = null;
            if (controllerDelta > updateFrequency) {

                a = controller.act( game.getGameState(), controllerDelta );
                System.out.println( a );
                controllerTime = System.currentTimeMillis();
            }
            game.preFrame();
            if (a != null) {
                game.getGameState().getAvatar().act( a );
            }
            game.postFrame();
            game.update( delta );
        }

        time = System.currentTimeMillis();

        System.out.println( "Game over" );

    }

}
