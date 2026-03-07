package me.siebe.flux.test.junit;

import me.siebe.flux.core.AppContext;
import me.siebe.flux.core.HeadlessContextInjector;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that clears {@link AppContext} after each test so the next test
 * gets a clean state. Register with {@code @RegisterExtension} so you don't have to
 * call {@link HeadlessContextInjector#resetAppContext()} in {@code @AfterEach}.
 * <p>
 * Example:
 * <pre>{@code
 * @RegisterExtension
 * static final EngineTestHarnessExtension clearContext = new EngineTestHarnessExtension();
 *
 * @Test
 * void myTest() {
 *     EngineTestHarness h = EngineTestHarness.builder().world(World.create("test", 100)).build();
 *     // ... use harness; no need to call h.teardown() in @AfterEach
 * }
 * }</pre>
 */
public final class FluxTestHarnessExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        HeadlessContextInjector.resetAppContext();
    }
}