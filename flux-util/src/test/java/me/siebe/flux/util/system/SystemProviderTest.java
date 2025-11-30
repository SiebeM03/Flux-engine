package me.siebe.flux.util.system;

import customsystems.mockabstract.CustomAbstractA;
import customsystems.mockinterface.AbstractCustomInterface;
import customsystems.mockinterface.CustomInterfaceA;
import customsystems.mockinterface.CustomInterfaceB;
import customsystems.mockinterface.InterfaceCustomInterface;
import customsystems.mocknormal.CustomNormalA;
import customsystems.mocknormal.CustomNormalNoArgsConstructor;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import me.siebe.flux.util.exceptions.FluxException;
import me.siebe.flux.util.system.mockabstract.EngineAbstractA;
import me.siebe.flux.util.system.mockabstract.EngineAbstractB;
import me.siebe.flux.util.system.mockabstract.MockSystemAbstractClass;
import me.siebe.flux.util.system.mockinterface.EngineInterfaceA;
import me.siebe.flux.util.system.mockinterface.MockSystemInterface;
import me.siebe.flux.util.system.mocknormal.EngineNormalA;
import me.siebe.flux.util.system.mocknormal.MockSystemNormalClass;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the SystemProvider class.
 * <p>
 * This test class verifies the functionality of the SystemProvider class by checking if it can correctly provide implementations of ProvidedSystem interfaces.
 * <p>
 * The mock implementations work as follows:
 * <ul>
 *   <li>Custom implementations are defined in the {@code customsystems} package</li>
 *   <li>Engine implementations are defined inside the {@code me.siebe.flux} package, they are also specified in the resources/META-INF/services folder</li>
 * </ul>
 * <p>
 * This separation is important because it allows for the engine to provide default implementations of the interfaces, while the game can provide custom implementations.
 */
public class SystemProviderTest {

