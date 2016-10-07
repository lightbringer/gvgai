package net.gvgai.vgdl.compiler;

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
    public static Class<? extends VGDLGame> load( String name, InputStream input ) throws IOException {
        if (name == null || input == null) {
            throw new IllegalArgumentException();
        }

        final vgdlLexer lexer = new vgdlLexer( new ANTLRInputStream( input ) );
        final vgdlParser parser = new vgdlParser( new CommonTokenStream( lexer ) );
        final GameContext game = parser.game();

        // Walk it and attach our listener
        final ParseTreeWalker walker = new ParseTreeWalker();
        final VGDLCompiler listener = new VGDLCompiler( name, false );
//        listener.getClass(); //trigger writeout
        walker.walk( listener, game );

        Thread.currentThread().setContextClassLoader( listener.getClassLoader() );

        return listener.getClasses().stream().filter( c -> VGDLGame.class.isAssignableFrom( c ) ).findFirst().get();

    }

}
