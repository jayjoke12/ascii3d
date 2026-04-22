package com.ascii3d.scene;

import com.ascii3d.math.Vec3;
import java.util.ArrayList;
import java.util.List;

/**
 * Triangle mesh: vertices + indices + optional per-vertex normals.
 */
public class Mesh {

    public final String name;
    public final Vec3[] vertices;
    public final int[]  indices;   // triples: (i0,i1,i2) per triangle
    public final Vec3[] normals;   // per-vertex normals (may be null)

    public Mesh(String name, Vec3[] vertices, int[] indices, Vec3[] normals) {
        this.name     = name;
        this.vertices = vertices;
        this.indices  = indices;
        this.normals  = normals;
    }

    public int triangleCount() { return indices.length / 3; }

    // ── Built-in geometry factories ───────────────────────────────────────────

    public static Mesh cube(double size) {
        double h = size / 2.0;
        Vec3[] v = {
            // Front face
            new Vec3(-h,-h, h), new Vec3( h,-h, h),
            new Vec3( h, h, h), new Vec3(-h, h, h),
            // Back face
            new Vec3(-h,-h,-h), new Vec3( h,-h,-h),
            new Vec3( h, h,-h), new Vec3(-h, h,-h),
        };
        int[] idx = {
            0,1,2, 2,3,0,   // front
            4,6,5, 6,4,7,   // back
            4,0,3, 3,7,4,   // left
            1,5,6, 6,2,1,   // right
            3,2,6, 6,7,3,   // top
            4,5,1, 1,0,4    // bottom
        };
        return new Mesh("cube", v, idx, computeNormals(v, idx));
    }

    public static Mesh sphere(double radius, int latBands, int lonBands) {
        List<Vec3> verts = new ArrayList<>();
        List<Integer> idxList = new ArrayList<>();

        for (int lat = 0; lat <= latBands; lat++) {
            double theta = lat * Math.PI / latBands;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);
            for (int lon = 0; lon <= lonBands; lon++) {
                double phi = lon * 2 * Math.PI / lonBands;
                verts.add(new Vec3(
                    radius * Math.cos(phi) * sinTheta,
                    radius * cosTheta,
                    radius * Math.sin(phi) * sinTheta
                ));
            }
        }

        for (int lat = 0; lat < latBands; lat++) {
            for (int lon = 0; lon < lonBands; lon++) {
                int first  = lat * (lonBands + 1) + lon;
                int second = first + lonBands + 1;
                idxList.add(first);    idxList.add(second); idxList.add(first+1);
                idxList.add(second);   idxList.add(second+1); idxList.add(first+1);
            }
        }

        Vec3[] va = verts.toArray(new Vec3[0]);
        int[]  ia = idxList.stream().mapToInt(i->i).toArray();
        return new Mesh("sphere", va, ia, computeNormals(va, ia));
    }

    public static Mesh tetrahedron(double size) {
        double a = size;
        Vec3[] v = {
            new Vec3( 0,       a,       0      ),
            new Vec3( 0,       -a/3,    2*a/3  ).mul(size/size),
            new Vec3(-a*0.866, -a/3,   -a/3   ),
            new Vec3( a*0.866, -a/3,   -a/3   )
        };
        int[] idx = { 0,1,2,  0,2,3,  0,3,1,  1,3,2 };
        return new Mesh("tetrahedron", v, idx, computeNormals(v, idx));
    }

    public static Mesh torus(double R, double r, int radialSegs, int tubeSegs) {
        List<Vec3> verts = new ArrayList<>();
        List<Integer> idxList = new ArrayList<>();

        for (int i = 0; i <= radialSegs; i++) {
            double u = (double) i / radialSegs * 2 * Math.PI;
            double cu = Math.cos(u), su = Math.sin(u);
            for (int j = 0; j <= tubeSegs; j++) {
                double v = (double) j / tubeSegs * 2 * Math.PI;
                double cv = Math.cos(v), sv = Math.sin(v);
                verts.add(new Vec3(
                    (R + r * cv) * cu,
                    r * sv,
                    (R + r * cv) * su
                ));
            }
        }

        for (int i = 0; i < radialSegs; i++) {
            for (int j = 0; j < tubeSegs; j++) {
                int a = i * (tubeSegs + 1) + j;
                int b = a + tubeSegs + 1;
                idxList.add(a); idxList.add(b);   idxList.add(a+1);
                idxList.add(b); idxList.add(b+1); idxList.add(a+1);
            }
        }

        Vec3[] va = verts.toArray(new Vec3[0]);
        int[]  ia = idxList.stream().mapToInt(i->i).toArray();
        return new Mesh("torus", va, ia, computeNormals(va, ia));
    }

    // ── Normal computation ────────────────────────────────────────────────────

    /** Compute smooth vertex normals by averaging face normals */
    public static Vec3[] computeNormals(Vec3[] verts, int[] idx) {
        Vec3[] normals = new Vec3[verts.length];
        for (int i = 0; i < normals.length; i++) normals[i] = Vec3.ZERO;

        for (int i = 0; i < idx.length; i += 3) {
            Vec3 a = verts[idx[i]];
            Vec3 b = verts[idx[i+1]];
            Vec3 c = verts[idx[i+2]];
            Vec3 faceNormal = b.sub(a).cross(c.sub(a));
            normals[idx[i]]   = normals[idx[i]].add(faceNormal);
            normals[idx[i+1]] = normals[idx[i+1]].add(faceNormal);
            normals[idx[i+2]] = normals[idx[i+2]].add(faceNormal);
        }

        for (int i = 0; i < normals.length; i++)
            normals[i] = normals[i].normalize();

        return normals;
    }
}
