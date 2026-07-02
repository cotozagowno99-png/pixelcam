# PixelCam 📷🟪

Aparat Pixel Art z podglądem renderowanym w czasie rzeczywistym na GPU (OpenGL ES 2.0).

## Uruchomienie
1. Otwórz katalog projektu w Android Studio (Koala lub nowsze).
2. Poczekaj na Gradle Sync (AS pobierze Gradle 8.9 i zależności).
3. Uruchom na fizycznym urządzeniu (min. Android 8.0 / API 26) - emulator nie pokaże prawdziwego obrazu kamery.

## Architektura
- MVVM + UseCases + Repository, Hilt DI, StateFlow, Navigation Compose
- CameraX Preview -> SurfaceTexture (GL_TEXTURE_EXTERNAL_OES) -> fragment shader
- Shader wykonuje: pikselizację, wyostrzanie, korekcję (kontrast/jasność/saturacja),
  kwantyzację do palety, krawędzie oraz voxelowy bevel 3D (światło/cień/szwy)
- Migawka zapisuje dokładnie to, co widać (glReadPixels z framebuffera) jako PNG i/lub JPEG
  do MediaStore (Pictures/PixelCam)
- RENDERMODE_WHEN_DIRTY = minimalne zużycie baterii (render tylko przy nowej klatce)

## Tryby
- Small Pixel (~128px), Medium Pixel (~64px), 3D Pixel (voxel/Minecraft look)

## Palety
Pastel, Game Boy, NES, Retro, Pink Dream, Blue Dream, Candy, Cyber, Soft, Mono
