package com.revature;

import com.revature.persistence.ObjectDao;
import com.revature.CoolClass;

public class Main {
	public static void main(String[] args) {
		ObjectDao<CoolClass> dao = new ObjectDao<CoolClass>(CoolClass.class);
		CoolClass coolObject = new CoolClass();
		//dao.create(coolObject);
	}
}
