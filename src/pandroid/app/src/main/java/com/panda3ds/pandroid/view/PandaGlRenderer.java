package com.panda3ds.pandroid.view;

import static android.opengl.GLES32.*;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.panda3ds.pandroid.AlberDriver;
import com.panda3ds.pandroid.data.SMDH;
import com.panda3ds.pandroid.data.config.GlobalConfig;
import com.panda3ds.pandroid.data.game.GameMetadata;
import com.panda3ds.pandroid.utils.Constants;
import com.panda3ds.pandroid.utils.GameUtils;
import com.panda3ds.pandroid.utils.PerformanceMonitor;
import com.panda3ds.pandroid.view.renderer.ConsoleRenderer;
import com.panda3ds.pandroid.view.renderer.layout.ConsoleLayout;
import com.panda3ds.pandroid.view.renderer.layout.DefaultScreenLayout;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PandaGlRenderer implements GLSurfaceView.Renderer, ConsoleRenderer {
	private final String romPath;
	private ConsoleLayout displayLayout;
	private int screenWidth, screenHeight;
	private int screenTexture;
	public int screenFbo;
	private final Context context;

	private int shaderProgram;
        private int inputTextureLocation;
        private int texelSizeLocation;
        private int screenSizeLocation;

	private String VertexShaderCode;
        private String FragmentShaderCode;

	PandaGlRenderer(Context context, String romPath) {
		super();
		this.context = context;
		this.romPath = romPath;

		screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
		screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
		setLayout(new DefaultScreenLayout());
	}

	@Override
	protected void finalize() throws Throwable {
		if (screenTexture != 0) {
			glDeleteTextures(1, new int[] {screenTexture}, 0);
		}

		if (screenFbo != 0) {
			glDeleteFramebuffers(1, new int[] {screenFbo}, 0);
		}

		PerformanceMonitor.destroy();
		super.finalize();
	}

	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		Log.i(Constants.LOG_TAG, glGetString(GL_EXTENSIONS));
		Log.w(Constants.LOG_TAG, glGetString(GL_VERSION));

		int[] version = new int[2];
		glGetIntegerv(GL_MAJOR_VERSION, version, 0);
		glGetIntegerv(GL_MINOR_VERSION, version, 1);

		if (version[0] < 3 || (version[0] == 3 && version[1] < 1)) {
			Log.e(Constants.LOG_TAG, "OpenGL 3.1 or higher is required");
		}

		// Load and compile shader
                loadShaders();
                compileShaderProgram();

		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		int[] generateBuffer = new int[1];
		glGenTextures(1, generateBuffer, 0);
		screenTexture = generateBuffer[0];
		glBindTexture(GL_TEXTURE_2D, screenTexture);
		glTexStorage2D(GL_TEXTURE_2D, 1, GL_RGBA8, screenWidth, screenHeight);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glBindTexture(GL_TEXTURE_2D, 0);

		glGenFramebuffers(1, generateBuffer, 0);
		screenFbo = generateBuffer[0];
		glBindFramebuffer(GL_FRAMEBUFFER, screenFbo);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, screenTexture, 0);
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
			Log.e(Constants.LOG_TAG, "Framebuffer is not complete");
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		AlberDriver.Initialize();
		AlberDriver.setShaderJitEnabled(GlobalConfig.get(GlobalConfig.KEY_SHADER_JIT));

		// If loading the ROM failed, display an error message and early exit
		if (!AlberDriver.LoadRom(romPath)) {
			// Get a handler that can be used to post to the main thread
			Handler mainHandler = new Handler(context.getMainLooper());

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Failed to load ROM")
						.setMessage("Make sure it's a valid 3DS ROM and that storage permissions are configured properly.")
						.setPositiveButton("OK", null)
						.setCancelable(false)
						.show();
				}
			};
			mainHandler.post(runnable);

			GameMetadata game = GameUtils.getCurrentGame();
			GameUtils.removeGame(game);
			return;
		}

		// Load the SMDH
		byte[] smdhData = AlberDriver.GetSmdh();
		if (smdhData.length == 0) {
			Log.w(Constants.LOG_TAG, "Failed to load SMDH");
		} else {
			SMDH smdh = new SMDH(smdhData);
			Log.i(Constants.LOG_TAG, "Loaded rom SDMH");
			Log.i(Constants.LOG_TAG, String.format("You are playing '%s' published by '%s'", smdh.getTitle(), smdh.getPublisher()));
			GameMetadata game = GameUtils.getCurrentGame();
			GameUtils.removeGame(game);
			GameUtils.addGame(GameMetadata.applySMDH(game, smdh));
		}

		PerformanceMonitor.initialize(getBackendName());
	}

	public void onDrawFrame(GL10 unused) {
		if (AlberDriver.HasRomLoaded()) {
			AlberDriver.RunFrame(screenFbo);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
			glBindFramebuffer(GL_READ_FRAMEBUFFER, screenFbo);

			Rect topScreen = displayLayout.getTopDisplayBounds();
			Rect bottomScreen = displayLayout.getBottomDisplayBounds();

			glBlitFramebuffer(
				0, Constants.N3DS_FULL_HEIGHT, Constants.N3DS_WIDTH, Constants.N3DS_HALF_HEIGHT, topScreen.left, screenHeight - topScreen.top,
				topScreen.right, screenHeight - topScreen.bottom, GL_COLOR_BUFFER_BIT, GL_LINEAR
			);

			// Remove the black bars on the bottom screen
			glBlitFramebuffer(
				40, Constants.N3DS_HALF_HEIGHT, Constants.N3DS_WIDTH - 40, 0, bottomScreen.left, screenHeight - bottomScreen.top, bottomScreen.right,
				screenHeight - bottomScreen.bottom, GL_COLOR_BUFFER_BIT, GL_LINEAR
			);
			// Apply Shader effect
                        applyShader();
		}

		PerformanceMonitor.runFrame();
	}

