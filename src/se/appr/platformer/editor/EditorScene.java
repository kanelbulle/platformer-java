package se.appr.platformer.editor;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.swing.JOptionPane;

import se.appr.platformer.ColorPicker;
import se.appr.platformer.ObjectSequence;
import se.appr.platformer.TextureLoader;

import com.github.kanelbulle.oglmathj.Matrix4f;
import com.github.kanelbulle.oglmathj.OGLMath;
import com.github.kanelbulle.oglmathj.Vector2f;

public class EditorScene extends WindowAdapter implements GLEventListener, KeyListener,
		MouseWheelListener, MouseListener, MouseMotionListener {
	float						mWheelRotation;
	Matrix4f					mProjection	= new Matrix4f();
	Matrix4f					mModelview	= new Matrix4f();
	Rectangle					mViewport	= new Rectangle();
	public static ColorPicker	mColorPicker;
	ObjectSequence				mSequence;
	Vector2f					mLastMousePosition;
	KeyEvent					mLastKeyEvent;
	long						mLastDisplayTime;
	boolean						mShiftIsDown;
	MouseEvent					savedMouseEvent;

	@Override
	public void init(GLAutoDrawable drawable) {
		drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
		final GL2 gl = drawable.getGL().getGL2();
		
		gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		
		TextureLoader.loadTexture(gl, "color-palette-1.png");

		mSequence = new ObjectSequence(gl);
		mColorPicker = new ColorPicker(gl, new Vector2f(0.0f, mViewport.height), new Vector2f(
				150.0f, 150.0f));

		mLastDisplayTime = System.nanoTime();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {

	}

	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();

		long currentTime = System.nanoTime();
		float tdx = (float) ((currentTime - mLastDisplayTime) / 10.0e8);
		mLastDisplayTime = currentTime;

		// update
		mSequence.update(gl, tdx);

		// render
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

		mProjection = OGLMath.perspective(100.0f, mViewport.width / (float) mViewport.height, 0.1f, 100.0f);
		
		mModelview = new Matrix4f();
		mModelview = mModelview.translate(0.0f, 0.0f, (float) mWheelRotation - 10.0f);

		mSequence.render(gl, mProjection, mModelview);

		if (savedMouseEvent != null) {
			mSequence.mouseClicked(gl, savedMouseEvent, mProjection, mModelview, mViewport);
		}

		// ortho
		mModelview = new Matrix4f();
		mProjection = OGLMath.ortho(0, mViewport.width, 0, mViewport.height, -1.0f, 1.0f);

		mColorPicker.render(gl, mProjection, mModelview);

		if (savedMouseEvent != null) {
			mColorPicker.mouseClicked(gl, savedMouseEvent, mProjection, mModelview, mViewport);
			savedMouseEvent = null;
		}
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		mViewport.x = x;
		mViewport.y = y;
		mViewport.width = width;
		mViewport.height = height;

		mColorPicker.mPosition = new Vector2f(x, height - mColorPicker.mSize.y());
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		if (JOptionPane.showConfirmDialog(
				null, "Do you really want to exit?", "Plopp", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			System.exit(0);
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		mShiftIsDown = arg0.isShiftDown();
		
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (JOptionPane.showConfirmDialog(
					null, "Do you really want to exit?", "Plopp", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				System.exit(0);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		mShiftIsDown = false;
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		mWheelRotation += (float) e.getWheelRotation() / 5.0f;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if (mLastMousePosition == null) {
			mLastMousePosition = new Vector2f();
			mLastMousePosition = new Vector2f(arg0.getX(), arg0.getY());
			return;
		}
		
		if (arg0.getButton() == MouseEvent.BUTTON2
				|| mShiftIsDown) {
			float ry = mSequence.mRotation.y() - (mLastMousePosition.x() - (float) arg0.getX()) / 75.0f;
			float rx = mSequence.mRotation.x() - (mLastMousePosition.y() - (float) arg0.getY()) / 75.0f;
			
			if (rx > Math.PI / 2)
				rx = (float) (Math.PI / 2);
			if (rx < -Math.PI / 2)
				rx = (float) (-Math.PI / 2);
			
			mSequence.mRotation = new Vector2f(rx, ry);
		} else if (arg0.getButton() == MouseEvent.BUTTON3) {
			float xdiff = -(mLastMousePosition.x() - (float) arg0.getX()) / 75.0f;
			float ydiff = (mLastMousePosition.y() - (float) arg0.getY()) / 75.0f;

			float rot = mSequence.mRotation.y();

			mSequence.mPosition = mSequence.mPosition.add((float) (xdiff * Math.cos(rot)), ydiff, (float) (xdiff * Math.sin(rot)));
		}

		mLastMousePosition = new Vector2f(arg0.getX(), arg0.getY());
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		savedMouseEvent = e;
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mLastMousePosition = null;
	}

}
;