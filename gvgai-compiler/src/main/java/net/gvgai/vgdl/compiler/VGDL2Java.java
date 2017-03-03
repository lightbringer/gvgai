package net.gvgai.vgdl.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;

import net.gvgai.vgdl.compiler.generated.vgdlLexer;
import net.gvgai.vgdl.compiler.generated.vgdlParser;
import net.gvgai.vgdl.compiler.generated.vgdlParser.GameContext;
import net.gvgai.vgdl.game.VGDLGame;

public class VGDL2Java extends Task {
    public static class Input {
        private final Vector<FileSet> files = new Vector<>();

        public void addFileSet( FileSet input ) {
            files.addElement( input );
        }

        public Vector<FileSet> getFiles() {
            return files;
        }
    }

    public static class Output {
        private File dir;

        public File getDir() {
            return dir;
        }

        public void setDir( File dir ) {
            this.dir = dir;
        }

    }

    private final static Logger LOGGER = Logger.getLogger( VGDL2Java.class.getName() );

    private Input input;

    private Output output;

    private Path classpath;

    private boolean generateDebugOutput;

    public void addClasspath( Path p ) {
        classpath = p;
    }

    public void addInput( Input input ) {
        this.input = input;
    }

    public void addOutput( Output o ) {
        output = o;
    }

    public void compile( File inputFile, File outputDir ) throws IOException {
        final String gameName = inputFile.getName().substring( 0, inputFile.getName().indexOf( '.' ) );
        if (gameName.isEmpty()) {
            throw new IllegalStateException( "could not guess game name from file pattern. Rename file or supply game name as parameter" );
        }
        compile( gameName, inputFile, outputDir );
    }

    public void compile( String gameName, File inputFile, File outputDir ) throws IOException {
        if (!outputDir.isDirectory()) {
            throw new IllegalArgumentException( "output destination is not a directory" );
        }
        if (!inputFile.isFile()) {
            throw new IllegalArgumentException( "input is not a normal file" );
        }
        final char c = gameName.charAt( 0 );
        if (!Character.isUpperCase( c )) {
            gameName = Character.toUpperCase( c ) + gameName.substring( 1 );
            LOGGER.warning( "Changing game name to " + gameName );
        }
        final VGDLCompiler compilerContext = load( gameName, new FileInputStream( inputFile ) );
        compilerContext.writeClassesToDisk( outputDir );
    }

    @Override
    public void execute() {
        final ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

        final URL[] classpathArray = Stream.of( classpath.list() ).map( s -> {
            try {
                final File f = new File( s );
                if (f.isFile()) {
                    return new URL( "file:///" + f.getAbsolutePath() );
                }
                else {
                    return new URL( "file:///" + f.getAbsolutePath() + "/" );
                }
            }
            catch (final MalformedURLException e1) {
                throw new IllegalStateException( e1 );
            }
        } ).toArray( URL[]::new );

        final URLClassLoader urlClassLoader = new URLClassLoader( classpathArray, currentThreadClassLoader );

        Thread.currentThread().setContextClassLoader( urlClassLoader );

        try {
            for (final FileSet f : input.getFiles()) {
                final Iterator<Resource> iter = f.iterator();
                while (iter.hasNext()) {
                    final Resource r = iter.next();
                    compile( new File( r.toString() ), output.dir );
                }

            }
        }
        catch (final IOException e) {
            throw new BuildException( e );
        }
    }

    public boolean isGenerateDebugOutput() {
        return generateDebugOutput;
    }

    public Class<? extends VGDLGame> loadIntoMemory( String name, InputStream input ) throws IOException {

        final VGDLCompiler compilerContext = load( name, input );
        Thread.currentThread().setContextClassLoader( compilerContext.getClassLoader() );

        return compilerContext.getClasses().stream().filter( c -> VGDLGame.class.isAssignableFrom( c ) ).findFirst().get();

    }

    public void setGenerateDebugOutput( boolean generateDebugOutput ) {
        this.generateDebugOutput = generateDebugOutput;
    }

    private VGDLCompiler load( String name, InputStream input ) throws IOException {
        if (name == null || input == null) {
            throw new IllegalArgumentException();
        }

        final vgdlLexer lexer = new vgdlLexer( new ANTLRInputStream( input ) );
        final vgdlParser parser = new vgdlParser( new CommonTokenStream( lexer ) );
        parser.removeErrorListeners();
        parser.addErrorListener( new BaseErrorListener() {

            @Override
            public void syntaxError( Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg,
                            RecognitionException e ) {
                throw new ParseCancellationException( "line " + line + ":" + charPositionInLine + " " + msg );
            }

        } );
        final GameContext game = parser.game();

        // Walk it and attach our listener
        final ParseTreeWalker walker = new ParseTreeWalker();
        final VGDLCompiler compilerContext = new VGDLCompiler( name, false );
        compilerContext.setGenerateDebugOutput( generateDebugOutput );
        walker.walk( compilerContext, game );

        return compilerContext;

    }
}
