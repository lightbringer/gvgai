package net.gvgai.vgdl.compiler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

class VGDLClassLoader extends ClassLoader {
    private final Map<Type, GeneratedType> generatedTypes;

    /**
    * @param vgdlCompiler
    */
    VGDLClassLoader() {
        generatedTypes = new HashMap<Type, GeneratedType>();

    }

    public Map<Type, GeneratedType> getGeneratedTypes() {
        return generatedTypes;
    }

    private Class<?> defineClass( String name, ClassWriter cw ) {
        final byte[] code = cw.toByteArray();
//            if (writeClassesToDisk) {
//                try {
//                    final FileOutputStream out = new FileOutputStream( name + ".class" );
//                    out.write( code );
//                    out.close();
//                }
//                catch (final Exception e) {
//
//                }
//            }
        return defineClass( name, code, 0, code.length );

    }

    @Override
    protected Class<?> findClass( String name ) throws ClassNotFoundException {
        final Type t = Type.getType( "L" + name.replace( ".", "/" ) + ";" );
        if (generatedTypes.containsKey( t )) {
            final GeneratedType gt = generatedTypes.get( t );
            if (gt.clazz == null) {
                gt.clazz = defineClass( name, gt.cw );
            }
            return gt.clazz;
        }
        else {
            return loadClass( name, false );
        }
    }

    Set<Class> getFQNs( String simpleName ) throws IOException {
        final ImmutableSet<ClassInfo> allClasses = ClassPath.from( this ).getAllClasses();
        final Set<Class> ret = new HashSet<Class>();
        for (final ClassInfo c : allClasses) {
            if (c.getSimpleName().equals( simpleName )) {
                ret.add( c.load() );
            }
        }

        return ret;
    }

}