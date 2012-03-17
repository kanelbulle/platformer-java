package se.appr.platformer;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.media.opengl.GL2;

import com.github.kanelbulle.oglmathj.Vector2f;
import com.github.kanelbulle.oglmathj.Vector3f;

public class ObjectSequence implements WorldObject, ItemListener {
	private final class AnimationButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			mAnimating = !mAnimating;
			if (mAnimating) {
				animationButton.setLabel("Stop animation");
			} else {
				animationButton.setLabel("Start animation");
			}
		}
	}

	private final class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			mAnimating = false;
			mAddFrameIndex = mCurrentIndex + 1;
		}
	}

	private final class RemoveButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			mAnimating = false;
			mRemoveFrameIndex = mCurrentIndex;
		}
	}

	private final class OpenMenuItemListener implements ActionListener {
		private File	file;

		@Override
		public void actionPerformed(ActionEvent e) {
			FileDialog fd = new FileDialog(mMainFrame, "Open animation", FileDialog.LOAD);
			fd.setVisible(true);

			String path = fd.getDirectory() + fd.getFile();
			file = new File(path);
		}

		public void openAnimation(GL2 gl) {
			if (file != null) {
				if (file.exists() && !file.isDirectory()) {
					mCurrentIndex = 0;
					mFrameList.removeAll();
					mObjects.clear();

					try {
						FileInputStream fis = new FileInputStream(file);
						BufferedInputStream bis = new BufferedInputStream(fis);

						Scanner sc = new Scanner(bis);
						int n = sc.nextInt();
						for (int i = 0; i < n; i++) {
							PixelObject po = new PixelObject(gl, sc);
							addFrame(gl, po);
						}

						bis.close();
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				file = null;
			}
		}

	}

	private final class SaveMenuItemListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			FileDialog fd = new FileDialog(mMainFrame, "Save animation", FileDialog.SAVE);
			fd.setVisible(true);

			String path = fd.getDirectory() + fd.getFile();
			File file = new File(path);

			file.delete();
			try {
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write("" + mObjects.size() + "\n");
				for (PixelObject po : mObjects) {
					System.out.println("writing");
					po.writeToStream(bw);
				}

				bw.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}

	private static final float	ANIMATION_RATE			= 1.0f / 15.0f;

	float						mAnimationTime;
	ArrayList<PixelObject>		mObjects				= new ArrayList<PixelObject>();
	int							mCurrentIndex;
	public Vector2f				mRotation				= new Vector2f();
	public Vector3f				mPosition				= new Vector3f();
	List						mFrameList;
	int							mAddFrameIndex			= -1;
	int							mRemoveFrameIndex		= -1;
	boolean						mAnimating				= false;

	Frame						mMainFrame;
	Button						animationButton			= new Button("Start animation");
	Button						addButton				= new Button("Add frame");
	Button						removeButton			= new Button("Remove frame");

	ActionListener				animationButtonListener	= new AnimationButtonListener();
	ActionListener				addButtonListener		= new AddButtonListener();
	ActionListener				removeButtonListener	= new RemoveButtonListener();
	SaveMenuItemListener		saveMenuItemListener	= new SaveMenuItemListener();
	OpenMenuItemListener		openMenuItemListener	= new OpenMenuItemListener();

	public ObjectSequence(GL2 gl) {
		mMainFrame = new Frame();
		Panel mainPanel = new Panel(new BorderLayout());

		animationButton.addActionListener(animationButtonListener);
		addButton.addActionListener(addButtonListener);
		removeButton.addActionListener(removeButtonListener);
		removeButton.setEnabled(false);

		Panel southPanel = new Panel(new GridLayout(2, 1));
		southPanel.add(addButton);
		southPanel.add(removeButton);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		mainPanel.add(animationButton, BorderLayout.NORTH);

		mFrameList = new List();
		mFrameList.addItemListener(this);
		mainPanel.add(mFrameList, BorderLayout.CENTER);

		mMainFrame.add(mainPanel);
		mMainFrame.setSize(400, 400);
		mMainFrame.setVisible(true);

		MenuItem saveItem = new MenuItem("Save...");
		MenuItem openItem = new MenuItem("Open...");

		saveItem.addActionListener(saveMenuItemListener);
		openItem.addActionListener(openMenuItemListener);

		Menu m = new Menu("Animation");
		m.add(saveItem);
		m.add(openItem);
		MenuBar mb = new MenuBar();
		mb.add(m);
		mMainFrame.setMenuBar(mb);

		addFrame(gl);
	}

	public void addFrame(GL2 gl, PixelObject po) {
		mObjects.add(po);
		mFrameList.add("frame");

		for (int i = 0; i < mFrameList.getItemCount(); i++) {
			mFrameList.replaceItem("frame " + i, i);
		}
	}

	public void addFrame(GL2 gl) {
		addFrame(gl, mCurrentIndex);
	}

	public void addFrame(GL2 gl, int index) {
		PixelObject po;
		System.out.println("adding at index " + index);
		if (index > 0 && index <= mObjects.size()) {
			po = new PixelObject(gl, mObjects.get(index - 1));
			mObjects.add(index, po);
			mFrameList.add("frame", index);
		} else {
			po = new PixelObject(gl);
			mObjects.add(po);
			mFrameList.add("frame", 0);
		}

		for (int i = 0; i < mFrameList.getItemCount(); i++) {
			mFrameList.replaceItem("frame " + i, i);
		}
	}

	public void removeFrame() {
		removeFrame(mCurrentIndex);
	}

	public void removeFrame(int index) {
		if (index >= 0 && index < mObjects.size()) {
			mObjects.remove(index);
			mFrameList.remove(index);

			for (int i = 0; i < mFrameList.getItemCount(); i++) {
				mFrameList.replaceItem("frame " + i, i);
			}

			mFrameList.select(0);
			mCurrentIndex = 0;
		}
	}

	private com.github.kanelbulle.oglmathj.Matrix4f getModelview(com.github.kanelbulle.oglmathj.Matrix4f modelview) {
		com.github.kanelbulle.oglmathj.Matrix4f view = modelview.rotate(mRotation.x(), new com.github.kanelbulle.oglmathj.Vector3f(1.0f, 0.0f, 0.0f));
		view = view.rotate(mRotation.y(), new com.github.kanelbulle.oglmathj.Vector3f(0.0f, 1.0f, 0.0f));
		view = view.translate(mPosition.x(), mPosition.y(), mPosition.z());
		return view;
	}

	@Override
	public void update(GL2 gl, double timedx) {
		if (mAnimating) {
			mAnimationTime += timedx;
			if (mAnimationTime > ANIMATION_RATE) {
				mCurrentIndex = (mCurrentIndex + 1) % mObjects.size();
				mAnimationTime -= ANIMATION_RATE;
			}
		}

		if (mAddFrameIndex != -1) {
			addFrame(gl, mAddFrameIndex);
			mAddFrameIndex = -1;
		} else if (mRemoveFrameIndex != -1) {
			removeFrame(mRemoveFrameIndex);
			mRemoveFrameIndex = -1;
		}

		openMenuItemListener.openAnimation(gl);
	}

	@Override
	public void render(GL2 gl, com.github.kanelbulle.oglmathj.Matrix4f projection, com.github.kanelbulle.oglmathj.Matrix4f modelview) {
		com.github.kanelbulle.oglmathj.Matrix4f view = getModelview(modelview);

		if (mCurrentIndex >= 0 && mCurrentIndex < mObjects.size()) {
			mObjects.get(mCurrentIndex).render(gl, projection, view);
		}
	}

	public void mouseClicked(GL2 gl, MouseEvent e, com.github.kanelbulle.oglmathj.Matrix4f projection, com.github.kanelbulle.oglmathj.Matrix4f modelview,
			Rectangle viewport) {
		mObjects.get(mCurrentIndex).mouseClicked(
				gl, e, projection, getModelview(modelview), viewport);
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.SELECTED) {
			mCurrentIndex = mFrameList.getSelectedIndex();
		}

		removeButton.setEnabled(arg0.getStateChange() == ItemEvent.SELECTED);
	}
}
