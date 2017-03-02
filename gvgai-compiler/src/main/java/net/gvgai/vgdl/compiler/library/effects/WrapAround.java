package net.gvgai.vgdl.compiler.library.effects;

import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;
import net.gvgai.vgdl.game.GameMap;
import net.gvgai.vgdl.sprites.VGDLSprite;

public class WrapAround extends BaseEffect {

    public WrapAround( VGDLCompiler context, Type my, Type[] others, String... parameters ) {
        super( context, my, others, parameters );
    }

    @Override
    public void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg ) {
        super.generate( vgdlCompiler, requiredFeatures, mg );

//        VGDLCompiler.generateConsoleMessage( mg, "wrap" );

        final Type mapType = Type.getType( GameMap.class );

        VGDLCompiler.generateGetGameMap( mg );
        VGDLCompiler.generateGetGameMap( mg );
        final Method wrapMethod = Method.getMethod( "Object wrap(Object)" );
        //Stack: [gameMap, gameMap]
        mg.loadThis();
        //Stack: [gameMap, gameMap, this]
        final Method getPositionMethod = Method.getMethod( "Object getPosition()" );
        mg.invokeVirtual( myType, getPositionMethod );
        final int posLocal = mg.newLocal( Type.getType( Object.class ) );
        mg.storeLocal( posLocal );
        //Stack: [gameMap, gameMap]
        VGDLCompiler.generateGetGameMap( mg );
        //Stack: [gameMap, gameMap, gameMap]
        mg.loadLocal( posLocal );
        //Stack: [gameMap, gameMap, gameMap, myPos]
        mg.loadThis();
        //Stack: [gameMap, gameMap, gameMap, myPos, this]
        final Method removeMethod = Method.getMethod( "void remove(Object, " + VGDLSprite.class.getName() + ")" );
        mg.invokeInterface( mapType, removeMethod );
        //Stack: [gameMap, gameMap]
        mg.loadLocal( posLocal );
        //Stack: [gameMap, gameMap, thisPosition]
        mg.invokeInterface( mapType, wrapMethod );
        //Stack: [gameMap, wrappedPosition]
        mg.loadThis();
        //Stack: [gameMap, wrappedPosition, this]

        final Method setMethod = Method.getMethod( "boolean set(Object, " + VGDLSprite.class.getName() + ")" );
        mg.invokeInterface( mapType, setMethod );
    }

}
