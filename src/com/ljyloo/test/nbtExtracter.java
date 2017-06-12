package com.ljyloo.test;

import org.iq80.leveldb.*;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
		int totalPairs = 0;
		try{
			Options options = new Options();
			options.createIfMissing(true);
			db = factory.open(new File(src), options);
			
			DBIterator iterator = db.iterator();
			for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()){
				byte[] key = iterator.peekNext().getKey();
				//System.out.println(byte2s(key, false));
				byte[] value = iterator.peekNext().getValue();
				//System.out.println("Length: " + value.length);
				
				Path path = Paths.get(des, byte2s(key, false));
				Files.write(path, value);
				totalPairs++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			db.close();
		}
		if(totalPairs > 0){
			System.out.println("\nDone! Total "+totalPairs+" pairs");
		}
		else{
			System.out.println("Oops! It seems that the input data does not contain any valid chunk.");
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
	
}
