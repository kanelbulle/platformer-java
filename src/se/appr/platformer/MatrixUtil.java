package se.appr.platformer;

import java.awt.Rectangle;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class MatrixUtil {

	/**
	 * Translates the matrix (in place) by vector v.
	 * 
	 * @param matrix
	 * @param v
	 */
	public static void translate(Matrix4f matrix, Vector3f v) {
		translate(matrix, v.x, v.y, v.z);
	}

	public static void translate(Matrix4f matrix, float x, float y, float z) {
		// TODO do this the more efficient way
		Matrix4f trans = new Matrix4f();
		trans.setIdentity();
		trans.m03 = x;
		trans.m13 = y;
		trans.m23 = z;
		matrix.mul(trans);
	}

	/**
	 * Rotates matrix by angle around axis.
	 * 
	 * @param matrix
	 * @param angle
	 * @param axis
	 */
	public static void rotate(Matrix4f matrix, float angle, Vector3f axis) {
		float c = (float) Math.cos((double) angle);
		float s = (float) Math.sin((double) angle);

		axis.normalize();

		Vector3f temp = new Vector3f(axis);
		temp.x *= 1.0f - c;
		temp.y *= 1.0f - c;
		temp.z *= 1.0f - c;

		Matrix3f rot = new Matrix3f();
		rot.m00 = c + temp.x * axis.x;
		rot.m01 = 0 + temp.x * axis.y + s * axis.z;
		rot.m02 = 0 + temp.x * axis.z - s * axis.y;

		rot.m10 = 0 + temp.y * axis.x - s * axis.z;
		rot.m11 = c + temp.y * axis.y;
		rot.m12 = 0 + temp.y * axis.z + s * axis.x;

		rot.m20 = 0 + temp.z * axis.x + s * axis.y;
		rot.m21 = 0 + temp.z * axis.y - s * axis.x;
		rot.m22 = c + temp.z * axis.z;

		Matrix4f result = new Matrix4f();
		Vector4f vres = new Vector4f();
		Vector4f i = new Vector4f();
		Vector4f j = new Vector4f();
		Vector4f k = new Vector4f();

		matrix.getColumn(0, i);
		matrix.getColumn(1, j);
		matrix.getColumn(2, k);

		vres.scaleAdd(rot.m00, i, vres);
		vres.scaleAdd(rot.m01, j, vres);
		vres.scaleAdd(rot.m02, k, vres);

		result.setColumn(0, vres);

		vres.scale(0);
		vres.scaleAdd(rot.m10, i, vres);
		vres.scaleAdd(rot.m11, j, vres);
		vres.scaleAdd(rot.m12, k, vres);

		result.setColumn(1, vres);

		vres.scale(0);
		vres.scaleAdd(rot.m20, i, vres);
		vres.scaleAdd(rot.m21, j, vres);
		vres.scaleAdd(rot.m22, k, vres);

		result.setColumn(2, vres);

		matrix.getColumn(3, vres);
		result.setColumn(3, vres);

		matrix.set(result);
	}

	/**
	 * Returns the object coordinates of the given window coordinate point win.
	 * gluUnproject replacement
	 * 
	 * @param win
	 * @param modelview
	 * @param projection
	 * @param viewport
	 * @return
	 */
	public static Point3f unProject(Vector3f win, Matrix4f modelview, Matrix4f projection,
			Rectangle viewport) {
		Point3f obj = new Point3f();

		Matrix4f inv = new Matrix4f();
		inv.mul(projection, modelview);
		inv.invert();

		Vector4f tmp = new Vector4f(win);
		tmp.w = 1.0f;

		tmp.x = (tmp.x - viewport.x) / viewport.width;
		tmp.y = (tmp.y - viewport.y) / viewport.height;
		tmp.scale(2.0f);
		tmp.x -= 1.0f;
		tmp.y -= 1.0f;
		tmp.z -= 1.0f;
		tmp.w -= 1.0f;

		// couldn't find Matrix4f * Vector4f in vecmath so doing it manually
		obj.x = inv.m00 * tmp.x + inv.m01 * tmp.y + inv.m02 * tmp.z + inv.m03 * tmp.w;
		obj.y = inv.m10 * tmp.x + inv.m11 * tmp.y + inv.m12 * tmp.z + inv.m13 * tmp.w;
		obj.z = inv.m20 * tmp.x + inv.m21 * tmp.y + inv.m22 * tmp.z + inv.m23 * tmp.w;
		float w = inv.m30 * tmp.x + inv.m31 * tmp.y + inv.m32 * tmp.z + inv.m33 * tmp.w;

		obj.x /= w;
		obj.y /= w;
		obj.z /= w;

		return obj;
	}

	/**
	 * Writes the 4x4 matrix into arr. arr must be of length greater or equal to
	 * 16.
	 * 
	 * @param m
	 * @param arr
	 */
	public static void toArray4f(Matrix4f m, float[] arr) {
		arr[0] = m.m00;
		arr[1] = m.m10;
		arr[2] = m.m20;
		arr[3] = m.m30;
		arr[4] = m.m01;
		arr[5] = m.m11;
		arr[6] = m.m21;
		arr[7] = m.m31;
		arr[8] = m.m02;
		arr[9] = m.m12;
		arr[10] = m.m22;
		arr[11] = m.m32;
		arr[12] = m.m03;
		arr[13] = m.m13;
		arr[14] = m.m23;
		arr[15] = m.m33;
	}

	/**
	 * Writes the upper left 3x3 portion of the 4x4 matrix into arr. arr must be
	 * of length greater or equal to 9.
	 * 
	 * @param m
	 * @param arr
	 */
	public static void toArray3f(Matrix4f m, float[] arr) {
		arr[0] = m.m00;
		arr[1] = m.m10;
		arr[2] = m.m20;
		arr[3] = m.m01;
		arr[4] = m.m11;
		arr[5] = m.m21;
		arr[6] = m.m02;
		arr[7] = m.m12;
		arr[8] = m.m22;
	}

}
