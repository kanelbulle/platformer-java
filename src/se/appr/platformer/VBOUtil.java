package se.appr.platformer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

import se.appr.platformer.ModelLoader.ModelData;

public class VBOUtil {

	public abstract static class VertexBufferObject {
		public int	vertexBuffer;
		public int	indexBuffer;
		public int	vertexCount;
		public int	indexCount;
		
		public abstract void useVBO(GL2 gl);
		public abstract void dontUseVBO(GL2 gl);
		
		@Override
		public String toString() {
			return String.format("{VB: %d, IB: %d, vcount: %d, icount: %d}\n", vertexBuffer, indexBuffer, vertexCount, indexCount);
		}
	}

	public static VertexBufferObject createVBO(GL2 gl, ModelData md) {
		VertexBufferObject vbo = new VertexBufferObject() {
			
			@Override
			public void useVBO(GL2 gl) {
				// Bind the buffers
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
				gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);

				// Enable the vertex attributes for this VBO
				gl.glEnableVertexAttribArray(Shader.ATTRIB_VERTEX);
				gl.glEnableVertexAttribArray(Shader.ATTRIB_NORMAL);
				gl.glEnableVertexAttribArray(Shader.ATTRIB_TEXTURE);

				// Specify the contents of this VBO
				gl.glVertexAttribPointer(Shader.ATTRIB_VERTEX, 3, GL.GL_FLOAT, false, 8 * 4, (long) 0);
				gl.glVertexAttribPointer(Shader.ATTRIB_NORMAL, 3, GL.GL_FLOAT, false, 8 * 4, (long) 3 * 4);
				gl.glVertexAttribPointer(Shader.ATTRIB_TEXTURE, 2, GL.GL_FLOAT, false, 8 * 4, (long) 6 * 4);
			}
			
			@Override
			public void dontUseVBO(GL2 gl) {
				// Unbind the buffers
				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
				gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
			}
		};

		vbo.indexCount = md.indicesCount;
		vbo.vertexCount = md.vertexCount;

		int[] n = new int[2];

		// Generate the vertex and indices buffers
		gl.glGenBuffers(2, n, 0);
		vbo.vertexBuffer = n[0];
		vbo.indexBuffer = n[1];
		
		System.out.println("Created vbo: " + vbo);

		// Bind and sett attribs
		vbo.useVBO(gl);

		// Upload the vertex and indices data to the buffers
		gl.glBufferData(
				GL.GL_ARRAY_BUFFER, md.vertexCount * 8 * Buffers.SIZEOF_FLOAT, md.vertexData,
				GL.GL_STATIC_DRAW);
		gl.glBufferData(
				GL.GL_ELEMENT_ARRAY_BUFFER, md.indicesCount * Buffers.SIZEOF_INT, md.indices,
				GL.GL_STATIC_DRAW);

		// Unbind and we are done
		vbo.dontUseVBO(gl);

		return vbo;
	}
}
