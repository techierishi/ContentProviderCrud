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
package com.grokkingandroid.sampleapp.samples.data.contentprovider.lentitems;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.grokkingandroid.sampleapp.samples.data.contentprovider.R;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.provider.LentItemsContract.ItemEntities;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.provider.LentItemsContract.Items;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.provider.LentItemsContract.Photos;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.util.DateUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class LentItemFormFragment extends Fragment implements LoaderCallbacks<Cursor> {

   /**
    * The identifier for the Photo-Activity used
    * to distinguish activity results.
    */
   private static final int RC_PHOTO = 10;
   /**
    * The unique identifier of the loader.
    */
   private static final int ENTITY_LOADER = 20;
   /**
    * The key for storing the current item id.
    */
   private static final String KEY_ITEM_ID = "keyItemid";
   /**
    * The key for storing whether startActivityForResult has been called.
    */
   private static final String KEY_ACTIVITY_FOR_RESULT_STARTED = "keyActivityForResultStarted";
   /**
    * The key for storing the two pane mode.
    */
   private static final String KEY_PHOTO_URI = "keyPhotouri";
   /**
    * The id of the currently active lent item. -1 if this is a form for a new item.
    */
   private long mItemId = -1;
   /**
    * The EditText field for the name.
    */
   private EditText mTxtName;
   /**
    * The EditText field for the borrower.
    */
   private EditText mTxtBorrower;
   /**
    * The (initially hidden) label for the photo.
    */
   private TextView mTxtPhotoLabel;
   /**
    * The (initially hidden) Imageview for the photo.
    */
   private ImageView mImgPhoto;
   /**
    * The Uri of the attached photo.
    */
   private Uri mPhotoUri;
   /**
    * The previous Uri of the attached photo.
    */
   private Uri mOldPhotoUri;
   /**
    * A flag indicating whether startActivityForResult has been called.
    */
   private boolean mWasActivityForResultStarted = false;
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (getArguments() != null) {
         mItemId = getArguments().getLong(KEY_ITEM_ID, -1);
      }
      if (savedInstanceState != null) {
         if (savedInstanceState.containsKey(KEY_ITEM_ID)) {
            mItemId = savedInstanceState.getLong(KEY_ITEM_ID, mItemId);
         }
         if (savedInstanceState.containsKey(KEY_PHOTO_URI)) {
            mPhotoUri = Uri.parse(savedInstanceState.getString(KEY_PHOTO_URI));
         }
         mWasActivityForResultStarted = savedInstanceState.getBoolean(KEY_ACTIVITY_FOR_RESULT_STARTED, false);
      }
      setHasOptionsMenu(true);
   }
   
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      inflater.inflate(R.menu.menu_detail_form_fragment, menu);
      if (mItemId == -1) {
         menu.removeItem(R.id.menu_delete);
      }
      if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
            || !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
         menu.removeItem(R.id.menu_photo);
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.menu_delete:
         if (mItemId != -1) {
            ((DetailCallback)getActivity()).deleteItem(mItemId);
         }
         break;
      case R.id.menu_photo:
         addPhoto();
         break;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_cpsample_detail_form, container, false);
      mTxtBorrower = (EditText)rootView.findViewById(R.id.txtBorrower);
      mTxtName = (EditText)rootView.findViewById(R.id.txtItemName);
      mTxtPhotoLabel = (TextView)rootView.findViewById(R.id.labelPhoto);
      mImgPhoto = (ImageView)rootView.findViewById(R.id.imgPhoto);
      return rootView;
   }
   
   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      if (mItemId != -1) {
         getLoaderManager().initLoader(ENTITY_LOADER, null, this);
      }
   }

   @Override
   public void onPause() {
      super.onPause();
      if (getActivity() != null && !mWasActivityForResultStarted) {
         Intent intent = new Intent(getActivity(), LentItemService.class);
         intent.putExtra(Items.NAME, mTxtName.getText().toString());
         intent.putExtra(Items.BORROWER, mTxtBorrower.getText().toString());
         if (mPhotoUri != null) {
            intent.putExtra(Photos._DATA, mPhotoUri.toString());
         }
         if (mItemId == -1) {
            intent.setAction(LentItemService.ACTION_CREATE_ITEM);
         } else {
            intent.putExtra(BaseColumns._ID, mItemId);
            intent.setAction(LentItemService.ACTION_UPDATE_ITEM);
         }
         getActivity().startService(intent);
      }
   }

   @Override
   public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
      Uri loaderUri = ContentUris.withAppendedId(ItemEntities.CONTENT_URI, mItemId);
      return new CursorLoader(
            getActivity(), 
            loaderUri, 
            ItemEntities.PROJECTION_ALL, 
            null, 
            null, 
            null);
   }

   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
      if (cursor != null && cursor.moveToFirst()) {
         mTxtName.setText(cursor.getString(1));
         mTxtBorrower.setText(cursor.getString(2));
         String uriString = cursor.getString(3);
         loadImage(uriString);
      }
   }
   
   private void loadImage(String uriString) {
      if (!TextUtils.isEmpty(uriString)) {
         mPhotoUri = Uri.parse(uriString);
         ImageHelper.loadImage(mPhotoUri, mImgPhoto, mTxtPhotoLabel);
      }
   }

   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
   }
   
   private void addPhoto() {
      // checking for intent availability is ignored here
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      mOldPhotoUri = mPhotoUri;
      mPhotoUri = createPhotoUri();
      intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
      mWasActivityForResultStarted = true;
      startActivityForResult(intent, RC_PHOTO);
   }

   private Uri createPhotoUri() {
      File file = null;
      int rand = new Random().nextInt(Integer.MAX_VALUE);
      String simpleFilename = DateUtils.formatDateTimeForIO(new Date()) + "-" + rand + ".jpg";
      file = new File(getDir(), simpleFilename);      
      return Uri.fromFile(file);
   }

   private File getDir() {
      return getAndCreateDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
   }

   private File getAndCreateDirectory(File dir) {
      if (!dir.exists()) {
         if (!dir.mkdirs()) {
            try {
               Log.w("cpsample", "couldn't create dir " + dir.getCanonicalPath());
            } catch (IOException e) {
               // looks like we're in deep trouble
               Log.e("cpsample", "YIKES! Can't even access canonicalPath of the dir to create");
            }
         }
      }
      return dir;
   }
   
	@Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      mWasActivityForResultStarted = false;
      if (requestCode == RC_PHOTO) {
         if (resultCode == Activity.RESULT_OK) {
            // the loader still holds the old pic;
            // we haven't saved the new one yet, so
            // discard the previous results
            getLoaderManager().destroyLoader(ENTITY_LOADER);
            // display new image
            ImageHelper.loadImage(mPhotoUri, mImgPhoto, mTxtPhotoLabel);
         }
         else {
            mPhotoUri = mOldPhotoUri;
         }
         mOldPhotoUri = null;
      }
   }

	/**
	 * Create a new instance of this fragment. 
	 * 
	 * @param itemId The id of an item to edit. -1 for a new item.
	 */
   public static LentItemFormFragment newInstance(long itemId) {
      LentItemFormFragment f = new LentItemFormFragment();
      Bundle bundle = new Bundle();
      bundle.putLong(KEY_ITEM_ID, itemId);
      f.setArguments(bundle);
      return f;
	}

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putBoolean(KEY_ACTIVITY_FOR_RESULT_STARTED, mWasActivityForResultStarted);
      if (mPhotoUri != null) {
         outState.putString(KEY_PHOTO_URI, mPhotoUri.toString());
      }
      outState.putLong(KEY_ITEM_ID, mItemId);
   }

   public void onEvent(Long id) {
      if (id != null) {
         this.mItemId = id;
      }
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      EventBus.getDefault().register(this, 1);
   }

   @Override
   public void onDetach() {
      super.onDetach();
      EventBus.getDefault().unregister(this);
   }
}
