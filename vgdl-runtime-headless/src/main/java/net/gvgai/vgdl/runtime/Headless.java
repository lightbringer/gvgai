package net.gvgai.vgdl.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.compiler.VGDL2Java;
import net.gvgai.vgdl.controllers.singleplayer.sampleMCTS.Agent;
import net.gvgai.vgdl.game.GameState2D;
import net.gvgai.vgdl.game.MovingAvatar;
import net.gvgai.vgdl.game.VGDLGame;
import net.gvgai.vgdl.game.VGDLSprite;
import net.gvgai.vgdl.input.Action;
import net.gvgai.vgdl.input.Controller;
import net.gvgai.vgdl.runtime.input.EventKeyHandler;

public class Headless implements VGDLRuntime {
    public static void main( String[] args ) throws IOException {
        final Class<? extends VGDLGame> gameClass = VGDL2Java.load( "Sokoban", VGDL2Java.class.getResource( "sokoban.txt" ).openStream() );
        final ServiceLoader<VGDLRuntime> loader = ServiceLoader.load( VGDLRuntime.class );
        final VGDLRuntime runtime = loader.iterator().next();
        runtime.loadGame( gameClass );
        runtime.loadLevel( VGDL2Java.class.getResourceAsStream( "sokoban_lvl0.txt" ) );
        runtime.run();
    }

    private JFrame window;

    private VGDLGame game;

    private Controller controller;

    private double updateFrequency;

    private DebugRenderer renderer;

    public VGDLGame getGame() {
        return game;
    }

    @Override
    public void loadGame( Class<? extends VGDLGame> gameClass ) {

        try {
            game = gameClass.newInstance();

            //TODO remove swing stuff
            window = new JFrame( game.getClass().toGenericString() );

            final Feature[] modes = game.getRequiredFeatures();
            final Feature mode = Arrays.asList( modes ).stream().filter( f -> f == Feature.DISCRETE_GAME ).findAny().orElseThrow( IllegalStateException::new );
            switch (mode) {
                case DISCRETE_GAME:
                    if (controller == null) {
//                        final EventKeyHandler ek = new EventKeyHandler();
//                        controller = ek;
//                        window.addKeyListener( ek );
                        updateFrequency = 0.05; //50ms
                    }
                    else {
                        System.out.println( "FIXME: set agent" );
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
            //XXX
            controller = new Agent( 1000L );
//            controller = new EventKeyHandler();
//            window.addKeyListener( (EventKeyHandler) controller );

            //FIXME Remove this
            renderer = new DebugRenderer( this );
            final JPanel p = new JPanel();
            p.setUI( renderer );
            window.getContentPane().add( p );
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

            //TODO remove swing stuff

            window.setSize( level.getWidth() * 50, level.getHeight() * 50 );

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
        //TODO remove swing stuff
        window.setVisible( true );
        window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        long time = System.currentTimeMillis();
        long controllerTime = System.currentTimeMillis();
        while (!game.isGameOver()) {
            game.preFrame();

            final double delta = (System.currentTimeMillis() - time) / 1000.0;
            final double controllerDelta = (System.currentTimeMillis() - controllerTime) / 1000.0;
            if (controllerDelta > updateFrequency) {
                //XXX
                if (controller instanceof EventKeyHandler) {
                    final Action a = controller.act( game.getGameState(), controllerDelta );
                    game.getGameState().getAvatar().act( a );
//                    game.setGameState( (GameState) game.getGameState().copy() );

                }
                else {
                    synchronized (game.getGameState()) {
                        final Action a = controller.act( game.getGameState(), controllerDelta );
                        System.out.println( a );
                        game.getGameState().getAvatar().act( a );
                    }
                }
                controllerTime = System.currentTimeMillis();
            }
            game.update( delta );

            time = System.currentTimeMillis();

            game.postFrame();

            //FIXME Remove me
            window.repaint();

        }
        System.out.println( "Game over" );
    }

    private void lose( int id ) {
        JOptionPane.showMessageDialog( null, "Player " + id + " lost" );
    }

    private void win( int id ) {
        JOptionPane.showMessageDialog( null, "Player " + id + " won" );
    }

}
