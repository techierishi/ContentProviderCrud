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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.grokkingandroid.sampleapp.samples.data.contentprovider.BaseActivity;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.R;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.lentitems.LentItemDisplayFragment.DisplayCallback;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.lentitems.LentItemListFragment.MasterCallback;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.provider.LentItemsContract.Items;

/**
 * The main activity of this sample. Since this sample uses a listview
 * it's structure is vastly different from the other samples apps
 * of GrokkingAndroid and thus this activity as well as the fragments 
 * are structured differently as well.
 *
 * @author Wolfram Rittmeyer
 */
public class CPSampleActivity extends BaseActivity implements MasterCallback, DisplayCallback {

   private static final String KEY_SPINNER_POS = "keySpinnerPos"; 

   private int mCurrSpinnerPos= 0;
   private boolean mTwoPane = false;
   
   @Override
   public void onCreate(Bundle icicle) {
      super.onCreate(icicle);
      setContentView(R.layout.activity_fragment_container);

      if (getResources().getBoolean(R.bool.twoPane)) {
         mTwoPane = true;
      }
      if (icicle == null) {
         // config change
         LentItemListFragment fragment = LentItemListFragment.newInstance(mTwoPane);
         FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
         transaction.replace(R.id.demo_fragment_container, fragment);
         if (mTwoPane) {
            //TODO: add detail fragment; may be empty
            // detail fragment must be informed when loader of the listfragment ends 
            // to get the id of the first item - which is highlighted by default
            transaction.add(null, null);
         }
         transaction.commit();
      }
   }

   
   @Override
   protected void onDestroy() {
      super.onDestroy();
      // do not forget to clean up, if necessary
   }

   @Override
   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putInt(KEY_SPINNER_POS, mCurrSpinnerPos);
   }

   public void switchToForm(long itemId) {
      if (mTwoPane) {
         // swap fragments
      } else {
         // call formactivity
         Fragment f = LentItemFormFragment.newInstance(itemId);
         FragmentTransaction transact = getSupportFragmentManager().beginTransaction();
         transact.replace(R.id.demo_fragment_container, f);
         transact.addToBackStack("cpsample");
         transact.commit();
      }
   }

   public void displayItem(long itemId) {
      if (mTwoPane) {
         // swap fragments
      } else {
         // call formactivity
         Fragment f = LentItemDisplayFragment.newInstance(itemId);
         FragmentTransaction transact = getSupportFragmentManager().beginTransaction();
         transact.replace(R.id.demo_fragment_container, f);
         transact.addToBackStack("cpsample");
         transact.commit();
      }
   }

   @Override
   public void addItem() {
      // it's the addition of a new item, so the id to pass is -1
      Fragment f = LentItemFormFragment.newInstance(-1);
      FragmentTransaction transact = getSupportFragmentManager().beginTransaction();
      transact.replace(R.id.demo_fragment_container, f);
      transact.addToBackStack("cpsample");
      transact.commit();
   }

   public void deleteItem(final long itemId) {
      final ContentResolver resolver = getContentResolver();
      new Thread(new Runnable() {
         // for as long as this runs the activity will not be GCed
         public void run() {
            Uri delUri = ContentUris.withAppendedId(Items.CONTENT_URI, itemId);
            int resCount = resolver.delete(delUri, null, null);
            if (resCount == 0) {
               // do s.th. useful
            }
         }
      }).start();
      getSupportFragmentManager().popBackStack();
   }
   
}
