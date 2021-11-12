package com.revature.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionSingleton {

	//Database login information
	private static final String url = "jdbc:postgresql://rev-db.c8olu3mzknhv.us-west-2.rds.amazonaws.com:5432/postgres?currentSchema=orm_example";
	private static final String username = "krakendb";
	private static final String password = "Revature1089";

	private static Connection instance;

	private ConnectionSingleton(){}

	//Connect to postgresql database
	public static Connection getConnection(){
		if(instance == null){
			//If there's no connection yet, try to make one
			try {
				Class.forName("org.postgresql.Driver");
				instance = DriverManager.getConnection(url, username, password);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			try {
				//If there's already a connection but it's been closed, open a new one
				if (instance.isClosed()) {
					Class.forName("org.postgresql.Driver");
					instance = DriverManager.getConnection(url, username, password);
				}
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}
}
