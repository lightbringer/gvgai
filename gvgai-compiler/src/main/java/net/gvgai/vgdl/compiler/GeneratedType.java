package net.gvgai.vgdl.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.gvgai.vgdl.compiler.library.Effect;

public class GeneratedType {
    public static class GeneratedInteraction {
        public GeneratorAdapter method;
        public final List<Effect> effects;
        public final Type[] types;

        public GeneratedInteraction( Type[] types ) {
            effects = new ArrayList<>();
            this.types = types;
        }
    }

    public final Type type;
    public Type parentType;
    public final ClassWriter cw;
    public final Map<String, GeneratedInteraction> definedInteractions;
    final Map<String, GeneratorAdapter> methods;
    public Class<?> clazz;
    public int classId;
    public final Map<String, String> options;

    GeneratedType( Type t, ClassWriter cw ) {
        type = t;
        this.cw = cw;
        definedInteractions = new HashMap<>();
        methods = new HashMap<>();
        options = new HashMap<>();

    }
}