package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.GeneratedType;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.compiler.library.Effect;

public class ShieldFrom extends BaseEffect {
    private final static String SHIELDED = "shielded";

    private Class<? extends Effect> ftype;
    private GeneratedType stype;

    private Label unshieldedLabel;

    public ShieldFrom( VGDLCompiler context, Type my, Type[] other, String[] parameters ) {
        super( context, my, other, parameters );

        for (int i = 0; i < parameters.length; i += 2) {
            final String key = parameters[i];
            final String value = parameters[i + 1];

            switch (key) {
                case "stype":
                    stype = context.getClassLoader().getGeneratedTypes().get( context.getTypeForSimpleName( VGDLCompiler.formatClassName( value ) ) );
                    break;
                case "ftype":
                    ftype = context.getEffectClass( value );
                    break;
                default:
                    Logger.getLogger( getClass().getName() ).warning( "Ignoring unknown option " + key + "=" + value );
                    break;
            }
        }

        if (stype == null || ftype == null) {
            throw new IllegalArgumentException( getClass().getName() + " requires both options stype and ftype" );
        }
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, requiredFeatures, mg );

        //XXX
        vgdlCompiler.generateLogMessage( myType.getClassName(), mg, "ShieldFrom called" );

        final GeneratedType otherGeneratedType = stype;
        final GeneratedType myGeneratedType = vgdlCompiler.getClassLoader().getGeneratedTypes().get( myType );
        final String flagName = SHIELDED + otherGeneratedType.classId + ftype.getSimpleName();
        final ClassWriter myCW = myGeneratedType.cw;

        //this inject flag
        myCW.visitField( ACC_PRIVATE, flagName, "Z", null, null );

        //shield
        mg.loadThis();
        mg.push( true );
        mg.putField( myType, flagName, Type.BOOLEAN_TYPE );

        //inject unshield to update
        GeneratorAdapter postFrameMethod = myGeneratedType.methods.get( VGDLCompiler.POST_FRAME );
        if (postFrameMethod == null) {
            postFrameMethod = new GeneratorAdapter( ACC_PUBLIC, Method.getMethod( "void postFrame()" ), null, null, myGeneratedType.cw );
            postFrameMethod.loadThis();
            postFrameMethod.visitMethodInsn( INVOKESPECIAL, myGeneratedType.parentType.getInternalName(), "postFrame", "()V", false );
            myGeneratedType.methods.put( VGDLCompiler.UPDATE, postFrameMethod );
        }
        postFrameMethod.loadThis();
        postFrameMethod.push( false );
        postFrameMethod.putField( myType, flagName, Type.BOOLEAN_TYPE );

        //TODO inject copy method
    }

    @Override
    public void postGeneration( VGDLCompiler vgdlCompiler, GeneratorAdapter method, Set<Feature> requiredFeatures, Effect ef ) {
        if (ftype.isAssignableFrom( ef.getClass() ) && ef.getOtherTypes()[0] == stype.type && ef.getMyType() == myType) {
            final Label l = method.newLabel();
            vgdlCompiler.generateLogMessage( myType.getClassName(), method, myType + " was shielded NOT from " + ftype, Level.FINER );
            method.goTo( l );
            method.mark( unshieldedLabel );
            vgdlCompiler.generateLogMessage( myType.getClassName(), method, myType + " was shielded from " + ftype, Level.FINER );
            method.mark( l );
        }

    }

    @Override
    public void preGeneration( VGDLCompiler vgdlCompiler, GeneratorAdapter method, Set<Feature> requiredFeatures, Effect ef ) {
        if (ftype.isAssignableFrom( ef.getClass() ) && ef.getOtherTypes()[0] == stype.type && ef.getMyType() == myType) {
            //if shielded skip ef
            unshieldedLabel = method.newLabel();
            final GeneratedType otherGeneratedType = stype;
            final String flagName = SHIELDED + otherGeneratedType.classId + ftype.getSimpleName();
            method.loadThis();
            method.visitFieldInsn( GETFIELD, myType.getInternalName(), flagName, "Z" );
            method.visitJumpInsn( IFNE, unshieldedLabel );

        }
    }

}
