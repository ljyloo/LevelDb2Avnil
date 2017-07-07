package com.ljyloo.PE2PC;

public class UnsharedData {

	/*
	 * Translate blocks' id which are not shared by PE and PC
	 * If PE have this id and PC don't, replace with air(id=0)
	 */
	public static byte[] blockFilter(byte id, byte data){
		switch (id) {
		case 43:
			switch (data) {
			case 6:  //Nether Brick Slab
				return new byte[]{43, 7};
			case 7:  //Quartz Slab
				return new byte[]{43, 6};
			default:
				return new byte[]{43, data};
			}
		case 44:
			switch (data) {
			case 6:  //Nether Brick Slab
				return new byte[]{44, 7};
			case 7:  //Quartz Slab
				return new byte[]{44, 6};
			case 14:  //Upper Quartz Slab
				return new byte[]{44, 15};
			case 15:  //Upper Nether Brick Slab
				return new byte[]{44, 14};
			default:
				return new byte[]{44, data};
			}
		case 85:  //fence
			switch (data) {
			case 0:  //oak_fence
				return new byte[]{85,0};
			case 1:  //spruce_fence
				return new byte[]{(byte) 188, 0};
			case 2:  //birch_fence
				return new byte[]{(byte) 189, 0};
			case 3:  //jungle_fence
				return new byte[]{(byte) 190, 0};
			case 4:  //dark_oak_fence
				return new byte[]{(byte) 192, 0};
			case 5:  //acacia_fence
				return new byte[]{(byte) 191, 0};
			default:
				return new byte[]{0, 0};
			}
		case 95:  //invisible bedrock
			return new byte[]{(byte) 166, 0}; //barrier
		case 125:  //dropper
			return new byte[]{(byte) 158, data};
		case 126:  //activator_rail
			return new byte[]{(byte) 157, data};
		case (byte) 157:  //double_wooden_slab
			return new byte[]{125, data};
		case (byte) 158:  //wooden_slab
			return new byte[]{126, data};
		case (byte) 188:  //repeating_command_block
			return new byte[]{(byte) 210, data};
		case (byte) 189:  //chain_command_block
			return new byte[]{(byte) 211, data};
		case (byte) 198:  //grass_path
			return new byte[]{(byte) 208, data};
		case (byte) 199:  //A hanging frame is a block in PE
			return new byte[]{0, 0};  //A hanging frame is a entity in PC, so replace it with air
		case (byte) 207:  //frosted_ice
			return new byte[]{(byte) 212, data};
		case (byte) 208: //end_rod
			return new byte[]{(byte) 198, data};
		case (byte) 218: //shulker_box
			return new byte[]{(byte) 229, 0};
		case (byte) 236:  //concrete
			return new byte[]{(byte) 251, data};
		case (byte) 237:  //concrete_powder
			return new byte[]{(byte) 252, data};
		case (byte) 240:  //chorus_plant
			return new byte[]{(byte) 199, data};
		case (byte) 241:  //stained_glass
			return new byte[]{(byte) 95, data};
		case (byte) 243:  //podzol
			return new byte[]{(byte) 3, 2};
		case (byte) 244:  //beetroot
			return new byte[]{(byte) 207, data};
		case (byte) 245:  //stonecutter
		case (byte) 246:  //glowingobsidian
		case (byte) 247:  //netherreactor
		case (byte) 248:  //info_update
		case (byte) 249:  //info_update2
		case (byte) 250:  //movingblock
		case (byte) 251:  //observer
			return new byte[]{(byte) 218, data};
		case (byte) 255:  //reserved6
			return new byte[]{(byte) 0, 0};
		default:
			return new byte[]{id, data};
		}
	}

}
