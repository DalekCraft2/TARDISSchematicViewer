package me.dalekcraft.structureedit.schematic.io;

import com.google.common.primitives.Bytes;
import me.dalekcraft.structureedit.schematic.container.*;
import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SpongeSchematicWriter extends NbtSchematicWriter {

    private final NBTOutputStream outputStream;

    public SpongeSchematicWriter(NBTOutputStream outputStream) {
        this.outputStream = Objects.requireNonNull(outputStream);
    }

    @Override
    public void write(Schematic schematic) throws IOException {
        // TODO How to determine which Sponge schematic version to use?
        writeV3(schematic);
    }

    private void writeV2(Schematic schematic) throws IOException {
        CompoundTag root = new CompoundTag();

        root.putInt("Version", 2);

        root.putInt("DataVersion", schematic.getDataVersion());

        CompoundTag metadata = schematic.getMetadata();
        if (metadata != null) {
            root.put("Metadata", metadata);
        }

        int[] size = schematic.getSize();
        root.putShort("Width", (short) size[0]);
        root.putShort("Height", (short) size[1]);
        root.putShort("Length", (short) size[2]);

        int[] offset = schematic.getOffset();
        if (offset[0] != 0 || offset[1] != 0 || offset[2] != 0) {
            root.putIntArray("Offset", offset);
        }

        // TODO Blocks, biomes, and entities.

        NamedTag namedTag = new NamedTag("Schematic", root);

        outputStream.writeTag(namedTag, Tag.DEFAULT_MAX_DEPTH);
    }

    private void writeV3(Schematic schematic) throws IOException {
        CompoundTag realRoot = new CompoundTag();
        CompoundTag root = new CompoundTag();
        realRoot.put("Schematic", root);

        root.putInt("Version", 3);

        root.putInt("DataVersion", schematic.getDataVersion());

        CompoundTag metadata = schematic.getMetadata();
        if (metadata != null) {
            root.put("Metadata", metadata);
        }

        int[] size = schematic.getSize();
        root.putShort("Width", (short) size[0]);
        root.putShort("Height", (short) size[1]);
        root.putShort("Length", (short) size[2]);

        int[] offset = schematic.getOffset();
        if (offset[0] != 0 || offset[1] != 0 || offset[2] != 0) {
            root.putIntArray("Offset", offset);
        }

        // TODO Blocks, biomes, and entities.
        CompoundTag blockContainer = new CompoundTag();

        List<BlockState> blockPalette = schematic.getBlockPalette();

        CompoundTag blockPaletteTag = new CompoundTag();

        for (int i = 0; i < blockPalette.size(); i++) {
            BlockState blockState = blockPalette.get(i);
            String properties = blockState.getProperties().isEmpty() ? "" : "[" + BlockState.JOINER.join(blockState.getProperties()) + "]";

            blockPaletteTag.putInt(blockState.getId() + properties, i);
        }

        blockContainer.put("Palette", blockPaletteTag);

        ListTag<CompoundTag> blockEntitiesTag = new ListTag<>(CompoundTag.class);
        List<Byte> blocksList = Arrays.asList(new Byte[size[0] * size[1] * size[2]]);
        for (int y = 0; y < size[1]; y++) {
            for (int z = 0; z < size[2]; z++) {
                for (int x = 0; x < size[0]; x++) {
                    int index = y * size[2] * size[0] + z * size[0] + x;
                    Block block = schematic.getBlock(x, y, z);
                    if (block != null) {
                        blocksList.set(index, (byte) block.getBlockStateIndex());

                        CompoundTag nbt = block.getNbt();
                        if (nbt.size() > 0) {
                            CompoundTag blockEntityTag = new CompoundTag();

                            String id = nbt.getString("id");
                            nbt.remove("id");
                            blockEntityTag.putString("Id", id);

                            blockEntityTag.putIntArray("Pos", new int[]{x, y, z});

                            blockEntityTag.put("Data", nbt);

                            blockEntitiesTag.add(blockEntityTag);
                        }
                    } else {
                        BlockState blockState = new BlockState("minecraft:air");
                        int airIndex;
                        if (!blockPalette.contains(blockState)) {
                            blockPalette.add(blockState);
                            blockPaletteTag.putInt("minecraft:air", blockPalette.indexOf(blockState));
                        }
                        airIndex = schematic.getBlockPalette().indexOf(blockState);
                        blocksList.set(index, (byte) airIndex);
                    }
                }
            }
        }

        byte[] blocks = Bytes.toArray(blocksList);
        blockContainer.putByteArray("Data", blocks);
        if (blockEntitiesTag.size() > 0) {
            blockContainer.put("BlockEntities", blockEntitiesTag);
        }

        root.put("Blocks", blockContainer);

        if (schematic.hasBiomes()) {
            CompoundTag biomeContainer = new CompoundTag();

            List<BiomeState> biomePalette = schematic.getBiomePalette();

            CompoundTag biomePaletteTag = new CompoundTag();

            for (int i = 0; i < biomePalette.size(); i++) {
                BiomeState biomeState = biomePalette.get(i);

                biomePaletteTag.putInt(biomeState.getId(), i);
            }

            biomeContainer.put("Palette", biomePaletteTag);

            List<Byte> biomesList = Arrays.asList(new Byte[size[0] * size[1] * size[2]]);
            for (int y = 0; y < size[1]; y++) {
                for (int z = 0; z < size[2]; z++) {
                    for (int x = 0; x < size[0]; x++) {
                        int index = y * size[2] * size[0] + z * size[0] + x;
                        Biome biome = schematic.getBiome(x, y, z);
                        if (biome != null) {
                            biomesList.set(index, (byte) biomePalette.indexOf(biome.getBiomeState()));
                        } else {
                            // TODO I'unno. Probably throw an exception.
                        }
                    }
                }
            }

            byte[] biomes = Bytes.toArray(biomesList);
            biomeContainer.putByteArray("Data", biomes);

            root.put("Biomes", biomeContainer);
        }

        NamedTag namedTag = new NamedTag("", realRoot);

        outputStream.writeTag(namedTag, Tag.DEFAULT_MAX_DEPTH);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
