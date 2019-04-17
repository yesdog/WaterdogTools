package protocolsupport.protocol.utils.types.nbt.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.md_5.bungee.protocol.DefinedPacket;
import protocolsupport.protocol.utils.types.nbt.NBT;
import protocolsupport.protocol.utils.types.nbt.NBTByte;
import protocolsupport.protocol.utils.types.nbt.NBTByteArray;
import protocolsupport.protocol.utils.types.nbt.NBTCompound;
import protocolsupport.protocol.utils.types.nbt.NBTDouble;
import protocolsupport.protocol.utils.types.nbt.NBTEnd;
import protocolsupport.protocol.utils.types.nbt.NBTFloat;
import protocolsupport.protocol.utils.types.nbt.NBTInt;
import protocolsupport.protocol.utils.types.nbt.NBTIntArray;
import protocolsupport.protocol.utils.types.nbt.NBTList;
import protocolsupport.protocol.utils.types.nbt.NBTLong;
import protocolsupport.protocol.utils.types.nbt.NBTShort;
import protocolsupport.protocol.utils.types.nbt.NBTString;
import protocolsupport.protocol.utils.types.nbt.NBTType;
import protocolsupport.protocol.utils.types.nbt.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

public class PENBTSerializer extends NBTSerializer<ByteBuf, ByteBuf> {

	public static final PENBTSerializer VI_INSTANCE = new PENBTSerializer(true);
	public static final PENBTSerializer LE_INSTANCE = new PENBTSerializer(false);

