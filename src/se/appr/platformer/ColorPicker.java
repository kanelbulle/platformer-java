package se.appr.platformer;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;

import se.appr.platformer.VBOUtil.VertexBufferObject;

import com.jogamp.opengl.util.texture.Texture;

import cz.advel.modern3d.util.MatrixUtil;

public class ColorPicker implements WorldObject {
	public Vector2f		mSize;
	public Vector2f		mPosition;
	VertexBufferObject	vbo;
	Texture				texture;
	Shader				shader;
	Vector2f			mTexCoord;

	public ColorPicker(GL2 gl, Vector2f position, Vector2f size) {
		texture = TextureLoader.loadTexture(gl, "color-palette-1.png");
		vbo = VBOUtil.createVBO(gl, ModelLoader.loadModel("plane.obj").get(0));
		shader = Shader.getShader(gl, "texture-shader");

		mSize = size;
		mPosition = position;
		mTexCoord = new Vector2f();
	}

	public Vector2f getTexCoord() {
		return new Vector2f(mTexCoord);
	}

	@Override
	public void update(GL2 gl, double timedx) {

	}

	@Override
	public void render(GL2 gl, Matrix4f projection, Matrix4f modelview) {
		shader.useShader(gl);

		shader.setProjectionMatrix(gl, projection);

		shader.setMinLight(gl, 1.0f);

		texture.enable(gl);
		texture.bind(gl);

		shader.setTextureID(gl, 0);

		Matrix4f view = new Matrix4f(modelview);
		MatrixUtil
				.translate(view, mPosition.x + mSize.x / 2.0f, mPosition.y + mSize.y / 2.0f, 0.0f);
		MatrixUtil.scale(view, mSize.x, mSize.y, 1.0f);

		shader.setModelviewMatrix(gl, view);

		vbo.useVBO(gl);

		gl.glDrawElements(GL.GL_TRIANGLES, vbo.indexCount, GL.GL_UNSIGNED_INT, 0);

		vbo.dontUseVBO(gl);

		shader.dontUseShader(gl);
	}

	public void mouseClicked(GL2 gl, MouseEvent e, Matrix4f projection, Matrix4f modelview,
			Rectangle viewport) {
		float x = e.getX();
		float y = e.getY();

		// TODO fix the bounds check here
		if (x < mPosition.x + mSize.x && y < mSize.y) {
			mTexCoord.x = 1.0f - ((x - (x % 8.0f) + 4.0f) / mSize.x);
			mTexCoord.y = 1.0f - ((y - (y % 8.0f) + 4.0f) / mSize.y);

			System.out.println(mTexCoord);

		}
	}

}
