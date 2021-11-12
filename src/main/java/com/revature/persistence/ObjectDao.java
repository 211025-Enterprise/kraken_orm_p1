package com.revature.persistence;

import com.revature.util.ConnectionSingleton;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ObjectDao<T> {

	private final Class<T> objectClass;
	private final Field[] fields;
	private final Method[] methods;
	private final String tableName;
	private ArrayList<String> columnNames;
	private ArrayList<Method> columnGetters;
	private ArrayList<Method> columnSetters;

	public ObjectDao(Class<T> type) {
		this.objectClass = type;
		this.tableName = type.getSimpleName().toLowerCase();
		this.fields = type.getDeclaredFields();
		this.methods = type.getDeclaredMethods();
		for (int i=0; i<this.fields.length; i++) {
			this.fields[i].setAccessible(true);
		}
		for (int i=0; i<this.methods.length; i++) {
			this.methods[i].setAccessible(true);
		}

		this.columnNames = new ArrayList<String>();
		this.columnGetters = new ArrayList<Method>();
		this.columnSetters = new ArrayList<Method>();
		for (int i=0; i<this.fields.length; i++) {
			if (this.javaToSqlType(this.fields[i].getType()) != "") {
				Boolean hasGetter = false;
				Boolean hasSetter = false;
				for (int j=0; j<this.methods.length; j++) {
					if (this.methods[j].getName().toLowerCase().endsWith(this.fields[i].getName().toLowerCase()) && (this.methods[j].getName().length() == this.fields[i].getName().length() + 3)) {
						if (this.methods[j].getName().startsWith("get")) {
							this.columnSetters.add(this.methods[j]);
							hasGetter = true;
						} else if (this.methods[j].getName().startsWith("set")) {
							this.columnSetters.add(this.methods[j]);
							hasSetter = true;
						}
					}
				}
				if (!hasGetter || !hasSetter) {
					//throw custom exception
				}
				this.columnNames.add(this.fields[i].getName().toLowerCase());

			}
		}

		StringBuffer sqlBuff = new StringBuffer("create table if not exists ");
		sqlBuff.append(this.tableName).append(" (");
		for (int i=0; i<this.fields.length; i++) {
			if (this.javaToSqlType(this.fields[i].getType()) != "") {
				sqlBuff.append(this.columnNames.get(i)).append(" ").append(this.javaToSqlType(this.fields[i].getType()));
				if (i < this.columnNames.size() - 1) {
					sqlBuff.append(", ");
				} else {
					sqlBuff.append(")");
				}
			}
		}

		String sql = sqlBuff.toString();
		try (Connection connection = ConnectionSingleton.getConnection()) {
			assert connection != null;
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Attempted sql statement: " + sql);
			System.out.println("Table creation error");
			e.printStackTrace();
		}
	}

	private static String javaToSqlType(Type type) {
		if (type.equals(Byte.TYPE)) {
			return "tinyint";
		} else if (type.equals(Short.TYPE)) {
			return "smallint";
		} else if (type.equals(Integer.TYPE)) {
			return "int";
		} else if (type.equals(Long.TYPE)) {
			return "bigint";
		} else if (type.equals(Float.TYPE)) {
			return "double";
		} else if (type.equals(Double.TYPE)) {
			return "double";
		} else if (type.equals(Character.TYPE)) {
			return "char";
		} else if (type.equals(Boolean.TYPE)) {
			return "boolean";
		} else if (type.toString().equals(String.class.toString())) {
			return "varchar (255)";
		} else if (type.toString().equals(Date.class.toString())) {
			return "date";
		}
		return "";
	}

	public void create(T t) {
		StringBuffer sqlBuff = new StringBuffer("insert into ");
		sqlBuff.append(this.tableName).append("(");
		for (int i=0; i<this.columnNames.size(); i++) {
			sqlBuff.append(this.columnNames.get(i));
			if (i < this.columnNames.size() - 1) {
				sqlBuff.append(", ");
			} else {
				sqlBuff.append(") values (");
			}
		}
		for (int i=0; i<this.columnNames.size(); i++) {
			sqlBuff.append("?");
			if (i < this.columnNames.size() - 1) {
				sqlBuff.append(", ");
			} else {
				sqlBuff.append(")");
			}
		}
		String sql = sqlBuff.toString();
		try (Connection connection = ConnectionSingleton.getConnection()) {
			assert connection != null;
			PreparedStatement stmt = connection.prepareStatement(sql);
			for (int i=0; i<this.columnNames.size(); i++) {
				stmt.setObject(i, this.columnGetters.get(i).invoke(t));
			}
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Object creation exception.");
			e.printStackTrace();
		}
	}

}
