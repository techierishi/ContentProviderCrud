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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.grokkingandroid.sampleapp.samples.data.contentprovider.R;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.provider.LentItemsContract.Items;

public class LentItemListFragment extends ListFragment implements
      LoaderCallbacks<Cursor> {

   interface MasterCallback {
      void addItem();
      void displayItem(long itemId);
   }
   
   /**
    * The key for storing fragment state
    */
   private static final String KEY_STATE_ACTIVATED_POSITION = "activated_position";
   /**
    * The key for storing the two pane mode
    */
   private static final String KEY_TWOPANE = "keyTwoPane";
   /**
    * The unique identifier of the loader.
    */
   private static final int ITEMLIST_LOADER = 10;
   /**
    * The position of the currently activated item in the listview
    */
   private int mActivatedPosition = ListView.INVALID_POSITION;
   /**
    * The callback for this fragment to handle user interaction.
    */
   private MasterCallback mCallback; 
   

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setHasOptionsMenu(true);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      if (!(activity instanceof MasterCallback)) {
         throw new IllegalStateException("Embedding activity must implement " + MasterCallback.class.getName());
      }
      mCallback = (MasterCallback)activity;
   }
   
   
   @Override
   public void onDetach() {
      mCallback = null;
      super.onDetach();
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      SimpleCursorAdapter adapter = 
            new SimpleCursorAdapter(
                  getActivity(), 
                  android.R.layout.simple_list_item_1, 
                  (Cursor)null, 
                  new String[]{Items.NAME},
                  new int[]{android.R.id.text1},
                  0);
      this.setListAdapter(adapter);
      getLoaderManager().initLoader(ITEMLIST_LOADER, null, this);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_cpsample_master, container, false);
   }

   @Override
   public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      // Restore the previously serialized activated item position.
      if (savedInstanceState != null
            && savedInstanceState.containsKey(KEY_STATE_ACTIVATED_POSITION)) {
         setActivatedPosition(savedInstanceState
               .getInt(KEY_STATE_ACTIVATED_POSITION));
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      if (mActivatedPosition != ListView.INVALID_POSITION) {
         // Serialize and persist the activated item position.
         outState.putInt(KEY_STATE_ACTIVATED_POSITION, mActivatedPosition);
      }
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      inflater.inflate(R.menu.menu_master_fragment, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.menu_add:
         ((MasterCallback)getActivity()).addItem();
         break;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onListItemClick(ListView listView, View view, int position, long id) {
       super.onListItemClick(listView, view, position, id);
       this.setActivatedPosition(position);
       mCallback.displayItem(id);
   }
   
   private void setActivatedPosition(int position) {
      if (position == ListView.INVALID_POSITION) {
         getListView().setItemChecked(mActivatedPosition, false);
      } else {
         getListView().setItemChecked(position, true);
      }
      mActivatedPosition = position;
   }

   @Override
   public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
      if (getActivity() != null) {
         return new CursorLoader(getActivity(), Items.CONTENT_URI, Items.PROJECTION_ALL, null, null, Items.SORT_ORDER_DEFAULT);
      }
      else {
         return null;
      }
   }

   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
      if (getListAdapter() != null) {
         ((SimpleCursorAdapter)this.getListAdapter()).swapCursor(cursor);
      }
   }

   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
      ((SimpleCursorAdapter)this.getListAdapter()).swapCursor(null);
   }

   public static LentItemListFragment newInstance(boolean isInTwoPaneMode) {
      LentItemListFragment f = new LentItemListFragment();
      Bundle bundle = new Bundle();
      bundle.putBoolean(KEY_TWOPANE, isInTwoPaneMode);
      f.setArguments(bundle);
      return f;
   }

}