	@SuppressWarnings("unchecked")
	public PENBTSerializer(boolean varint) {
		super(ByteBuf::readByte, ByteBuf::writeByte, getPEStringReader(varint), getPEStringWriter(varint));
		registerType(NBTType.END, 0, stream -> NBTEnd.INSTANCE, (stream, tag) -> {});
		registerType(NBTType.BYTE, 1, stream -> new NBTByte(stream.readByte()), (stream, tag) -> stream.writeByte(tag.getAsByte()));
		registerType(NBTType.SHORT, 2, stream -> new NBTShort(stream.readShortLE()), (stream, tag) -> stream.writeShortLE(tag.getAsShort()));
		registerType(NBTType.FLOAT, 5, stream -> new NBTFloat(stream.readFloatLE()), (stream, tag) -> stream.writeFloatLE(tag.getAsFloat()));
		registerType(NBTType.DOUBLE, 6, stream -> new NBTDouble(stream.readDoubleLE()), (stream, tag) -> stream.writeDoubleLE(tag.getAsDouble()));
		registerType(
			NBTType.COMPOUND, 10,
			stream -> {
				NBTCompound compound = new NBTCompound();
				NBTType<?> valueType = null;
				while ((valueType = readTagType(stream)) != NBTType.END) {
					compound.setTag(readTagName(stream), readTag(stream, valueType));
				}
				return compound;
			},
			(stream, tag) -> {
				for (Entry<String, NBT> entry : tag.getTags().entrySet()) {
					NBT value = entry.getValue();
					writeTagType(stream, value.getType());
					writeTagName(stream, entry.getKey());
					writeTag(stream, value);
				}
				writeTagType(stream, NBTType.END);
			}
		);
		// Pe has two similar but different formats.
		if (varint) {
			registerType(NBTType.INT, 3, stream -> new NBTInt(DefinedPacket.readSVarInt(stream)), (stream, tag) -> DefinedPacket.writeSVarInt(stream, tag.getAsInt()));
			registerType(NBTType.LONG, 4, stream -> new NBTLong(DefinedPacket.readSVarLong(stream)), (stream, tag) -> DefinedPacket.writeSVarLong(stream, tag.getAsLong()));
			registerType(
				NBTType.STRING, 8,
				stream -> {
					return new NBTString(new String(Utils.readBytes(stream, DefinedPacket.readVarInt(stream)), StandardCharsets.UTF_8));
				},
				(stream, tag) -> {
					byte[] data = tag.getValue().getBytes(StandardCharsets.UTF_8);
					DefinedPacket.writeVarInt(stream, data.length);
					stream.writeBytes(data);
				});
			registerType(
				NBTType.BYTE_ARRAY, 7,
				stream -> {
					byte[] array = new byte[DefinedPacket.readSVarInt(stream)];
					for (int i = 0; i < array.length; i++) {
						array[i] = stream.readByte();
					}
					return new NBTByteArray(array);
				},
				(stream, tag) -> {
					byte[] array = tag.getValue();
					DefinedPacket.writeSVarInt(stream, array.length);
					stream.writeBytes(array);
				}
			);
			registerType(
				NBTType.INT_ARRAY, 11,
				stream -> {
					int[] array = new int[DefinedPacket.readSVarInt(stream)];
					for (int i = 0; i < array.length; i++) {
						array[i] = DefinedPacket.readSVarInt(stream);
					}
					return new NBTIntArray(array);
				},
				(stream, tag) -> {
					int[] array = tag.getValue();
					DefinedPacket.writeSVarInt(stream, array.length);
					for (int i : array) {
						DefinedPacket.writeSVarInt(stream, i);
					}
				}
			);
			registerType(
				NBTType.LIST, 9,
				stream -> {
					NBTType<? extends NBT> valueType = readTagType(stream);
					int size = DefinedPacket.readSVarInt(stream);
					if ((valueType == NBTType.END) && (size > 0)) {
						throw new DecoderException("Missing nbt list values tag type");
					}
					NBTList<NBT> list = new NBTList<>((NBTType<NBT>) valueType);
					for (int i = 0; i < size; i++) {
						list.addTag(readTag(stream, valueType));
					}
					return list;
				},
				(stream, tag) -> {
					writeTagType(stream, tag.getTagsType());
					DefinedPacket.writeSVarInt(stream, tag.size());
					for (NBT value : ((List<NBT>) tag.getTags())) {
						writeTag(stream, value);
					}
				}
			);
		} else {
			registerType(NBTType.INT, 3, stream -> new NBTInt(stream.readIntLE()), (stream, tag) -> stream.writeIntLE(tag.getAsInt()));
			registerType(NBTType.LONG, 4, stream -> new NBTLong(stream.readLongLE()), (stream, tag) -> stream.writeLongLE(tag.getAsLong()));
			registerType(
				NBTType.STRING, 8,
				stream -> {
					return new NBTString(new String(Utils.readBytes(stream, stream.readShortLE()), StandardCharsets.UTF_8));
				},
				(stream, tag) -> {
					byte[] data = tag.getValue().getBytes(StandardCharsets.UTF_8);
					stream.writeShortLE(data.length);
					stream.writeBytes(data);
				});
			registerType(
				NBTType.BYTE_ARRAY, 7,
				stream -> {
					byte[] array = new byte[stream.readIntLE()];
					for (int i = 0; i < array.length; i++) {
						array[i] = stream.readByte();
					}
					return new NBTByteArray(array);
				},
				(stream, tag) -> {
					byte[] array = tag.getValue();
					stream.writeIntLE(array.length);
					stream.writeBytes(array);
				}
			);
			registerType(
				NBTType.INT_ARRAY, 11,
				stream -> {
					int[] array = new int[stream.readIntLE()];
					for (int i = 0; i < array.length; i++) {
						array[i] = stream.readIntLE();
					}
					return new NBTIntArray(array);
				},
				(stream, tag) -> {
					int[] array = tag.getValue();
					stream.writeIntLE(array.length);
					for (int i : array) {
						stream.writeIntLE(i);
					}
				}
			);
			registerType(
				NBTType.LIST, 9,
				stream -> {
					NBTType<? extends NBT> valueType = readTagType(stream);
					int size = stream.readIntLE();
					if ((valueType == NBTType.END) && (size > 0)) {
						throw new DecoderException("Missing nbt list values tag type");
					}
					NBTList<NBT> list = new NBTList<>((NBTType<NBT>) valueType);
					for (int i = 0; i < size; i++) {
						list.addTag(readTag(stream, valueType));
					}
					return list;
				},
				(stream, tag) -> {
					writeTagType(stream, tag.getTagsType());
					stream.writeIntLE(tag.size());
					for (NBT value : ((List<NBT>) tag.getTags())) {
						writeTag(stream, value);
					}
				}
			);
		}
	}

	private static NameReader<ByteBuf> getPEStringReader(boolean varint) {
		return (stream) -> {
			int length = 0;
			if (varint) {
				length = DefinedPacket.readVarInt(stream);
			} else {
				length = stream.readShortLE();
			}
			return new String(Utils.readBytes(stream, length), StandardCharsets.UTF_8);
		};
	}

	private static NameWriter<ByteBuf> getPEStringWriter(boolean varint) {
		return (stream, string) -> {
			byte[] data = string.getBytes(StandardCharsets.UTF_8);
			if (varint) {
				DefinedPacket.writeVarInt(stream, data.length);
			} else {
				stream.writeShortLE(data.length);
			}
			stream.writeBytes(data);
		};
	}

}