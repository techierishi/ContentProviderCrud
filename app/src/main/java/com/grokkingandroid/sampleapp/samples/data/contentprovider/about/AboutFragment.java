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
package com.grokkingandroid.sampleapp.samples.data.contentprovider.about;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grokkingandroid.sampleapp.samples.data.contentprovider.R;


public class AboutFragment extends DialogFragment {

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
      getDialog().setTitle(getResources().getString(R.string.cpsample_about));
      View view = inflater.inflate(R.layout.fragment_about, container, false);
      ViewGroup libParent = (ViewGroup)view.findViewById(R.id.about_container);

      String[] libTitles = getResources().getStringArray(R.array.cpsample_about_titles);
      String[] libDescriptions = getResources().getStringArray(R.array.cpsample_about_contents);
      String libraryPlural = getResources().getQuantityString(R.plurals.cpsample_libraries_plural, libTitles.length);
      String aboutText = getResources().getString(R.string.cpsample_about_text, libraryPlural);
      Spanned spannedAboutText = Html.fromHtml(aboutText);
      TextView aboutTv = (TextView)libParent.findViewById(R.id.about_text);
      aboutTv.setText(spannedAboutText);
      aboutTv.setMovementMethod(LinkMovementMethod.getInstance());
      
      for (int i = 0; i < libTitles.length; i++) {
         View libContainer = inflater.inflate(R.layout.single_library_layout, libParent, false);
         TextView currLibTitle = (TextView)libContainer.findViewById(R.id.library_title);
         currLibTitle.setText(libTitles[i]);
         TextView currLibDesc = (TextView)libContainer.findViewById(R.id.library_text);
         Spanned spanned = Html.fromHtml(libDescriptions[i]);
         currLibDesc.setText(spanned);
         currLibDesc.setMovementMethod(LinkMovementMethod.getInstance());
         libParent.addView(libContainer);
      }
      return view;
   }

   public static AboutFragment newInstance() {
      return new AboutFragment();
   }
   
}
