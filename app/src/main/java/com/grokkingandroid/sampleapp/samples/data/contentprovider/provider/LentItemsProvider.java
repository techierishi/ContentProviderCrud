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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.grokkingandroid.sampleapp.samples.data.contentprovider.BuildConfig;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.provider.LentItemsContract.ItemEntities;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.provider.LentItemsContract.Items;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.provider.LentItemsContract.Photos;

/**
 * The actual provider class for the lentitems provider. Clients do not use it directly. Nor
 * do they see it.
 *
 * @author Wolfram Rittmeyer
 */
public class LentItemsProvider extends ContentProvider {

	// helper constants for use with the UriMatcher
	private static final int ITEM_LIST = 1;
	private static final int ITEM_ID = 2;
	private static final int PHOTO_LIST = 5;
	private static final int PHOTO_ID = 6;
	private static final int ENTITY_LIST = 10;
	private static final int ENTITY_ID = 11;
   private static final UriMatcher URI_MATCHER;
	
	private LentItemsOpenHelper mHelper = null;
   private final ThreadLocal<Boolean> mIsInBatchMode = new ThreadLocal<Boolean>();
	
	// prepare the UriMatcher
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(LentItemsContract.AUTHORITY, "items", ITEM_LIST);
		URI_MATCHER.addURI(LentItemsContract.AUTHORITY, "items/#", ITEM_ID);
		URI_MATCHER.addURI(LentItemsContract.AUTHORITY, "photos", PHOTO_LIST);
		URI_MATCHER.addURI(LentItemsContract.AUTHORITY, "photos/#", PHOTO_ID);
      URI_MATCHER.addURI(LentItemsContract.AUTHORITY, "entities", ENTITY_LIST);
      URI_MATCHER.addURI(LentItemsContract.AUTHORITY, "entities/#", ENTITY_ID);
	}

	@Override
	public boolean onCreate() {
		mHelper = new LentItemsOpenHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
	   doAnalytics(uri, "delete");
	   
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int delCount = 0;
		switch (URI_MATCHER.match(uri)) {
		case ITEM_LIST:
			delCount = db.delete(DbSchema.TBL_ITEMS, selection, selectionArgs);
			break;
		case ITEM_ID:
			String idStr = uri.getLastPathSegment();
			String where = Items._ID + " = " + idStr;
			if (!TextUtils.isEmpty(selection)) {
				where += " AND " + selection;
			}
			delCount = db.delete(DbSchema.TBL_ITEMS, where, selectionArgs);
			break;
		default:
			// no support for deleting photos or entities -
			// photos are deleted by a trigger when the item is deleted
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		// notify all listeners of changes:
		if (delCount > 0 && !isInBatchMode()) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return delCount;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case ITEM_LIST:
			return Items.CONTENT_TYPE;
		case ITEM_ID:
			return Items.CONTENT_ITEM_TYPE;
		case PHOTO_ID:
			return Photos.CONTENT_PHOTO_TYPE;
		case PHOTO_LIST:
			return Photos.CONTENT_TYPE;
      case ENTITY_ID:
         return ItemEntities.CONTENT_ENTITY_TYPE;
      case ENTITY_LIST:
         return ItemEntities.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
      doAnalytics(uri, "insert");
		if (URI_MATCHER.match(uri) != ITEM_LIST
				&& URI_MATCHER.match(uri) != PHOTO_LIST) {
			throw new IllegalArgumentException(
					"Unsupported URI for insertion: " + uri);
		}
		SQLiteDatabase db = mHelper.getWritableDatabase();
		if (URI_MATCHER.match(uri) == ITEM_LIST) {
			long id = db.insert(DbSchema.TBL_ITEMS, null, values);
			return getUriForId(id, uri);
		} else {
			// this insertWithOnConflict is a special case; CONFLICT_REPLACE
			// means that an existing entry which violates the UNIQUE constraint
			// on the item_id column gets deleted. That is this INSERT behaves
			// nearly like an UPDATE. Though the new row has a new primary key.
		   // See how I mentioned this in the Contract class.
			long id = db.insertWithOnConflict(DbSchema.TBL_PHOTOS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
         return getUriForId(id, uri);
		}
	}
	
	private Uri getUriForId(long id, Uri uri) {
      if (id > 0) {
         Uri itemUri = ContentUris.withAppendedId(uri, id);
         if (!isInBatchMode()) {
            // notify all listeners of changes and return itemUri:
            getContext().
                  getContentResolver().
                        notifyChange(itemUri, null);
         }
         return itemUri;
      }
      // s.th. went wrong:
      throw new SQLException("Problem while inserting into uri: " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
	   doAnalytics(uri, "query");

		SQLiteDatabase db = mHelper.getReadableDatabase();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
	   boolean useAuthorityUri = false;
		switch (URI_MATCHER.match(uri)) {
		case ITEM_LIST:
	      builder.setTables(DbSchema.TBL_ITEMS);
	      if (TextUtils.isEmpty(sortOrder)) {
	         sortOrder = Items.SORT_ORDER_DEFAULT;
	      }
			break;
		case ITEM_ID:
	      builder.setTables(DbSchema.TBL_ITEMS);
			// limit query to one row at most:
			builder.appendWhere(Items._ID + " = "
					+ uri.getLastPathSegment());
			break;
		case PHOTO_LIST:
			builder.setTables(DbSchema.TBL_PHOTOS);
			break;
		case PHOTO_ID:
			builder.setTables(DbSchema.TBL_PHOTOS);
			// limit query to one row at most:
			builder.appendWhere(Photos._ID + " = " + uri.getLastPathSegment());
			break;
      case ENTITY_LIST:
         builder.setTables(DbSchema.LEFT_OUTER_JOIN_STATEMENT);
         if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = ItemEntities.SORT_ORDER_DEFAULT;
         }
         useAuthorityUri = true;
         break;
      case ENTITY_ID:
         builder.setTables(DbSchema.LEFT_OUTER_JOIN_STATEMENT);
         // limit query to one row at most:
         builder.appendWhere(DbSchema.TBL_ITEMS + "." + Items._ID + " = " + uri.getLastPathSegment());
         useAuthorityUri = true;
         break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		// if you like you can log the query
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		   logQuery(builder,  projection, selection, sortOrder);
		}
		else {
		   logQueryDeprecated(builder, projection, selection, sortOrder);
		}
		Cursor cursor = builder.query(db, projection, selection, selectionArgs,
				null, null, sortOrder);
		// if we want to be notified of any changes:
	   if (useAuthorityUri) {
         cursor.setNotificationUri(getContext().getContentResolver(), LentItemsContract.CONTENT_URI);
	   }
	   else {
	      cursor.setNotificationUri(getContext().getContentResolver(), uri);
	   }
		return cursor;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
   private void logQuery(SQLiteQueryBuilder builder, String[] projection, String selection, String sortOrder) {
	   if (BuildConfig.DEBUG) {
	      Log.v("cpsample", "query: " + builder.buildQuery(projection, selection, null, null, sortOrder, null));
	   }
	}

	@SuppressWarnings("deprecation")
   private void logQueryDeprecated(SQLiteQueryBuilder builder, String[] projection, String selection, String sortOrder) {
      if (BuildConfig.DEBUG) {
         Log.v("cpsample", "query: " + builder.buildQuery(projection, selection, null, null, null, sortOrder, null));
      }
   }
	
   @Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
      doAnalytics(uri, "update");
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int updateCount = 0;
		switch (URI_MATCHER.match(uri)) {
		case ITEM_LIST:
			updateCount = db.update(DbSchema.TBL_ITEMS, values, selection,
					selectionArgs);
			break;
		case ITEM_ID:
			String idStr = uri.getLastPathSegment();
			String where = Items._ID + " = " + idStr;
			if (!TextUtils.isEmpty(selection)) {
				where += " AND " + selection;
			}
			updateCount = db.update(DbSchema.TBL_ITEMS, values, where,
					selectionArgs);
			break;
		default:
			// no support for updating photos!
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		// notify all listeners of changes:
		if (updateCount > 0 && !isInBatchMode()) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return updateCount;
	}
   
   @Override
   public ContentProviderResult[] applyBatch(
         ArrayList<ContentProviderOperation> operations)
         throws OperationApplicationException {
      SQLiteDatabase db = mHelper.getWritableDatabase();
      mIsInBatchMode.set(true);
      // the next line works because SQLiteDatabase 
      // uses a thread local SQLiteSession object for 
      // all manipulations
      db.beginTransaction();
      try {
         final ContentProviderResult[] retResult = super.applyBatch(operations);
         db.setTransactionSuccessful();
         getContext().getContentResolver().notifyChange(LentItemsContract.CONTENT_URI, null);
         return retResult;
      }
      finally {
         mIsInBatchMode.remove();
         db.endTransaction();       
      }
   }

   private boolean isInBatchMode() {
      return mIsInBatchMode.get() != null && mIsInBatchMode.get();
   }

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
	   doAnalytics(uri, "openFile");
		if (URI_MATCHER.match(uri) != PHOTO_ID) {
			throw new IllegalArgumentException(
					"URI invalid. Use an id-based URI only.");
		}
		return openFileHelper(uri, mode);
	}

	/**
	 * I do not really use analytics, but if you export
	 * your content provider it makes sense to do so, to get
	 * a feeling for client usage. Especially if you want to
	 * _change_ something which might break existing clients,
	 * please check first if you can safely do so.
	 */
	private void doAnalytics(Uri uri, String event) {
	   if (BuildConfig.DEBUG) {
         Log.v("cpsample", event + " -> " + uri);
         Log.v("cpsample", "caller: " + detectCaller());
	   }
	}
	
   /** 
    * You can use this for Analytics. 
    * 
    * Be aware though: This might be costly if many apps 
    * are running.
    */
   private String detectCaller() {
      // found here:
      // https://groups.google.com/forum/#!topic/android-developers/0HsvyTYZldA
      int pid = Binder.getCallingPid();
      return getProcessNameFromPid(pid);
   }

   /**
    * Returns the name of the process the pid belongs to. Can be null if neither
    * an Activity nor a Service could be found.
    * @param givenPid
    * @return
    */
   private String getProcessNameFromPid(int givenPid) {
      ActivityManager am = (ActivityManager) getContext().getSystemService(
            Activity.ACTIVITY_SERVICE);
      List<ActivityManager.RunningAppProcessInfo> lstAppInfo = am
            .getRunningAppProcesses();
      for (ActivityManager.RunningAppProcessInfo ai : lstAppInfo) {
         if (ai.pid == givenPid) {
            return ai.processName;
         }
      }
      // added to take care of calling services as well:
      List<ActivityManager.RunningServiceInfo> srvInfo = am
            .getRunningServices(Integer.MAX_VALUE);
      for (ActivityManager.RunningServiceInfo si : srvInfo) {
         if (si.pid == givenPid) {
            return si.process;
         }
      }
      return null;
   }
	
}
