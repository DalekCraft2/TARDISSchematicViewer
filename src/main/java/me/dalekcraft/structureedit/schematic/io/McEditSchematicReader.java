package me.dalekcraft.structureedit.schematic.io;

import me.dalekcraft.structureedit.exception.ValidationException;
import me.dalekcraft.structureedit.schematic.container.Block;
import me.dalekcraft.structureedit.schematic.container.BlockState;
import me.dalekcraft.structureedit.schematic.container.Entity;
import me.dalekcraft.structureedit.schematic.container.Schematic;
import me.dalekcraft.structureedit.schematic.io.legacycompat.LegacyMapper;
import net.querz.nbt.io.NBTInputStream;
import net.querz.nbt.tag.*;

import java.io.IOException;
import java.util.OptionalInt;

public class McEditSchematicReader extends NbtSchematicReader {

    private final NBTInputStream inputStream;

    public McEditSchematicReader(NBTInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public Schematic read() throws IOException, ValidationException {
        Schematic schematic = new Schematic();

        CompoundTag root = (CompoundTag) inputStream.readTag(Tag.DEFAULT_MAX_DEPTH).getTag();

        short sizeX = requireTag(root, "Width", ShortTag.class).asShort();
        short sizeY = requireTag(root, "Height", ShortTag.class).asShort();
        short sizeZ = requireTag(root, "Length", ShortTag.class).asShort();
        schematic.setSize(sizeX, sizeY, sizeZ);

        String materials = requireTag(root, "Materials", StringTag.class).getValue();
        if (!materials.equals("Classic") && !materials.equals("Pocket") && !materials.equals("Alpha")) {
            throw new ValidationException("Materials tag is not \"Classic\", \"Pocket\", or \"Alpha\"");
        }

        byte[] blockIds = requireTag(root, "Blocks", ByteArrayTag.class).getValue();
        byte[] addIds = new byte[0];
        byte[] blockData = requireTag(root, "Data", ByteArrayTag.class).getValue();
        short[] blocks = new short[blockIds.length]; // Have to later combine IDs

        // We support 4096 block IDs using the same method as vanilla Minecraft, where
        // the highest 4 bits are stored in a separate byte array.
        if (root.containsKey("AddBlocks")) {
            addIds = requireTag(root, "AddBlocks", ByteArrayTag.class).getValue();
        }

        // Combine the AddBlocks data with the first 8-bit block ID
        for (int index = 0; index < blockIds.length; index++) {
            if (index >> 1 >= addIds.length) { // No corresponding AddBlocks index
                blocks[index] = (short) (blockIds[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addIds[index >> 1] & 0x0F) << 8) + (blockIds[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addIds[index >> 1] & 0xF0) << 4) + (blockIds[index] & 0xFF));
                }
            }
        }

        for (int i = 0; i < blocks.length; i++) {
            // index = (y * length * width) + (z * width) + x
            int x = i % (sizeX * sizeZ) % sizeX;
            int y = i / (sizeX * sizeZ);
            int z = i % (sizeX * sizeZ) / sizeX;

            BlockState blockState = LegacyMapper.getInstance().getBlockFromLegacy(blocks[i], blockData[i]);
            if (!schematic.getBlockPalette().contains(blockState)) {
                schematic.getBlockPalette().add(blockState);
            } else {
                blockState = schematic.getBlockState(schematic.getBlockPalette().indexOf(blockState));
            }

            schematic.setBlock(x, y, z, new Block(schematic.getBlockPalette().indexOf(blockState)));
        }

        ListTag<?> entities = optTag(root, "Entities", ListTag.class);
        if (entities != null) {
            for (int i = 0; i < entities.size(); i++) {
                CompoundTag entity = requireTag(entities, i, CompoundTag.class);
                ListTag<?> position = requireTag(entity, "Pos", ListTag.class);
                double x = requireTag(position, 0, DoubleTag.class).asDouble();
                double y = requireTag(position, 1, DoubleTag.class).asDouble();
                double z = requireTag(position, 2, DoubleTag.class).asDouble();

                String id = requireTag(entity, "id", StringTag.class).getValue();

                Entity entityObject = new Entity();
                entityObject.setPosition(x, y, z);
                entityObject.setId(id);
                entityObject.setNbt(entity);

                schematic.getEntities().add(entityObject);
            }
        }

        ListTag<?> tileEntities = optTag(root, "TileEntities", ListTag.class);
        if (tileEntities != null) {
            for (int i = 0; i < tileEntities.size(); i++) {
                CompoundTag tileEntity = requireTag(tileEntities, i, CompoundTag.class);
                int x = requireTag(tileEntity, "x", IntTag.class).asInt();
                int y = requireTag(tileEntity, "y", IntTag.class).asInt();
                int z = requireTag(tileEntity, "z", IntTag.class).asInt();
                schematic.getBlock(x, y, z).setNbt(tileEntity);
            }
        }

        return schematic;
    }

    @Override
    public OptionalInt getDataVersion() {
        return super.getDataVersion();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
