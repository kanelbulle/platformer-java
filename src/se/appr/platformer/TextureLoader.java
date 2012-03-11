package se.appr.platformer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TextureLoader {
	private static final HashMap<String, Texture>	textureMap	= new HashMap<String, Texture>();

	public static Texture loadTexture(GL2 gl, String textureName) {
		Texture lookupTex = textureMap.get(textureName);
		if (lookupTex != null) {
			return lookupTex;
		}

		try {
			InputStream is = TextureLoader.class.getResourceAsStream("/textures/" + textureName);
			Texture texture = TextureIO.newTexture(
					is, false, textureName.substring(textureName.lastIndexOf('.') + 1));

			texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
			texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
			
			textureMap.put(textureName, texture);

			System.out.println("loaded texture");
			
			return texture;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
