package net.bananacheese.bananasarmory.datagen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Group-aware armor texture generator
 * Respects component groups - won't combine iron_pauldrons + gold_pauldrons
 *
 * Component naming convention: {material}_{group}
 * Examples:
 *   - iron_pauldrons, gold_pauldrons, diamond_pauldrons → group: "pauldrons"
 *   - iron_gorget, gold_gorget → group: "gorget"
 *   - steel_reinforcement → group: "reinforcement"
 *   - visor → group: "visor"
 */
public class ArmorTextureGenerator {

    private static String findResourcesPath() {
        String[] possiblePaths = {
                "src/main/resources/assets/barmory/textures/item/armor/",
                "./src/main/resources/assets/barmory/textures/item/armor/",
                "../src/main/resources/assets/barmory/textures/item/armor/",
                "../../src/main/resources/assets/barmory/textures/item/armor/"
        };

        for (String path : possiblePaths) {
            File testFile = new File(path + "frames/");
            if (testFile.exists()) {
                System.out.println("✓ Found resources at: " + testFile.getAbsolutePath());
                return path;
            }
        }

        return "src/main/resources/assets/barmory/textures/item/armor/";
    }

    private static final String TEXTURES_PATH = findResourcesPath();
    private static final String OUTPUT_PATH = TEXTURES_PATH + "generated/";
    private static final List<String> FRAME_TYPES = List.of("helmet", "chestplate", "leggings", "boots");
    private static final int MAX_COMPONENTS = 6;

