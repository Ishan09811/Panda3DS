package com.panda3ds.pandroid.view;

import static android.opengl.GLES32.*;

import android.content.res.Resources;
import android.graphics.Rect;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;
import com.panda3ds.pandroid.AlberDriver;
import com.panda3ds.pandroid.utils.Constants;
import com.panda3ds.pandroid.utils.GameUtils;
import com.panda3ds.pandroid.view.renderer.ConsoleRenderer;
import com.panda3ds.pandroid.view.renderer.layout.ConsoleLayout;
import com.panda3ds.pandroid.view.renderer.layout.DefaultScreenLayout;
import com.panda3ds.pandroid.data.SMDH;
import com.panda3ds.pandroid.data.game.GameMetadata;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PandaGlRenderer implements GLSurfaceView.Renderer, ConsoleRenderer {
    private final String romPath;
    private ConsoleLayout displayLayout;
    private int screenWidth, screenHeight;
    private int screenTexture;
    private int screenFbo;

    PandaGlRenderer(String romPath) {
        super();
        this.romPath = romPath;

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        setLayout(new DefaultScreenLayout());
    }

    @Override
    public void finalize() throws Throwable {
        cleanupOpenGLResources();
        super.finalize();
    }

    private void cleanupOpenGLResources() {
        if (screenTexture != 0) {
            GLES30.glDeleteTextures(1, new int[]{screenTexture}, 0);
        }
        if (screenFbo != 0) {
            GLES30.glDeleteFramebuffers(1, new int[]{screenFbo}, 0);
        }
    }

    private void checkOpenGLError() {
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            Log.e(Constants.LOG_TAG, "OpenGL error: " + error);
        }
    }

    private void createFramebuffer() {
        int[] generateBuffer = new int[1];
        GLES30.glGenTextures(1, generateBuffer, 0);
        screenTexture = generateBuffer[0];
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, screenTexture);
        GLES30.glTexStorage2D(GLES30.GL_TEXTURE_2D, 1, GLES30.GL_RGBA8, screenWidth, screenHeight);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        GLES30.glGenFramebuffers(1, generateBuffer, 0);
        screenFbo = generateBuffer[0];
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, screenFbo);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, screenTexture, 0);
        if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(Constants.LOG_TAG, "Framebuffer is not complete");
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.i(Constants.LOG_TAG, GLES30.glGetString(GLES30.GL_EXTENSIONS));
        Log.w(Constants.LOG_TAG, GLES30.glGetString(GLES30.GL_VERSION));

        int[] version = new int[2];
        GLES30.glGetIntegerv(GLES30.GL_MAJOR_VERSION, version, 0);
        GLES30.glGetIntegerv(GLES30.GL_MINOR_VERSION, version, 1);

        if (version[0] < 3 || (version[0] == 3 && version[1] < 1)) {
            Log.e(Constants.LOG_TAG, "OpenGL 3.1 or higher is required");
        }

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        createFramebuffer();

        AlberDriver.Initialize();
        AlberDriver.LoadRom(romPath);

        // Load the SMDH
        byte[] smdhData = AlberDriver.GetSmdh();
        if (smdhData.length == 0) {
            Log.w(Constants.LOG_TAG, "Failed to load SMDH");
        } else {
            SMDH smdh = new SMDH(smdhData);
            Log.i(Constants.LOG_TAG, "Loaded rom SDMH");
            Log.i(Constants.LOG_TAG, String.format("Are you playing '%s' published by '%s'", smdh.getTitle(), smdh.getPublisher()));
            GameMetadata game = GameUtils.getCurrentGame();
            GameUtils.removeGame(game);
            GameUtils.addGame(GameMetadata.applySMDH(game, smdh));
        }
    }

    public void onDrawFrame(GL10 unused) {
        if (AlberDriver.HasRomLoaded()) {
            AlberDriver.RunFrame(screenFbo);
            GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);
            GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, screenFbo);

            Rect topScreen = displayLayout.getTopDisplayBounds();
            Rect bottomScreen = displayLayout.getBottomDisplayBounds();

            GLES30.glBlitFramebuffer(
                0, Constants.N3DS_FULL_HEIGHT, Constants.N3DS_WIDTH, Constants.N3DS_HALF_HEIGHT,
                topScreen.left, screenHeight - topScreen.top, topScreen.right, screenHeight - topScreen.bottom,
                GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_LINEAR
            );

            // Remove the black bars on the bottom screen
            GLES30.glBlitFramebuffer(
                40, Constants.N3DS_HALF_HEIGHT, Constants.N3DS_WIDTH - 40, 0,
                bottomScreen.left, screenHeight - bottomScreen.top, bottomScreen.right, screenHeight - bottomScreen.bottom,
                GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_LINEAR
            );

            checkOpenGLError();
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        screenWidth = width;
        screenHeight = height;

        displayLayout.update(screenWidth, screenHeight);
    }

    @Override
    public void setLayout(ConsoleLayout layout) {
        displayLayout = layout;
        displayLayout.setTopDisplaySourceSize(Constants.N3DS_WIDTH, Constants.N3DS_HALF_HEIGHT);
        displayLayout.setBottomDisplaySourceSize(Constants.N3DS_WIDTH - 40 - 40, Constants.N3DS_HALF_HEIGHT);
        displayLayout.update(screenWidth, screenHeight);
    }

    @Override
    public ConsoleLayout getLayout() {
        return displayLayout;
    }

    @Override
    public String getBackendName() {
        return "OpenGL";
    }
		    }
