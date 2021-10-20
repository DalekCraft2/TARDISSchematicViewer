package me.dalekcraft.structureedit.schematic;

import me.dalekcraft.structureedit.exception.ValidationException;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class McEditSchematic implements Schematic {

    public static final String EXTENSION = "schematic";
    private final NamedTag schematic;
    private final CompoundTag root;


    public McEditSchematic(@NotNull NamedTag schematic) throws ValidationException, UnsupportedOperationException {
        if (true) {
            throw new UnsupportedOperationException("MCEdit schematics are not yet supported!");
        }
        this.schematic = schematic;
        if (!(schematic.getTag() instanceof CompoundTag compoundTag)) {
            throw new ValidationException("Root tag is not an instance of " + CompoundTag.class.getSimpleName());
        }
        root = compoundTag;
        validate();
    }

    @Override
    public void validate() throws ValidationException {

    }

    @Override
    public void saveTo(File file) throws IOException {
        NBTUtil.write(schematic, file);
    }

    @Contract(pure = true)
    @Override
    public Object getData() {
        return schematic;
    }

    @Contract(pure = true)
    @Override
    public String getFormat() {
        return EXTENSION;
    }

    @Override
    public int @NotNull [] getSize() {
        return new int[]{root.getShort("Width"), root.getShort("Height"), root.getShort("Length")};
    }

    @Override
    public void setSize(int sizeX, int sizeY, int sizeZ) {
        CompoundTag tag = (CompoundTag) schematic.getTag();
        tag.putShort("Width", (short) sizeX);
        tag.putShort("Height", (short) sizeY);
        tag.putShort("Length", (short) sizeZ);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return null;
    }

    @Override
    public void setBlock(int x, int y, int z, Block block) {

    }
}
