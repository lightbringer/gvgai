package net.gvgai.vgdl.compiler.library;

import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.VGDLRuntime;

public interface Termination {

    void generate( String gameName, Set<VGDLRuntime.Feature> requiredFeatures, ClassWriter cw, GeneratorAdapter m );

}
