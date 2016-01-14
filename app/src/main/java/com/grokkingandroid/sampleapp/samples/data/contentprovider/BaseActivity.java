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
package com.grokkingandroid.sampleapp.samples.data.contentprovider;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.grokkingandroid.sampleapp.samples.data.contentprovider.about.AboutFragment;
import com.grokkingandroid.sampleapp.samples.data.contentprovider.description.DescriptionActivity;

public abstract class BaseActivity extends ActionBarActivity {
   
   @Override
   protected void onCreate(Bundle icicle) {
      super.onCreate(icicle);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_cpsampleactivity, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.about: 
         showAboutDialog();
         return true;
      case R.id.menu_description:
         showDescription();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

   private void showAboutDialog() {
      DialogFragment newFragment = AboutFragment.newInstance();
      newFragment.show(getSupportFragmentManager(), "dialog");
   }
   
   private void showDescription() {
      Intent descIntent = new Intent(this, DescriptionActivity.class);
      descIntent.putExtra(Constants.KEY_DESCRIPTION_ID, R.string.cpsample_contentprovidersampledemo_desc);
      descIntent.putExtra(Constants.KEY_LINK_TEXTS_ID, R.array.cpsample_link_texts);
      descIntent.putExtra(Constants.KEY_LINK_TARGETS_ID, R.array.cpsample_link_targets);
      startActivity(descIntent);
   }
   
}
