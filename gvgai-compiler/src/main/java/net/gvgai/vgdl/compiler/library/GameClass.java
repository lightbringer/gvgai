package net.gvgai.vgdl.compiler.library;

import java.lang.reflect.Field;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.generated.vgdlParser.OptionContext;

public class GameClass {
    public final static String KEY_HANDLER = "key_handler";
    public final static String KEY_HANDLER_PULSE = "Pulse";

    public static void handleGameClassOption( Set<Feature> requiredFeatures, GeneratorAdapter currentMehod, Type parent, OptionContext o ) {
        Class<?> parentClass;
        try {
            parentClass = Class.forName( parent.getClassName() );
        }
        catch (final ClassNotFoundException e1) {
            throw new RuntimeException( e1 );
        }
        switch (o.option_key().getText()) {
            case KEY_HANDLER:
                injectKeyHandler( requiredFeatures, currentMehod, o.option_value().getText() );
                break;
            default:

                try {
                    currentMehod.loadThis();
                    final Field f = parentClass.getDeclaredField( o.option_key().getText() );
                    if (f.getType() == int.class) {
                        final int i = Integer.parseInt( o.option_value().getText() );
                        currentMehod.push( i );
                    }
                    else if (f.getType() == String.class) {
                        currentMehod.push( o.option_value().getText() );
                    }
                    else {
                        throw new IllegalStateException( "unhandled field type" );
                    }
                    currentMehod.putField( parent, o.option_key().getText(), Type.getType( f.getType() ) );
                }
                catch (final NoSuchFieldException | SecurityException e) {
                    throw new RuntimeException( e );
                }
        }
    }

    private static void injectKeyHandler( Set<Feature> requiredFeatures, GeneratorAdapter currentMehod, String text ) {
        switch (text) {
            case KEY_HANDLER_PULSE:
                requiredFeatures.add( Feature.DISCRETE_GAME );
                break;
            default:
                throw new IllegalArgumentException( "unknown key handler type" );
        }

    }
}
