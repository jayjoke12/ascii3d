package com.ascii3d.demo;

import com.ascii3d.engine.Framebuffer;
import com.ascii3d.engine.RenderPipeline;
import com.ascii3d.math.Vec3;
import com.ascii3d.renderer.AsciiPalette;
import com.ascii3d.renderer.AsciiRenderer;
import com.ascii3d.scene.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ASCII 3D Engine — entry point.
 *
 * ── Usage ──────────────────────────────────────────────────────────────────
 *
 *  No arguments → default demo (cube + torus + sphere)
 *
 *  With arguments:
 *    java -cp target/classes com.ascii3d.demo.Main [options] [file.obj ...]
 *
 *  Options (all optional, order doesn't matter):
 *    --contrast   <value>   Contrast multiplier.  Default 1.5  (try 0.5–3.0)
 *    --brightness <value>   Brightness offset.    Default 0.0  (try -0.3–0.3)
 *    --gamma      <value>   Gamma exponent.       Default 0.9  (try 0.5–2.0)
 *    --palette    <name>    DETAILED|CLASSIC|COMPACT|BLOCK. Default DETAILED
 *    --width      <cols>    Framebuffer width.    Default 120
 *    --height     <rows>    Framebuffer height.   Default 80
 *    --scale      <value>   Scale for all OBJ models. Default 1.0
 *    --preset     <name>    default|highcontrast|soft|dramatic
 *
 *  OBJ files: any argument ending in .obj is loaded as a 3D model.
 *             Multiple OBJ files are placed side-by-side automatically.
 *
 * ── Examples ───────────────────────────────────────────────────────────────
 *
 *  Default demo, high contrast:
 *    java ... Main --contrast 2.5 --gamma 0.7
 *
 *  Load a custom OBJ:
 *    java ... Main mymodel.obj
 *
 *  Load two OBJs with a soft look:
 *    java ... Main --preset soft skull.obj teapot.obj
 *
 *  Load OBJ scaled down, wide framebuffer:
 *    java ... Main --scale 0.5 --width 160 --height 100 dragon.obj
 */
public class Main {

    static int    WIDTH    = 120;
    static int    HEIGHT   = 80;
    static int    FRAME_MS = 50;

    public static void main(String[] args) throws InterruptedException, IOException {

        // ── Parse arguments ────────────────────────────────────────────────────
        double contrast   = 1.5;
        double brightness = 0.0;
        double gamma      = 0.9;
        double objScale   = 1.0;
        AsciiPalette palette = AsciiPalette.DETAILED;
        List<String> objFiles = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "--contrast"   -> contrast   = Double.parseDouble(args[++i]);
                case "--brightness" -> brightness = Double.parseDouble(args[++i]);
                case "--gamma"      -> gamma       = Double.parseDouble(args[++i]);
                case "--scale"      -> objScale    = Double.parseDouble(args[++i]);
                case "--width"      -> WIDTH       = Integer.parseInt(args[++i]);
                case "--height"     -> HEIGHT      = Integer.parseInt(args[++i]);
                case "--palette"    -> palette     = AsciiPalette.valueOf(args[++i].toUpperCase());
                case "--preset" -> {
                    switch (args[++i].toLowerCase()) {
                        case "highcontrast" -> { contrast = 2.0; brightness = -0.1; gamma = 0.8; }
                        case "soft"         -> { contrast = 0.7; brightness =  0.1; gamma = 1.2; }
                        case "dramatic"     -> { contrast = 3.0; brightness = -0.2; gamma = 0.6; }
                        default             -> { contrast = 1.5; brightness =  0.0; gamma = 0.9; }
                    }
                }
                default -> {
                    if (args[i].toLowerCase().endsWith(".obj")) {
                        objFiles.add(args[i]);
                    } else {
                        System.err.println("Unknown argument: " + args[i]);
                    }
                }
            }
        }

        // ── Build renderer ─────────────────────────────────────────────────────
        AsciiRenderer renderer = new AsciiRenderer(palette)
                .contrast(contrast)
                .brightness(brightness)
                .gamma(gamma);

        Framebuffer    fb       = new Framebuffer(WIDTH, HEIGHT);
        RenderPipeline pipeline = new RenderPipeline(fb);
        Light          light    = Light.defaultLight();

        Camera camera = new Camera(
                new Vec3(0, 2, 5),
                Vec3.ZERO,
                Math.toRadians(60),
                (double) WIDTH / HEIGHT * 0.5,
                0.1, 200.0
        );

        // ── Build scene ────────────────────────────────────────────────────────
        List<SceneObject> objects = new ArrayList<>();

        if (!objFiles.isEmpty()) {
            // Load each OBJ and space them out horizontally
            double spacing = 2.5;
            double startX  = -spacing * (objFiles.size() - 1) / 2.0;

            for (int i = 0; i < objFiles.size(); i++) {
                String file = objFiles.get(i);
                System.out.println("Loading: " + file);
                try {
                    Mesh mesh = ObjLoader.load(file, objScale);
                    System.out.printf("  → %d vertices, %d triangles%n",
                            mesh.vertices.length, mesh.triangleCount());
                    Transform t = new Transform(
                            new Vec3(startX + i * spacing, 0, 0),
                            Vec3.ZERO,
                            Vec3.ONE
                    );
                    objects.add(new SceneObject(mesh.name, mesh, t));
                } catch (IOException e) {
                    System.err.println("  Failed to load: " + e.getMessage());
                }
            }

            if (objects.isEmpty()) {
                System.err.println("No models loaded — falling back to default demo.");
                objects.addAll(defaultScene());
            }
        } else {
            objects.addAll(defaultScene());
        }

        // ── Render loop ────────────────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(renderer::cleanup));

        SceneObject[] sceneArr = objects.toArray(new SceneObject[0]);
        int frame = 0;

        while (!Thread.currentThread().isInterrupted()) {
            double t = frame * 0.04;

            // Spin each object
            for (SceneObject obj : sceneArr) {
                obj.transform.rotation = new Vec3(t * 0.5, t * 0.9, t * 0.3);
            }

            // Slowly orbit the camera
            camera.position = new Vec3(
                    Math.sin(t * 0.12) * 6.0,
                    2.0 + Math.sin(t * 0.08) * 0.8,
                    Math.cos(t * 0.12) * 6.0
            );

            fb.clear();
            for (SceneObject obj : sceneArr) pipeline.render(obj, camera, light);
            renderer.renderAndPrint(fb, frame);

            Thread.sleep(FRAME_MS);
            frame++;
        }
    }

    // ── Default built-in scene ─────────────────────────────────────────────────

    private static List<SceneObject> defaultScene() {
        return List.of(
            new SceneObject("cube",
                Mesh.cube(1.2),
                new Transform(new Vec3(-2.2, 0, 0), Vec3.ZERO, Vec3.ONE)),
            new SceneObject("torus",
                Mesh.torus(0.7, 0.25, 28, 18),
                new Transform(new Vec3(0, 0, 0), Vec3.ZERO, Vec3.ONE)),
            new SceneObject("sphere",
                Mesh.sphere(0.7, 20, 20),
                new Transform(new Vec3(2.2, 0, 0), Vec3.ZERO, Vec3.ONE))
        );
    }
}
