package net.gvgai.vgdl.compiler.library;

import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.compiler.VGDLCompiler;

public interface Effect {

    void generate( VGDLCompiler vgdlCompiler, GeneratorAdapter mg );

}
