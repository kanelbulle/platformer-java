package se.appr.platformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class ModelLoader {

	public static class ModelData {
		FloatBuffer	vertexData;
		int			vertexCount;
		IntBuffer	indices;
		int			indicesCount;
	}

	private static class VertexData {
		Vector3f	vertex;
		Vector3f	normal;
		Vector2f	texture;
	}

	private static ModelData createModel(ArrayList<VertexData> vertexData,
			ArrayList<Integer> indices) {
		ModelData md = new ModelData();

		FloatBuffer vdata = FloatBuffer.allocate(vertexData.size() * 8);
		IntBuffer idata = IntBuffer.allocate(indices.size());

		System.out.printf(
				"creating model with %d vertexdata and %d indices\n", vertexData.size(),
				indices.size());

		for (VertexData vd : vertexData) {
			vdata.put(vd.vertex.x);
			vdata.put(vd.vertex.y);
			vdata.put(vd.vertex.z);
			vdata.put(vd.normal.x);
			vdata.put(vd.normal.y);
			vdata.put(vd.normal.z);
			vdata.put(vd.texture.x);
			vdata.put(vd.texture.y);
			
//			System.out.printf("v: {%f, %f, %f}, vt: {%f, %f}, vn: {%f, %f, %f}\n",
//					vd.vertex.x, vd.vertex.y, vd.vertex.z,
//					vd.texture.x, vd.texture.y,
//					vd.normal.x, vd.normal.y, vd.normal.z);
		}

		for (Integer i : indices) {
			idata.put(i);
		}

		vdata.rewind();
		idata.rewind();
		
		md.vertexData = vdata;
		md.indices = idata;
		
		md.vertexCount = vertexData.size();
		md.indicesCount = indices.size();

		return md;
	}

	public static List<ModelData> loadModel(String modelName) {
		try {
			InputStream is = ModelLoader.class.getResourceAsStream("/models/" + modelName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			ArrayList<ModelData> modelData = new ArrayList<ModelLoader.ModelData>();
			ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
			ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
			ArrayList<Vector2f> textures = new ArrayList<Vector2f>();
			HashMap<String, Integer> faceMap = new HashMap<String, Integer>();
			ArrayList<VertexData> vertexData = new ArrayList<ModelLoader.VertexData>();
			ArrayList<Integer> indices = new ArrayList<Integer>();

			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("o ")) {
					if (!indices.isEmpty()) {
						modelData.add(createModel(vertexData, indices));
					}

					faceMap.clear();
					vertexData.clear();
					indices.clear();
				} else if (line.startsWith("v ")) {
					Vector3f v = new Vector3f();
					String[] s = line.split(" ");
					v.x = Float.parseFloat(s[1]);
					v.y = Float.parseFloat(s[2]);
					v.z = Float.parseFloat(s[3]);
					vertices.add(v);
				} else if (line.startsWith("vt ")) {
					Vector2f v = new Vector2f();
					String[] s = line.split(" ");
					v.x = Float.parseFloat(s[1]);
					v.y = Float.parseFloat(s[2]);
					textures.add(v);
				} else if (line.startsWith("vn ")) {
					Vector3f v = new Vector3f();
					String[] s = line.split(" ");
					v.x = Float.parseFloat(s[1]);
					v.y = Float.parseFloat(s[2]);
					v.z = Float.parseFloat(s[3]);
					normals.add(v);
				} else if (line.startsWith("f ")) {
					String[] f = line.split(" ");
					for (int i = 1; i < 4; i++) {
						String[] ff = f[i].split("/");
						Integer lookupIdx = faceMap.get(f[i]);
						if (lookupIdx != null) {
							indices.add(lookupIdx);
						} else {
							int v = Integer.parseInt(ff[0]);
							int vt = Integer.parseInt(ff[1]);
							int vn = Integer.parseInt(ff[2]);

							VertexData vd = new VertexData();
							vd.vertex = vertices.get(v - 1);
							vd.normal = normals.get(vn - 1);
							vd.texture = textures.get(vt - 1);

							vertexData.add(vd);
							int idx = vertexData.size() - 1;
							indices.add(idx);
							faceMap.put(f[i], idx);
						}
					}
				}
			}

			if (!indices.isEmpty()) {
				modelData.add(createModel(vertexData, indices));
			}

			return modelData;
		} catch (IOException e) {

		}

		return null;
	}
}
