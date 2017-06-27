package com.ljyloo.test;

import org.iq80.leveldb.*;

import com.ljyloo.io.LittleEndianDataInputStream;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;
import com.mojang.nbt.Tag;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.io.*;

public class nbtExtracter {
	
	public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            printUsageAndExit();
        }
        
		extract(args[0], args[1]);
	}

    private static void printUsageAndExit() {
    	System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        System.out.println("Extract each pair in a LevelDB database into individual raw data files. (c) ljyloo 2017");
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("\tjava -jar Extracter.jar <import folder> <export folder>");
        System.out.println("Where:");
        System.out.println("\t<import folder>\tThe full path to the folder of LevelDB database");
        System.out.println("\t<export folder>\tThe full path to the folder which you want to export");
        System.out.println("Example:");
        System.out.println("\tjava -jar Extracter.jar /home/ljyloo/import /home/ljyloo/export");
        System.out.println("");
        System.out.println("Visit the homepage of this project for more information:");
        System.out.println("\tgithub.com/ljyloo/LevelDb2Avnil");
        System.exit(1);
    }
	
	public static void extract(String src, String des) throws IOException{
		DB db = null;
		LittleEndianDataInputStream ledis = null;
		int totalPairs = 0;
		try{
			
			Options options = new Options();
			options.createIfMissing(true);
			db = factory.open(new File(src), options);
			byte[] key = new byte[]{3, 0, 0, 0, -3, -1, -1, -1, 49};
			//byte[] key = "~local_player".getBytes();
			byte[] biome = db.get(key);
			ledis = new LittleEndianDataInputStream(new ByteArrayInputStream(biome));
			CompoundTag tag = NbtIo.read(ledis);
			//String fileName = des+"\\"+new String(key, "UTF-8")+".dat";
			String fileName = des+"\\"+byte2s(key, false)+".dat";
			System.out.println(fileName);
			NbtIo.write(tag, new File(fileName));
			//DataInputStream dis = new DataInputStream(new ByteArrayInputStream(biome));
			//System.out.println(ledis.readByte());
			//System.out.println(ledis.readUTF());
			//System.out.println(ledis.readUnsignedShort());
			/*
			CompoundTag cTag = NbtIo.read(ledis);
			for (Tag tag : cTag.getAllTags()) {
				System.out.println(Tag.getTagName(tag.getId()));
			}*/
			//System.out.println(byte2s(biome, false));
			/*
			DBIterator iterator = db.iterator();
			for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()){
				byte[] key = iterator.peekNext().getKey();
				//System.out.println(byte2s(key, false));
				byte[] value = iterator.peekNext().getValue();
				//System.out.println("Length: " + value.length);
				
				Path path = Paths.get(des, byte2s(key, false) + ".dat");
				Files.write(path, value);
				totalPairs++;
			}*/
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			db.close();
			ledis.close();
			
		}
		if(totalPairs > 0){
			System.out.println("\nDone! Total "+totalPairs+" pairs");
		}
		else{
			System.out.println("Done.");
			//System.out.println("Oops! It seems that the input data does not contain any valid chunk.");
		}
	}
	
	public static void printTag(Tag tag, int layers) {
		//CompoundTag
		if (tag.getId() == 10) {
			
		}
	}
	
	public static String byte2s(byte[] b, boolean ignoreTooLong){
		String s = "0x";
		int length = b.length;
		boolean tooLong = false;
		if(length > 100){
			if (ignoreTooLong)
				length = 100;
			tooLong = true;
		}
		for(int i = 0; i < length; i++){
			if (i > 0 && (i % 8 == 0))
				;
				//s += "\n0x";
			String temp = "0" + Integer.toHexString(b[i]);
			s = s + temp.substring(temp.length() - 2) + " ";
		}
		if(tooLong && ignoreTooLong)
			s = s + "...";
		return s.substring(0, s.length() - 1);
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
	
}
