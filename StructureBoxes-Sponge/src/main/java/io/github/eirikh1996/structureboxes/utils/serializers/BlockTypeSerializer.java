package io.github.eirikh1996.structureboxes.utils.serializers;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Optional;

public class BlockTypeSerializer implements TypeSerializer<BlockType> {
    @Override
    public BlockType deserialize(Type type, ConfigurationNode node) throws SerializationException {
        final Optional<RegistryEntry<BlockType>> entryOptional;
        final String nodeName = node.getString();
        try {
            entryOptional = BlockTypes.registry().findEntry(ResourceKey.minecraft(nodeName));
        } catch (Throwable t) {
            throw new SerializationException(node, type, t);
        }
        if (entryOptional.isEmpty()) {
            throw new SerializationException("Invalid block type: " + nodeName);
        }
        final RegistryEntry<BlockType> entry = entryOptional.get();
        return entry.value();
    }

    @Override
    public void serialize(Type type, @Nullable BlockType obj, ConfigurationNode node) throws SerializationException {
        try {
            node.set(obj.get(Keys.ITEM_NAME));
        } catch (Throwable t) {
            throw new SerializationException(node, type, t);
        }

    }
}
