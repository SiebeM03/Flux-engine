package me.siebe.flux.opengl;

import me.siebe.flux.core.system.StartupBannerSection;
import me.siebe.flux.core.system.SystemInfoProvider;

import java.util.LinkedHashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class OpenGLInfoProvider implements SystemInfoProvider {
    @Override
    public StartupBannerSection provide() {
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        properties.put("OpenGL Version", glGetString(GL_VERSION));
        properties.put("GLSL Version", glGetString(GL_SHADING_LANGUAGE_VERSION));
        properties.put("Max Texture Size", getMaxTextureSize());
        properties.put("Max Texture Units", getMaxTextureUnits());
        properties.put("Max Vertex Attribs", getMaxVertexAttribs());
        return new StartupBannerSection("Graphics", properties);
    }

    private String getMaxTextureSize() {
        int[] maxSize = new int[1];
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, maxSize);
        return maxSize[0] + " x " + maxSize[0];
    }

    private String getMaxTextureUnits() {
        int[] maxUnits = new int[1];
        glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, maxUnits);
        return String.valueOf(maxUnits[0]);
    }

    private String getMaxVertexAttribs() {
        int[] maxAttribs = new int[1];
        glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, maxAttribs);
        return String.valueOf(maxAttribs[0]);
    }
}
