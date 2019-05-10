const net = Packages.net;
const com = Packages.com;
const io = Packages.io;

const DefinedPacket = net.md_5.bungee.protocol.DefinedPacket;
const ByteBufUtil = io.netty.buffer.ByteBufUtil;

var isPEUser;
var user;

function init(user0) {
	user = user0;
	isPEUser = !user0 || user.getPendingConnection().getVersion() < -1;
}

function read(buf) { //clientbound
	if(!isPEUser || !buf.isReadable()) return;
	const packetId = DefinedPacket.readVarInt(buf);
	const packetName = reversePacketIdMap[packetId] || "unknown";

	const playerId = user.getServerEntityId();

	switch(packetId) {
		case CHAT:
			//print(ByteBufUtil.prettyHexDump(buf));
			break;
		case SET_ATTRIBUTES:
			var entityId = DefinedPacket.readVarLong(buf);
			var nEntries = DefinedPacket.readVarInt(buf);

			for(var i = 0 ; i < nEntries ; i++) {
				buf.readFloatLE(); //min
				buf.readFloatLE(); //max
				const value = buf.readFloatLE();
				buf.readFloatLE(); //default
				const name = DefinedPacket.readString(buf);

				if(playerId == entityId && name.contains("hunger")) {
					print("name: " + name + " value: " + value);
				}
			}
			break;
		case ADD_ITEM_ENTITY:
			DefinedPacket.readSVarLong(buf);
			DefinedPacket.readVarLong(buf);
			printPEItemStack(buf);
			break;
	}
}

function write(buf) { //serverbound
	if(!isPEUser || !buf.isReadable()) return;
	const packetId = DefinedPacket.readVarInt(buf);
	const packetName = reversePacketIdMap[packetId] || "unknown";
}


// Packet IDs, utils, etc

/*
		register(DataWatcherObjectByte.class, 0, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectShortLe.class, 1, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectVarInt.class, 2, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectSVarInt.class, 2, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectFloatLe.class, 3, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectString.class, 4, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectItemStack.class, 5, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectVector3vi.class, 6, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectVarLong.class, 7, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectSVarLong.class, 7, ProtocolVersionsHelper.ALL_PE);
		register(DataWatcherObjectVector3fLe.class, 8, ProtocolVersionsHelper.ALL_PE);
*/

function printPEItemStack(from) {
	const MojangsonSerializer = PluginClass("protocolsupport.protocol.utils.types.nbt.mojangson.MojangsonSerializer").static;

	const type = DefinedPacket.readSVarInt(from);
	if (!type) {
		print("AIR");
		return;
	};

	const amountdata = DefinedPacket.readSVarInt(from);
	const amount = amountdata & 0x7F;
	const data = (amountdata >> 8) & 0x7FFF;
	var nbt = readPETag(from, false);

	if(nbt) nbt = MojangsonSerializer["serialize(NBTCompound)"](nbt);

	print(type+":"+data+" " + nbt);

	//TODO: CanPlaceOn PE
	var n = DefinedPacket.readSVarInt(from);
	for (var i = 0; i < n ; i++) {
		DefinedPacket.readString(from);
	}
	//TODO: CanDestroy PE
	n = DefinedPacket.readSVarInt(from);
	for (var i = 0; i < n ; i++) {
		DefinedPacket.readString(from);
	}
}

function readPETag(from, varint) {
	const PENBTSerializer = PluginClass("protocolsupport.protocol.utils.types.nbt.serializer.PENBTSerializer").static;
	if (!varint) {
		const length = from.readShortLE();
		if (length == 0) {
			return null;
		} else if (length == -1) {
			const numNBT = from.readByte(); //TODO: why multiple NBT?
			if (numNBT != 1) {
				throw new Error("Unexpected number of NBT fragments: " + numNBT);
			}
			return PENBTSerializer.VI_INSTANCE.deserializeTag(from);
		}
		return PENBTSerializer.LE_INSTANCE.deserializeTag(from);
	}
	return PENBTSerializer.VI_INSTANCE.deserializeTag(from);
}

