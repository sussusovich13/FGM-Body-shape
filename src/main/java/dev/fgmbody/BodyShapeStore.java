package dev.fgmbody;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.fml.loading.FMLPaths;

public final class BodyShapeStore {
    /** Shapes known to the client (including our own). */
    public static final Map<UUID, BodyShape> CLIENT = new ConcurrentHashMap<>();
    /** Shapes known to the (dedicated or integrated) server. */
    public static final Map<UUID, BodyShape> SERVER = new ConcurrentHashMap<>();

    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("fgmbody.json");
    private static BodyShape local;

    private BodyShapeStore() {}

    public static BodyShape local() {
        if (local == null) local = load();
        return local;
    }

    public static void setLocal(BodyShape shape) {
        local = shape.clamped();
        save(local);
    }

    private static BodyShape load() {
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                return new BodyShape(
                        o.get("glutesSize").getAsFloat(),
                        o.get("glutesHeight").getAsFloat(),
                        o.get("hipsWidth").getAsFloat(),
                        o.has("waist") ? o.get("waist").getAsFloat() : 0f).clamped();
            }
        } catch (Exception ignored) {}
        return BodyShape.DEFAULT;
    }

    private static void save(BodyShape s) {
        JsonObject o = new JsonObject();
        o.addProperty("glutesSize", s.glutesSize());
        o.addProperty("glutesHeight", s.glutesHeight());
        o.addProperty("hipsWidth", s.hipsWidth());
        o.addProperty("waist", s.waist());
        try {
            Files.writeString(FILE, o.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
