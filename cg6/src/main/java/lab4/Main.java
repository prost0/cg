//Lab 6
//Yakimovich Alexander 8O-308B
//Variant 12 (diffuse = sin(time))
package lab4;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import lab4.Misc.Shader;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;


public class Main extends Application{

    private long win;
    private Matrix4f m = new Matrix4f().identity();
    private double curX, curY;
    public static float red = 0,green = 0,blue = 0;
    public static float amb = 0,dif = 0,spec = 0;

    private void st() {
        initial();
        loop();

        glfwFreeCallbacks(win);
        glfwDestroyWindow(win);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void initial() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to init GLFW");
        }

        glfwDefaultWindowHints();

        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        win = glfwCreateWindow(800, 800, "Лабораторная работа №6", 0, 0);
        glfwSetWindowSizeLimits(win, 400, 400, 900, 900);

        glfwSetKeyCallback(win, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
                System.exit(0);
            }
            if (key == GLFW_KEY_UP && action == GLFW_PRESS) {
                m.rotateX((float) Math.toRadians(-30));
            }
            if (key == GLFW_KEY_DOWN && action == GLFW_PRESS) {
                m.rotateX((float) Math.toRadians(30));
            }
            if (key == GLFW_KEY_LEFT && action == GLFW_PRESS) {
                m.rotateY((float) Math.toRadians(30));
            }
            if (key == GLFW_KEY_RIGHT && action == GLFW_PRESS) {
                m.rotateY((float) Math.toRadians(-30));
            }
        });

        glfwSetFramebufferSizeCallback(win, (win, w, h) -> {
            glViewport(0, 0, w, h);
            glOrtho(0, w, h, 0, -1, 1);
        });

        glfwSetMouseButtonCallback(win,(win,button,action,mods) ->{
            if(button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS){
                DoubleBuffer posX = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer posY = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(win, posX, posY);
                double x = posX.get(0);
                double y = posY.get(0);
                curX = x;
                curY = y;
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); //    int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(win, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    win,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(win);
        // Enable v-sync
        glfwSwapInterval(0);

        // Make the window visible
        glfwShowWindow(win);
    }

    private void loop() {
        GL.createCapabilities();
        glEnable(GL_TEXTURE_2D);

        // Set the background color
        glClearColor(0f / 255, .0f / 255, 0f / 255, 0.0f);
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        m.scale(0.5f);
        Shader shader = new Shader("simple");
        Figure figure = new Figure(15, 140);
        int extra_time = 0;
        int delta_color = 0;
        while (!glfwWindowShouldClose(win)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            // Load transform matrix
            glLoadMatrixf(m.get(fb));

            if (glfwGetMouseButton(win, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
                DoubleBuffer posX = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer posY = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(win, posX, posY);
                double x = posX.get(0);
                double y = posY.get(0);
                int dy = (int) (x - curX);
                int dx = (int) (y - curY);
                m.rotateY((float) Math.toRadians(dy));
                m.rotateX((float) Math.toRadians(dx));
                curX = x;
                curY = y;
            }

            shader.setUniform("col", new Vector4f(red / 255.f, green / 255.f, blue / 255.f, 1));
            shader.bind();
            shader.setUniform("project", m);
            shader.setUniform("col", new Vector4f((float)Math.sin(Math.toRadians((delta_color + red)%360)),
                    (float)Math.sin(Math.toRadians((delta_color + green)%360)),
                    (float)Math.sin(Math.toRadians((delta_color + blue)%360)), 1 ));

            figure.draw(m, shader);
            if(extra_time % 25 == 0) {
                delta_color++;
                delta_color %= 360;
            }

            extra_time++;
            extra_time %= 26;

            glfwSwapBuffers(win);
            glfwPollEvents();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        final Parent root = FXMLLoader.load(getClass().getResource("/scheme/lab4.fxml"));
        final Scene scene = new Scene(root);
        stage.setTitle("Лабораторная работа №6 Вариант 20, Якимович Александр");
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(scene.getWindow().getWidth());
        stage.setMinHeight(scene.getWindow().getHeight());
    }

    public static void main(String[] args) throws Exception {
        Main m = new Main();
        launch(args);
        m.st();
    }
}