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

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ImageHelper {

   /**
    * This method delegates Image loading to the library
    * Universal Image Loader. Image loading is not the
    * topic of this demo, so I do not comment
    * any of the following code!
    * 
    * Please refer to the documentation of the library instead:
    * https://github.com/nostra13/Android-Universal-Image-Loader
    */
   public static void loadImage(final Uri photoUri, final ImageView imageView, final View... viewsToMakeVisible) {
      ImageLoader imageLoader = ImageLoader.getInstance();
      final DisplayImageOptions options = new DisplayImageOptions.Builder()
               .cacheOnDisc(true)
               .build();
      imageLoader.displayImage(photoUri.toString(), imageView, options, new ImageLoadingListener() {
          @Override
          public void onLoadingStarted(String imageUri, View view) {
          }
          @Override
          public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
          }
          @Override
          public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
              if (viewsToMakeVisible != null) {
                 // these are other views like labels, containers and so on
                 for (View v: viewsToMakeVisible) {
                    v.setVisibility(View.VISIBLE);
                 }
              }
              // that's the imageview itself
              view.setVisibility(View.VISIBLE);
          }
          @Override
          public void onLoadingCancelled(String imageUri, View view) {
          }
      });
   }
   
}
