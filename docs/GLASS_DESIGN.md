# Clear Liquid Glass Design System (iOS 26/27 Optics for Android)

LastWave-Native features a ground-up redesign rendering every user-facing surface in **Clear Liquid Glass** with Spotify-style structural hierarchy and silky spring physics.

---

## 1. Vision & Core Principles

- **True Clarity, Never Frosted:** The glass maintains high transparency (tint alpha 4%–10%). Content, artwork, and mesh gradients drifting behind remain vividly recognizable and colorful.
- **Vibrant Refraction:** Backdrops undergo real-time 1.6× saturation boost and 1.05× brightness enhancement so light passing through the glass appears energized.
- **Physical Depth:** Specular highlights, edge refraction lensing, subtle chromatic aberration, and soft ambient shadows create tactile physical presence.
- **Continuous Curvature:** All surfaces use continuous-corner squircles (28dp–36dp) or liquid capsules. No harsh 90° corners exist anywhere in the app.
- **Silky Spring Motion:** 120 Hz interruptible springs (damping ratio 0.55, stiffness 380) with liquid press-compression (scale 0.96) and drag stretching.

---

## 2. The 7-Layer Optical Recipe

Every glass surface in the `com.lastwave.app.ui.glass` design system renders through a precise 7-layer stack:

```
┌────────────────────────────────────────────────────────┐
│  Layer 7: Motion-Reactive Sheen (Gyro / Scroll Tilt)   │
├────────────────────────────────────────────────────────┤
│  Layer 6: Ambient Drop Shadow (24dp blur, y+8dp, 18%)  │
├────────────────────────────────────────────────────────┤
│  Layer 5: Inner Highlight (Top) & Soft Shadow (Bottom) │
├────────────────────────────────────────────────────────┤
│  Layer 4: Specular Rim Light (1.5dp gradient stroke)   │
├────────────────────────────────────────────────────────┤
│  Layer 3: Dynamic Tint (4–8% White / Gradient)         │
├────────────────────────────────────────────────────────┤
│  Layer 2: Edge Refraction Lens + Chromatic Aberration  │
├────────────────────────────────────────────────────────┤
│  Layer 1: Backdrop Capture + Blur (6–16dp) + Sat Boost │
└────────────────────────────────────────────────────────┘
```

1. **Backdrop Capture & Saturation Boost:**
   - Captured via `Modifier.backdrop()` or hardware blur buffers.
   - Blur radius: 6dp–16dp for floating components, chips, and bars; 24dp–32dp for full-screen sheets.
   - Saturation enhanced 1.4×–1.8× and brightness multiplied by 1.05×.
2. **Edge Refraction & Chromatic Dispersion:**
   - AGSL runtime shader simulates a convex meniscus lens near outer boundaries.
   - Refraction strength peaks at 10%–25% along outer 12dp of edges and decays smoothly to 0% in center.
   - 1.5px chromatic split separates RGB channels along high-curvature contours.
3. **Tint Gradient:**
   - Dark mode: 6% white tint with vertical gradient (8% top fading to 4% bottom).
   - Light mode: 4% dark tint.
   - Pure Black AMOLED: 3% white tint with dimmed background blobs.
4. **Specular Rim Light:**
   - 1.5dp continuous stroke.
   - Top-left corner: 55% white specular highlight.
   - Mid-edges: 5% falloff.
   - Bottom-right corner: 35% ambient reflection.
5. **Inner Edge Definition:**
   - 1dp inner highlight along top perimeter.
   - Soft inner shadow along bottom perimeter for depth.
6. **Soft Ambient Shadow:**
   - Large, diffused elevation shadow (blur radius 24dp, y-offset 8dp, opacity 18% black).
7. **Motion-Reactive Sheen:**
   - Dynamic specular glint reacting to device orientation sensor (gyroscope) and scroll velocity.

---

## 3. Component Architecture (`com.lastwave.app.ui.glass`)

The entire user interface is built exclusively on the glass design system located at `com.lastwave.app.ui.glass`:

| Component | Class | Description |
|---|---|---|
| **Base Surface** | `GlassSurface` | Core composable applying squircle clipping, backdrop blur, tint, rim light, and ambient shadow. |
| **Pill Buttons** | `GlassButton` | Primary action buttons with liquid press physics (0.96 scale, overshoot release). |
| **Icon Buttons** | `GlassIconButton` | Capsule and circular glass controls with interactive ripple and spring response. |
| **Interactive Chips** | `GlassChip` | Filter pills with active selection states, glowing glass rims, and badge support. |
| **Search Bar** | `GlassSearchBar` | Floating capsule search input with embedded glass query field and clear action. |
| **Seek & Volume Slider** | `GlassSlider` | Continuous slider with liquid magnifying glass lens thumb that stretches during drag. |
| **Toggles** | `GlassSwitch` | Fluid toggle switch with glass pill track and liquid droplet thumb. |
| **Bottom Navigation** | `GlassBottomTabBar` | Floating glass capsule bar with liquid droplet selection bubble sliding between tabs. |
| **Mini Player** | `GlassMiniPlayer` | Floating glass capsule displaying thumbnail, marquee title, play/pause, and hairline progress. |
| **Top App Bar** | `GlassTopBar` | Transparent header with progressive backdrop blur that activates on scroll. |
| **Bottom Sheets** | `GlassBottomSheet` | Smooth drag-to-dismiss sheet with blurred backdrop, handle pill, and top specular rim. |
| **Dialogs** | `GlassDialog` / `GlassBasicDialog` | Glass modal dialogs replacing all legacy `AlertDialog` instances app-wide. |
| **Cards & Rows** | `GlassCard`, `GlassListRow` | Content grouping containers with 28dp squircles and glass hover states. |
| **Segmented Controls** | `GlassSegmentedControl` | Multi-option tab switcher with sliding liquid glass selection pill. |
| **Liquid Droplet Loader** | `GlassProgressLoader` | Fluid droplet animation for pull-to-refresh and network loading states. |

