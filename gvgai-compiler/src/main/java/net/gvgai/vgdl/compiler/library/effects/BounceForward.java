package net.gvgai.vgdl.compiler.library.effects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;

public class BounceForward implements Effect, Opcodes {
    private final Type myType;
    private final Type otherType;

    public BounceForward( Type myType, Type otherType ) {
        System.out.println( "Bounce " + myType );
        this.myType = myType;
        this.otherType = otherType;
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg ) {
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
