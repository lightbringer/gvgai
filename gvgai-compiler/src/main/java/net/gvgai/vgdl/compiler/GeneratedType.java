package net.gvgai.vgdl.compiler;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class GeneratedType {
    public final Type type;
    public Type parentType;
    public final ClassWriter cw;
    public final Map<String, Map.Entry<Type[], GeneratorAdapter>> definedInteractions;

    public GeneratorAdapter constructor;
    public GeneratorAdapter onBoundary;
    public Class<?> clazz;
    public int classId;
    public final Map<String, String> options;

    GeneratedType( Type t, ClassWriter cw ) {
        type = t;
        this.cw = cw;
        definedInteractions = new HashMap<>();
        options = new HashMap<>();
    }
}