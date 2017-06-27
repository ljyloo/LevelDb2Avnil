package com.ljyloo.PE2PC;

import org.iq80.leveldb.*;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.NbtIo;

import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.OldDataLayer;
import net.minecraft.world.level.chunk.storage.*;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class PE2PC {
	private final static int DATALAYER_BITS_LAGACY = 7;
	private final static int BLOCKDATA_BYTES_LAGACY = 32768;
	private final static int METADATA_BYTES_LAGACY = 16384;
	private final static int SKYLIGHTDATA_BYTES_LAGACY = 16384;
	private final static int BLOCKLIGHTDATA_BYTES_LAGACY = 16384;
	
	private final static int DATALAYER_BITS = 4;
	private final static int BLOCKDATA_BYTES = 4096;
	private final static int METADATA_BYTES = 2048;
	private final static int SKYLIGHTDATA_BYTES = 2048;
	private final static int BLOCKLIGHTDATA_BYTES = 2048;
	
	public static void main(String[] args) throws IOException {
		//System.out.println((-1 % 32 + 32) % 32);
        if (args.length != 2) {
            printUsageAndExit();
        }
        
        File srcFolder;
        try {
            srcFolder = new File(args[0]+"/db");
            if (!srcFolder.exists()) {
                throw new RuntimeException(args[0] + " doesn't exist");
            } else if (!srcFolder.isDirectory()) {
                throw new RuntimeException(args[0] + " is not a folder");
            }
        } catch (Exception e) {
            System.err.println("import folder problem: " + e.getMessage());
            System.out.println("");
            printUsageAndExit();
            return;
        }
        
        File desFolder;
        try {
        	desFolder = new File(args[1]);
            if (!desFolder.exists()) {
                throw new RuntimeException(args[1] + " doesn't exist");
            } else if (!desFolder.isDirectory()) {
                throw new RuntimeException(args[1] + " is not a folder");
            }
        } catch (Exception e) {
            System.err.println("export folder problem: " + e.getMessage());
            System.out.println("");
            printUsageAndExit();
            return;
        }
        
		//convert(srcFolder, desFolder);
        System.out.println("Searching for legacy chunks, may take a while...");
        convertLegacy(srcFolder, desFolder);
        convert(srcFolder, desFolder);
	}

    private static void printUsageAndExit() {
    	System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        System.out.println("Convert Minecraft: Pocket Edition Maps(LevelDB) to Minecraft Maps(Anvil) or reversely. (c) ljyloo 2017");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("\tjava -jar Converter.jar <import folder> <export folder>");
        System.out.println("Where:");
        System.out.println("\t<import folder>\tThe full path to the folder containing Minecraft:Pocket Edition world");
        System.out.println("\t<export folder>\tThe full path to the folder which you want to export");
        System.out.println("Example:");
        System.out.println("\tjava -jar Converter.jar /home/ljyloo/import /home/ljyloo/export");
        System.out.println("");
        System.out.println("Visit the homepage of this project for more information:");
        System.out.println("\tgithub.com/ljyloo/LevelDb2Avnil");
        System.exit(1);
    }
	
    public static void legacy(File src) throws IOException{
    	DB db = null;
    	System.out.println("Reading...");
		try{
			Options options = new Options();
			options.createIfMissing(false);
			db = factory.open(src, options);
			
			DBIterator iterator = db.iterator();
			
			for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()){
				byte[] key = iterator.peekNext().getKey();
				if (key.length >= 9 && key[8] == 48){
					System.out.println(byte2s(key, false));
					System.out.println(iterator.peekNext().getValue().length);
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			db.close();
		}
    }
    
    public static void convertLegacy(File src, File des) throws IOException{
		DB db = null;
		int totalChunk = 0;
		try{
			Options options = new Options();
			options.createIfMissing(true);
			db = factory.open(src, options);
			
			DBIterator iterator = db.iterator();
			//ArrayList<byte[]> keys = new ArrayList<byte[]>();
			HashMap<String, RegionFile> regions = new HashMap<String, RegionFile>();
			try{
				for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()){
					byte[] key = iterator.peekNext().getKey();
					if(key.length == 9 && key[8] == 48){
						byte[] value = iterator.peekNext().getValue();
						int chunkX = byteArrayToInt(new byte[]{key[3], key[2], key[1], key[0]});
						int chunkZ = byteArrayToInt(new byte[]{key[7], key[6], key[5], key[4]});
						System.out.print("\rConverting legacy chunk X: "+chunkX+" Z: "+chunkZ+"                     ");
						System.out.flush();
						totalChunk++;
						CompoundTag tag = new CompoundTag();
						CompoundTag levelData = new CompoundTag();
						tag.put("Level", levelData);
						
						levelData.putByte("LightPopulated", (byte)1);
						levelData.putByte("TerrainPopulated", (byte)1);
						levelData.putByte("V", (byte)1);
						levelData.putInt("xPos", chunkX);
						levelData.putInt("zPos", chunkZ);
						levelData.putLong("InhabitedTime", 0);
						levelData.putLong("LastUpdate", 0);
						byte[] biomes = new byte[16 * 16];
						for(int i = 0; i <256; i++)
							biomes[i] = -1;
						levelData.putByteArray("Biomes", biomes);
						levelData.put("Entities", new ListTag<CompoundTag>("Entities"));
						
						ListTag<CompoundTag> sectionTags = new ListTag<CompoundTag>("Sections");
						
						LevelDBChunk data = new LevelDBChunk(chunkX, chunkZ);
						
						data.blocks = new byte[BLOCKDATA_BYTES_LAGACY];
						System.arraycopy(value, 0, data.blocks, 0, BLOCKDATA_BYTES_LAGACY);
						
						byte[] metadata = new byte[METADATA_BYTES_LAGACY];
						System.arraycopy(value, BLOCKDATA_BYTES_LAGACY, metadata, 0, METADATA_BYTES_LAGACY);
						data.data = new OldDataLayer(metadata, DATALAYER_BITS_LAGACY);
						
						byte[] skyLightData = new byte[SKYLIGHTDATA_BYTES_LAGACY];
						System.arraycopy(value, BLOCKDATA_BYTES_LAGACY + METADATA_BYTES_LAGACY, skyLightData, 0, SKYLIGHTDATA_BYTES_LAGACY);
						data.skyLight = new OldDataLayer(skyLightData, DATALAYER_BITS);
						
						byte[] blockLightData = new byte[BLOCKLIGHTDATA_BYTES_LAGACY];
						System.arraycopy(value, BLOCKDATA_BYTES_LAGACY + METADATA_BYTES_LAGACY + SKYLIGHTDATA_BYTES_LAGACY, blockLightData, 0, BLOCKLIGHTDATA_BYTES_LAGACY);
						data.blockLight = new OldDataLayer(blockLightData, DATALAYER_BITS_LAGACY);
						
						for (int yBase = 0; yBase < (128 / 16); yBase++) {

				            // find non-air
				            boolean allAir = true;
				            for (int x = 0; x < 16 && allAir; x++) {
				                for (int y = 0; y < 16 && allAir; y++) {
				                    for (int z = 0; z < 16; z++) {
				                        int pos = (x << 11) | (z << 7) | (y + (yBase << 4));
				                        int block = data.blocks[pos];
				                        if (block != 0) {
				                            allAir = false;
				                            break;
				                        }
				                    }
				                }
				            }

				            if (allAir) {
				                continue;
				            }

				            // build section
				            byte[] blocks = new byte[16 * 16 * 16];
				            DataLayer dataValues = new DataLayer(blocks.length, 4);
				            DataLayer skyLight = new DataLayer(blocks.length, 4);
				            DataLayer blockLight = new DataLayer(blocks.length, 4);

				            for (int x = 0; x < 16; x++) {
				                for (int y = 0; y < 16; y++) {
				                    for (int z = 0; z < 16; z++) {
				                        int pos = (x << 11) | (z << 7) | (y + (yBase << 4));
				                        int block = data.blocks[pos];
				                        int extraData = data.data.get(x, y + (yBase << 4), z);
				                        byte[] temp = filter((byte)(block & 0xff), (byte)extraData);
				                        blocks[(y << 8) | (z << 4) | x] = temp[0];
				                        dataValues.set(x, y, z, temp[1]);
				                        skyLight.set(x, y, z, data.skyLight.get(x, y + (yBase << 4), z));
				                        blockLight.set(x, y, z, data.blockLight.get(x, y + (yBase << 4), z));
				                    }
				                }
				            }

				            CompoundTag sectionTag = new CompoundTag();

				            sectionTag.putByte("Y", (byte) (yBase & 0xff));
				            sectionTag.putByteArray("Blocks", blocks);
				            sectionTag.putByteArray("Data", dataValues.data);
				            sectionTag.putByteArray("SkyLight", skyLight.data);
				            sectionTag.putByteArray("BlockLight", blockLight.data);

				            sectionTags.add(sectionTag);
				        }
						levelData.put("Sections", sectionTags);
				        
				        levelData.put("TileEntities", new ListTag<CompoundTag>("TileEntities"));
				        int[] heightMap = new int[256];
				        for(int x = 0; x < 16; x++){
				        	for(int z = 0; z < 16; z++){
				        		for(int y = 127; y >= 0; y--){
				        			int pos = (x << 11) | (z << 7) | y;
				        			int block = data.blocks[pos];
				        			if(block != 0){
				        				heightMap[(x << 4) | z] = y;
				        				break;
				        			}
				        		}
				        	}
				        }
				        levelData.putIntArray("HeightMap", heightMap);
						
						String k = (chunkX >> 5) + "." + (chunkZ >> 5);
						if(!regions.containsKey(k)){
							regions.put(k, new RegionFile(new File(des, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca")));
						}
						RegionFile regionDest = regions.get(k);
						int regionX = (chunkX % 32 + 32) % 32;
						int regionZ = (chunkZ % 32 + 32) % 32;
						/*if(chunkX < 0 || chunkZ < 0){
							@SuppressWarnings("unused")
							int i = 1+1;
						}*/
						DataOutputStream chunkDataOutputStream = regionDest.getChunkDataOutputStream(regionX, regionZ);
						if(chunkDataOutputStream == null){
							System.out.println(chunkX % 32);
							System.out.println(chunkZ % 32);
						}
						NbtIo.write(tag, chunkDataOutputStream);
						chunkDataOutputStream.close();
					}
				}
			}
			finally{
				Iterator<Entry<String, RegionFile>> iter = regions.entrySet().iterator();
				while (iter.hasNext()){
					Entry<String, RegionFile> entry = iter.next();
					RegionFile region = entry.getValue();
					region.close();
				}
				iterator.close();
			}
		}
		finally{
			db.close();
		}
		if(totalChunk > 0){
			System.out.println("\nDone! Converted legacy chunks: " + totalChunk);
		}
		else{
			System.out.println("\nIt seems that the input data does not contain any legacy chunk.");
		}
	}
    
	public static void convert(File src, File des) throws IOException{
		DB db = null;
		int totalChunk = 0;
		try{
			Options options = new Options();
			options.createIfMissing(true);
			db = factory.open(src, options);
			
			DBIterator iterator = db.iterator();
			//ArrayList<byte[]> keys = new ArrayList<byte[]>();
			HashMap<String, RegionFile> regions = new HashMap<String, RegionFile>();
			HashMap<String, CompoundTag> comChunks = new HashMap<>();
			HashMap<String, Integer> chunkHeight = new HashMap<>();
			try{
				for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()){
					byte[] key = iterator.peekNext().getKey();
					//System.out.println(byte2s(key, false));
					byte[] value = iterator.peekNext().getValue();
					//System.out.println("Length: " + value.length);
					if(key.length == 10 && key[8] == 47){
						
						
						int chunkX = byteArrayToInt(new byte[]{key[3], key[2], key[1], key[0]});
						int chunkZ = byteArrayToInt(new byte[]{key[7], key[6], key[5], key[4]});
						int chunkY = (int) key[9];
						
						System.out.print("\rConverting subchunk X: "+chunkX+" Z: "+chunkZ+" Y: "+chunkY+"                     ");
						System.out.flush();
						totalChunk++;
						
						String comKey = chunkX+","+chunkZ;
						if (!comChunks.containsKey(comKey)){
							//System.out.println("New comChunks");
							CompoundTag tag = new CompoundTag();
							CompoundTag levelData = new CompoundTag();
							tag.put("Level", levelData);
							
							levelData.putByte("LightPopulated", (byte)1);
							levelData.putByte("TerrainPopulated", (byte)1);
							levelData.putByte("V", (byte)1);
							levelData.putInt("xPos", chunkX);
							levelData.putInt("zPos", chunkZ);
							levelData.putLong("InhabitedTime", 0);
							levelData.putLong("LastUpdate", 0);
							
							byte[] biomes = new byte[16 * 16];
							for(int i = 0; i <256; i++)
								biomes[i] = -1;
							levelData.putByteArray("Biomes", biomes);
							
							levelData.put("Entities", new ListTag<CompoundTag>("Entities"));
							
							ListTag<CompoundTag> sectionTags = new ListTag<CompoundTag>("Sections");
							levelData.put("Sections", sectionTags);
							
							levelData.put("TileEntities", new ListTag<CompoundTag>("TileEntities"));
							
							comChunks.put(comKey, tag);
						}
						
						
						
						CompoundTag tag = comChunks.get(comKey);
						CompoundTag levelData = tag.getCompound("Level");
						
						@SuppressWarnings("unchecked")
						ListTag<CompoundTag> sectionTags = (ListTag<CompoundTag>) levelData.getList("Sections");
						
						LevelDBChunk data = new LevelDBChunk(chunkX, chunkZ);
						
						int offset = 1;
						
						data.blocks = new byte[BLOCKDATA_BYTES];
						System.arraycopy(value, offset, data.blocks, 0, BLOCKDATA_BYTES);
						offset += BLOCKDATA_BYTES;
						
						byte[] metadata = new byte[METADATA_BYTES];
						System.arraycopy(value, offset, metadata, 0, METADATA_BYTES);
						offset += METADATA_BYTES;
						data.data = new OldDataLayer(metadata, DATALAYER_BITS);
						
						byte[] skyLightData = new byte[SKYLIGHTDATA_BYTES];
						if (offset + SKYLIGHTDATA_BYTES < value.length)
							System.arraycopy(value, offset, skyLightData, 0, SKYLIGHTDATA_BYTES);
						offset += SKYLIGHTDATA_BYTES;
						data.skyLight = new OldDataLayer(skyLightData, DATALAYER_BITS);
						
						byte[] blockLightData = new byte[BLOCKLIGHTDATA_BYTES];
						if (offset + BLOCKLIGHTDATA_BYTES < value.length)
							System.arraycopy(value, offset, blockLightData, 0, BLOCKLIGHTDATA_BYTES);
						data.blockLight = new OldDataLayer(blockLightData, DATALAYER_BITS);
						
						byte[] blocks = new byte[16 * 16 * 16];
			            DataLayer dataValues = new DataLayer(blocks.length, 4);
			            DataLayer skyLight = new DataLayer(blocks.length, 4);
			            DataLayer blockLight = new DataLayer(blocks.length, 4);

			            for (int x = 0; x < 16; x++) {
			                for (int y = 0; y < 16; y++) {
			                    for (int z = 0; z < 16; z++) {
			                        int pos = (x << 8) | (z << 4) | y;
			                        byte block = data.blocks[pos];
			                        int extraData = data.data.get(x, y, z);
			                        byte[] temp = filter((byte)(block & 0xff), (byte)extraData);
			                        blocks[(y << 8) | (z << 4) | x] = temp[0];
			                        dataValues.set(x, y, z, temp[1]);
			                        //skyLight.set(x, y, z, data.skyLight.get(x, y, z));
			                        //blockLight.set(x, y, z, data.blockLight.get(x, y, z));
			                        skyLight.set(x, y, z, 0xf);
			                        blockLight.set(x, y, z, 0xf);
			                    }
			                }
			            }

			            CompoundTag sectionTag = new CompoundTag();

			            sectionTag.putByte("Y", (byte) (chunkY & 0xff));
			            sectionTag.putByteArray("Blocks", blocks);
			            sectionTag.putByteArray("Data", dataValues.data);
			            sectionTag.putByteArray("SkyLight", skyLight.data);
			            sectionTag.putByteArray("BlockLight", blockLight.data);

			            sectionTags.add(sectionTag);
				        
			            if (!chunkHeight.containsKey(comKey)) {
			            	chunkHeight.put(comKey, chunkY);
			            }
			            else {
			            	int temp = chunkHeight.get(comKey);
			            	if (chunkY > temp)
			            		chunkHeight.put(comKey, chunkY);
			            }
					}
				}
				
				
				
				Iterator<Entry<String, CompoundTag>> iter = comChunks.entrySet().iterator();
				while (iter.hasNext()){
					Entry<String, CompoundTag> entry = iter.next();
					String key = entry.getKey();
					
					CompoundTag tag = entry.getValue();
					CompoundTag levelData = tag.getCompound("Level");
					@SuppressWarnings("unchecked")
					ListTag<CompoundTag> sectionTags = (ListTag<CompoundTag>) levelData.getList("Sections");
					int topChunk = chunkHeight.get(key);
					
					for (int i = 0; i < sectionTags.size(); i++) {
						CompoundTag subChunk = sectionTags.get(i);
						int Y = subChunk.getByte("Y");
						if (Y == topChunk) {
							DataLayer dataValues = new DataLayer(subChunk.getByteArray("Data"), 4);
							
							int[] heightMap = new int[256];
					        for(int x = 0; x < 16; x++){
					        	for(int z = 0; z < 16; z++){
					        		for(int y = 15; y >= 0; y--){
					        			int block = dataValues.get(x, y, z);
					        			if(block != 0){
					        				heightMap[(x << 4) | z] = (Y << 4) | y;
					        				break;
					        			}
					        		}
					        	}
					        }
					        levelData.putIntArray("HeightMap", heightMap);
							break;
						}
					}
					/*
					int[] heightMap = new int[256];
			        for(int x = 0; x < 16; x++){
			        	for(int z = 0; z < 16; z++){
			        		heightMap[(x << 4) | z] = 0;
			        	}
			        }
			        levelData.putIntArray("HeightMap", heightMap);
					*/
					String[] parts = key.split(",");
					int chunkX = Integer.parseInt(parts[0]);
					int chunkZ = Integer.parseInt(parts[1]);
					
					String k = (chunkX >> 5) + "." + (chunkZ >> 5);
					if(!regions.containsKey(k)){
						regions.put(k, new RegionFile(new File(des, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca")));
					}
					RegionFile regionDest = regions.get(k);
					int regionX = (chunkX % 32 + 32) % 32;
					int regionZ = (chunkZ % 32 + 32) % 32;
					DataOutputStream chunkDataOutputStream = regionDest.getChunkDataOutputStream(regionX, regionZ);
					if(chunkDataOutputStream == null){
						System.out.println(chunkX % 32);
						System.out.println(chunkZ % 32);
					}
					NbtIo.write(tag, chunkDataOutputStream);
					chunkDataOutputStream.close();
				}
			}
			finally{
				Iterator<Entry<String, RegionFile>> iter = regions.entrySet().iterator();
				while (iter.hasNext()){
					Entry<String, RegionFile> entry = iter.next();
					RegionFile region = entry.getValue();
					region.close();
				}
				iterator.close();
			}
		}
		finally{
			db.close();
		}
		if(totalChunk > 0){
			System.out.println("\nDone! Converted sub chunks: " + totalChunk);
		}
		else{
			System.out.println("\nIt seems that the input data does not contain any sub chunk.");
		}
	}
	
	/*
	 * Translate blocks' id which are not shared by PE and PC
	 * If PE have this id and PC don't, replace with air(id=0)
	 * I know it's better to store the id replace table in a individual file, but I'm just too lazy to do so...Maybe next time
	 * */
	public static byte[] filter(byte id, byte data){
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
		case (byte) 255:  //reserved6
			return new byte[]{(byte) 0, 0};
		default:
			return new byte[]{id, data};
		}
	}
	
	public static String byte2s(byte[] b, boolean ignoreTooLong){
		String s = "0x";
		int length = b.length;
		boolean tooLong = false;
		if(length > 100){
			length = 100;
			tooLong = true;
		}
		for(int i = 0; i < length; i++){
			s = s + b[i] + " ";
		}
		if(tooLong && ignoreTooLong)
			s = s + "...";
		return s;
	}
	
	public static byte[] intToByteArray(int i){
		byte[] result = new byte[4];
		result[0] = (byte)((i >> 24) & 0xFF);
		result[1] = (byte)((i >> 16) & 0xFF);
		result[2] = (byte)((i >> 8) & 0xFF);
		result[3] = (byte)(i & 0xFF);
		return result;
	}
	
	public static int byteArrayToInt(byte[] bytes){
		int value= 0;
		for (int i = 0; i < 4; i++){
			if (bytes.length - i < 1)
				break;
			int shift = (3 - i) * 8;
			value += (bytes[i] & 0x000000FF) << shift;
		}
		return value;
	}
	
	public static class LevelDBChunk{
		public OldDataLayer blockLight;
		public OldDataLayer skyLight;
		public OldDataLayer data;
		public byte[] blocks;
		
		public final int x;
		public final int z;
		
		public LevelDBChunk(int x, int z){
			this.x = x;
			this.z = z;
		}
	}
}
