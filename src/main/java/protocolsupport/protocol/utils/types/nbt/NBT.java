package protocolsupport.protocol.utils.types.nbt;

public abstract class NBT {

	public abstract NBTType<?> getType();

	@Override
	public abstract boolean equals(Object other);

	@Override
	public abstract int hashCode();

	@Override
	public abstract NBT clone();

	@Override
	public String toString() {
		return Utils.toStringAllFields(this).replaceAll("protocolsupport\\.protocol\\.utils\\.types\\.nbt\\.", "");
	}

}
