package com.revature;

import com.revature.persistence.ObjectDao;
import com.revature.CoolClass;

public class Main {
	public static void main(String[] args) {
		ObjectDao<CoolClass> dao = new ObjectDao<CoolClass>(CoolClass.class);
		CoolClass coolObject = new CoolClass();
		//coolObject.setAwesomeInt(24);
		coolObject.setEpicString("hi");
		dao.create(coolObject);
		CoolClass coolObject2 = new CoolClass();
		coolObject2.setEpicString("howdy");
		CoolClass awesomeObject = dao.getById("hi");
		System.out.println(awesomeObject.getEpicString());
	}
}
