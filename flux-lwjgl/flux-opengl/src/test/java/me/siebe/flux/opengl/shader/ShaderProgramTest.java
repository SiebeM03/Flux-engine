package me.siebe.flux.opengl.shader;

import me.siebe.flux.test.implementations.window.TestGlfwWindow;
import me.siebe.flux.util.exceptions.ShaderException;
import org.joml.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShaderProgramTest {
    private static TestGlfwWindow window;

    private static ShaderProgram correctShader;
    private static ShaderProgram incorrectShader;

    @BeforeAll
    static void init() {
        window = new TestGlfwWindow();
    }

    @AfterAll
    static void destroy() {
        if (correctShader != null) {
            correctShader.delete();
        }
        if (incorrectShader != null) {
            incorrectShader.delete();
        }

        window.destroy();
    }

    @Test
    @Order(1)
    void testCompileProgram() {
        assertDoesNotThrow(() -> {
            correctShader = new ShaderProgram("shaders/correct", null);
        });

        assertThrows(ShaderException.class, () -> {
            incorrectShader = new ShaderProgram("shaders/incorrect", null);
        });
    }

    @Test
    @Order(2)
    void correctProgram_uniformsLoaded() {
        // Unused uniforms are filtered out of this list by OpenGL
        // uniform float unusedUniformInVert;
        assertNull(correctShader.getUniform("unusedUniformInVert"));
        // uniform mat4 unusedUniformInFrag;
        assertNull(correctShader.getUniform("unusedUniformInFrag"));

        // Random uniforms also don't exist
        assertNull(correctShader.getUniform("someRandomUniform"));

        assertEquals(3, correctShader.uniforms.size());

        // uniform mat4 usedUniformInVert;
        ShaderUniform vertexUniform = correctShader.getUniform("usedUniformInVert");
        assertNotNull(vertexUniform);
        assertEquals("usedUniformInVert", vertexUniform.name());
        assertEquals(GL_FLOAT_MAT4, vertexUniform.glType());

        // uniform vec4 usedUniformInFrag;
        ShaderUniform fragUniform = correctShader.getUniform("usedUniformInFrag");
        assertNotNull(fragUniform);
        assertEquals("usedUniformInFrag", fragUniform.name());
        assertEquals(GL_FLOAT_VEC4, fragUniform.glType());

        // uniform float[3] arrayUniform;
        ShaderUniform arrayUniform = correctShader.getUniform("arrayUniform");
        assertNotNull(arrayUniform);
        assertEquals("arrayUniform", arrayUniform.name());
        assertEquals(GL_FLOAT, arrayUniform.glType());
        assertEquals(3, arrayUniform.size());
    }

    @Test
    @Order(2)
    void correctProgram_attributesLoaded() {
        Map<String, ShaderAttribute> attributes = correctShader.attributes;

        // Unused attributes are filtered out of this list by OpenGL
        // layout (location = 2) in float unusedAttribute;
        assertNull(attributes.get("unusedAttribute"));

        assertEquals(2, attributes.size());

        // layout (location = 0) in vec3 usedAttributeLoc0;
        ShaderAttribute usedAttributeLoc0 = attributes.get("usedAttributeLoc0");
        assertEquals("usedAttributeLoc0", usedAttributeLoc0.name());
        assertEquals(ShaderDataType.Float3, usedAttributeLoc0.type());
        assertEquals(0, usedAttributeLoc0.location());

        // layout (location = 8) in vec2 usedAttributeLoc8;
        ShaderAttribute usedAttributeLoc8 = attributes.get("usedAttributeLoc8");
        assertEquals("usedAttributeLoc8", usedAttributeLoc8.name());
        assertEquals(ShaderDataType.Float2, usedAttributeLoc8.type());
        assertEquals(8, usedAttributeLoc8.location());
    }

    @Test
    @Order(2)
    void shaderBind_setsActiveShader() {
        ShaderProgram.ACTIVE_SHADER = null;

        correctShader.bind();
        assertEquals(correctShader, ShaderProgram.getActiveShader());
        correctShader.unbind();
        assertNull(ShaderProgram.getActiveShader());
    }

    @Test
    @Order(2)
    void shaderUniformUpload_uploadsCorrectType() {
        correctShader.uniforms.put("matrix4f", new ShaderUniform("matrix4f", 0, GL_FLOAT_MAT4, 1));
        correctShader.uniforms.put("matrix3f", new ShaderUniform("matrix3f", 0, GL_FLOAT_MAT3, 1));
        correctShader.uniforms.put("vec4f", new ShaderUniform("vec4f", 0, GL_FLOAT_VEC4, 1));
        correctShader.uniforms.put("vec3f", new ShaderUniform("vec3f", 0, GL_FLOAT_VEC3, 1));
        correctShader.uniforms.put("vec2f", new ShaderUniform("vec2f", 0, GL_FLOAT_VEC2, 1));
        correctShader.uniforms.put("float", new ShaderUniform("float", 0, GL_FLOAT, 1));
        correctShader.uniforms.put("integer", new ShaderUniform("integer", 0, GL_INT, 1));
        correctShader.uniforms.put("intArray", new ShaderUniform("intArray", 0, GL_INT, 2));

        // Uploading data to a uniform with a compatible type will not throw any errors
        assertDoesNotThrow(() -> correctShader.upload("matrix4f", new Matrix4f()));
        assertDoesNotThrow(() -> correctShader.upload("matrix3f", new Matrix3f()));
        assertDoesNotThrow(() -> correctShader.upload("vec4f", new Vector4f()));
        assertDoesNotThrow(() -> correctShader.upload("vec3f", new Vector3f()));
        assertDoesNotThrow(() -> correctShader.upload("float", 1.0f));
        assertDoesNotThrow(() -> correctShader.upload("integer", 2));
        assertDoesNotThrow(() -> correctShader.upload("intArray", new int[]{0, 1}));

        // Uploading a texture that is not in the shader's uniforms will exit early without throwing anything
        assertDoesNotThrow(() -> correctShader.upload("someRandomUniform", new Vector2f()));

        // Uploading an unsupported uniform type (Object) will throw an error
        assertThrows(ShaderException.class, () -> correctShader.upload("intArray", new Object()));

        // Uploading a value (Vector2f) to a uniform that has an incompatible GL_TYPE (GL_FLOAT) will throw an error
        assertThrows(IllegalArgumentException.class, () -> correctShader.upload("float", new Vector2f()));

        // Uploading a null value will throw an error
        assertThrows(NullPointerException.class, () -> correctShader.upload("intArray", null));
    }
}
