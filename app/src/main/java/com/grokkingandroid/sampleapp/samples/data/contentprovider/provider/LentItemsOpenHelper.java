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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The SQLiteOpenhelper implementation for lent items.
 *
 * @author Wolfram Rittmeyer
 *
 */
/* package */ class LentItemsOpenHelper extends SQLiteOpenHelper {

	private static final String NAME = DbSchema.DB_NAME;
	private static final int VERSION = 1;
	
	public LentItemsOpenHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DbSchema.DDL_CREATE_TBL_ITEMS);
		db.execSQL(DbSchema.DDL_CREATE_TBL_PHOTOS);
		db.execSQL(DbSchema.DDL_CREATE_TRIGGER_DEL_ITEMS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// this is no sample for how to handle SQLite databases
		// thus I simply drop and recreate the database here.
		//
		// NEVER do this in real apps. Your users wouldn't like
		// loosing data just because you decided to change the schema
		db.execSQL(DbSchema.DDL_DROP_TBL_ITEMS);
		db.execSQL(DbSchema.DDL_DROP_TBL_PHOTOS);
		db.execSQL(DbSchema.DDL_DROP_TRIGGER_DEL_ITEMS);
		onCreate(db);
	}

}
