package com.revature.persistence;

//Dao object to serve as a buffer between program and persistent data
public interface Dao<T> {
	// create
	int create(T t);

	// read
	T getById(int id);

	// update
	boolean update(T t);

	// delete
	boolean deleteById(int id);
}
