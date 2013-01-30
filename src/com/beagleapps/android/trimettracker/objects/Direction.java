package com.beagleapps.android.trimettracker.objects;

public class Direction {
	private int direction;
	private String description;
	
	Direction(){
		
	}
	
	public Direction(String desc, int dir){
		setDir(dir);
		setDesc(desc);
	}

	public int getDir() {
		return direction;
	}

	public void setDir(int mDir) {
		this.direction = mDir;
	}

	public String getDesc() {
		return description;
	}

	public void setDesc(String mDesc) {
		this.description = mDesc;
	}
}
