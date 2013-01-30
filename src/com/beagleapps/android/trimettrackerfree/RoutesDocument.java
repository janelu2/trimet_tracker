package com.beagleapps.android.trimettrackerfree;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RoutesDocument extends XMLDocument{

	public static Document mRouteXMLDoc;
	public static Document mStopsXMLDoc;
	
	private NodeList mDirectionNodes = null;

	
	// Used when going from chooseDirection to chooseStop
	private static int chosenDirection;
	public static String mRoutesFileName = "RoutesDocument_Routes.xml";;
	public static String mStopsFileName = "RoutesDocument_Stops.xml";;
	
	public RoutesDocument(Document routesDoc) {
		RoutesDocument.mRouteXMLDoc = routesDoc;
	}
	
	public RoutesDocument() {
		// TODO Auto-generated constructor stub
	}

	public NodeList getRoutesNodes(){
		return mRouteXMLDoc.getElementsByTagName("route");
	}
	
	public NodeList getDirectionNodes(){
		if (mDirectionNodes == null){
			mDirectionNodes = mStopsXMLDoc.getElementsByTagName("dir");
		}
		
		return mDirectionNodes;
	}
	
	public NodeList getStopNodes(int dir){
		Node dirNode = getDirectionNodes().item(dir);
		
		return dirNode.getChildNodes();
	}
	
	public String getDirDescription(int dir){
		if (getDirectionNodes() != null){
			Node dirNode = getDirectionNodes().item(dir);
	        return getAttributeValue(dirNode, "desc");
		}
		else {
			return "";
		}
	}
	
	public String getStopDescription(int dir, int index){
		return getAttributeValue(getStopNodes(dir).item(index), "desc");
	}
	
	public String getStopID(int dir, int index){
		return getAttributeValue(getStopNodes(dir).item(index), "locid");
	}
	
	public String getRouteDescription(int index){
		if (getRoutesNodes() != null){
			Node routeNode = getRoutesNodes().item(index);
			return getAttributeValue(routeNode, "desc");
		}
		else {
			return "";
		}
	}
	
	public String getStopsRouteDescription(){
		
		Node route = mStopsXMLDoc.getElementsByTagName("route").item(0);
		
		return getAttributeValue(route, "desc");
	}
	
	public String getRouteNumber(int index){
		if (getRoutesNodes() != null){
			Node routeNode = getRoutesNodes().item(index);
	        return getAttributeValue(routeNode, "route");
		}
		else{
			return "";
		}
	}

	public static int getChosenDirection() {
		return chosenDirection;
	}

	public static void setChosenDirection(int chosenDirection) {
		RoutesDocument.chosenDirection = chosenDirection;
	}
}
