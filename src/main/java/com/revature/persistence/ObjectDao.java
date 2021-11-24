package com.revature.persistence;

import com.revature.util.ConnectionSingleton;
import com.revature.PKeyAnnotation;

import java.lang.reflect.*;
import java.lang.annotation.*;
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
	private Field primary_key;
	private Method keyGetter;
	private Method keySetter;
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
			System.out.println(i);
			if (this.javaToSqlType(this.fields[i].getType()) != "") {
				System.out.println(i);
				Boolean hasGetter = false;
				Boolean hasSetter = false;

				Boolean isPKey = false;
				Annotation[] annotations = this.fields[i].getDeclaredAnnotations();
				for (int j=0; j<annotations.length; j++) {
					if (annotations[j].annotationType().equals(PKeyAnnotation.class)) {
						this.primary_key = this.fields[i];
						isPKey = true;
					}
				}

				for (int j=0; j<this.methods.length; j++) {
					if (this.methods[j].getName().toLowerCase().endsWith(this.fields[i].getName().toLowerCase()) && (this.methods[j].getName().length() == this.fields[i].getName().length() + 3)) {
						if (this.methods[j].getName().startsWith("get")) {
							this.columnGetters.add(this.methods[j]);
							hasGetter = true;
							if (isPKey) {
								this.keyGetter = this.methods[j];
							}
						} else if (this.methods[j].getName().startsWith("set")) {
							this.columnSetters.add(this.methods[j]);
							hasSetter = true;
							if (isPKey) {
								this.keySetter = this.methods[j];
							}
						}
					}
				}
				if (!hasGetter || !hasSetter) {
					System.out.println("No getter and/or setter! This might cause problems in CRUD operations.");
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
		} else if (type.toString().equals(Boolean.class.toString())) {
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
				stmt.setObject((i + 1), this.columnGetters.get(i).invoke(t));
			}
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Object creation exception.");
			e.printStackTrace();
		}
	}

	public T getById (Object o) {
		if (!o.getClass().equals(this.primary_key.getType())) {
			return null;
		}
		StringBuffer sqlBuff = new StringBuffer("select * from ");
		sqlBuff.append(this.tableName).append(" where ").append(this.primary_key.getName().toLowerCase()).append("=?");
		String sql = sqlBuff.toString();
		try (Connection connection = ConnectionSingleton.getConnection()) {
			assert connection != null;
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setObject(1, o);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				T t = (T) this.objectClass.getConstructor().newInstance();
				for (int i=0; i<this.columnNames.size(); i++) {
					this.columnSetters.get(i).invoke(t, rs.getObject(i+1));
				}
				return t;
			}
		} catch (Exception e) {
			System.out.println("Object get by id exception.");
			e.printStackTrace();
		}
		return null;
	}

	public void update (T t) {
		StringBuffer sqlBuff = new StringBuffer("update ");
		sqlBuff.append(this.tableName).append(" set ");
		for (int i=0; i<this.columnNames.size(); i++) {
			sqlBuff.append(this.columnNames.get(i)).append("=?");
			if (i < this.columnNames.size() - 1) {
				sqlBuff.append(", ");
			}
		}
		sqlBuff.append(" where ").append(this.primary_key.getName().toLowerCase()).append("=?");
		String sql = sqlBuff.toString();
		try (Connection connection = ConnectionSingleton.getConnection()) {
			assert connection != null;
			PreparedStatement stmt = connection.prepareStatement(sql);
			for (int i=0; i<this.columnNames.size(); i++) {
				stmt.setObject((i + 1), this.columnGetters.get(i).invoke(t));
			}
			stmt.setObject((this.columnNames.size() + 1), this.keyGetter.invoke(t));
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("Object update exception.");
			e.printStackTrace();
		}
	}

	public void deleteById (Object o) {
		if (o.getClass().equals(this.primary_key.getType())) {
			StringBuffer sqlBuff = new StringBuffer("delete from ");
			sqlBuff.append(this.tableName).append(" where ").append(this.primary_key.getName().toLowerCase()).append("=?");
			String sql = sqlBuff.toString();
			try (Connection connection = ConnectionSingleton.getConnection()) {
				assert connection != null;
				PreparedStatement stmt = connection.prepareStatement(sql);
				stmt.setObject(1, o);
				stmt.executeUpdate();
			} catch (Exception e) {
				System.out.println("Object delete exception.");
				e.printStackTrace();
			}
		}
	}
}
