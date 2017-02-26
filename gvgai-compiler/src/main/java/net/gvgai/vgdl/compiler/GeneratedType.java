package net.gvgai.vgdl.compiler;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

class GeneratedType {
    final Type type;
    Type parentType;
    final ClassWriter cw;
    final Map<String, Map.Entry<Type[], GeneratorAdapter>> definedInteractions;

    GeneratorAdapter onBoundary;
    Class<?> clazz;
    int classId;

    GeneratedType( Type t, ClassWriter cw ) {
        type = t;
        this.cw = cw;
        definedInteractions = new HashMap<>();
    }
}