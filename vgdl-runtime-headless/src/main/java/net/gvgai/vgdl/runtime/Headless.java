package net.gvgai.vgdl.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.Pair;

import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.compiler.VGDL2Java;
import net.gvgai.vgdl.game.DiscreteGameState;
import net.gvgai.vgdl.game.GameState;
import net.gvgai.vgdl.game.MovingAvatar;
import net.gvgai.vgdl.game.RecordingMap;
import net.gvgai.vgdl.game.VGDLException;
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

    private static List<Field> getAllFields( List<Field> fields, Class<?> type ) {
        fields.addAll( Arrays.asList( type.getDeclaredFields() ) );

        if (type.getSuperclass() != null) {
            fields = getAllFields( fields, type.getSuperclass() );
        }

        return fields;
    }

    private JFrame window;

    private VGDLGame game;

    private Controller controller;

    private double updateFrequency;

    private MovingAvatar avatar;

    private DebugRenderer renderer;

    public VGDLGame getGame() {
        return game;
    }

    @Override
    public void loadGame( Class<? extends VGDLGame> gameClass ) {

        try {
            game = gameClass.newInstance();
            game.setGameState( new DiscreteGameState() );

            //TODO remove swing stuff
            window = new JFrame( game.getClass().toGenericString() );

            final Feature[] modes = game.getRequiredFeatures();
            final Feature mode = Arrays.asList( modes ).stream().filter( f -> f == Feature.DISCRETE_GAME ).findAny().orElseThrow( IllegalStateException::new );
            switch (mode) {
                case DISCRETE_GAME:
                    if (controller == null) {
                        final EventKeyHandler ek = new EventKeyHandler();
                        controller = ek;
                        window.addKeyListener( ek );
                        updateFrequency = 0.05; //50ms
                    }
                    else {
                        System.out.println( "FIXME: set agent" );
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }

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
            final RecordingMap level = new RecordingMap( lines.get( 0 ).length(), lines.size() );
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
//                        case 'w':
//                            sprites = new VGDLSprite[] { new Wall() };
//                            break;
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
//                                wireObject( sprite );
                                sprite.setDirection( Action.ACTION_UP );
                                sprite.setPosition( Pair.of( colIndex, lineIndex ) );
                                level.set( colIndex, lineIndex, sprite );
                            }
                            break;
                    }

                    colIndex++;
                }
                lineIndex++;
            }

            //TODO remove swing stuff

            window.setSize( level.getWidth() * 50, level.getHeight() * 50 );
            ((DiscreteGameState) game.getGameState()).setLevel( level );
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
            try {
                final double delta = (System.currentTimeMillis() - time) / 1000.0;
                final double controllerDelta = (System.currentTimeMillis() - controllerTime) / 1000.0;
                if (controllerDelta > updateFrequency) {
                    final Action a = controller.act( controllerDelta );
                    final GameState state = game.getGameState();
                    game.getGameState().getAvatar().act( a );
                    controllerTime = System.currentTimeMillis();
                }
                game.update( delta );
            }
            catch (final VGDLException e) {
                game.resetFrame();
            }
            time = System.currentTimeMillis();

            game.postFrame();

            //FIXME Remove me
            window.repaint();

        }
    }

    private void lose( int id ) {
        JOptionPane.showMessageDialog( null, "Player " + id + " lost" );
    }

    private void win( int id ) {
        JOptionPane.showMessageDialog( null, "Player " + id + " won" );
    }

}
