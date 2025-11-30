package me.siebe.flux.util.exceptions;

public class ApplicationException extends EngineException {
    public ApplicationException(String message) {
        super(message);
    }

    public static ApplicationException notInitialized() {
        return new ApplicationException("Application was not yet initialized, make sure to call init() on it");
    }

    public static ApplicationException alreadyInitialized() {
        return new ApplicationException("Tried initializing application when it is already initialized");
    }

    public static ApplicationException noAppProviderImplementationFound() {
        return new ApplicationException("No FluxApplication implementation found, " +
                "make sure to extend the class in your game project. " +
                "Also make sure the package name in your project does not start with `me.siebe.flux`");
    }

    public static ApplicationException multipleAppProviderImplementationFound() {
        return new ApplicationException("Multiple FluxApplication implementations were specified, " +
                "you must have exactly 1 FluxApplication implementation");
    }
}
