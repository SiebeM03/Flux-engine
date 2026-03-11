package me.siebe.flux.util.system;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import me.siebe.flux.util.logging.Logger;
import me.siebe.flux.util.logging.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassGraph {
    private static final Logger logger = LoggerFactory.getLogger(ClassGraph.class, "classgraph");
    static final String ENGINE_PACKAGE_PREFIX = "me.siebe.flux";
    private static String GAME_PACKAGE_PREFIX;

    public static void setGamePackage(Class<?> clazz) {
        if (GAME_PACKAGE_PREFIX != null) {
            logger.error("Game Package Prefix already set, don't call this method manually!");
            return;
        }
        GAME_PACKAGE_PREFIX = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    private boolean ignoreAbstractClasses = false;
    private boolean ignoreInterfaces = false;
    private boolean ignoreFluxPackages = false;


    public ClassGraph() {

    }

    public ClassGraph ignoreAbstract() {
        this.ignoreAbstractClasses = true;
        return this;
    }

    public ClassGraph ignoreInterface() {
        this.ignoreInterfaces = true;
        return this;
    }

    public ClassGraph ignoreFluxPackages() {
        this.ignoreFluxPackages = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> List<Class<T>> scanForSubclasses(Class<T> clazz) {
        List<Class<T>> implementations = new ArrayList<>();

        try (ScanResult scan = new io.github.classgraph.ClassGraph()
                .enableClassInfo()
                .scan()
        ) {
            List<Class<?>> classes;
            if (Modifier.isInterface(clazz.getModifiers())) {
                classes = scan.getClassesImplementing(clazz).loadClasses();
            } else {
                classes = scan.getSubclasses(clazz).loadClasses();
            }

            for (Class<?> c : classes) {
                if (ignoreFluxPackages && c.getPackageName().startsWith(ENGINE_PACKAGE_PREFIX)) continue;
                if (ignoreInterfaces && Modifier.isInterface(c.getModifiers())) continue;
                if (ignoreAbstractClasses && Modifier.isAbstract(c.getModifiers())) continue;

                implementations.add((Class<T>) c);
            }
        }
        return implementations;
    }
}
