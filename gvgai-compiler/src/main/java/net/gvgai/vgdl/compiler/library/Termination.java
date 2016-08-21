package net.gvgai.vgdl.compiler.library;

import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.VGDLRuntime;
import net.gvgai.vgdl.compiler.VGDLCompiler;

public interface Termination {

    void generate( VGDLCompiler context, Type gameType, Set<VGDLRuntime.Feature> requiredFeatures, ClassWriter cw, GeneratorAdapter m );

}
