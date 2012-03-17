package se.appr.platformer;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;

import se.appr.platformer.VBOUtil.VertexBufferObject;
import se.appr.platformer.editor.EditorScene;

import com.github.kanelbulle.oglmathj.Matrix3f;
import com.github.kanelbulle.oglmathj.Matrix4f;
import com.github.kanelbulle.oglmathj.OGLMath;
import com.github.kanelbulle.oglmathj.Vector2f;
import com.github.kanelbulle.oglmathj.Vector3f;
import com.jogamp.opengl.util.texture.Texture;

public class PixelObject implements WorldObject {
	VertexBufferObject	vbo;
	Texture				texture;
	Shader				shader;
	ArrayList<Pixel>	pixels	= new ArrayList<Pixel>();

	public class Pixel {
		Vector3f	position;
		Vector2f	texture;

		public Pixel(Pixel p) {
			this.position = p.position;
			this.texture = p.texture;
		}

		public Pixel(int p1, int p2, int p3, float t1, float t2) {
			position = new Vector3f(p1, p2, p3);
			texture = new Vector2f(t1, t2);
		}
		
		public Pixel(Vector3f position) {
			this.position = position;
			this.texture = EditorScene.mColorPicker.getTexCoord();
		}

		public Pixel(Vector3f position, float t1, float t2) {
			this.position = position;
			texture = new Vector2f(t1, t2);
		}

		@Override
		public String toString() {
			return "{" + position.x() + ", " + position.y() + ", " + position.z() + "}";
		}
	}

	private void init(GL2 gl) {
		vbo = VBOUtil.createVBO(gl, ModelLoader.loadModel("pixel.obj").get(0));
		texture = TextureLoader.loadTexture(gl, "color-palette-1.png");
		shader = Shader.getShader(gl, "fixed-texture-shader");
	}

	public PixelObject(GL2 gl) {
		init(gl);

		pixels.add(new Pixel(0, 0, 0, 0.5f, 0.5f));
		pixels.add(new Pixel(2, 0, 0, 0.5f, 0.5f));
		pixels.add(new Pixel(2, 2, 0, 0.5f, 0.5f));
	}

	public PixelObject(GL2 gl, PixelObject po) {
		init(gl);

		for (Pixel p : po.pixels) {
			System.out.println("loopit");
			Pixel pc = new Pixel(p);
			addPixel(pc);
		}
	}

	public PixelObject(GL2 gl, Scanner scanner) {
		init(gl);

		int n = scanner.nextInt();
		for (int i = 0; i < n; i++) {
			int p1 = scanner.nextInt();
			int p2 = scanner.nextInt();
			int p3 = scanner.nextInt();
			float t1 = scanner.nextFloat();
			float t2 = scanner.nextFloat();

			addPixel(new Pixel(p1, p2, p3, t1, t2));
		}
	}

	public void writeToStream(BufferedWriter bw) throws IOException {
		bw.write("" + pixels.size() + "\n");
		for (Pixel p : pixels) {
			bw.write("" + (int) p.position.x());
			bw.write(" " + (int) p.position.y());
			bw.write(" " + (int) p.position.z());
			bw.write(" " + p.texture.x());
			bw.write(" " + p.texture.y() + "\n");
		}
	}

	public void addPixel(Pixel pixel) {
		removePixel(pixel);
		pixels.add(pixel);
	}

	public void removePixel(Pixel pixel) {
		removePixel(pixel.position);
	}

	public void removePixel(int x, int y, int z) {
		removePixel(new Vector3f(x, y, z));
	}
	
	public void removePixel(Vector3f pos) {
		for (int i = 0; i < pixels.size(); i++) {
			Pixel p = pixels.get(i);
			if (p.position.equals(pos, 0.1f)) {
				pixels.remove(i);
				break;
			}
		}
	}

	@Override
	public void update(GL2 gl, double timedx) {

	}

	@Override
	public void render(GL2 gl, Matrix4f projection, Matrix4f modelview) {
		shader.useShader(gl);
		
		shader.setProjectionMatrix(gl, projection);

		texture.enable(gl);
		texture.bind(gl);

		shader.setTextureID(gl, 0);

		vbo.useVBO(gl);

		for (Pixel pixel : pixels) {
			Matrix4f tmpModelview = modelview.translate(pixel.position.x(), pixel.position.y(), pixel.position.z());

			Matrix3f normalMatrix = tmpModelview.invert().transpose().subMatrix3(Matrix4f.UPPER_LEFT);
			
			shader.setModelviewMatrix(gl, tmpModelview);
			shader.setNormalMatrix(gl, normalMatrix);			
			shader.setTexCoord(gl, pixel.texture);

			gl.glDrawElements(GL.GL_TRIANGLES, vbo.indexCount, GL.GL_UNSIGNED_INT, 0);
		}

		vbo.dontUseVBO(gl);

		shader.dontUseShader(gl);
	}

	public void mouseClicked(GL2 gl, MouseEvent e, Matrix4f projection, Matrix4f modelview,
			Rectangle viewport) {
		if (pixels.isEmpty()) {
			addPixel(new Pixel(0, 0, 0, 0.5f, 0.5f));
			return;
		}

		float wx = e.getX();
		float wy = viewport.height - e.getY();
		FloatBuffer fb = FloatBuffer.allocate(1);
		gl.glReadPixels((int) wx, (int) wy, 1, 1, GL2ES2.GL_DEPTH_COMPONENT, GL.GL_FLOAT, fb);
		Vector3f win = new Vector3f(wx, wy, fb.get(0));

		Vector3f obj = OGLMath.unProject(win, modelview, projection, viewport);

		for (int i = 0; i < pixels.size(); i++) {
			Pixel pixel = pixels.get(i);
			
			float dist = obj.distance(pixel.position);
			if (dist < 0.75) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					Vector3f dir = obj.sub(pixel.position);

					// find which side dir points towards
					int idx = 0;
					float max = Math.abs(dir.x());
					if (Math.abs(dir.y()) > max) {
						idx = 1;
						max = Math.abs(dir.y());
					}
					if (Math.abs(dir.z()) > max)
						idx = 2;

					float newx = pixel.position.x();
					float newy = pixel.position.y();
					float newz = pixel.position.z();
					switch (idx) {
					case 0:
						newx += dir.x() > 0 ? 1 : -1;
						break;
					case 1:
						newy += dir.y() > 0 ? 1 : -1;
						break;
					case 2:
						newz += dir.z() > 0 ? 1 : -1;
						break;
					}

					addPixel(new Pixel(new Vector3f(newx, newy, newz)));
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					removePixel(pixel);
				}

				break;
			}
		}
	}
}
