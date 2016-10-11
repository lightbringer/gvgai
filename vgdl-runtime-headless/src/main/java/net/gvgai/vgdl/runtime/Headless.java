package net.gvgai.vgdl.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang3.tuple.Pair;

import net.gvgai.vgdl.AutoWire;
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

    GameState gameState;

    private Controller controller;

    private double updateFrequency;

    private MovingAvatar avatar;

    private DebugRenderer renderer;

    @Override
    public void loadGame( Class<? extends VGDLGame> gameClass ) {

        try {
            game = gameClass.newInstance();
            gameState = new DiscreteGameState();

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
            wireObject( game );

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
                                    if (gameState.getAvatar() != null) {
                                        throw new IllegalStateException( "avatar already defined" );
                                    }
                                    gameState.setAvatar( (MovingAvatar) sprite );
                                }
                                wireObject( sprite );
                                sprite.setDirection( DiscreteGameState.Direction.NORTH );
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
            ((DiscreteGameState) gameState).setLevel( level );
        }
        catch (final IOException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void run() {
        if (game == null || gameState == null || !gameState.isReady()) {
            throw new IllegalStateException( "load game and level first" );
        }
        //TODO remove swing stuff
        window.setVisible( true );
        window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        long time = System.currentTimeMillis();
        long controllerTime = System.currentTimeMillis();
        while (!game.isGameOver()) {
            gameState.preFrame();
            try {
                final double delta = (System.currentTimeMillis() - time) / 1000.0;
                final double controllerDelta = (System.currentTimeMillis() - controllerTime) / 1000.0;
                if (controllerDelta > updateFrequency) {
                    final Action a = controller.act( controllerDelta );
                    gameState.getAvatar().act( a );
                    controllerTime = System.currentTimeMillis();
                }
                game.update( delta );
            }
            catch (final VGDLException e) {
                gameState.resetFrame();
            }
            time = System.currentTimeMillis();

            gameState.postFrame();

            //FIXME Remove me
            window.repaint();

        }
    }

    private <T extends VGDLSprite> void collide( VGDLSprite s ) {
        final Set<VGDLSprite> collided = new HashSet<>();
        final boolean allCollided[] = new boolean[] { true };
        final Object pos = s.getPosition(); //pos is immutable, so if s actually gets moved, that has no effect as we hold the original position here
        while (true) {
            allCollided[0] = true;
            gameState.getSpritesAt( pos ).stream().filter( t -> t != s && !collided.contains( t ) ).findAny().ifPresent( t -> {
                collided.add( t );
                System.out.println( "Calling collide with " + t + " on " + s );
                s.collide( t );
                t.collide( s );
                allCollided[0] = false;
            } );
            if (allCollided[0]) {
                break;
            }
        }

    }

    private int countSprites( Class<? extends VGDLSprite> clazz ) {
        return gameState.getSpriteCount( clazz );

    }

    private void forward( VGDLSprite s ) {
        System.out.println( "forward" );
        final boolean collision = gameState.forward( s );
        if (collision) {
            collide( s );
        }
    }

    private void injectMethod( Object o, Field f ) {
        try {
            f.setAccessible( true );

            switch (f.getName()) {
                case "moveUp":
                    f.set( o, (Consumer<VGDLSprite>) this::moveUp );
                    break;
                case "moveDown":
                    f.set( o, (Consumer<VGDLSprite>) this::moveDown );
                    break;
                case "moveLeft":
                    f.set( o, (Consumer<VGDLSprite>) this::moveLeft );
                    break;
                case "moveRight":
                    f.set( o, (Consumer<VGDLSprite>) this::moveRight );
                    break;
                case "countSprites":
                    f.set( o, (Function<Class<? extends VGDLSprite>, Integer>) this::countSprites );
                    break;
                case "win":
                    f.set( o, (Consumer<Integer>) this::win );
                    break;
                case "lose":
                    f.set( o, (Consumer<Integer>) this::lose );
                    break;
                case "reverse":
                    f.set( o, (Consumer<VGDLSprite>) this::reverse );
                    break;
                case "forward":
                    f.set( o, (Consumer<VGDLSprite>) this::forward );
                    break;
                case "move":
                    f.set( o, (BiConsumer<VGDLSprite, Object>) this::move );
                    break;
                case "kill":
                    f.set( o, (Consumer<VGDLSprite>) this::kill );
                    break;
                case "position":
                case "direction":
                    break;
                default:
                    throw new IllegalArgumentException( "unrecognized field \"" + f.getName() + "\" marked for @AutoWire in class " + f.getDeclaringClass() );
            }

        }
        catch (final IllegalAccessException e) {
            throw new RuntimeException( e );
        }

    }

    private void kill( VGDLSprite s ) {
        gameState.remove( s );
    }

    private void lose( int id ) {
        JOptionPane.showMessageDialog( null, "Player " + id + " lost" );
    }

    private void move( VGDLSprite s, Object dir ) {
        System.out.println( "move" );
        final boolean collision = gameState.move( s, dir );
        if (collision) {
            collide( s );
        }
    }

    private void moveDown( VGDLSprite s ) {
        System.out.println( "down" );
        final boolean collision = gameState.moveDown( s );
        if (collision) {
            collide( s );
        }
    }

    private void moveLeft( VGDLSprite s ) {
        System.out.println( "left" );

        final boolean collision = gameState.moveLeft( s );

        if (collision) {
            collide( s );
        }
    }

    private void moveRight( VGDLSprite s ) {
        System.out.println( "right" );
        final boolean collision = gameState.moveRight( s );
        if (collision) {
            collide( s );
        }
    }

    private void moveUp( VGDLSprite s ) {
        System.out.println( "up" );
        final boolean collision = gameState.moveUp( s );
        if (collision) {
            collide( s );
        }

    }

    private void reverse( VGDLSprite s ) {
        System.out.println( "reverse" );
        gameState.reverse( s );

    }

    private void win( int id ) {
        JOptionPane.showMessageDialog( null, "Player " + id + " won" );
    }

    private void wireObject( Object o ) {
        final List<Field> allFields = new ArrayList<>();
        getAllFields( allFields, o.getClass() );
        for (final Field f : allFields) {
            if (f.isAnnotationPresent( AutoWire.class )) {
                injectMethod( o, f );
            }
        }
    }
}
