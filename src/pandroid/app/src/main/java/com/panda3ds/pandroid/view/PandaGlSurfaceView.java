package com.panda3ds.pandroid.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Debug;
import android.os.SystemClock;
import android.opengl.GLES20;

import androidx.annotation.NonNull;
import com.panda3ds.pandroid.math.Vector2;
import com.panda3ds.pandroid.view.controller.TouchEvent;
import com.panda3ds.pandroid.view.controller.nodes.TouchScreenNodeImpl;
import com.panda3ds.pandroid.view.renderer.ConsoleRenderer;

public class PandaGlSurfaceView extends GLSurfaceView implements TouchScreenNodeImpl {
	final PandaGlRenderer renderer;
	private int width;
	private int height;
	private OnFpsUpdateListener fpsUpdateListener;
	private int frameCount = 0;
        private long lastTimeMillis = SystemClock.elapsedRealtime();
	

	public PandaGlSurfaceView(Context context, String romPath) {
		super(context);
		setEGLContextClientVersion(3);
		if (Debug.isDebuggerConnected()) {
			setDebugFlags(DEBUG_LOG_GL_CALLS);
		}
		renderer = new PandaGlRenderer(romPath);
		setRenderer(renderer);
	}

	public ConsoleRenderer getRenderer() { return renderer; }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = getMeasuredWidth();
		height = getMeasuredHeight();
	}

	@NonNull
	@Override
	public Vector2 getSize() {
		return new Vector2(width, height);
	}

	@Override
	public void onTouch(TouchEvent event) {
		onTouchScreenPress(renderer, event);
	}
	public void setOnFpsUpdateListener(OnFpsUpdateListener listener) {
        this.fpsUpdateListener = listener;
	}

	@Override
        public void onDrawFrame(GLES20 gl) {
        super.onDrawFrame(gl);

        // Increment frameCount for FPS calculation
        frameCount++;

        // Calculate FPS every second
        long currentTimeMillis = SystemClock.elapsedRealtime();
        long elapsedTimeMillis = currentTimeMillis - lastTimeMillis;
        if (elapsedTimeMillis >= 1000) {  // Check if one second has passed
            double fps = (frameCount * 1000.0) / elapsedTimeMillis;

            if (fpsUpdateListener != null) {
                fpsUpdateListener.onFpsUpdate(fps);  // Notify the listener with the calculated FPS
            }

            // Reset counters for the next second
            frameCount = 0;
            lastTimeMillis = currentTimeMillis;
        }
    }
}
