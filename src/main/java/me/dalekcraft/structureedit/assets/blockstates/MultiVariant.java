/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package me.dalekcraft.structureedit.assets.blockstates;

import com.google.common.collect.Lists;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MultiVariant {
    private final List<Variant> variants;

    public MultiVariant(List<Variant> list) {
        variants = list;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MultiVariant multiVariant) {
            return variants.equals(multiVariant.variants);
        }
        return false;
    }

    public int hashCode() {
        return variants.hashCode();
    }

    public static class Deserializer implements JsonDeserializer<MultiVariant> {
        @Override
        public MultiVariant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ArrayList<Variant> arrayList = Lists.newArrayList();
            if (json.isJsonArray()) {
                JsonArray jsonArray = json.getAsJsonArray();
                if (jsonArray.isEmpty()) {
                    throw new JsonParseException("Empty variant array");
                }
                for (JsonElement jsonElement2 : jsonArray) {
                    arrayList.add(context.deserialize(jsonElement2, Variant.class));
                }
            } else {
                arrayList.add(context.deserialize(json, Variant.class));
            }
            return new MultiVariant(arrayList);
        }
    }
}

