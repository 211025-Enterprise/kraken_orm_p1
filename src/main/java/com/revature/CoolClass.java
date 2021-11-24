package com.revature;

import com.revature.PKeyAnnotation;

public class CoolClass {

	private int awesomeInt;

	@PKeyAnnotation
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

	public String getEpicString () {
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
