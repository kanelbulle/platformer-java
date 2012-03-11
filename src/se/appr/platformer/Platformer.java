package se.appr.platformer;

import java.awt.Frame;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import se.appr.platformer.editor.EditorScene;

import com.jogamp.opengl.util.FPSAnimator;

public class Platformer {

	public static void main(String[] args) {
		GLProfile.initSingleton();

		GLProfile glp = GLProfile.get(GLProfile.GL2);
		GLCapabilities caps = new GLCapabilities(glp);

		caps.setRedBits(8);
		caps.setGreenBits(8);
		caps.setBlueBits(8);
		caps.setDepthBits(32);

		GLCanvas canvas = new GLCanvas(caps);

		Frame frame = new Frame("platformer");
		frame.setSize(1280, 720);
		frame.setLocationRelativeTo(null);
		frame.add(canvas);
		frame.setVisible(true);

		EditorScene editorScene = new EditorScene();

		canvas.addGLEventListener(editorScene);
		
		frame.addWindowListener(editorScene);
		frame.addKeyListener(editorScene);
		canvas.addKeyListener(editorScene);
		canvas.addMouseWheelListener(editorScene);
		canvas.addMouseMotionListener(editorScene);
		canvas.addMouseListener(editorScene);

		FPSAnimator animator = new FPSAnimator(canvas, 60);
		animator.start();
	}

}
