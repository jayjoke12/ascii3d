package com.ascii3d.scene;

import com.ascii3d.math.Vec3;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Wavefront OBJ loader.
 *
 * Supports:
 *   v   x y z          — vertex positions
 *   vn  x y z          — vertex normals
 *   f   v v v          — triangle faces (position only)
 *   f   v//vn v//vn v//vn  — faces with normals
 *   f   v/vt/vn ...    — faces with UVs and normals (UVs ignored)
 *
 * Quads (4-vertex faces) are automatically triangulated.
 * Negative indices are resolved relative to current vertex count.
 *
 */
public class ObjLoader {

    public static Mesh load(String path) throws IOException {
        return load(path, 1.0);
    }

    public static Mesh load(String path, double scale) throws IOException {
        return load(Path.of(path), scale);
    }

    public static Mesh load(Path path, double scale) throws IOException {
        List<Vec3> positions = new ArrayList<>();
        List<Vec3> normals   = new ArrayList<>();
        List<int[]> posIdx   = new ArrayList<>();
        List<int[]> nrmIdx   = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] tok = line.split("\\s+");
                switch (tok[0]) {
                    case "v" -> positions.add(new Vec3(
                            d(tok[1]) * scale,
                            d(tok[2]) * scale,
                            d(tok[3]) * scale));

                    case "vn" -> normals.add(new Vec3(d(tok[1]), d(tok[2]), d(tok[3])).normalize());

                    case "f" -> {
                        int vertCount = tok.length - 1;
                        int[] pi = new int[vertCount];
                        int[] ni = new int[vertCount];
                        boolean hasNormals = false;

                        for (int i = 0; i < vertCount; i++) {
                            String[] parts = tok[i + 1].split("/");
                            pi[i] = resolveIdx(parts[0], positions.size());
                            if (parts.length == 3 && !parts[2].isEmpty()) {
                                ni[i] = resolveIdx(parts[2], normals.size());
                                hasNormals = true;
                            }
                        }

                        for (int i = 1; i < vertCount - 1; i++) {
                            posIdx.add(new int[]{pi[0], pi[i], pi[i + 1]});
                            nrmIdx.add(hasNormals
                                    ? new int[]{ni[0], ni[i], ni[i + 1]}
                                    : new int[]{-1, -1, -1});
                        }
                    }
                }
            }
        }

        if (positions.isEmpty()) throw new IOException("No vertices found in: " + path);
        if (posIdx.isEmpty())    throw new IOException("No faces found in: " + path);

        // Build flat vertex/index arrays
        Vec3[] verts = positions.toArray(new Vec3[0]);
        int[]  idx   = new int[posIdx.size() * 3];
        for (int i = 0; i < posIdx.size(); i++) {
            idx[i*3]   = posIdx.get(i)[0];
            idx[i*3+1] = posIdx.get(i)[1];
            idx[i*3+2] = posIdx.get(i)[2];
        }

        // Build per-vertex normals
        Vec3[] vertNormals;
        boolean hasAnyNormals = nrmIdx.stream().anyMatch(n -> n[0] >= 0);

        if (hasAnyNormals && !normals.isEmpty()) {
            // Average the OBJ normals into per-vertex slots
            Vec3[] acc = new Vec3[verts.length];
            Arrays.fill(acc, Vec3.ZERO);
            for (int i = 0; i < posIdx.size(); i++) {
                int[] pi = posIdx.get(i);
                int[] ni = nrmIdx.get(i);
                for (int j = 0; j < 3; j++) {
                    if (ni[j] >= 0 && ni[j] < normals.size()) {
                        acc[pi[j]] = acc[pi[j]].add(normals.get(ni[j]));
                    }
                }
            }
            vertNormals = new Vec3[verts.length];
            for (int i = 0; i < verts.length; i++)
                vertNormals[i] = acc[i].length() < 1e-10 ? Vec3.UP : acc[i].normalize();
        } else {

            vertNormals = Mesh.computeNormals(verts, idx);
        }

        String name = path.getFileName().toString().replaceFirst("\\.obj$", "");
        return new Mesh(name, verts, idx, vertNormals);
    }



    private static double d(String s) { return Double.parseDouble(s); }


    private static int resolveIdx(String token, int count) {
        int i = Integer.parseInt(token);
        return i < 0 ? count + i : i - 1;
    }
}
