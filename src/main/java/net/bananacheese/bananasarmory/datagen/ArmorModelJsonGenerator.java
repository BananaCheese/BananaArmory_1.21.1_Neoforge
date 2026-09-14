package net.bananacheese.bananasarmory.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Group-aware auto-detecting model JSON generator
 *
 * Automatically scans overlay folders to find components
 * Respects component groups (won't combine iron_pauldrons + gold_pauldrons)
 * Matches the naming scheme from ArmorTextureGenerator_GROUP_AWARE
 */
public class ArmorModelJsonGenerator {

    private static String findBasePath() {
        String[] possiblePaths = {
                "src/main/resources/assets/barmory/",
                "./src/main/resources/assets/barmory/",
                "../src/main/resources/assets/barmory/"
        };

        for (String path : possiblePaths) {
            if (Files.exists(Paths.get(path))) {
                System.out.println("✓ Using base path: " + Paths.get(path).toAbsolutePath());
                return path;
            }
        }

        return "src/main/resources/assets/barmory/";
    }

    private static final String BASE_PATH = findBasePath();
    private static final String ITEMS_PATH = BASE_PATH + "items/";
    private static final String MODELS_PATH = BASE_PATH + "models/item/";
    private static final String TEXTURES_PATH = BASE_PATH + "textures/item/armor/";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<String> FRAME_TYPES = List.of("helmet", "chestplate", "leggings", "boots");
    private static final int MAX_COMPONENTS = 6;

    public static void main(String[] args) {
        System.out.println("Starting GROUP-AWARE model JSON generation...");
        System.out.println("(Auto-detects components, respects groups)");
        System.out.println();

        try {
            Files.createDirectories(Paths.get(ITEMS_PATH));
            Files.createDirectories(Paths.get(MODELS_PATH));

            int totalModels = 0;
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

                // Group components
                Map<String, List<Component>> groups = groupComponents(components);

                System.out.println("  ✓ Found " + components.size() + " components in " + groups.size() + " groups:");
                for (Map.Entry<String, List<Component>> entry : groups.entrySet()) {
                    System.out.println("    📦 " + entry.getKey() + ": " +
                            entry.getValue().stream().map(c -> c.name).toList());
                }
                System.out.println();

                // Generate models
                GenerationResult result = generateFrameModel(frameType, components, groups);
                totalModels += result.generated;
                totalSkipped += result.skipped;

                System.out.println("  ✓ Generated: " + result.generated + " variant models");
                System.out.println("  ⊘ Skipped (group conflicts): " + result.skipped);
                System.out.println();
            }

            System.out.println("═══════════════════════════════════════");
            System.out.println("✅ COMPLETE!");
            System.out.println("═══════════════════════════════════════");
            System.out.println("✓ Models generated: " + totalModels);
            System.out.println("⊘ Skipped (group conflicts): " + totalSkipped);
            System.out.println("📁 Main frames: " + Paths.get(ITEMS_PATH).toAbsolutePath());
            System.out.println("📁 Variants: " + Paths.get(MODELS_PATH).toAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Auto-detect components from overlay folder
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
     * Extract group from component name (matches texture generator logic)
     */
    private static String extractGroup(String componentName) {
        String[] parts = componentName.split("_", 2);

        if (parts.length == 2) {
            return parts[1]; // iron_pauldrons → pauldrons
        } else {
            return componentName; // visor → visor
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
     * Check if combination is valid (no group conflicts)
     */
    private static boolean isValidCombination(List<Component> combination) {
        Set<String> usedGroups = new HashSet<>();

        for (Component comp : combination) {
            if (usedGroups.contains(comp.group)) {
                return false;
            }
            usedGroups.add(comp.group);
        }

        return true;
    }

    /**
     * Generate all models for a frame type
     */
    private static GenerationResult generateFrameModel(String frameType, List<Component> components,
                                                       Map<String, List<Component>> groups) throws IOException {

        // Build list of all valid combinations
        List<CombinationEntry> entries = new ArrayList<>();
        int skipped = 0;

        System.out.println("  Generating model entries...");

        for (int size = 1; size <= Math.min(MAX_COMPONENTS, components.size()); size++) {
            List<List<Component>> combinations = generateCombinationsOfSize(components, size);

            int validCount = 0;
            int invalidCount = 0;

            for (List<Component> combo : combinations) {
                if (!isValidCombination(combo)) {
                    skipped++;
                    invalidCount++;
                    continue;
                }

                entries.add(new CombinationEntry(
                        combo,
                        calculatePredicateValue(combo)
                ));
                validCount++;
            }

            System.out.println("    Size " + size + ": " + validCount + " valid, " + invalidCount + " skipped");
        }

        // Sort by threshold
        entries.sort(Comparator.comparingInt(e -> e.threshold));

        // Old-style (pre item-model-definitions) override system: a single
        // models/item/<frame>_frame.json with a base texture + "overrides"
        // list matched by an int-valued "custom_model_data" predicate.
        // (Earlier this wrote a "minecraft:range_dispatch" file into
        // items/<frame>_frame.json — that format/location is only read on
        // MC versions newer than 1.21.1, so those files were silently
        // ignored by the game and every armor frame showed a missing
        // texture in the inventory. This format is what 1.21.1 actually
        // reads.)
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "barmory:item/armor/frames/" + frameType + "_frame");
        model.add("textures", textures);

        JsonArray overrides = new JsonArray();
        for (CombinationEntry entry : entries) {
            // Build model name (sorted alphabetically like texture generator does)
            List<String> names = entry.components.stream().map(c -> c.name).sorted().toList();
            String modelName = frameType + "_frame_" + String.join("_", names);

            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", entry.threshold);

            JsonObject overrideObj = new JsonObject();
            overrideObj.add("predicate", predicate);
            overrideObj.addProperty("model", "barmory:item/" + modelName);
            overrides.add(overrideObj);

            // Generate variant model
            generateVariantModel(frameType, modelName);
        }
        model.add("overrides", overrides);

        // Generate base model (still used as the layer0 texture fallback
        // above, and kept as its own file for anything that references it
        // directly, e.g. tooltips/previews)
        generateBaseModel(frameType);

        // Write main model to models/item/ (NOT items/ — see note above)
        String filename = frameType + "_frame.json";
        try (FileWriter writer = new FileWriter(MODELS_PATH + filename)) {
            GSON.toJson(model, writer);
        }

        return new GenerationResult(entries.size() + 1, skipped); // +1 for base
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

    /**
     * Generate base model in models/item/
     */
    private static void generateBaseModel(String frameType) throws IOException {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "barmory:item/armor/frames/" + frameType + "_frame");
        model.add("textures", textures);

        String filename = frameType + "_frame_base.json";
        try (FileWriter writer = new FileWriter(MODELS_PATH + filename)) {
            GSON.toJson(model, writer);
        }
    }

    /**
     * Generate variant model in models/item/
     */
    private static void generateVariantModel(String frameType, String modelName) throws IOException {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "barmory:item/armor/generated/" + modelName);
        model.add("textures", textures);

        String filename = modelName + ".json";
        try (FileWriter writer = new FileWriter(MODELS_PATH + filename)) {
            GSON.toJson(model, writer);
        }
    }

    /**
     * Calculates a unique value based on attached components. Matches
     * ArmorComponentsProperty.calculatePredicateValue exactly (int, not the
     * old 0.0–1.0 float) — these two MUST stay in sync, since one writes
     * the override predicates into the model JSON and the other sets the
     * matching value on the item stack at runtime.
     */
    private static int calculatePredicateValue(List<Component> components) {
        // Sort component names
        List<String> names = components.stream().map(c -> c.name).sorted().toList();

        String combined = String.join("|", names);
        int hash = combined.hashCode();
        int value = Math.abs(hash) % 9999;

        return Math.max(value, 1);
    }

    private static class Component {
        final String name;
        final String group;

        Component(String name, String group) {
            this.name = name;
            this.group = group;
        }
    }

    private static class CombinationEntry {
        final List<Component> components;
        final int threshold;

        CombinationEntry(List<Component> components, int threshold) {
            this.components = components;
            this.threshold = threshold;
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