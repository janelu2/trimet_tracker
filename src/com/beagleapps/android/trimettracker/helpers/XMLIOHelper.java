package com.beagleapps.android.trimettracker.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.content.Context;

public class XMLIOHelper {
	public static boolean saveFile(Context context, String fileName, InputStream inputStream){
		File cacheDir = context.getCacheDir();
		File outputFile = new File(cacheDir, fileName);
		
		writeFile(outputFile, inputStream);
		return true;
	}

	public static Document loadFile(Context context, String fileName) {
		File cacheDir = context.getCacheDir();
		File file = new File(cacheDir, fileName);
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document document = null;
		
		try {
			db = dbf.newDocumentBuilder();
			
			document = db.parse(file);
			document.getDocumentElement().normalize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return document;
	}
	
	public static void writeFile(File outputFile, InputStream inputStream){
		try {
			// write the inputStream to a FileOutputStream
			OutputStream out = new FileOutputStream(outputFile);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			
			out.flush();
			out.close();
		} 
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
