package com.beagleapps.android.trimettracker.objects;

public class HistoryEntry extends Favorite {
	private  int visits;
	private int lastVisited;
	private int order;
	
	public int getVisits() {
		return visits;
	}
	public void setVisits(int visits) {
		this.visits = visits;
	}
	public int getLastVisited() {
		return lastVisited;
	}
	public void setLastVisited(int lastVisited) {
		this.lastVisited = lastVisited;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	
}
