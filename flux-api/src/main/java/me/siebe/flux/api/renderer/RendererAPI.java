package me.siebe.flux.api.renderer;

public interface RendererAPI {
    void init();

    void setViewport(int x, int y, int width, int height);

    void setClearColor(float r, float g, float b, float a);

    void clear();


}
