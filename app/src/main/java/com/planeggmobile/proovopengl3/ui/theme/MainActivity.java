package com.planeggmobile.proovopengl3.ui.theme;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MK001", "alustasime");
        glSurfaceView = new GLSurfaceView(this);

        // Kontrollime, kas OpenGL ES 3.0 on toetatud
        if (supportsEs3()) {
            // Seadistame OpenGL ES 3.0 renderdamise ja GLSurfaceView-i
            Log.d("MK001", "Toetab OpenGL ES 3.0");
            glSurfaceView.setEGLContextClientVersion(3);
            glSurfaceView.setRenderer(new GameRenderer(this));
            rendererSet = true;
        } else {
            Log.d("MK001", "OpenGL ES 3.0 ei ole toetatud");
            // OpenGL ES 3.0 ei ole toetatud, lõpetame tegevuse
            finish();
            return;
        }

        setContentView(glSurfaceView);
        Log.d("MK001", "lõpetasime");
    }

    private boolean supportsEs3() {
        // Kontrollime seadme OpenGL versiooni
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        // Kas seade toetab OpenGL ES 3.0
        return configurationInfo.reqGlEsVersion >= 0x30000;  // OpenGL ES 3.0 kontroll
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }
}
