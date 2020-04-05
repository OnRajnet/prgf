package main;
//package lvl2advanced.p01gui.p01simple;

import lwjglutils.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

    private int shaderProgramViewer, shaderProgramLight;
    private int locView, locProjection, locType, locTime, locLightPosition, locLightVP;
    private int locViewLight, locProjectionLight, locTypeLight, locTimeLight;

    private OGLBuffers buffers;
    private Camera camera, cameraLight;
    private Mat4 projection;
    private OGLTexture2D textureMosaic;
    private OGLTexture2D.Viewer viewer;
    private OGLRenderTarget renderTarget;

    private float time = 0;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        // Set the clear color
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
        textRenderer = new OGLTextRenderer(width, height);
        glEnable(GL_DEPTH_TEST); // zapne z-test (z-buffer) - až po new OGLTextRenderer
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // vyplnění přivrácených i odvrácených stran

        shaderProgramViewer = ShaderUtils.loadProgram("/shaders/start");
        shaderProgramLight = ShaderUtils.loadProgram("/shaders/light");

        locView = glGetUniformLocation(shaderProgramViewer, "view");
        locProjection = glGetUniformLocation(shaderProgramViewer, "projection");
        locType = glGetUniformLocation(shaderProgramViewer, "type");
        locTime = glGetUniformLocation(shaderProgramViewer, "time");
        locLightPosition = glGetUniformLocation(shaderProgramViewer, "lightPosition");
        locLightVP = glGetUniformLocation(shaderProgramViewer, "lightViewProjection");

        locViewLight = glGetUniformLocation(shaderProgramLight, "view");
        locProjectionLight = glGetUniformLocation(shaderProgramLight, "projection");
        locTypeLight = glGetUniformLocation(shaderProgramLight, "type");
        locTimeLight = glGetUniformLocation(shaderProgramLight, "time");

        renderTarget = new OGLRenderTarget(1024, 1024);

        buffers = GridFactory.generateGrid(100, 100);

        cameraLight = new Camera()
                .withPosition(new Vec3D(6, 6, 6))
                .withAzimuth(5 / 4f * Math.PI)
                .withZenith(-1 / 5f * Math.PI);

//        new Mat4ViewRH()
        camera = new Camera()
                .withPosition(new Vec3D(6, 6, 5))
                .withAzimuth(5 / 4f * Math.PI)
                .withZenith(-1 / 5f * Math.PI);
//                .withFirstPerson(false)
//                .withRadius(6);
//        camera = camera.forward(0.5);

        projection = new Mat4PerspRH(
                Math.PI / 3,
                LwjglWindow.HEIGHT / (float) LwjglWindow.WIDTH,
                1, // 0.1
                20 // 50
        );

//        new Mat4OrthoRH(20, 20, 1, 20);

        viewer = new OGLTexture2D.Viewer();
        try {
            textureMosaic = new OGLTexture2D("textures/mosaic.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void display() {
        time += 0.1;
        glEnable(GL_DEPTH_TEST); // zapnout z-test (kvůli textRenderer)

        renderFromLight();
        renderFromViewer();

        viewer.view(renderTarget.getColorTexture(), -1, 0, 0.5);
        viewer.view(renderTarget.getDepthTexture(), -1, -0.5, 0.5);
        viewer.view(textureMosaic, -1, -1, 0.5);
    }

    private void renderFromLight() {
        glUseProgram(shaderProgramLight);
        renderTarget.bind();

        glClearColor(0, 0.5f, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locViewLight, false, cameraLight.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionLight, false, projection.floatArray());
        glUniform1f(locTimeLight, time);

        // renderuj stěnu
        glUniform1f(locTypeLight, 0f);
        buffers.draw(GL_TRIANGLES, shaderProgramLight);

        // renderuj elipsoid
        glUniform1f(locTypeLight, 1f);
        buffers.draw(GL_TRIANGLES, shaderProgramLight);
    }

    private void renderFromViewer() {
        glUseProgram(shaderProgramViewer);

        // nutno opravit viewport, protože render target si nastavuje vlastní
        glViewport(0, 0, width, height);

        // výchozí framebuffer - render do obrazovky
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glClearColor(0.5f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());
        glUniform3fv(locLightPosition, ToFloatArray.convert(cameraLight.getPosition()));
        glUniformMatrix4fv(locLightVP, false, cameraLight.getViewMatrix().mul(projection).floatArray());
        glUniform1f(locTime, time);
        renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture", 1);
        textureMosaic.bind(shaderProgramViewer, "textureMosaic", 0);

        // renderuj stěnu
        glUniform1f(locType, 0f);
        buffers.draw(GL_TRIANGLES, shaderProgramViewer);

        // renderuj elipsoid
        glUniform1f(locType, 1f);
        buffers.draw(GL_TRIANGLES, shaderProgramViewer);

        // create and draw text
        textRenderer.clear();
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }


    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cursorPosCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private double oldMx, oldMy;
    private boolean mousePressed;

    private GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                camera = camera.addAzimuth(Math.PI / 2 * (oldMx - x) / LwjglWindow.WIDTH);
                camera = camera.addZenith(Math.PI / 2 * (oldMy - y) / LwjglWindow.HEIGHT);
                oldMx = x;
                oldMy = y;
            }
        }
    };

    private GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                mousePressed = action == GLFW_PRESS;
            }
        }
    };

    private GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_D:
                        camera = camera.right(0.1);
                        break;
                }
            }
        }
    };

}
