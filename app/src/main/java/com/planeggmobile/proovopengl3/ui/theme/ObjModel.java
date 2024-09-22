package com.planeggmobile.proovopengl3.ui.theme;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjModel {
    private FloatBuffer vertexBuffer;
    private IntBuffer indexBuffer;
    private int[] vao = new int[1];   // Vertex Array Object
    private int[] vbo = new int[1];   // Vertex Buffer Object
    private int[] ebo = new int[1];   // Element Buffer Object

    private List<Float> vertices = new ArrayList<>();
    private List<Integer> indices = new ArrayList<>();

    public interface ModelLoadCallback {
        void onModelLoaded();
    }

    public ObjModel(Context context, int objResId, ModelLoadCallback callback) {
        // Mudeli sisselugemine toimub Kotlinis taustlõimes
        ModelLoaderKt.loadModelAsync(context, objResId, this, callback);
    }

    // See meetod kutsub tagasi, kui mudel on taustal laetud
    private void onModelLoaded() {
        setupBuffers();
    }

    // OpenGL vea logija
    private void checkGlError(String label) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e("MK001 ObjModel", label + ": OpenGL Error: " + error);
        }
    }

    // .obj failist vertexide ja indeksite lugemine
    private void loadObj(Context context, int objResId) {
        try {
            InputStream is = context.getResources().openRawResource(objResId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");

                if (parts[0].equals("v")) {  // Vertex
                    vertices.add(Float.parseFloat(parts[1]));
                    vertices.add(Float.parseFloat(parts[2]));
                    vertices.add(Float.parseFloat(parts[3]));
                } else if (parts[0].equals("f")) {  // Face
                    for (int i = 1; i <= 3; i++) {
                        String[] faceParts = parts[i].split("/");
                        indices.add(Integer.parseInt(faceParts[0]) - 1);  // Vertex index
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public   void setupBuffers() {
        if (vertices.isEmpty() || indices.isEmpty()) {
            Log.e("MK001 ObjModel", "Vertexid või indeksid puuduvad. Joonistamine katkestatakse.");
            return;
        }

        // Vertex buffer
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.size() * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexByteBuffer.asFloatBuffer();
        for (Float v : vertices) {
            vertexBuffer.put(v);
        }
        vertexBuffer.position(0);

        // Index buffer
        ByteBuffer indexByteBuffer = ByteBuffer.allocateDirect(indices.size() * 4);
        indexByteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer = indexByteBuffer.asIntBuffer();
        for (Integer index : indices) {
            indexBuffer.put(index);
        }
        indexBuffer.position(0);

        // OpenGL VAO ja VBO seadistamine
        GLES30.glGenVertexArrays(1, vao, 0);
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            Log.e("MK001 OpenGL Error", "VAO loomise viga: " + error);
        }
        if (vao[0] == 0) {
            Log.e("MK001 ObjModel", "VAO loomine ebaõnnestus. OpenGL Error võib esineda.");
            checkGlError("VAO loomine");
            return;
        }
        GLES30.glBindVertexArray(vao[0]);
        checkGlError("MK001 VIGA VAO bindimine");

        GLES30.glGenBuffers(1, vbo, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0);
        GLES30.glEnableVertexAttribArray(0);
        checkGlError("MK001 Viga VBO seadistamine");

        // Element Buffer Object (indices)
        GLES30.glGenBuffers(1, ebo, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 4, indexBuffer, GLES30.GL_STATIC_DRAW);
        checkGlError("MK001 Viga EBO seadistamine");

        GLES30.glBindVertexArray(0); // Unbind VAO
    }

    public void draw(int shaderProgram) {
        if (vao == null || vao[0] == 0) {
         //   Log.e("MK001 ObjModel", "VAO puudub. Joonistamine katkestatakse.");
            checkGlError("MK001 Viga ObjModel.draw");
            return;
        }

        GLES30.glUseProgram(shaderProgram);
        GLES30.glBindVertexArray(vao[0]);
        checkGlError("MK001 Viga VAO sidumine draw jaoks");

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexBuffer.capacity(), GLES30.GL_UNSIGNED_INT, 0);
        checkGlError("MK001 Viga Joonistamine");

        GLES30.glBindVertexArray(0); // Unbind VAO
    }
}
