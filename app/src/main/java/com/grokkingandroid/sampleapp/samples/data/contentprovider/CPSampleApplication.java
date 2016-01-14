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

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class CPSampleApplication extends Application {

   @Override
   public void onCreate() {
      super.onCreate();
      
      // Image loading is not the topic of this demo app,
      // I simply use Universal Image loader without 
      // any further explanation.
      // Universal image loader initialization:
      ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
            getApplicationContext()).build();
      ImageLoader.getInstance().init(config);
   }

}
