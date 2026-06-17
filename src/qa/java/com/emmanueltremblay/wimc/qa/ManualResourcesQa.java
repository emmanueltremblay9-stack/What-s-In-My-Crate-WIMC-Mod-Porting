package com.emmanueltremblay.wimc.qa;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ManualResourcesQa {
    private static final Set<String> WIMC_ENTRIES = Set.of(
            "crate_preview_overview",
            "supported_crates",
            "preview_settings"
    );
    private static final Map<String, Set<String>> REQUIRED_ANCHORS = Map.of(
            "crate_preview_overview", Set.of("crate_icons"),
            "supported_crates", Set.of("crate_recipe", "reinforced_crate_recipe", "crate_icons"),
            "preview_settings", Set.of("settings_icon")
    );
    private static final Set<String> ALLOWED_IE_LINKS = Set.of("storage_crates");
    private static final Set<String> ALLOWED_IE_RECIPES = Set.of(
            "immersiveengineering:crafting/crate",
            "immersiveengineering:crafting/reinforced_crate"
    );
    private static final Set<String> ALLOWED_SPECIAL_TYPES = Set.of("crafting", "item_display");
    private static final Pattern ANCHOR_PATTERN = Pattern.compile("<&([A-Za-z0-9_:\\-./]+)>");
    private static final Pattern LINK_PATTERN = Pattern.compile("<link;([^;>]+);([^;>]+)(?:;([^>]+))?>");

    private ManualResourcesQa() {
    }

    public static void main(String[] args) throws IOException {
        Path root = args.length > 0 ? Path.of(args[0]) : Path.of("").toAbsolutePath();
        Path resources = root.resolve("src/main/resources");

        validateAutoload(resources);
        validateLocalization(resources);
        for (String entry : WIMC_ENTRIES) {
            validateEntry(resources, entry);
        }
    }

    private static void validateAutoload(Path resources) throws IOException {
        JsonObject autoload = readJson(resources.resolve("assets/immersiveengineering/manual/autoload.json"));
        require(autoload.has("autoload_priority"), "manual autoload is missing autoload_priority");
        require(autoload.has("storage_transport"), "manual autoload must attach under IE storage_transport");
        require(autoload.size() == 2, "manual autoload should only define autoload_priority and storage_transport");

        JsonObject storage = autoload.getAsJsonObject("storage_transport");
        require(storage.has("wimc:factory_inventory_inspector"), "manual autoload missing WIMC subcategory");

        JsonObject category = storage.getAsJsonObject("wimc:factory_inventory_inspector");
        require(category.has("category_weight"), "manual category missing category_weight");
        JsonArray entries = category.getAsJsonArray("entry_list");
        require(entries != null, "manual category missing entry_list");
        require(entries.size() == WIMC_ENTRIES.size(), "manual category should list every WIMC entry exactly once");

        Set<String> seen = new HashSet<>();
        for (JsonElement element : entries) {
            JsonObject entry = element.getAsJsonObject();
            require(entry.has("source"), "manual entry list object missing source");
            require(entry.has("weight"), "manual entry list object missing weight");
            String source = entry.get("source").getAsString();
            require(source.startsWith("wimc:"), "manual entry source must be namespaced to wimc: " + source);
            String path = source.substring("wimc:".length());
            require(WIMC_ENTRIES.contains(path), "unknown WIMC manual entry in autoload: " + source);
            require(seen.add(path), "duplicate WIMC manual entry in autoload: " + source);
        }
        require(seen.equals(WIMC_ENTRIES), "manual autoload does not list the expected WIMC entries");
    }

    private static void validateLocalization(Path resources) throws IOException {
        JsonObject lang = readJson(resources.resolve("assets/wimc/lang/en_us.json"));
        require(
                lang.has("manual.wimc.factory_inventory_inspector"),
                "missing manual.wimc.factory_inventory_inspector localization"
        );
    }

    private static void validateEntry(Path resources, String entry) throws IOException {
        Path jsonPath = resources.resolve("assets/wimc/manual/" + entry + ".json");
        Path textPath = resources.resolve("assets/wimc/manual/en_us/" + entry + ".txt");
        JsonObject json = readJson(jsonPath);
        String text = Files.readString(textPath);

        validateTextShape(entry, text);
        validateAnchors(entry, json, text);
        validateLinks(entry, text);
        validateSpecials(entry, json);
    }

    private static void validateTextShape(String entry, String text) {
        require(!text.contains("`"), entry + " uses Markdown backticks, which IE manual text does not render as code");
        String[] lines = text.split("\\R", 3);
        require(lines.length == 3, entry + " must contain title, subtitle, and body lines");
        require(!lines[0].isBlank(), entry + " title is blank");
        require(!lines[1].isBlank(), entry + " subtitle is blank");
        require(!lines[2].isBlank(), entry + " body is blank");

        String[] pages = lines[2].split("<np>");
        for (String page : pages) {
            require(page.length() <= 750, entry + " has an oversized manual page segment");
        }
    }

    private static void validateAnchors(String entry, JsonObject json, String text) {
        Set<String> anchors = new HashSet<>();
        Matcher matcher = ANCHOR_PATTERN.matcher(text);
        while (matcher.find()) {
            anchors.add(matcher.group(1));
        }

        Set<String> required = REQUIRED_ANCHORS.get(entry);
        require(required != null, "no required anchor contract for " + entry);
        for (String anchor : required) {
            require(json.has(anchor), entry + " JSON is missing special anchor " + anchor);
            require(anchors.contains(anchor), entry + " text is missing anchor marker <&" + anchor + ">");
        }
    }

    private static void validateLinks(String entry, String text) {
        Matcher matcher = LINK_PATTERN.matcher(text);
        while (matcher.find()) {
            String target = matcher.group(1);
            if (target.startsWith("wimc:")) {
                String path = target.substring("wimc:".length());
                require(WIMC_ENTRIES.contains(path), entry + " links to unknown WIMC manual entry " + target);
            } else {
                require(ALLOWED_IE_LINKS.contains(target), entry + " links to unexpected IE manual entry " + target);
            }
        }
    }

    private static void validateSpecials(String entry, JsonObject json) {
        for (Map.Entry<String, JsonElement> special : json.entrySet()) {
            require(special.getValue().isJsonObject(), entry + " special " + special.getKey() + " must be an object");
            JsonObject specialObject = special.getValue().getAsJsonObject();
            String type = requiredString(specialObject, "type", entry + " special " + special.getKey());
            require(ALLOWED_SPECIAL_TYPES.contains(type), entry + " special " + special.getKey() + " has unexpected type " + type);

            if ("crafting".equals(type)) {
                String recipe = requiredString(specialObject, "recipe", entry + " crafting special " + special.getKey());
                require(recipe.contains(":"), entry + " recipe reference must be namespaced: " + recipe);
                require(ALLOWED_IE_RECIPES.contains(recipe), entry + " references unexpected IE recipe " + recipe);
            } else if ("item_display".equals(type)) {
                validateItemDisplay(entry, special.getKey(), specialObject);
            }
        }
    }

    private static void validateItemDisplay(String entry, String anchor, JsonObject specialObject) {
        if (specialObject.has("id")) {
            String id = specialObject.get("id").getAsString();
            require(id.contains(":"), entry + " item_display " + anchor + " id must be namespaced");
            return;
        }

        JsonArray items = specialObject.getAsJsonArray("items");
        require(items != null && !items.isEmpty(), entry + " item_display " + anchor + " must list at least one item");
        for (JsonElement item : items) {
            require(item.isJsonPrimitive() && item.getAsJsonPrimitive().isString(), entry + " item_display " + anchor + " item must be a string");
            String id = item.getAsString();
            require(id.contains(":"), entry + " item_display " + anchor + " item must be namespaced: " + id);
        }
    }

    private static JsonObject readJson(Path path) throws IOException {
        require(Files.isRegularFile(path), "missing JSON file: " + path);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String requiredString(JsonObject json, String key, String context) {
        require(json.has(key), context + " is missing " + key);
        require(json.get(key).isJsonPrimitive() && json.get(key).getAsJsonPrimitive().isString(), context + " " + key + " must be a string");
        return json.get(key).getAsString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
