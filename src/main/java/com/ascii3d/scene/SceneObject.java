package com.ascii3d.scene;

/**
 * A renderable object: mesh + transform + optional name.
 */
public class SceneObject {

    public final String    name;
    public final Mesh      mesh;
    public final Transform transform;

    public SceneObject(String name, Mesh mesh, Transform transform) {
        this.name      = name;
        this.mesh      = mesh;
        this.transform = transform;
    }

    public SceneObject(Mesh mesh) {
        this(mesh.name, mesh, new Transform());
    }
}