const packetIds = {
LOGIN: 1,
PLAY_STATUS: 2,
SERVER_TO_CLIENT_HANDSHAKE: 3,
CLIENT_TO_SERVER_HANDSHAKE: 4,
DISCONNECT: 5,
RESOURCE_PACK: 6,
RESOURCE_STACK: 7,
RESOURCE_RESPONSE: 8,
CHAT: 9,
UPDATE_TIME: 10,
START_GAME: 11,
SPAWN_PLAYER: 12,
SPAWN_ENTITY: 13,
ENTITY_DESTROY: 14,
ADD_ITEM_ENTITY: 15,
ADD_HANING_ENTITY: 16, //Simulated
TAKE_ITEM_ENTITY: 17,
ENTITY_TELEPORT: 18,
PLAYER_MOVE: 19,
RIDER_JUMP: 20,
UPDATE_BLOCK: 21,
SPAWN_PAINTING: 22,
EXPLODE: 23,
LEVEL_SOUND_EVENT: 24,
LEVEL_EVENT: 25,
TILE_EVENT: 26,
ENTITY_EVENT: 27,
ENTITY_EFFECT: 28,
SET_ATTRIBUTES: 29,
GOD_PACKET: 30,
MOB_EQUIPMENT: 31,
MOB_ARMOR_EQUIPMENT: 32,
INTERACT: 33,
BLOCK_PICK_REQUEST: 34,
ENTITY_PICK_REQUEST: 35,
PLAYER_ACTION: 36,
ENTITY_FALL: 37, //Simulated
HURT_ARMOR: 38, //Simulated
SET_ENTITY_DATA: 39,
ENTITY_VELOCITY: 40,
ENTITY_LINK: 41,
SET_HEALTH: 42, //Simulated
SPAWN_POS: 43,
ANIMATION: 44,
RESPAWN_POS: 45,
CONTAINER_OPEN: 46,
CONTAINER_CLOSE: 47,
PLAYER_HOTBAR: 48,
INVENTORY_CONTENT: 49,
INVENTORY_SLOT: 50,
CONTAINER_DATA: 51,
CRAFTING_DATA: 52,
CRAFTING_EVENT: 53, //Simulated
GUI_DATA_PICK_ITEM: 54,
ADVENTURE_SETTINGS: 55,
TILE_DATA_UPDATE: 56,
PLAYER_STEER: 57,
CHUNK_DATA: 58,
SET_COMMANDS_ENABLED: 59,
SET_DIFFICULTY: 60,
CHANGE_DIMENSION: 61,
CHANGE_PLAYER_GAMETYPE: 62,
PLAYER_INFO: 63,
SIMPLE_EVENT: 64,
TELEMETRY_EVENT: 65,
SPAWN_XP_ORB: 66, //Simulated
MAP_ITEM_DATA: 67,
MAP_INFO_REQUEST: 68,
CLIENT_SETTINGS: 69,
CHUNK_RADIUS: 70,
ITEM_FRAME_DROP: 71,
GAME_RULE_CHANGE: 72,
BOSS_EVENT: 74,
SHOW_CREDITS: 75,
TAB_COMPLETE: 76,
COMMAND_REQUEST: 77,
COMMAND_BLOCK_UPDATE: 78,
TRADE_UPDATE: 80,
EQUIPMENT: 81,
RESOURCE_INFO: 82,
RESOURCE_DATA: 83,
RESOURCE_REQUEST: 84,
TRANSFER: 85,
PLAY_SOUND: 86,
STOP_SOUND: 87,
SET_TITLE: 88,
PLAYER_SKIN: 93,
SUB_LOGIN: 94, //TODO: Splitscreen support? :O
LAST_HURT: 96,
EDIT_BOOK: 97,
NPC_REQUEST: 98,
PHOTO_TRANSFER: 99,
MODAL_REQUEST: 100,
MODAL_RESPONSE: 101,
SETTINGS_REQUEST: 102,
SETTINGS_RESPONSE: 103,
SHOW_PROFILE: 104,
SET_DEFAULT_GAMEMODE: 105,
REMOVE_OBJECTIVE: 106,
DISPLAY_OBJECTIVE: 107,
SET_SCORE: 108,
LAB_TABLE: 109,
UPDATE_BLOCK_SYNCED: 110,
MOVE_ENTITY_DELTA: 111,
SET_SCOREBOARD_IDENTITY: 112,
SET_LOCAL_PLAYER_INITIALISED: 113,
UPDATE_SOFT_ENUM: 114,
STACK_LATANCY: 115,
UNKOWN: 116,
CUSTOM_EVENT: 117,
SPAWN_PARTICLE_EFFECT_PACKET: 118,
AVAILABLE_ENTITY_IDENTIFIERS_PACKET: 119,
LEVEL_SOUND_EVENT_PACKET_V2: 120,
NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET: 121,
BIOME_DEFINITION_LIST_PACKET: 122,
LEVEL_SOUND_EVENT_PACKET_V3: 123,

//special extension packets
EXT_PS_AWAIT_DIM_SWITCH_ACK: -100
}

const reversePacketIdMap = {};
for(var i in packetIds) reversePacketIdMap[packetIds[i]] = i;

//register in global scope
for(var i in packetIds) this[i] = packetIds[i];