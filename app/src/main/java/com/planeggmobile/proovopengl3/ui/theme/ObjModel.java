package com.planeggmobile.proovopengl3.ui.theme;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ObjModel {
    private FloatBuffer vertexBuffer;
    private int vao[] = new int[1];
    private List<Float> vertices = new ArrayList<>();

    public ObjModel(Context context, int objResId) {
        loadObj(context, objResId);
        setupBuffers();
    }

    // .obj failist vertexide lugemine
    private void loadObj(Context context, int objResId) {
        try {
            InputStream is = context.getResources().openRawResource(objResId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) {  // Kui rida algab 'v' (vertex)
                    String[] parts = line.split(" ");
                    vertices.add(Float.parseFloat(parts[1]));
                    vertices.add(Float.parseFloat(parts[2]));
                    vertices.add(Float.parseFloat(parts[3]));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupBuffers() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.size() * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        for (Float vertex : vertices) {
            vertexBuffer.put(vertex);
        }
        vertexBuffer.position(0);

        GLES30.glGenVertexArrays(1, vao, 0);
        GLES30.glBindVertexArray(vao[0]);

        int[] vbo = new int[1];
        GLES30.glGenBuffers(1, vbo, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, 0);
        GLES30.glBindVertexArray(0);
    }

    public void draw(int shaderProgram) {
        GLES30.glUseProgram(shaderProgram);
        GLES30.glBindVertexArray(vao[0]);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertices.size() / 3);
        GLES30.glBindVertexArray(0);
    }
}