---

## 4. Hardware Acceleration & Tier Fallbacks

To ensure 120 FPS performance across all Android devices, the engine dynamically selects the optimal rendering tier:

```
┌─────────────────────────────────────────────────────────────┐
│ Tier A (Android 13+ / API 33+)                              │
│ • Full AGSL runtime shaders with real-time edge refraction │
│ • 1.5px chromatic dispersion and gyroscopic sheen           │
├─────────────────────────────────────────────────────────────┤
│ Tier B (Android 12 / API 31–32)                             │
│ • RenderEffect backdrop blur + saturation boost             │
│ • Hardware-accelerated specular rim gradient & shadow       │
├─────────────────────────────────────────────────────────────┤
│ Tier C (Android 10–11 / Low-End / Battery Saver)            │
│ • Translucent gradient tint + 1.5dp specular stroke         │
│ • Zero backdrop capture overhead                            │
└─────────────────────────────────────────────────────────────┘
```

- **Render Layer Caching:** All backdrop layers use `Modifier.drawWithCache` to avoid recomposition overhead.
- **Maximum Layers:** Limited to a maximum of 4 simultaneous backdrop capture nodes per frame.
- **Accessibility:** Respects system accessibility flags:
  - *Reduce Transparency:* Switches automatically to high-contrast opaque dark panels.
  - *Remove Animations:* Snaps spring transitions instantly.

---

## 5. Spotify-Style Hierarchy & Navigation

The application navigation has been structured around Spotify's industry-standard music layout:

1. **Floating Navigation Chrome:**
   - Bottom Tab Bar: Floating glass capsule containing **Home**, **Search**, and **Your Library**.
   - Mini Player: Floats directly above the tab bar with a 12dp gap, featuring track artwork, title, play/pause, and track progress hairline.
2. **Now Playing Screen (`FullPlayer`):**
   - High-fidelity layout with artwork-derived drifting mesh gradient.
   - Album artwork with liquid spring gesture interaction.
   - Glass playback controls: Previous, Play/Pause droplet, Next, Shuffle, Repeat.
   - Synced lyrics preview card peeking at bottom with one-tap expansion.
   - Real-time Audio Quality Badge (Bit-Perfect / Hi-Res / Lossless) rendered as a glass chip.
3. **Synced Lyrics (`ModernLyricsView`):**
   - Expanded full-screen glass sheet.
   - Active singing line rendered large and bright with subtle glow; background lines blurred.
   - Synchronized spring auto-scroll.
4. **15-Band Equalizer & Audio Settings:**
   - 15 individual glass frequency sliders with haptic feedback ticks at 25%, 50%, and 75%.
   - Glass toggle switches for Bit-Perfect USB DAC mode, Automix crossfade, and Loudness Normalization.

---

## 6. Glass Lab Debugging Suite

A built-in interactive calibration suite allows real-time tuning of the optical engine:

- **How to Access:** Navigate to **Settings → About**, and tap the **App Version** 7 times.
- **Live Calibration Parameters:**
  - *Backdrop Blur Radius:* 0dp – 48dp
  - *Glass Tint Alpha:* 0% – 40%
  - *Refraction Meniscus Strength:* 0% – 50%
  - *Chromatic Dispersion Split:* 0px – 4px
  - *Specular Rim Alpha:* 0% – 100%
  - *Backdrop Saturation Boost:* 1.0× – 2.5×
- **Interactive Component Playground:** Renders live instances of buttons, chips, sliders, cards, switches, and progress droplets reacting instantly to slider adjustments.

---

## 7. Performance & 120 FPS Rendering Architecture

To guarantee 120 FPS buttery-smooth scrolling on high-refresh Android displays:
1. **`drawWithCache` Optical Pipeline:** Specular rim gradient brushes, strokes, and squircle paths are allocated only when container size changes, preventing thousands of garbage-collection allocations per second during scroll.
2. **Selective Ambient Shadows (`hasShadow = false`):** List rows and grouping containers skip `Modifier.shadow()`, completely avoiding excess RenderNode allocations inside `LazyColumn`.
3. **Decoupled Gesture Layer (`interactive = false`):** Passive scroll rows do not bind custom multi-touch gesture listeners, eliminating touch-slop latency.
4. **Draw-Phase Dynamic Backgrounds (`drawBehind`):** Animated mesh gradients compute animated phase offsets exclusively in the draw pass, eliminating recomposition loops.
5. **High-Contrast Obsidian Glass for Modals (`isSheet = true`):** Bottom sheets and menus employ an obsidian glass foundation (`Color(0xFF141418).copy(alpha = 0.88f)`) with a 72% dark scrim and top specular sheen, ensuring complete legibility over busy underlying content while preserving rim light aesthetics.

---

## 8. Release Build Configuration

To build an optimized production release APK with R8 minification and resource shrinking:

### Signing Environment Variables

Set the following environment variables or add them to your `local.properties`:

```properties
# Option 1: Base64-encoded keystore
SIGNING_KEY=<base64_encoded_jks_keystore>
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password

# Option 2: Path to keystore file
RELEASE_STORE_FILE=/path/to/release.keystore
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password
```

### Build Commands

```bash
# Build optimized minified release APK
./gradlew assembleRelease

# Or build unminified raw release APK
./gradlew assembleRawRelease
```

The compiled release APK will be located at:
`app/build/outputs/apk/release/app-release.apk`
