//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.android;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.ObjectConverter;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;

/**
 * Responsible for parsing and setting up Package information that is used by PackageManager.
 * 
 * @author Heila van der Merwe
 * 
 */
public class JPF_android_content_pm_PackageManager {
  private static final JPFLogger logger = JPF.getLogger("JPF_android_content_pm_PackageManager");

  private static AndroidManifestParser parser;
  private static PackageInfo packageInfo;
  private static Map<PackageItemInfo, List<IntentFilter>> filterMap;

  /**
   * Intercept default constructor and initialize package information.
   * 
   * @param env
   * @param robj
   */
  public static void $init____V(MJIEnv env, int robj) {
    ThreadInfo ti = env.getThreadInfo();

    if (!ti.hasReturnedFromDirectCall("[clinit]")) { // Make sure that when we repeat the code during static
                                                     // class initialization in ObjectConverter, this is not
                                                     // executed again.
      parser = AndroidManifestParser.getInstance();

      // build path to SUT's AndroidManifest.xml file
      Config conf = env.getConfig();
      String path = conf.getString("path");
      if (path == null || path.length() == 0) {
        logger
            .severe("Path configuation variable was empty. Please add the project location in the config.jpf file. For example: \"path=path/to/project/ExampleProject/\" ");
        return;
      } else if (path.endsWith("/")) {
        path += "AndroidManifest.xml";
      } else {
        path += "/AndroidManifest.xml";
      }
      parser.parseFile(path);
      packageInfo = parser.getPackageInfo();
      filterMap = parser.getFilters();
    }
    // If we have reached this point the package has been parsed and we need to populate the PackagManager on
    // the JPF side
    int packageRef = ObjectConverter.JPFObjectFromJavaObject(env, packageInfo);
    env.setReferenceField(robj, "packageInfo", packageRef);
  }

  /**
   * Intercept Constructore used during testing. The constructor is proided with an XML string that contains
   * the contents of the AndroidManifestFile.
   * 
   * @param env
   * @param robj
   * @param ref
   *          a String containing the AndroidManifest contents as a XML string.
   */
  public static void $init__Ljava_lang_String_2__V(MJIEnv env, int robj, int ref) {
    ThreadInfo ti = env.getThreadInfo();
    if (!ti.hasReturnedFromDirectCall("[clinit]")) {
      try {
        parser.parseStream(new ByteArrayInputStream(env.getStringObject(ref).getBytes("UTF-8")));
      } catch (UnsupportedEncodingException e) {
      }

      packageInfo = parser.getPackageInfo();
      // if we have reached this point the package has been parsed and we need to populate the packageInfo
    }
    int packageRef = ObjectConverter.JPFObjectFromJavaObject(env, packageInfo);
    env.setReferenceField(robj, "packageInfo", packageRef);
  }

  public static String getPackageName() {
    return packageInfo.packageName;
  }
}