package com.beagleapps.android.trimettracker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NearbyStopsDocument extends XMLDocument{
	
	protected static Document mXMLDoc;
	protected static NodeList mLocationNodes;
	public static String mFileName = "NearbyStopsDocument.xml";
	
	public NearbyStopsDocument(Document document) {
		NearbyStopsDocument.mXMLDoc = document;
		mLocationNodes = mXMLDoc.getElementsByTagName("location");
	}
	
	public NearbyStopsDocument() {
		
	}

	public static Document getXMLDoc() {
		return mXMLDoc;
	}

	public static void setXMLDoc(Document mXMLDoc) {
		NearbyStopsDocument.mXMLDoc = mXMLDoc;
		if(mXMLDoc != null){
			mLocationNodes = mXMLDoc.getElementsByTagName("location");
		}
	}

	
	public NodeList getLocationNodes(){
		return mLocationNodes;
	}
	
	public Node getLocationNode(int index){
		return getLocationNodes().item(index);
	}
	
	
	public String getDescription(int index){
		return getAttributeValue(getLocationNode(index), "desc");
	}
	
	public String getDirection(int index){
		return getAttributeValue(getLocationNode(index), "dir");
	}
	
	public double getLat(int index){
		return Double.parseDouble(getAttributeValue(getLocationNode(index), "lat"));
	}
	
	public double getLon(int index){
		return Double.parseDouble(getAttributeValue(getLocationNode(index), "lng"));
	}
	
	public String getLocationID(int index){
		return getAttributeValue(getLocationNode(index), "locid");
	}
	
	public int lengthLocations() {
		return getLocationNodes().getLength();
	}

	/////////////////////////////////////////////
	// Route functions
	public NodeList getRouteList(int index){
		return getLocationNode(index).getChildNodes();
	}
	
	public Node getRouteNode(int locationIndex, int routeIndex){
		return getRouteList(locationIndex).item(routeIndex);
	}
	
	public String getRouteDesc(int locationIndex, int routeIndex){
		Node routeNode = getRouteNode(locationIndex, routeIndex);
		return getAttributeValue(routeNode, "desc");
	}
	
	public String getRouteNumber(int locationIndex, int routeIndex){
		Node routeNode = getRouteNode(locationIndex, routeIndex);
		return getAttributeValue(routeNode, "route");
	}

	public int lengthRoutes(int locationIndex) {
		return getRouteList(locationIndex).getLength();
	}

}
