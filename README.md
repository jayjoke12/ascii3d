# ASCII 3D Rendering Engine

A  3D rendering pipeline written in  Java, with terminal ASCII output.

---

## Architecture

```
src/main/java/com/ascii3d/
├── math/
│   ├── Vec3.java         — Immutable 3D vector (dot, cross, normalize, lerp, reflect)
│   ├── Vec4.java         — Homogeneous 4D vector + perspective divide
│   └── Mat4.java         — Column-major 4×4 matrix: translation, scale, rotX/Y/Z,
│                           lookAt, perspective, mul(Mat4), mul(Vec4)
│
├── scene/
│   ├── Mesh.java         — Triangle mesh (vertices + indices + normals)
│   │                       Built-ins: cube, sphere, torus, tetrahedron
│   ├── Transform.java    — TRS: position / Euler rotation / scale → model matrix
│   ├── Camera.java       — Perspective camera: view + projection + orbit controls
│   ├── Light.java        — Directional light with Phong model (ambient+diffuse+specular)
│   └── SceneObject.java  — Mesh bound to a Transform
│
├── engine/
│   ├── Framebuffer.java  — Software pixel + depth (z-buffer) store
│   ├── Rasterizer.java   — Barycentric triangle fill + depth test
│   └── RenderPipeline.java — Full pipeline: model→view→clip→NDC→screen + Gouraud shading
│
├── renderer/
│   ├── AsciiPalette.java — Intensity→character palettes (DETAILED, CLASSIC, BLOCK …)
│   └── AsciiRenderer.java — Framebuffer → ASCII string + ANSI in-place animation
│
└── demo/
    └── Main.java         — Animated scene: rotating cube + torus + sphere
```

---

## The 3D Pipeline (stage by stage)

```
Object vertices  ──►  Model matrix   ──►  World space
World space      ──►  View matrix    ──►  Camera space
Camera space     ──►  Projection     ──►  Clip space (homogeneous)
Clip space       ──►  Perspective ÷  ──►  NDC  [-1,+1]³
NDC              ──►  Viewport       ──►  Screen pixels
Screen pixels    ──►  Rasterizer     ──►  Framebuffer (intensity + depth)
Framebuffer      ──►  AsciiRenderer  ──►  Terminal text
```

### Key algorithms

| Stage | Algorithm |
|-------|-----------|
| Projection | Perspective (FOV, aspect, near/far planes) |
| Shading | Gouraud (per-vertex Phong, interpolated across triangle) |
| Culling | Back-face (signed triangle area < 0 → skip) |
| Hidden surface | Z-buffer (per-pixel depth test) |
| Rasterisation | Barycentric coordinates + bounding-box scan |
| Normal computation | Smooth vertex normals (averaged face normals) |
| Output | Intensity → ASCII palette character |

---

## Getting Started

### Requirements
- Java 17+
- Maven 3.8+

### Run from terminal
```bash
cd ascii3d
mvn package -q
java -jar target/ascii3d.jar
```

### Change resolution
Edit `Main.java`:
```java
static final int WIDTH  = 120;   // character columns
static final int HEIGHT = 80;    // pixel rows (displayed rows = HEIGHT/2)
```

### Change palette
```java
new AsciiRenderer(AsciiPalette.CLASSIC)   // sparse ramp
new AsciiRenderer(AsciiPalette.BLOCK)     // block characters
new AsciiRenderer(AsciiPalette.DETAILED)  // dense ramp (default)
```

---

## Extending the Engine

### Add a new mesh
```java
// In Mesh.java or your own class:
public static Mesh pyramid(double size) {
    Vec3[] v = { ... };
    int[]  i = { ... };
    return new Mesh("pyramid", v, i, Mesh.computeNormals(v, i));
}
```

### Add a new scene object
```java
SceneObject myObj = new SceneObject(
    "myObj",
    Mesh.pyramid(1.0),
    new Transform(new Vec3(0, 0, 0), Vec3.ZERO, Vec3.ONE)
);
```

### Implement a fragment shader
Override `Rasterizer.drawTriangle` to pass extra per-vertex attributes
(UV, colour) and interpolate them via barycentric weights `w0/w1/w2`.

---

## Palette reference

```
DETAILED  — 70 characters, smooth gradients, best for Phong shading
CLASSIC   — 10 characters, high contrast
COMPACT   — 11 characters, balanced
BLOCK     — Unicode block elements (▒░▓█), terminal must support UTF-8
```



made with IntelliJ ❤️