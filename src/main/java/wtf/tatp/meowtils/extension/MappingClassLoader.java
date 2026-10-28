package wtf.tatp.meowtils.extension;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public class MappingClassLoader extends ClassLoader {
    private static final Map<String, String> METHOD_MAP = loadCsv("mappings/methods.csv");
    private static final Map<String, String> FIELD_MAP = loadCsv("mappings/fields.csv");
    private final ExtensionClassLoader source;

    public MappingClassLoader(ExtensionClassLoader source) {
        super(source.getParent());
        this.source = source;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/') + ".class";

        try (InputStream is = this.source.getResourceAsStream(path)) {
            if (is == null) {
                throw new ClassNotFoundException(name);
            } else {
                byte[] remapped = this.remap(this.readAllBytes(is));
                return this.defineClass(name, remapped, 0, remapped.length);
            }
        } catch (IOException var18) {
            throw new ClassNotFoundException(name, var18);
        }
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toByteArray();
    }

    private byte[] remap(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, 1);
        cr.accept(new ClassRemapper(cw, new Remapper() {
            public String mapMethodName(String owner, String name, String descriptor) {
                String m = MappingClassLoader.METHOD_MAP.get(name);
                return m != null ? m : name;
            }

            public String mapFieldName(String owner, String name, String descriptor) {
                String f = MappingClassLoader.FIELD_MAP.get(name);
                return f != null ? f : name;
            }
        }), 0);
        return cw.toByteArray();
    }

    private static Map<String, String> loadCsv(String resourcePath) {
        try (InputStream is = MappingClassLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                Map<String, String> map = new HashMap<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                reader.readLine();

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] cols = line.split(",", -1);
                    if (cols.length >= 2) {
                        map.put(cols[0].trim(), cols[1].trim());
                    }
                }

                return map;
            } else {
                return Collections.emptyMap();
            }
        } catch (IOException var19) {
            return Collections.emptyMap();
        }
    }
}
