package io.arona74.aronalayersextras;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public final class Compat {
    private Compat() {}

    public static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static String modelIdNamespace(ModelResourceLocation id) {
        return id.getNamespace();
    }

    public static String modelIdPath(ModelResourceLocation id) {
        return id.getPath();
    }
}
