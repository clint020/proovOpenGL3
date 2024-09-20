package com.planeggmobile.proovopengl3.ui.theme;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {

    private static final String VERTEX_SHADER_CODE =
            "#version 300 es\n" +
                    "layout(location = 0) in vec4 vPosition;\n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * vPosition;\n" +
                    "}";

    private static final String FRAGMENT_SHADER_CODE_RED =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "out vec4 fragColor;\n" +
                    "void main() {\n" +
                    "    fragColor = vec4(1.0, 0.0, 0.0, 1.0);\n" +  // Punane värv
                    "}";

    private static final String FRAGMENT_SHADER_CODE_BLUE =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "out vec4 fragColor;\n" +
                    "void main() {\n" +
                    "    fragColor = vec4(0.0, 0.0, 1.0, 1.0);\n" +  // Sinine värv
                    "}";

    private final float[] triangleCoords = {
            0.0f, 0.5f, 0.0f,  // Tipp
            -0.5f, -0.5f, 0.0f,  // Vasak
            0.5f, -0.5f, 0.0f   // Parem
    };

    private final float[] squareCoords = {
            -0.25f,  0.25f, 0.0f,  // Ülemine vasak
            -0.25f, -0.25f, 0.0f,  // Alumine vasak
            0.25f, -0.25f, 0.0f,   // Alumine parem
            0.25f,  0.25f, 0.0f    // Ülemine parem
    };

    private int triangleProgram;
    private int squareProgram;
    private int mvpMatrixHandle;

    private final FloatBuffer triangleVertexBuffer;
    private final FloatBuffer squareVertexBuffer;

    private final int[] triangleVao = new int[1];
    private final int[] squareVao = new int[1];

    private final float[] mvpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] rotationMatrix = new float[16];  // Lisame pöörlemismaatriksi

    private float accumulatedTime = 0.0f;  // Akumuleeritud aeg pöörlemise jaoks
    private long previousTime;
    private long lastFrameTime = System.nanoTime();  // Kaadrisageduse haldamiseks

    public GameRenderer(Context context) {
        triangleVertexBuffer = initVertexBuffer(triangleCoords);
        squareVertexBuffer = initVertexBuffer(squareCoords);
        previousTime = System.currentTimeMillis();  // Algusaeg pöörlemise jaoks
    }

    private FloatBuffer initVertexBuffer(float[] coords) {
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(coords);
        buffer.position(0);
        return buffer;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        triangleProgram = createShaderProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE_RED);
        squareProgram = createShaderProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE_BLUE);
        setupTriangleBuffers();
        setupSquareBuffers();
    }

    private int createShaderProgram(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode);
        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
        return program;
    }

    private int compileShader(int type, String shaderCode) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);
        return shader;
    }

    private void setupTriangleBuffers() {
        GLES30.glGenVertexArrays(1, triangleVao, 0);
        GLES30.glBindVertexArray(triangleVao[0]);

        int[] vbo = new int[1];
        GLES30.glGenBuffers(1, vbo, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, triangleCoords.length * 4, triangleVertexBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0);
        GLES30.glBindVertexArray(0);
    }

    private void setupSquareBuffers() {
        GLES30.glGenVertexArrays(1, squareVao, 0);
        GLES30.glBindVertexArray(squareVao[0]);

        int[] vbo = new int[1];
        GLES30.glGenBuffers(1, vbo, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, squareCoords.length * 4, squareVertexBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0);
        GLES30.glBindVertexArray(0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - previousTime) / 1000.0f;  // Aeg sekundites
        previousTime = currentTime;

        accumulatedTime += deltaTime;  // Akumuleerime aja, et pöörlemine oleks sujuv

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // Pöörlemise arvutamine (1 ringi sekundis, 360 kraadi sekundis)
        float angle = (360.0f * accumulatedTime) % 360.0f;  // Akumuleeritud pöörlemisnurk
        Matrix.setRotateM(rotationMatrix, 0, angle, 0.0f, 0.0f, 1.0f);  // Z-telje ümber pööramine

        // Joonistame kolmnurga
        GLES30.glUseProgram(triangleProgram);
        drawRotatingTriangle();

        // Joonistame ruudu (liikumatu)
        GLES30.glUseProgram(squareProgram);
        drawSquare();

        // Kaadrisageduse piiramine 60 FPS-ini
        limitFrameRate(60);
    }

    private void drawRotatingTriangle() {
        GLES30.glBindVertexArray(triangleVao[0]);

        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Pöörlemismaatriksi lisamine MVP maatriksile
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, rotationMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0);

        mvpMatrixHandle = GLES30.glGetUniformLocation(triangleProgram, "uMVPMatrix");
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

        GLES30.glBindVertexArray(0);
    }

    private void drawSquare() {
        GLES30.glBindVertexArray(squareVao[0]);

        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        mvpMatrixHandle = GLES30.glGetUniformLocation(squareProgram, "uMVPMatrix");
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 4);  // Ruudu jaoks kasutame TRIANGLE_FAN

        GLES30.glBindVertexArray(0);
    }

    private void limitFrameRate(int targetFPS) {
        long currentTime = System.nanoTime();
        long frameTime = currentTime - lastFrameTime;
        long targetFrameTime = 1000000000 / targetFPS;  // Nanosekundites

        if (frameTime < targetFrameTime) {
            long sleepTime = (targetFrameTime - frameTime) / 1000000;  // Teisendame millisekunditeks
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        lastFrameTime = currentTime;  // Uuenda viimase kaadri aega
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES30.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }
}
