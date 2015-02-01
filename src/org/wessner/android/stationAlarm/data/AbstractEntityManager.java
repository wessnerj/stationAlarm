/**
 * stationAlarm - Android app which wakes you before you reach your target station.
 * Copyright (C) 2015  Joseph Wessner <joseph@wessner.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wessner.android.stationAlarm.data;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Abstract class for an entity manager.
 * 
 * @author Joseph Wessner <joseph@wessner.org>
 */
public abstract class AbstractEntityManager<T extends AbstractEntity> {
	/**
	 * DataBaseHelper used for database communication
	 */
	protected DataBaseHelper dbh;
	/**
	 * Name of the default PRIMARY KEY
	 */
	protected final static String primaryKey = "_id";
	/**
	 * Table name where the entities are stored
	 */
	protected final String tableName;
	/**
	 * Columns used for queries
	 */
	public final String[] columns;
	
	/**
	 * Method to convert one row of the table to one entity
	 * 
	 * @param c
	 *            result cursor
	 * @return
	 */
	abstract protected T cursorToItem(Cursor c);

	/**
	 * Default constructor
	 * 
	 * @param dbh
	 * @param tableName
	 */
	public AbstractEntityManager(DataBaseHelper dbh, String tableName, String[] columns) {
		this.dbh = dbh;
		this.tableName = tableName;
		this.columns = columns;
	}
	
	/**
	 * Get one entry by primaryKey (id field)
	 * 
	 * @param id
	 * @return
	 */
	public T get(long id) {
		String where = primaryKey + " = ?";
		String[] whereArgs = { "" + id };

		T entity = null;

		SQLiteDatabase db = this.dbh.getWritableDatabase();
		Cursor result = db.query(this.tableName, this.columns, where, whereArgs, null, null, null, "1");
		result.moveToFirst();

		if (result.isAfterLast() != true) {
			entity = this.cursorToItem(result);
		}
		result.close();
		db.close();

		return entity;
	}
	
	/**
	 * Get ArrayList with all entities in database
	 * 
	 * @return
	 */
	public ArrayList<T> getAll() {
		return getAll(null);
	}
	
	public Cursor getAllCursor() {
		return getAllCursor(null);
	}
	
	public Cursor getAllCursor(String orderBy) {
		SQLiteDatabase db = this.dbh.getWritableDatabase();
		return db.query(this.tableName, this.columns, null, null, null, null, orderBy, null);
	}
	
	/**
	 * Get ArrayList with all entities in database
	 * 
	 * @return
	 */
	public ArrayList<T> getAll(String orderBy) {
		ArrayList<T> entities = new ArrayList<T>();
		
		Cursor result = getAllCursor(orderBy);
		result.moveToFirst();
		
		while (result.isAfterLast() != true) {
			entities.add(this.cursorToItem(result));

			result.moveToNext();
		}
		result.close();
		// db.close();

		return entities;
	}
	
	/**
	 * Delete single entity from database
	 * 
	 * @param entity
	 * @return number of deleted rows
	 */
	public int delete(T entity) {
		return this.delete(entity._id);
	}

	/**
	 * Delete single entity by id
	 * 
	 * @param id
	 * @return number of deleted rows
	 */
	public int delete(long id) {
		SQLiteDatabase db = this.dbh.getWritableDatabase();

		String where = primaryKey + " = ?";
		String[] whereArgs = { "" + id };
		int del = db.delete(tableName, where, whereArgs);

		db.close();

		return del;
	}
}
