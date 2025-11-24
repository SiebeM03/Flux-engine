package me.siebe.flux.api.window;

class MockWindowBuilder extends WindowBuilder {
    MockWindowBuilder() {
        super(WindowPlatform.GLFW);
    }

    @Override
    public Window build() {
        return null;
    }


    public WindowConfig getConfig() {return config;}
}
