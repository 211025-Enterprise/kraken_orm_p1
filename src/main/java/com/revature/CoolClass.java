package com.revature;

public class CoolClass {
	private int awesomeInt;
	public String epicString;
	protected static Boolean coolBool;

	public CoolClass () {

	}

	private int getAwesomeInt () {
		return this.awesomeInt;
	}

	private void setAwesomeInt (int a) {
		this.awesomeInt = a;
	}

	private String getEpicString () {
		return this.epicString;
	}

	public void setEpicString (String s) {
		this.epicString = s;
	}

	protected Boolean getCoolBool () {
		return this.coolBool;
	}

	public void setCoolBool (Boolean b) {
		this.coolBool = b;
	}
}