// Load the shader code
private void loadShaders() {
// vertex shader code
    VertexShaderCode = "#version 330 core\n" +
            "layout(location = 0) in vec3 aPos;\n" +
            "void main() {\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}\n";

    // fragment shader code with improved brightness, contrast, and gamma correction
    FragmentShaderCode = "#version 330 core\n" +
            "uniform sampler2D inputTexture;\n" +
            "uniform float brightness;\n" +
            "uniform float contrast;\n" +
            "uniform float gamma;\n" +
            "in vec2 TexCoords;\n" +
            "out vec4 FragColor;\n" +
            "void main() {\n" +
            "    vec4 texColor = texture(inputTexture, TexCoords);\n" +
            "    texColor.rgb *= brightness;\n" +  // Apply brightness adjustment directly to RGB components
            "    texColor.rgb = pow(texColor.rgb, vec3(1.0 / gamma));\n" +  // Apply gamma correction
            "    texColor.rgb = (texColor.rgb - 0.5) * contrast + 0.5;\n" +  // Apply contrast adjustment
            "    FragColor = texColor;\n" +
            "}\n";
}
	
// Method to check the compilation status of a shader
private boolean checkShaderCompileStatus(int shader) {
    final int[] compileStatus = new int[1];
    glGetShaderiv(shader, GL_COMPILE_STATUS, compileStatus, 0);
    if (compileStatus[0] == 0) {
        Log.e(Constants.LOG_TAG, "Shader compilation failed: " + glGetShaderInfoLog(shader));
        glDeleteShader(shader);
        return false;
    }
    return true;
}

// Method to check the linking status of a shader program
private boolean checkProgramLinkStatus(int program) {
    final int[] linkStatus = new int[1];
    glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
    if (linkStatus[0] == 0) {
        Log.e(Constants.LOG_TAG, "Shader program linking failed: " + glGetProgramInfoLog(program));
        glDeleteProgram(program);
        return false;
    }
    return true;
}

// Compile and link the shader program
private void compileShaderProgram() {
    int vertexShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertexShader, VertexShaderCode);
    glCompileShader(vertexShader);
    checkShaderCompileStatus(vertexShader);

    int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragmentShader, FragmentShaderCode);
    glCompileShader(fragmentShader);
    checkShaderCompileStatus(fragmentShader);

    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);
    checkProgramLinkStatus(shaderProgram);

    // Get uniform locations
    inputTextureLocation = glGetUniformLocation(shaderProgram, "inputTexture");
    texelSizeLocation = glGetUniformLocation(shaderProgram, "texelSize");
    screenSizeLocation = glGetUniformLocation(shaderProgram, "screenSize");
}

// Render a fullscreen quad with the FXAA shader
private void applyShader() {
    glUseProgram(shaderProgram);

    // Set shader uniforms
    glUniform1i(inputTextureLocation, 0); // Assuming input texture is bound to texture unit 0
    glUniform2f(texelSizeLocation, 1.0f / screenWidth, 1.0f / screenHeight);
    glUniform2f(screenSizeLocation, screenWidth, screenHeight);

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