    public static void main(String[] args) {
        System.out.println("Starting GROUP-AWARE armor texture generation...");
        System.out.println("(Respects component groups - won't mix iron_pauldrons + gold_pauldrons)");
        System.out.println();

        File framesDir = new File(TEXTURES_PATH + "frames/");
        File overlayDir = new File(TEXTURES_PATH + "components/overlay/");

        if (!framesDir.exists() || !overlayDir.exists()) {
            System.err.println("❌ ERROR: Required directories not found!");
            printDirectoryStructure();
            return;
        }

        System.out.println("✓ Directories found");
        System.out.println();

        try {
            Files.createDirectories(Paths.get(OUTPUT_PATH));

            int totalGenerated = 0;
            int totalSkipped = 0;

            for (String frameType : FRAME_TYPES) {
                System.out.println("═══════════════════════════════════════");
                System.out.println("Processing: " + frameType.toUpperCase());
                System.out.println("═══════════════════════════════════════");

                // Auto-detect components
                List<Component> components = detectComponentsForFrame(frameType);

                if (components.isEmpty()) {
                    System.out.println("  ⚠️  No components found");
                    continue;
                }

                // Group components by their group
                Map<String, List<Component>> groups = groupComponents(components);

                System.out.println("  ✓ Found " + components.size() + " components in " + groups.size() + " groups:");
                for (Map.Entry<String, List<Component>> entry : groups.entrySet()) {
                    System.out.println("    📦 " + entry.getKey() + ": " +
                            entry.getValue().stream().map(c -> c.name).toList());
                }
                System.out.println();

                // Load base frame
                BufferedImage baseFrame = loadTexture("frames/" + frameType + "_frame.png");
                if (baseFrame == null) {
                    System.err.println("  ❌ Failed to load base frame");
                    continue;
                }

                System.out.println("  ✓ Loaded base frame (" + baseFrame.getWidth() + "x" + baseFrame.getHeight() + ")");

                // Generate group-aware combinations
                GenerationResult result = generateGroupAwareCombinations(frameType, baseFrame, components, groups, MAX_COMPONENTS);
                totalGenerated += result.generated;
                totalSkipped += result.skipped;

                System.out.println("  ✓ Generated: " + result.generated);
                System.out.println("  ⊘ Skipped (group conflicts): " + result.skipped);
                System.out.println();
            }

            System.out.println("═══════════════════════════════════════");
            System.out.println("✅ COMPLETE!");
            System.out.println("═══════════════════════════════════════");
            System.out.println("✓ Textures generated: " + totalGenerated);
            System.out.println("⊘ Combinations skipped (group conflicts): " + totalSkipped);
            System.out.println("📁 Saved to: " + new File(OUTPUT_PATH).getAbsolutePath());
            System.out.println();

            printNamingConvention();

        } catch (IOException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Detect components and extract their groups from naming convention
     */
    private static List<Component> detectComponentsForFrame(String frameType) {
        List<Component> components = new ArrayList<>();

        File overlayDir = new File(TEXTURES_PATH + "components/overlay/" + frameType + "/");
        if (!overlayDir.exists()) return components;

        File[] files = overlayDir.listFiles((dir, name) -> name.endsWith(".png"));
        if (files == null) return components;

        for (File file : files) {
            String name = file.getName().replace(".png", "");
            String group = extractGroup(name);
            components.add(new Component(name, group));
        }

        Collections.sort(components, Comparator.comparing(c -> c.name));
        return components;
    }

    /**
     * Extract group from component name
     * Examples:
     *   iron_pauldrons → pauldrons
     *   gold_pauldrons → pauldrons
     *   steel_reinforcement → reinforcement
     *   visor → visor
     */
    private static String extractGroup(String componentName) {
        // Check if it has a material prefix (iron_, gold_, diamond_, steel_, etc.)
        String[] parts = componentName.split("_", 2);

        if (parts.length == 2) {
            // Has prefix - use the second part as group
            // iron_pauldrons → pauldrons
            // steel_reinforcement → reinforcement
            return parts[1];
        } else {
            // No prefix - entire name is the group
            // visor → visor
            return componentName;
        }
    }

    /**
     * Group components by their group name
     */
    private static Map<String, List<Component>> groupComponents(List<Component> components) {
        Map<String, List<Component>> groups = new HashMap<>();

        for (Component comp : components) {
            groups.computeIfAbsent(comp.group, k -> new ArrayList<>()).add(comp);
        }

        return groups;
    }

    /**
     * Check if a combination has group conflicts
     * Returns true if combination is VALID (no conflicts)
     */
    private static boolean isValidCombination(List<Component> combination) {
        Set<String> usedGroups = new HashSet<>();

        for (Component comp : combination) {
            if (usedGroups.contains(comp.group)) {
                return false; // Conflict! Two components from same group
            }
            usedGroups.add(comp.group);
        }

        return true; // No conflicts
    }

    /**
     * Generate combinations respecting group constraints
     */
    private static GenerationResult generateGroupAwareCombinations(String frameType, BufferedImage baseFrame,
                                                                   List<Component> components,
                                                                   Map<String, List<Component>> groups,
                                                                   int maxComponents) {
        int generated = 0;
        int skipped = 0;

        System.out.println("  Generating group-aware combinations...");

        for (int size = 1; size <= Math.min(maxComponents, components.size()); size++) {
            List<List<Component>> combinations = generateCombinationsOfSize(components, size);

            int validCount = 0;
            int invalidCount = 0;

            for (List<Component> combo : combinations) {
                if (!isValidCombination(combo)) {
                    skipped++;
                    invalidCount++;
                    continue; // Skip - has group conflict
                }

                if (generateComposite(frameType, baseFrame, combo)) {
                    generated++;
                    validCount++;
                }
            }

            System.out.println("    Size " + size + ": " + validCount + " valid, " + invalidCount + " skipped (conflicts)");
        }

        return new GenerationResult(generated, skipped);
    }

    private static List<List<Component>> generateCombinationsOfSize(List<Component> components, int size) {
        List<List<Component>> result = new ArrayList<>();
        generateCombinationsHelper(components, size, 0, new ArrayList<>(), result);
        return result;
    }

    private static void generateCombinationsHelper(List<Component> components, int size,
                                                   int start, List<Component> current,
                                                   List<List<Component>> result) {
        if (current.size() == size) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < components.size(); i++) {
            current.add(components.get(i));
            generateCombinationsHelper(components, size, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    private static boolean generateComposite(String frameType, BufferedImage baseFrame,
                                             List<Component> componentList) {
        try {
            BufferedImage composite = deepCopy(baseFrame);

            // Build filename (sorted for consistency)
            List<String> names = componentList.stream().map(c -> c.name).sorted().toList();

            StringBuilder filename = new StringBuilder(frameType + "_frame");
            for (String name : names) {
                filename.append("_").append(name);
            }
            filename.append(".png");

            // Overlay each component
            for (Component comp : componentList) {
                String overlayPath = "components/overlay/" + frameType + "/" + comp.name + ".png";
                BufferedImage componentImg = loadTexture(overlayPath);

                if (componentImg == null) {
                    return false;
                }

                overlayImage(composite, componentImg);
            }

            File outputFile = new File(OUTPUT_PATH + filename);
            ImageIO.write(composite, "PNG", outputFile);

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private static BufferedImage loadTexture(String relativePath) {
        try {
            File file = new File(TEXTURES_PATH + relativePath);
            return file.exists() ? ImageIO.read(file) : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    private static void overlayImage(BufferedImage base, BufferedImage overlay) {
        int width = Math.min(base.getWidth(), overlay.getWidth());
        int height = Math.min(base.getHeight(), overlay.getHeight());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int overlayPixel = overlay.getRGB(x, y);
                int overlayAlpha = (overlayPixel >> 24) & 0xFF;

                if (overlayAlpha > 0) {
                    int basePixel = base.getRGB(x, y);
                    int blended = blendPixels(basePixel, overlayPixel, overlayAlpha);
                    base.setRGB(x, y, blended);
                }
            }
        }
    }

    private static int blendPixels(int base, int overlay, int overlayAlpha) {
        if (overlayAlpha == 255) return overlay;

        float alpha = overlayAlpha / 255f;

        int baseA = (base >> 24) & 0xFF;
        int baseR = (base >> 16) & 0xFF;
        int baseG = (base >> 8) & 0xFF;
        int baseB = base & 0xFF;

        int overlayR = (overlay >> 16) & 0xFF;
        int overlayG = (overlay >> 8) & 0xFF;
        int overlayB = overlay & 0xFF;

        int r = (int) (overlayR * alpha + baseR * (1 - alpha));
        int g = (int) (overlayG * alpha + baseG * (1 - alpha));
        int b = (int) (overlayB * alpha + baseB * (1 - alpha));
        int a = Math.max(baseA, overlayAlpha);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void printDirectoryStructure() {
        System.out.println("\n📁 Directory Structure:");
        System.out.println("components/overlay/chestplate/");
        System.out.println("├── iron_pauldrons.png      ← Group: pauldrons");
        System.out.println("├── gold_pauldrons.png      ← Group: pauldrons");
        System.out.println("├── diamond_pauldrons.png   ← Group: pauldrons");
        System.out.println("├── iron_gorget.png         ← Group: gorget");
        System.out.println("├── gold_gorget.png         ← Group: gorget");
        System.out.println("└── iron_reinforcement.png ← Group: reinforcement");
    }

    private static void printNamingConvention() {
        System.out.println("📝 Naming Convention:");
        System.out.println("  Format: {material}_{group} or {group}");
        System.out.println();
        System.out.println("  ✓ iron_pauldrons, gold_pauldrons → Same group (pauldrons)");
        System.out.println("    Won't combine with each other");
        System.out.println();
        System.out.println("  ✓ iron_pauldrons, iron_gorget → Different groups");
        System.out.println("    CAN combine together");
        System.out.println();
        System.out.println("  ✓ visor, plume → Different groups (no material prefix)");
        System.out.println("    CAN combine together");
    }

    private static class Component {
        final String name;  // Full name: iron_pauldrons
        final String group; // Group name: pauldrons

        Component(String name, String group) {
            this.name = name;
            this.group = group;
        }
    }

    private static class GenerationResult {
        final int generated;
        final int skipped;

        GenerationResult(int generated, int skipped) {
            this.generated = generated;
            this.skipped = skipped;
        }
    }
}