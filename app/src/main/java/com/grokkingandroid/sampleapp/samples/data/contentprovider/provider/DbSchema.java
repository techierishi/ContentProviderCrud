/*
 * Copyright (C) 2013 Wolfram Rittmeyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.grokkingandroid.sampleapp.samples.data.contentprovider.provider;

import android.provider.BaseColumns;

/**
 * A helper interface which defines constants for work with the DB.
 *
 * @author Wolfram Rittmeyer
 *
 */
/* package private */ interface DbSchema {

	String DB_NAME = "lentitems.db";
	
	String TBL_ITEMS = "items";
	String TBL_PHOTOS = "photos";
	
	String COL_ID = BaseColumns._ID;
	String COL_ITEM_NAME = "item_name";
	String COL_BORROWER = "borrower";
	String COL_DATA = "_data";
	String COL_ITEMS_ID = "items_id";
	
	// BE AWARE: Normally you would store the LOOKUP_KEY
	// of a contact from the device. But this would
	// have needless complicated the sample. Thus I
	// omitted it.
	String DDL_CREATE_TBL_ITEMS = 
			"CREATE TABLE items (" +
			"_id           INTEGER  PRIMARY KEY AUTOINCREMENT, \n" +
			"item_name     TEXT,\n" +
			"borrower      TEXT \n" +
			")";

	// BE AWARE: old sqlite versions didn't support referential
	// integrity; for this reasons I do _not_ use foreign keys!
	// I use triggers instead (see the sample trigger below).
	// 
	// If you only target newer Android versions you could
	// of course use proper foreign keys instead.
	String DDL_CREATE_TBL_PHOTOS = 
			"CREATE TABLE photos (" +
			"_id           INTEGER  PRIMARY KEY AUTOINCREMENT, \n" +
			"_data         TEXT,\n" +
			"items_id      INTEGER  NOT NULL  UNIQUE \n" +
			")";
		
	// The following trigger is here to show you how to
	// achieve referential integrity without foreign keys.
	String DDL_CREATE_TRIGGER_DEL_ITEMS = 
			  "CREATE TRIGGER delete_items DELETE ON items \n"
			+ "begin\n"
			+ "  delete from photos where items_id = old._id;\n"
			+ "end\n";
	
	String DDL_DROP_TBL_ITEMS =
			"DROP TABLE IF EXISTS items";

	String DDL_DROP_TBL_PHOTOS =
			"DROP TABLE IF EXISTS photos";
	
	String DDL_DROP_TRIGGER_DEL_ITEMS = 
			"DROP TRIGGER IF EXISTS delete_items";

	String DML_WHERE_ID_CLAUSE = "_id = ?";
	
	String DEFAULT_TBL_ITEMS_SORT_ORDER = "name ASC";
	
	String LEFT_OUTER_JOIN_STATEMENT = TBL_ITEMS + " LEFT OUTER JOIN " + TBL_PHOTOS + " ON(items._id = photos.items_id)";
}
