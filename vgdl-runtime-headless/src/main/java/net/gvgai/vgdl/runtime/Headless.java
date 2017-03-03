package net.gvgai.vgdl.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.controllers.singleplayer.sampleMCTS.Agent;
import net.gvgai.vgdl.game.GameState2D;
import net.gvgai.vgdl.game.VGDLGame;
import net.gvgai.vgdl.input.Action;
import net.gvgai.vgdl.input.Controller;
import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.VGDLSprite;
import se.lu.lucs.vgdl.manualminimal.Minimal;

public class Headless implements VGDLRuntime {
    public static void main( String[] args ) throws IOException, ClassNotFoundException {
//      final Class<? extends VGDLGame> gameClass = VGDL2Java.loadIntoMemory( "Sokoban", VGDL2Java.class.getResource( "sokoban.txt" ).openStream() );
//        final Class<? extends VGDLGame> gameClass = (Class<? extends VGDLGame>) Class.forName( "net.gvgai.game.frogs.Frogs" );
//      final Class<? extends VGDLGame> gameClass = (Class<? extends VGDLGame>) Class.forName( "net.gvgai.game.sokoban.Sokoban" );
//        final Class<? extends VGDLGame> gameClass = MinimalGame.class;
        final Class<? extends VGDLGame> gameClass = (Class<? extends VGDLGame>) Class.forName( "net.gvgai.game.minimal.Minimal" );

        final ServiceLoader<VGDLRuntime> loader = ServiceLoader.load( VGDLRuntime.class );
        final VGDLRuntime runtime = loader.iterator().next();
        runtime.loadGame( gameClass );
//        runtime.loadLevel( gameClass.getResourceAsStream( "/frogs_lvl0.txt" ) );
//      runtime.loadLevel( gameClass.getResourceAsStream( "/sokoban_lvl0.txt" ) );
        runtime.loadLevel( Minimal.class.getResourceAsStream( "minimal_lvl0.txt" ) );
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

//            final Feature[] modes = game.getRequiredFeatures();
//            final Feature mode = Arrays.asList( modes ).stream().filter( f -> f == Feature.DISCRETE_GAME ).findAny().orElseThrow( IllegalStateException::new );
//            switch (mode) {
//                case DISCRETE_GAME:
//                    if (controller == null) {
////REMOVE
            updateFrequency = 0.05; //50ms
//                    }
//                    else {
//                        System.out.println( "FIXME: set agent" );
//                    }
//                    break;
//                default:
//                    throw new IllegalStateException();
//            }
////            XXX
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
            final Feature[] modes = game.getRequiredFeatures();
            for (final Feature mode : modes) {
                switch (mode) {

                    case PRE_BUFFER_FRAME:
                        level.setBufferFrame( true );
                        break;
                    case OBEY_END_OF_BOUNDARIES:
                        level.setObeyBoundaries( true );
                        break;
                    default:
                        //We already checked for requirements. At this point, we only go over things that concern the level
                        break;
                }
            }
            game.setGameState( level );

            VGDLSprite.UpDirection = level::up;
            VGDLSprite.DownDirection = level::down;
            VGDLSprite.LeftDirection = level::left;
            VGDLSprite.RightDirection = level::right;

            int lineIndex = 0;
            for (final String line : lines) {
                int offset = 0;
                final int strLen = line.length();

                int colIndex = 0;
                while (offset < strLen) {
                    final int curChar = line.codePointAt( offset );
                    offset += Character.charCount( curChar );
                    // do something with curChar
                    final int[] p = new int[] { colIndex, lineIndex };
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
//                                sprite.setDirection( Action.ACTION_UP );

                                level.set( Arrays.copyOf( p, 2 ), sprite );
                            }
                            break;
                    }

                    // Trigger a collision for each sprite on the field, e.g. to enabled effects
                    final Collection<VGDLSprite> src = level.get( p, false );

                    final VGDLSprite[] copy = new VGDLSprite[src.size()];
                    src.toArray( copy );

                    for (final VGDLSprite o : copy) {
                        o.collide( copy );
                    }
                    //End Trigger
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
        ((GameState2D) game.getGameState()).setObeyBoundaries( true );

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
            game.update( delta );
            if (a != null) {
                game.getGameState().getAvatar().act( a );
            }
            game.postFrame();
        }

        time = System.currentTimeMillis();

        System.out.println( "Game over" );

    }

}
