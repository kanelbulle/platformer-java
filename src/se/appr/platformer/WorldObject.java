package se.appr.platformer;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix4f;

public interface WorldObject {
	public void update(final GL2 gl, double timedx);
	public void render(final GL2 gl, Matrix4f projection, Matrix4f modelview);
}
