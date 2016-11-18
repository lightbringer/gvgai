package net.gvgai.vgdl.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.gvgai.vgdl.compiler.generated.vgdlLexer;
import net.gvgai.vgdl.compiler.generated.vgdlParser;
import net.gvgai.vgdl.compiler.generated.vgdlParser.GameContext;
import net.gvgai.vgdl.game.VGDLGame;

public class VGDL2Java {
    public static void compile( File inputFile, File outputDir ) throws IOException {
        final String gameName = inputFile.getName().substring( 0, inputFile.getName().indexOf( '.' ) );
        if (gameName.isEmpty()) {
            throw new IllegalStateException( "could not guess game name from file pattern. Rename file or supply game name as parameter" );
        }
        compile( gameName, inputFile, outputDir );
    }

    public static void compile( String gameName, File inputFile, File outputDir ) throws IOException {
        if (!outputDir.isDirectory()) {
            throw new IllegalArgumentException( "output destination is not a directory" );
        }
        if (!inputFile.isFile()) {
            throw new IllegalArgumentException( "input is not a normal file" );
        }
        final char c = gameName.charAt( 0 );
        if (!Character.isUpperCase( c )) {
            gameName = Character.toUpperCase( c ) + gameName.substring( 1 );
            System.err.println( "Warning: changing game name to " + gameName );
        }
        final VGDLCompiler compilerContext = load( gameName, new FileInputStream( inputFile ) );
        compilerContext.writeClassesToDisk( outputDir );
    }

    public static Class<? extends VGDLGame> loadIntoMemory( String name, InputStream input ) throws IOException {

        final VGDLCompiler compilerContext = load( name, input );
        Thread.currentThread().setContextClassLoader( compilerContext.getClassLoader() );

        return compilerContext.getClasses().stream().filter( c -> VGDLGame.class.isAssignableFrom( c ) ).findFirst().get();

    }

    private static VGDLCompiler load( String name, InputStream input ) throws IOException {
        if (name == null || input == null) {
            throw new IllegalArgumentException();
        }

        final vgdlLexer lexer = new vgdlLexer( new ANTLRInputStream( input ) );
        final vgdlParser parser = new vgdlParser( new CommonTokenStream( lexer ) );
        final GameContext game = parser.game();

        // Walk it and attach our listener
        final ParseTreeWalker walker = new ParseTreeWalker();
        final VGDLCompiler compilerContext = new VGDLCompiler( name, false );
        walker.walk( compilerContext, game );

        return compilerContext;
    }
}
