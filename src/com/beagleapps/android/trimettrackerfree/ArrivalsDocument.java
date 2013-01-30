package com.beagleapps.android.trimettrackerfree;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.location.Location;

public class ArrivalsDocument extends XMLDocument{

	private static final String[] DaysOfWeek = 
	{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	public static Document mXMLDoc;
	public static long mRequestTime;
	
	public static String mFileName = "ArrivalsDocument.xml";
	
	public ArrivalsDocument(Document arrivalsDoc, long requestTime) {
		ArrivalsDocument.mXMLDoc = arrivalsDoc;
		mRequestTime = requestTime;
	}
	
	public ArrivalsDocument() {
	}

	public int getStopID(){
		NodeList nodeList = mXMLDoc.getElementsByTagName("location");
        
		Node locationNode = nodeList.item(0);
		String stopString = getAttributeValue(locationNode, "locid"); 
		return Integer.parseInt(stopString);
	}
	
	public float getLatitude(){
		NodeList nodeList = mXMLDoc.getElementsByTagName("location");
		Node locationNode = nodeList.item(0);

		return Float.parseFloat(getAttributeValue(locationNode, "lat"));
	}
	
	public float getLongitude(){
		NodeList nodeList = mXMLDoc.getElementsByTagName("location");
		Node locationNode = nodeList.item(0);

		return Float.parseFloat(getAttributeValue(locationNode, "lng"));
	}
	
	public NodeList getArrivalNodes(){
		return mXMLDoc.getElementsByTagName("arrival");
	}
	
	public String getStopDescription(){
		NodeList nodeList = mXMLDoc.getElementsByTagName("location");
	        
		Node locationNode = nodeList.item(0);
        return getAttributeValue(locationNode, "desc"); 
	}
	
	public String getDirection(){
		NodeList nodeList = mXMLDoc.getElementsByTagName("location");
        
		Node locationNode = nodeList.item(0);
        return getAttributeValue(locationNode, "dir"); 
	}
	
	public String getBusDescription(int index){
		String description = null;
		NodeList arrivalNodes = getArrivalNodes();
        
		Node arrival = arrivalNodes.item(index);
		description = getAttributeValue(arrival, "shortSign"); 

        return description;
	}
	
	public String getScheduledTime(int index){
		String scheduledTime = null;
        
		Node arrival = getArrivalNodes().item(index);
		scheduledTime = getAttributeValue(arrival, "scheduled"); 
		
        return scheduledTime;
	}
	
	// Returns the time of the request in milliseconds since epoch
	public String getRequestTime(){
        Node resultSet = mXMLDoc.getElementsByTagName("resultSet").item(0);
		String requestTime = null;
		requestTime = getAttributeValue(resultSet, "queryTime");
		
        return requestTime;
	}
	
	public String getScheduledTimeText(int index){
		String timeText = null;
        
		Node arrival = getArrivalNodes().item(index);
		
		if(arrival != null){
			long unixTime = Long.parseLong(getAttributeValue(arrival, "scheduled"));
			
			timeText = getReadableTime(unixTime);
		}
        return timeText;
	}
	
	private String getReadableTime(long unixTime) {
		Date arrivalTime = new Date(unixTime);
		Date now = new Date();
		String timeString;
		timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(arrivalTime);
		
		if(now.getDay() != arrivalTime.getDay()){
			timeString = timeString + ", " + DaysOfWeek[arrivalTime.getDay()];
		}
		
		return timeString;
	}

	public String getRemainingMinutes(int index) {
		Date currentTime = new Date();
		String epochTimeString, timeLeftString;
		long epochTimeLong, minutes;
		
		if(getArrivalNodes().item(index) != null){
			if (isEstimated(index))
				epochTimeString = getEstimatedTime(index);
			else
				epochTimeString = getScheduledTime(index);
			
			epochTimeLong = Long.parseLong(epochTimeString);
			
			minutes = ((epochTimeLong - currentTime.getTime())/1000)/60;
			
			if (minutes >= 60){
				timeLeftString = getLongWaitTime(minutes);
			}
			else{
				timeLeftString = Long.toString(minutes) + " min";
			}
		}
		else{
			timeLeftString = "Error";
		}
		
		return timeLeftString;
	}

	private String getLongWaitTime(long minutes) {
		String timeString = Long.toString(minutes);
		
		// Hours
		if (minutes >= 60){
			timeString = Long.toString(minutes/60) + " hour(s)";
			
			// Days
			if (minutes >= 60*24){
				timeString = Long.toString(minutes/60/24) + " day(s)";
			}
		}
		
		return timeString;
	}

	public String getEstimatedTime(int index){
		String estimatedTime = null;
		NodeList arrivalNodes = getArrivalNodes();
        
		Node arrival = arrivalNodes.item(index);
		
		if(arrival != null){
			estimatedTime = getAttributeValue(arrival, "estimated");
		}
        return estimatedTime;
	}
	
	public boolean isEstimated(int index){
		Node arrival = getArrivalNodes().item(index);
		String status = getAttributeValue(arrival, "status");
		
        return status.compareTo("estimated") == 0;
        
	}
	
	public boolean hasDetour(int index){
		Node arrival = getArrivalNodes().item(index);
		String status = getAttributeValue(arrival, "detour");
		
        return status.compareTo("true") == 0;
        
	}

	public int getNumArrivals() {
		return getArrivalNodes().getLength();
	}
	
	public ArrayList<String> getRouteList(){
		ArrayList<String> busRouteList = new ArrayList<String>();
		
		for (int index = 0; index < this.getArrivalNodes().getLength(); ++index) {
			String routeNumber = getAttributeValue(getArrivalNodes().item(index), "route");
			
			if (busRouteList.lastIndexOf((routeNumber)) < 0){
				busRouteList.add(routeNumber);
			}
		}
		
		Collections.sort(busRouteList);
		
		return busRouteList;
	}

	// Returns number of seconds since the time of the request
	public int getAge() {
		return (int) ((new Date().getTime() - mRequestTime)/1000);
	}

	public static boolean isNull() {
		return mXMLDoc == null;
	}
}
