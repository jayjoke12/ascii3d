package com.ascii3d.engine;

import com.ascii3d.math.Mat4;
import com.ascii3d.math.Vec3;
import com.ascii3d.math.Vec4;
import com.ascii3d.scene.*;

public class RenderPipeline {

    private final Framebuffer framebuffer;
    private final Rasterizer  rasterizer;

    public RenderPipeline(Framebuffer framebuffer) {
        this.framebuffer = framebuffer;
        this.rasterizer  = new Rasterizer(framebuffer);
    }

    /**
     * Render a scene object with given camera and light.
     *
     * @param obj    The object to render (mesh + transform)
     * @param camera Scene camera
     * @param light  Directional light
     */
    public void render(SceneObject obj, Camera camera, Light light) {
        Mesh  mesh  = obj.mesh;
        Mat4  model = obj.transform.toMatrix();
        Mat4  vp    = camera.viewProjection();
        Mat4  mvp   = vp.mul(model);

        Vec3[] screenPos   = new Vec3[mesh.vertices.length];
        double[] intensity = new double[mesh.vertices.length];

        Vec3 camDir = camera.position.sub(camera.target).normalize();

        for (int i = 0; i < mesh.vertices.length; i++) {
            Vec4 clip = mvp.mul(Vec4.point(mesh.vertices[i]));
            Vec3 ndc  = clip.perspectiveDivide();

            double sx = (ndc.x + 1.0) * 0.5 * (framebuffer.width  - 1);
            double sy = (1.0 - ndc.y) * 0.5 * (framebuffer.height - 1);  // flip Y
            screenPos[i] = new Vec3(sx, sy, ndc.z);

            Vec3 worldNormal = Vec3.UP;
            if (mesh.normals != null) {
                worldNormal = model.transformDir(mesh.normals[i]).normalize();
            }
            intensity[i] = light.phong(worldNormal, camDir);
        }

        for (int tri = 0; tri < mesh.triangleCount(); tri++) {
            int i0 = mesh.indices[tri * 3];
            int i1 = mesh.indices[tri * 3 + 1];
            int i2 = mesh.indices[tri * 3 + 2];

            Vec3 p0 = screenPos[i0];
            Vec3 p1 = screenPos[i1];
            Vec3 p2 = screenPos[i2];

            if (p0.z < -1 || p0.z > 1 || p1.z < -1 || p1.z > 1 || p2.z < -1 || p2.z > 1)
                continue;

            rasterizer.drawTriangle(p0, p1, p2,
                intensity[i0], intensity[i1], intensity[i2]);
        }
    }
}
