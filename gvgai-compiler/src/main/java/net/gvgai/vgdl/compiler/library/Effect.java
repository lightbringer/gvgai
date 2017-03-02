package net.gvgai.vgdl.compiler.library;

import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.VGDLRuntime.Feature;
import net.gvgai.vgdl.compiler.VGDLCompiler;

public interface Effect {

    void generate( VGDLCompiler vgdlCompiler, Set<Feature> requiredFeatures, GeneratorAdapter mg );

    Type getMyType();

    Type[] getOtherTypes();

    void postGeneration( VGDLCompiler vgdlCompiler, GeneratorAdapter method, Set<Feature> requiredFeatures, Effect ef );

    void preGeneration( VGDLCompiler vgdlCompiler, GeneratorAdapter method, Set<Feature> requiredFeatures, Effect ef );

}
