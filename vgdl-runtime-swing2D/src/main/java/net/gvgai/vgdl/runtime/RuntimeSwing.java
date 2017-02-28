package net.gvgai.vgdl.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.game.GameState2D;
import net.gvgai.vgdl.game.VGDLGame;
import net.gvgai.vgdl.input.Action;
import net.gvgai.vgdl.input.Controller;
import net.gvgai.vgdl.runtime.input.EventKeyHandler;
import net.gvgai.vgdl.sprites.MovingAvatar;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class RuntimeSwing implements VGDLRuntime {
    public static void main( String[] args ) throws IOException, ClassNotFoundException {
//      final Class<? extends VGDLGame> gameClass = VGDL2Java.loadIntoMemory( "Sokoban", VGDL2Java.class.getResource( "sokoban.txt" ).openStream() );

        final Class<? extends VGDLGame> gameClass = (Class<? extends VGDLGame>) Class.forName( "net.gvgai.game.frogs.Frogs" );
//        final Class<? extends VGDLGame> gameClass = (Class<? extends VGDLGame>) Class.forName( "net.gvgai.game.sokoban.Sokoban" );
        final ServiceLoader<VGDLRuntime> loader = ServiceLoader.load( VGDLRuntime.class );
        final VGDLRuntime runtime = loader.iterator().next();
        runtime.loadGame( gameClass );
        runtime.loadLevel( gameClass.getResourceAsStream( "/frogs_lvl0.txt" ) );
//        runtime.loadLevel( gameClass.getResourceAsStream( "/sokoban_lvl0.txt" ) );
        runtime.run();
    }

    private JFrame window;

    private VGDLGame game;

    private Controller controller;

    private double updateFrequency;

    private DebugRenderer renderer;

    private final Object paintMutex = new Object();

    public VGDLGame getGame() {
        return game;
    }

    public Object getPaintMutex() {
        return paintMutex;
    }

    @Override
    public void loadGame( Class<? extends VGDLGame> gameClass ) {

        try {
            game = gameClass.newInstance();

            //TODO remove swing stuff
            window = new JFrame( game.getClass().toGenericString() );

            final Feature[] modes = game.getRequiredFeatures();
            for (final Feature mode : modes) {
                switch (mode) {
                    case DISCRETE_GAME:
                        //XXX
                        if (controller == null) {
                            updateFrequency = 0.05; //50ms
                        }
                        else {
                            System.out.println( "FIXME: set agent" );
                        }
                    case PRE_BUFFER_FRAME:
                    case OBEY_END_OF_BOUNDARIES:
                        break;
                    default:
                        throw new IllegalStateException( mode + " is not supported" );
                }
            }
//          XXX
//            controller = new Agent( 1000L );
            controller = new EventKeyHandler();
            window.addKeyListener( (EventKeyHandler) controller );

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
                                final int[] p = new int[] { colIndex, lineIndex };
                                level.set( p, sprite );
                            }
                            break;
                    }

                    colIndex++;
                }
                lineIndex++;
            }

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
        int tick = 0;
        while (!game.isGameOver()) {

            final double delta = (System.currentTimeMillis() - time) / 1000.0;
            final double controllerDelta = (System.currentTimeMillis() - controllerTime) / 1000.0;
            game.preFrame();
            if (controllerDelta > updateFrequency) {

                Action a;
                //XXX
                if (controller instanceof EventKeyHandler) {
                    a = controller.act( game.getGameState(), controllerDelta );
                }
                else {
                    synchronized (game.getGameState()) {
                        a = controller.act( game.getGameState(), controllerDelta );
                        System.out.println( a );
                    }
                }

                final MovingAvatar av = game.getGameState().getAvatar();
                if (av == null) {
                    break;
                }

                av.act( a );

                tick++;
                controllerTime = System.currentTimeMillis();
            }
            game.update( 1 );
            game.postFrame();

            time = System.currentTimeMillis();

//            if (controller instanceof EventKeyHandler) {
//                game.setGameState( (GameState) game.getGameState().copy() );
//            }

            try {
                Thread.sleep( 200 );
            }
            catch (final InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            synchronized (paintMutex) {
                window.setTitle( "Score: " + game.getScore() + " Tick: " + tick );
                window.repaint();
                try {
                    paintMutex.wait();
                }
                catch (final InterruptedException e) {
                    throw new RuntimeException( e );
                }
            }

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
