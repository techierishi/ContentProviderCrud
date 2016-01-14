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
package com.grokkingandroid.sampleapp.samples.data.contentprovider.description;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.grokkingandroid.sampleapp.samples.data.contentprovider.BaseActivity;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.Constants;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.R;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.lentitems.CPSampleActivity;

public class DescriptionActivity extends BaseActivity {

   public static final String EXTRA_DESC_INSTANCE = "extraDescInstance";
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      Bundle extras = getIntent().getExtras();
      int descId = extras.getInt(Constants.KEY_DESCRIPTION_ID);
      int linkTextsId = extras.getInt(Constants.KEY_LINK_TEXTS_ID);
      int linkTargetsId = extras.getInt(Constants.KEY_LINK_TARGETS_ID);
      setContentView(R.layout.activity_fragment_container);
      if (savedInstanceState == null) {
         DescriptionFragment fragment = DescriptionFragment.newInstance(descId, linkTextsId, linkTargetsId);
         getSupportFragmentManager()
               .beginTransaction()
               .add(R.id.demo_fragment_container, fragment)
               .commit();
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this,
                  new Intent(this, CPSampleActivity.class));
            return true;
      }
      return super.onOptionsItemSelected(item);
   }  

}
