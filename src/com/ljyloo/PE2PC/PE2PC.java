package com.ljyloo.PE2PC;

import org.iq80.leveldb.*;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.NbtIo;

import net.minecraft.world.level.chunk.storage.RegionFile;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.io.*;
import java.util.*;

public class PE2PC {
	
	public static void main(String[] args) throws IOException {
		//System.out.println((-1 % 32 + 32) % 32);
        if (args.length != 2) {
            printUsageAndExit();
        }
        
        File srcFolder, desFolder;
        try{
        	srcFolder = checkFolder(args[0] + "/db");
            desFolder = checkFolder(args[1]);
        }catch(Exception e){
        	System.err.println("Folder problem: " + e.getMessage() + "\n");
        	printUsageAndExit();
        	return;
        }

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
    	
	public static void convert(File src, File des) throws IOException{
		DB db = null;
		int totalChunk = 0;
		try{
			Options options = new Options();
			options.createIfMissing(true);
			db = factory.open(src, options);
			
			DBIterator iterator = db.iterator();
			
			HashMap<String,RegionFile> regions = new HashMap<String,RegionFile>();
			
			try{
				ConvertSource cs = new ConvertSource();
				iterator.seekToFirst();
				
				Loop1:
				while(iterator.hasNext()){

					byte[] key = iterator.peekNext().getKey();

					if(key.length == 9){// key for chunk
						if(key[8] > 44 && key[8] < 51) {
							int currentX = byteArrayToInt(new byte[]{key[3], key[2], key[1], key[0]});
							int currentZ = byteArrayToInt(new byte[]{key[7], key[6], key[5], key[4]});
							
							cs.setCurrent(currentX, currentZ);
							
							System.out.printf("\rConverting chunk %d,%d...\n",currentX,currentZ);
							
							for(;iterator.hasNext();iterator.next()){
								key = iterator.peekNext().getKey();
								
								if(key.length != 9) {
									continue Loop1;
								}
								
								int chunkX = byteArrayToInt(new byte[]{key[3], key[2], key[1], key[0]});
								int chunkZ = byteArrayToInt(new byte[]{key[7], key[6], key[5], key[4]});
								
								if(chunkX != currentX | chunkZ != currentZ){
									continue Loop1;
								}
								
								byte tag = key[8];
								byte[] value = iterator.peekNext().getValue();
								
								switch (tag){
									case 45://Data2D
										cs.convertData2D(value);
										break;
									case 46://Data2DLegacy
										break;
									case 47://SubChunkPrefix
										if(key.length == 10)
											cs.convertSubChunk(key[9], value);
										break;
									case 48://LegacyTerrain
										cs.convertLegacyTerrain(value);
										break;
									case 49://BlockEntity
										break;
									case 50://Entity
										cs.convertEntity(value);
										break;	
									case 52://BlockExtraData
										break;
									/*
									 * tag
									 * - 51 PendingTicks
									 * - 53 BiomeState
									 * - 54 FInalizedState
									 * - 118 Version
									 * have been excluded.
									 */
								}
								System.out.flush();
							}
						}
					}else{
						//other key
						//System.out.println("\rUnknown Key: \n" + byte2s(key,false));
					}
					iterator.next();
				}
				
				//Save converted data
				List<CompoundTag> converted = cs.getConvertedChunks();
				Iterator<CompoundTag> iter = converted.iterator();
				
				totalChunk = converted.size();
				
				while (iter.hasNext()){
					
					//Check whether the chunk NBTTag is complete.
					CompoundTag chunk = iter.next();
					CompoundTag levelData = chunk.getCompound("Level");
					
					int chunkX = levelData.getInt("xPos");
					int chunkZ = levelData.getInt("zPos");
					
					if(!levelData.contains("Biomes")){
						//System.out.printf("Warning : Chunk %d,%d don't have Biomes tag.\n", chunkX, chunkZ);
						byte[] biomes = new byte[16*16];
						Arrays.fill(biomes, (byte) -1);
						levelData.putByteArray("BIomes", biomes);
					}
					
					if(!levelData.contains("Entities")){
						//System.out.printf("Warning : Chunk %d,%d don't have Entities tag.\n", chunkX, chunkZ);
						levelData.put("Entities", new ListTag<CompoundTag>());
					}
					
					if(!levelData.contains("TileEntities")){
						//System.out.printf("Warning : Chunk %d,%d don't have TileEntities tag.\n", chunkX, chunkZ);
						levelData.put("TileEntities", new ListTag<CompoundTag>());
					}
					
					//Save chunks
					
					String k = (chunkX >> 5) + "." + (chunkZ >> 5);
					
					RegionFile regionDest;
					if(!regions.containsKey(k)){
						regionDest = new RegionFile(new File(des, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca")); 
						regions.put(k, regionDest);
					}else{
						regionDest = regions.get(k);
					}
					
					int regionX = (chunkX % 32 + 32) % 32;
					int regionZ = (chunkZ % 32 + 32) % 32;
					
					DataOutputStream chunkDataOutputStream = regionDest.getChunkDataOutputStream(regionX, regionZ);
					if(chunkDataOutputStream == null){	
						System.out.println(chunkX +","+ chunkZ);
					}
					NbtIo.write(chunk, chunkDataOutputStream);
					chunkDataOutputStream.close();
				}
			}
			finally{
				Iterator<RegionFile> iter = regions.values().iterator();
				while(iter.hasNext()){
					iter.next().close();
				}
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
    
	public static String byte2s(byte[] b, boolean ignoreTooLong){
		String s = "";
		int length = b.length;
		boolean tooLong = false;
		if(length > 100){
			length = 100;
			tooLong = true;
		}
		for(int i = 0; i < length; i++){
			s = s + String.format("%03d", b[i]) + " ";
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
	
	private static File checkFolder(String path){
		File folder = new File(path);

        if (!folder.exists())
            throw new RuntimeException(path + " doesn't exist");
        else if (!folder.isDirectory())
            throw new RuntimeException(path + " is not a folder");
        
		return folder;
	}
}
