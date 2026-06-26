package io.arona74.aronalayersextras;

import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

public final class Compat {
    private Compat() {}

    public static Identifier id(String namespace, String path) {
        return Identifier.of(namespace, path);
    }

    public static String modelIdNamespace(ModelIdentifier id) {
        return id.id().getNamespace();
    }

    public static String modelIdPath(ModelIdentifier id) {
        return id.id().getPath();
    }
}
