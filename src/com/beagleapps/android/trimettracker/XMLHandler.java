package com.beagleapps.android.trimettracker;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;

import com.beagleapps.android.trimettracker.helpers.XMLIOHelper;

// This class is just for downloading the xml data 
public class XMLHandler {

	private Document mDocument;
	private URL mUrl;
	private long mRequestTime;
	private Context mContext;
	private String mFileName;
	
	public XMLHandler(String url, Context context, String fileName) throws MalformedURLException {
		mUrl = new URL(url);
		mContext = context;
		mFileName = fileName;
	}
	
	public void refreshXmlData() 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			//db = dbf.newDocumentBuilder();
			
			// Save File
			InputStream stream = mUrl.openStream();
			XMLIOHelper.saveFile(mContext, mFileName, stream);
			
			//document = db.parse(new InputSource(stream));
			//document.getDocumentElement().normalize();
			mDocument = XMLIOHelper.loadFile(mContext, mFileName);
			mRequestTime = new Date().getTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Document getXmlDoc() 
	{
		return this.mDocument;
	}
	
	public boolean hasError(){
		boolean hasError = true;
		if (mDocument != null){
			NodeList nodeList = mDocument.getElementsByTagName("errorMessage");
			if (nodeList.getLength() <= 0)
				hasError = false;
		}
		
		return hasError;
	}
	
	public String getError(){
		String errorMessage = null;
		if (mDocument != null){
			NodeList nodeList = mDocument.getElementsByTagName("errorMessage");
			if (nodeList.getLength() <= 0)
				errorMessage = "No Error";
			else{
				Element errorElement = (Element) nodeList.item(0);
				NodeList errorNodeList = errorElement.getChildNodes();
		        errorMessage = errorNodeList.item(0).getNodeValue();
			}
		}
		else{
			errorMessage = "Error: Problem getting data";
		}
		
		return errorMessage;
	}

	public long getRequestTime() {
		return mRequestTime;
	}
}
