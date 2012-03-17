package se.appr.platformer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;

import com.github.kanelbulle.oglmathj.Matrix3f;
import com.github.kanelbulle.oglmathj.Matrix4f;
import com.github.kanelbulle.oglmathj.Vector2f;
import com.github.kanelbulle.oglmathj.Vector3f;
import com.jogamp.common.nio.Buffers;

public class Shader {
	public static final int					ATTRIB_VERTEX				= 0;
	public static final int					ATTRIB_NORMAL				= 1;
	public static final int					ATTRIB_TEXTURE				= 2;

	public static final int					UNIFORM_PROJECTION_MATRIX	= 0;
	public static final int					UNIFORM_MODELVIEW_MATRIX	= 1;
	public static final int					UNIFORM_NORMAL_MATRIX		= 2;
	public static final int					UNIFORM_LIGHT_1				= 3;
	public static final int					UNIFORM_TEXCOORD			= 4;
	public static final int					UNIFORM_MIN_LIGHT			= 5;
	public static final int					UNIFORM_TEXTURE_ID			= 6;
	public static final int					UNIFORM_BUMP_MAP			= 7;

	private static HashMap<String, Shader>	mShaderMap					= new HashMap<String, Shader>();
	private float[]							floatBuffer					= new float[16];
	private int[]							mUniforms;
	private int								mProgram;

	public static Shader getShader(GL2 gl, String name) {
		Shader s = mShaderMap.get(name);
		if (s != null) {
			return s;
		}

		s = new Shader(gl, name);
		mShaderMap.put(name, s);

		return s;
	}

