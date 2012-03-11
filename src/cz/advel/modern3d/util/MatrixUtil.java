/*
 * Copyright (c) 2008-2010 ADVEL s.r.o.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of ADVEL s.r.o. nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package cz.advel.modern3d.util;

/* 
 * Marked methods are from LWJGL library with following license:
 *
 * Copyright (c) 2002-2004 LWJGL Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * * Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of 
 *   its contributors may be used to endorse or promote products derived 
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.Rectangle;
import javax.vecmath.*;

/**
 *
 * @author Martin Dvorak <jezek2@advel.cz>
 */
public class MatrixUtil {

	private MatrixUtil() {}
	
	// from LWJGL:
	public static void setPerspective(Matrix4f mat, float fovy, float aspect, float zNear, float zFar) {
		float sine, cotangent, deltaZ;
		float radians = fovy / 2 * (float)Math.PI / 180;

		deltaZ = zFar - zNear;
		sine = (float)Math.sin(radians);

		if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
			return;
		}

		cotangent = (float)Math.cos(radians) / sine;

		mat.setIdentity();
		mat.m00 = cotangent / aspect;
		mat.m11 = cotangent;
		mat.m22 = - (zFar + zNear) / deltaZ;
		mat.m32 = -1;
		mat.m23 = -2 * zNear * zFar / deltaZ;
		mat.m33 = 0;
	}
	
	public static void setOrtho(Matrix4f mat, float left, float right, float bottom, float top, float near, float far) {
		mat.setIdentity();
		mat.m00 = 2f / (right - left);
		mat.m11 = 2f / (top - bottom);
		mat.m22 = -2f / (far - near);
		mat.m03 = -(right + left) / (right - left);
		mat.m13 = -(top + bottom) / (top - bottom);
		mat.m23 = -(far + near) / (far - near);
	}
	
	public static int getMipMapLevelCount(int width, int height) {
		int level = 1;
		while (width > 1 || height > 1) {
			//System.out.println(w+"x"+h);
			width = (width>1)? width>>1 : 1;
			height = (height>1)? height>>1 : 1;
			level++;
		}
		//System.out.println("1x1");
		return level;
	}
	
	public static void translate(Matrix4f mat, float x, float y, float z) {
		Matrix4f tmpMat = new Matrix4f();
		tmpMat.setIdentity();
		tmpMat.m03 = x;
		tmpMat.m13 = y;
		tmpMat.m23 = z;
		mat.mul(tmpMat);
	}
	
	public static void scale(Matrix4f mat, float x, float y, float z) {
		Matrix4f tmpMat = new Matrix4f();
		tmpMat.setIdentity();
		tmpMat.m00 = x;
		tmpMat.m11 = y;
		tmpMat.m22 = z;
		mat.mul(tmpMat);
	}

	// from LWJGL:
	public static void setLookAt(
		Matrix4f mat,
		float eyex,
		float eyey,
		float eyez,
		float centerx,
		float centery,
		float centerz,
		float upx,
		float upy,
		float upz) {

		// TODO: optimize
		Vector3f forward = new Vector3f();
		Vector3f side = new Vector3f();
		Vector3f up = new Vector3f();

		forward.x = centerx - eyex;
		forward.y = centery - eyey;
		forward.z = centerz - eyez;

		up.x = upx;
		up.y = upy;
		up.z = upz;

		forward.normalize();

		/* Side = forward x up */
		side.cross(forward, up);
		side.normalize();

		/* Recompute up as: up = side x forward */
		up.cross(side, forward);

		mat.setIdentity();
		mat.m00 = side.x;
		mat.m01 = side.y;
		mat.m02 = side.z;

		mat.m10 = up.x;
		mat.m11 = up.y;
		mat.m12 = up.z;

		mat.m20 = -forward.x;
		mat.m21 = -forward.y;
		mat.m22 = -forward.z;

		translate(mat, -eyex, -eyey, -eyez);
	}
	
	public static Point3f project(Point3f pos, Matrix4f modelProjView, Rectangle viewport, Point3f out) {
		Point4f p = new Point4f(pos);
		modelProjView.transform(p);

		float iw = 1f / p.w;
		p.x = (p.x * iw + 1f) * 0.5f;
		p.y = (p.y * iw + 1f) * 0.5f;
		p.z = (p.z * iw + 1f) * 0.5f;
		
		out.x = p.x * viewport.width + viewport.x;
		out.y = p.y * viewport.height + viewport.y;
		out.z = p.z;
		return out;
	}

	public static Point3f unproject(Point3f pos, Matrix4f modelProjViewInv, Rectangle viewport, Point3f out) {
		Point4f p = new Point4f(pos);
		p.x = (p.x - viewport.x) / viewport.width;
		p.y = (p.y - viewport.y) / viewport.height;
		
		p.x = p.x * 2f - 1f;
		p.y = p.y * 2f - 1f;
		p.z = p.z * 2f - 1f;
		
		modelProjViewInv.transform(p);
		
		float iw = 1f / p.w;
		out.x = p.x * iw;
		out.y = p.y * iw;
		out.z = p.z * iw;
		return out;
	}
	
//	public static void mulPickMatrix(Matrix4f mat, float x, float y, float width, float height, Rectangle viewport) {
//		Matrix4f tmpMat = new Matrix4f();
//		tmpMat.setIdentity();
//		
//		tmpMat.m03 = (viewport.width - 2f * (x - viewport.x)) / width;
//		tmpMat.m13 = (viewport.height - 2f * (y - viewport.y)) / height;
//		mat.mul(tmpMat);
//		tmpMat.m03 = 0f;
//		tmpMat.m13 = 0f;
//		
//		tmpMat.m00 = viewport.width / width;
//		tmpMat.m11 = viewport.height / height;
//		mat.mul(tmpMat);
//	}

	public static void setPickMatrix(Matrix4f mat, float x, float y, float width, float height, Rectangle viewport) {
		mat.setIdentity();
		mat.m03 = (viewport.width - 2f * (x - viewport.x)) / width;
		mat.m13 = (viewport.height - 2f * (y - viewport.y)) / height;
		mat.m00 = viewport.width / width;
		mat.m11 = viewport.height / height;
	}
	
	public static int getNearestPowerOfTwo(int x) {
		if (x <= 0) return 0;
		return 1 << (32 - Integer.numberOfLeadingZeros(x - 1));
	}
	
}