    // =================================================================================================================
    // Interface implementations
    // =================================================================================================================
    @Test
    void provideAllInterface_CustomAndEngine() {
        List<MockSystemInterface> systems = SystemProvider.provideAll(MockSystemInterface.class, SystemProviderType.ALL);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(3, systems.size(), "Systems should contain 3 implementations");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomInterfaceA.class.getSimpleName())));
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomInterfaceB.class.getSimpleName())));
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(EngineInterfaceA.class.getSimpleName())));
    }

    @Test
    void provideAllInterface_CustomOnly() {
        List<MockSystemInterface> systems = SystemProvider.provideAll(MockSystemInterface.class, SystemProviderType.CUSTOM_ONLY);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(2, systems.size(), "Systems should contain 2 implementations");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomInterfaceA.class.getSimpleName())));
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomInterfaceB.class.getSimpleName())));
    }

    @Test
    void testProvideAllInterface_EngineOnly() {
        List<MockSystemInterface> systems = SystemProvider.provideAll(MockSystemInterface.class, SystemProviderType.ENGINE_ONLY);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(1, systems.size(), "Systems should contain 1 implementation");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(EngineInterfaceA.class.getSimpleName())));
    }

    @Test
    void provideWithSelectorInterface_CustomAndEngine() {
        MockSystemInterface system = SystemProvider.provide(MockSystemInterface.class, SystemProviderType.ALL, impl -> impl.getValue() == 1);
        // EngineInterfaceA is the only (or first) implementations that has the getValue() method to return 1
        assertEquals(system.getClass().getSimpleName(), EngineInterfaceA.class.getSimpleName());
    }

    @Test
    void provideWithSelectorInterface_ReturnsFirstImplementation() {
        MockSystemInterface system = SystemProvider.provide(MockSystemInterface.class, SystemProviderType.ALL, s -> true);
        // Returns first class it finds (custom takes priority)
        assertEquals(system.getClass().getSimpleName(), CustomInterfaceA.class.getSimpleName());
    }


    // =================================================================================================================
    // Abstract class implementations
    // =================================================================================================================
    @Test
    void provideAllAbstract_CustomAndEngine() {
        List<MockSystemAbstractClass> systems = SystemProvider.provideAll(MockSystemAbstractClass.class, SystemProviderType.ALL);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(3, systems.size(), "Systems should contain 3 implementations");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(EngineAbstractA.class.getSimpleName())));
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(EngineAbstractB.class.getSimpleName())));
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomAbstractA.class.getSimpleName())));
    }

    @Test
    void provideAllAbstract_CustomOnly() {
        List<MockSystemAbstractClass> systems = SystemProvider.provideAll(MockSystemAbstractClass.class, SystemProviderType.CUSTOM_ONLY);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(1, systems.size(), "Systems should contain 1 implementation");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomAbstractA.class.getSimpleName())));
    }

    @Test
    void provideAllAbstract_EngineOnly() {
        List<MockSystemAbstractClass> systems = SystemProvider.provideAll(MockSystemAbstractClass.class, SystemProviderType.ENGINE_ONLY);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(2, systems.size(), "Systems should contain 2 implementations");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(EngineAbstractA.class.getSimpleName())));
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(EngineAbstractB.class.getSimpleName())));
    }


    // =================================================================================================================
    // Normal class extensions
    // =================================================================================================================
    @Test
    void provideAllNormal_CustomAndEngine() {
        List<MockSystemNormalClass> systems = SystemProvider.provideAll(MockSystemNormalClass.class, SystemProviderType.ALL);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(2, systems.size(), "Systems should contain 2 implementations");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(EngineNormalA.class.getSimpleName())));
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomNormalA.class.getSimpleName())));
    }

    @Test
    void provideAllNormal_CustomOnly() {
        List<MockSystemNormalClass> systems = SystemProvider.provideAll(MockSystemNormalClass.class, SystemProviderType.CUSTOM_ONLY);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(1, systems.size(), "Systems should contain 1 implementation");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomNormalA.class.getSimpleName())));
    }

    @Test
    void provideAllNormal_EngineOnly() {
        List<MockSystemNormalClass> systems = SystemProvider.provideAll(MockSystemNormalClass.class, SystemProviderType.ENGINE_ONLY);

        assertFalse(systems.isEmpty(), "Systems should not be empty");
        assertEquals(1, systems.size(), "Systems should contain 1 implementation");
        assertTrue(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(EngineNormalA.class.getSimpleName())));
    }

    @Test
    void provideNormal_CustomAndEngine_CustomTakesPriority() {
        // MockSystemNormalClass has 2 implementations (1 engine and 1 custom),
        // the custom implementation always has priority over the engine implementation
        MockSystemNormalClass system = SystemProvider.provide(MockSystemNormalClass.class, SystemProviderType.ALL);

        assertEquals(system.getClass().getSimpleName(), CustomNormalA.class.getSimpleName());
    }


    // =================================================================================================================
    // Additional cases
    // =================================================================================================================
    @Test
    void provideNoImplementations_ShouldThrow() {
        assertThrows(FluxException.class, () -> SystemProvider.provide(NoImplementationInterface.class, SystemProviderType.ALL));
    }

    @Test
    void provideAll_AbstractAndInterfaceIgnored() {
        List<Class<?>> classes;
        try (ScanResult scan = new ClassGraph().enableClassInfo().scan()) {
            classes = scan.getClassesImplementing(MockSystemInterface.class).loadClasses();
        }

        assertEquals(5, classes.size());
        // Abstract class and interface are included in the scan result from classgraph
        assertTrue(classes.stream().anyMatch(c -> c.getSimpleName().equals(AbstractCustomInterface.class.getSimpleName())));
        assertTrue(classes.stream().anyMatch(c -> c.getSimpleName().equals(InterfaceCustomInterface.class.getSimpleName())));

        List<MockSystemInterface> systems = SystemProvider.provideAll(MockSystemInterface.class, SystemProviderType.ALL);
        // Abstract class and interface are not included in the response from SystemProvider.provideAll()
        assertFalse(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(AbstractCustomInterface.class.getSimpleName())));
        assertFalse(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(InterfaceCustomInterface.class.getSimpleName())));
    }

    @Test
    void provideAll_NoArgsConstructorsIgnored() {
        List<Class<?>> classes;
        try (ScanResult scan = new ClassGraph().enableClassInfo().scan()) {
            classes = scan.getSubclasses(MockSystemNormalClass.class).loadClasses();
        }

        assertEquals(3, classes.size());
        // Classes without no args constructor are included
        assertTrue(classes.stream().anyMatch(c -> c.getSimpleName().equals(CustomNormalNoArgsConstructor.class.getSimpleName())));

        List<MockSystemNormalClass> systems = SystemProvider.provideAll(MockSystemNormalClass.class, SystemProviderType.ALL);
        // Classes without no args constructor are ignored
        assertFalse(systems.stream().anyMatch(s -> s.getClass().getSimpleName().equals(CustomNormalNoArgsConstructor.class.getSimpleName())));
    }


    @Test
    void provideAll_CustomTakesPriority() {
        List<MockSystemInterface> systems = SystemProvider.provideAll(MockSystemInterface.class, SystemProviderType.ALL);
        List<MockSystemInterface> engine = SystemProvider.provideAll(MockSystemInterface.class, SystemProviderType.ENGINE_ONLY);
        List<MockSystemInterface> custom = SystemProvider.provideAll(MockSystemInterface.class, SystemProviderType.CUSTOM_ONLY);

        // Find the first index where an engine implementation appears
        int firstEngineImplementationIndex = -1;
        for (int i = 0; i < systems.size(); i++) {
            final Class<?> systemClass = systems.get(i).getClass();
            if (engine.stream().anyMatch(e -> e.getClass().equals(systemClass))) {
                firstEngineImplementationIndex = i;
                break;
            }
        }

        assertNotEquals(-1, firstEngineImplementationIndex, "Engine implementation should appear at some point in the list");

        // Now ensure all custom implementations appear BEFORE the first engine implementation
        for (int i = 0; i < firstEngineImplementationIndex; i++) {
            var system = systems.get(i);
            boolean isCustom = custom.stream().anyMatch(c -> c.getClass().equals(system.getClass()));
            assertTrue(isCustom, "Custom implementation not placed before engine implementations");
        }

        // Additionally ensure no custom implementations appear AFTER engine ones
        for (int i = firstEngineImplementationIndex; i < systems.size(); i++) {
            var system = systems.get(i);
            boolean isEngine = engine.stream().anyMatch(e -> e.getClass().equals(system.getClass()));
            assertTrue(isEngine, "Engine implementations should only appear after all custom implementations");
        }
    }
}



