	public String convertStreamToString(java.io.InputStream is) {
		try {
			return new java.util.Scanner(is).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}

	private Shader(GL2 gl, String name) {
		mUniforms = new int[UNIFORM_BUMP_MAP + 1];

		InputStream vis = Shader.class.getResourceAsStream("/shaders/" + name + ".vsh");
		InputStream fis = Shader.class.getResourceAsStream("/shaders/" + name + ".fsh");

		String vsrc = convertStreamToString(vis);
		String fsrc = convertStreamToString(fis);

		int v = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
		gl.glShaderSource(v, 1, new String[] { vsrc }, null);
		gl.glCompileShader(v);
		printShaderError(gl, v, GL2ES2.GL_COMPILE_STATUS, "Vertex shader");

		int f = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
		gl.glShaderSource(f, 1, new String[] { fsrc }, null);
		gl.glCompileShader(f);
		printShaderError(gl, f, GL2ES2.GL_COMPILE_STATUS, "Fragment shader");

		mProgram = gl.glCreateProgram();
		gl.glAttachShader(mProgram, v);
		gl.glAttachShader(mProgram, f);

		// bind attributes
		gl.glBindAttribLocation(mProgram, ATTRIB_VERTEX, "vertex");
		gl.glBindAttribLocation(mProgram, ATTRIB_NORMAL, "normal");
		gl.glBindAttribLocation(mProgram, ATTRIB_TEXTURE, "texture");

		gl.glLinkProgram(mProgram);
		System.out.println("" + gl.glGetError());
		printProgramError(gl, mProgram, GL2ES2.GL_LINK_STATUS, "Shader program");

		// find uniform locations
		mUniforms[UNIFORM_PROJECTION_MATRIX] = gl.glGetUniformLocation(
				mProgram, "projection_matrix");
		mUniforms[UNIFORM_MODELVIEW_MATRIX] = gl.glGetUniformLocation(mProgram, "modelview_matrix");
		mUniforms[UNIFORM_NORMAL_MATRIX] = gl.glGetUniformLocation(mProgram, "normal_matrix");
		mUniforms[UNIFORM_LIGHT_1] = gl.glGetUniformLocation(mProgram, "light1");
		mUniforms[UNIFORM_MIN_LIGHT] = gl.glGetUniformLocation(mProgram, "u_min_light");
		mUniforms[UNIFORM_TEXCOORD] = gl.glGetUniformLocation(mProgram, "u_texcoord");
		mUniforms[UNIFORM_TEXTURE_ID] = gl.glGetUniformLocation(mProgram, "textureID");
		mUniforms[UNIFORM_BUMP_MAP] = gl.glGetUniformLocation(mProgram, "u_bump_map");

		gl.glValidateProgram(mProgram);
		printProgramError(gl, mProgram, GL2ES2.GL_VALIDATE_STATUS, "Shader program");
	}

	public void useShader(GL2 gl) {
		gl.glUseProgram(mProgram);
	}

	public void dontUseShader(GL2 gl) {
		gl.glUseProgram(0);
	}

	public void setProjectionMatrix(GL2 gl, Matrix4f matrix) {
		matrix.toOpenGLArray(floatBuffer);
		gl.glUniformMatrix4fv(mUniforms[UNIFORM_PROJECTION_MATRIX], 1, false, floatBuffer, 0);
	}

	public void setModelviewMatrix(GL2 gl, com.github.kanelbulle.oglmathj.Matrix4f matrix) {
		matrix.toOpenGLArray(floatBuffer);
		gl.glUniformMatrix4fv(mUniforms[UNIFORM_MODELVIEW_MATRIX], 1, false, floatBuffer, 0);
	}

	public void setNormalMatrix(GL2 gl, Matrix3f matrix) {
		matrix.toOpenGLArray(floatBuffer);
		gl.glUniformMatrix3fv(mUniforms[UNIFORM_NORMAL_MATRIX], 1, false, floatBuffer, 0);
	}

	public void setLight1(GL2 gl, Vector3f light) {
		floatBuffer[0] = light.x();
		floatBuffer[1] = light.y();
		floatBuffer[2] = light.z();
		gl.glUniform3fv(mUniforms[UNIFORM_LIGHT_1], 1, floatBuffer, 0);
	}

	public void setMinLight(GL2 gl, float minLight) {
		gl.glUniform1f(mUniforms[UNIFORM_MIN_LIGHT], minLight);
	}

	public void setTextureID(GL2 gl, int id) {
		gl.glUniform1i(mUniforms[UNIFORM_TEXTURE_ID], 0);
	}

	public void setTexCoord(GL2 gl, Vector2f coord) {
		gl.glUniform2f(mUniforms[UNIFORM_TEXCOORD], coord.x(), coord.y());
	}

	private static void printShaderError(final GL2GL3 gl, int shader, int type, String name) {
		int statusLinker[] = new int[1];
		gl.glGetShaderiv(shader, type, IntBuffer.wrap(statusLinker));
		if (statusLinker[0] == GL3.GL_FALSE) {
			int infoLogLenght[] = new int[1];
			gl.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH, IntBuffer.wrap(infoLogLenght));
			ByteBuffer infoLog = Buffers.newDirectByteBuffer(infoLogLenght[0]);
			gl.glGetShaderInfoLog(shader, infoLogLenght[0], null, infoLog);
			byte[] infoBytes = new byte[infoLogLenght[0]];
			infoLog.get(infoBytes);
			String out = new String(infoBytes);
			System.out.println(name + " error:\n" + out);
		}
		gl.glGetError();
	}

	private static void printProgramError(final GL2GL3 gl, int shader, int type, String name) {
		int statusLinker[] = new int[1];
		gl.glGetProgramiv(shader, type, IntBuffer.wrap(statusLinker));
		if (statusLinker[0] == GL3.GL_FALSE) {
			int infoLogLenght[] = new int[1];
			gl.glGetProgramiv(shader, GL3.GL_INFO_LOG_LENGTH, IntBuffer.wrap(infoLogLenght));
			ByteBuffer infoLog = Buffers.newDirectByteBuffer(infoLogLenght[0]);
			gl.glGetShaderInfoLog(shader, infoLogLenght[0], null, infoLog);
			byte[] infoBytes = new byte[infoLogLenght[0]];
			infoLog.get(infoBytes);
			String out = new String(infoBytes);
			System.out.println(name + " error:\n" + out);
		}
		gl.glGetError();
	}
}
