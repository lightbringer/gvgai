package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;

public class BounceForward extends BaseEffect {

    public BounceForward( Type myType, Type otherType, String... s ) {
        super( myType, otherType, s );
        System.out.println( "Bounce " + myType );

    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, mg );
//        VGDLCompiler.generateConsoleMessage( mg, "Bounce forward" );

        //Move object 0 (this) into the direction that 1 (0) is facing
        mg.loadThis();
        mg.loadArg( 0 );
        final Method getDir = Method.getMethod( "Object getDirection( )" );
        mg.invokeVirtual( otherType, getDir );
        final Method move = Method.getMethod( "void move(Object)" );
        mg.invokeVirtual( myType, move );
    }

}
