package net.gvgai.vgdl.compiler;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

class GeneratedType {
    Type type;
    int classId;
    Type parentType;
    ClassWriter cw;
    Set<Type> definedInteractions;
    Class<?> clazz;

    GeneratedType( Type t, ClassWriter cw ) {
        type = t;
        this.cw = cw;
        definedInteractions = new HashSet<Type>();
    }
}