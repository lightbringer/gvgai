package net.gvgai.vgdl.compiler;

import java.io.IOException;
import java.util.ServiceLoader;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.compiler.generated.vgdlLexer;
import net.gvgai.vgdl.compiler.generated.vgdlParser;
import net.gvgai.vgdl.compiler.generated.vgdlParser.GameContext;
import net.gvgai.vgdl.game.VGDLGame;

public class VGDL2Java {
    public static void main( String[] args ) throws IOException {
        final vgdlLexer lexer = new vgdlLexer( new ANTLRFileStream( VGDL2Java.class.getResource( "sokoban.txt" ).getFile() ) );
        final vgdlParser parser = new vgdlParser( new CommonTokenStream( lexer ) );
        final GameContext game = parser.game();

        // Walk it and attach our listener
        final ParseTreeWalker walker = new ParseTreeWalker();
        final VGDLCompiler listener = new VGDLCompiler( "Sokoban", true );
        listener.getClass(); //trigger writeout
        walker.walk( listener, game );

        Thread.currentThread().setContextClassLoader( listener.getClassLoader() );

        final Class<? extends VGDLGame> gameClass = listener.getClasses().stream().filter( c -> VGDLGame.class.isAssignableFrom( c ) ).findFirst().get();

        final ServiceLoader<VGDLRuntime> loader = ServiceLoader.load( VGDLRuntime.class );
        final VGDLRuntime runtime = loader.iterator().next();
        runtime.loadGame( gameClass );
        runtime.loadLevel( VGDL2Java.class.getResourceAsStream( "sokoban_lvl0.txt" ) );
        runtime.run();
    }

}
