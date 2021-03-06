/*
 * Copyright 2016 The Depan Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.devtools.depan.ruby.eclipse;

import com.google.devtools.depan.eclipse.preferences.PreferencesIds;
import com.google.devtools.depan.eclipse.utils.Resources;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * Initially generated by Eclipse's New PDE Project wizard.
 * @author <a href="mailto:leeca@pnambic.com">Lee Carver</a>
 */
public class RubyActivator extends AbstractUIPlugin {

  /** Plug-in ID used to identify this plug-in. */
  public static final String PLUGIN_ID = "com.google.devtools.depan.ruby";

  /**
   * Bundle that is responsible for storing the resources for this plug-in.
   */
  public static final Bundle BUNDLE = Platform.getBundle(PLUGIN_ID);

  /**
   * Prefix to use for preferences.
   */
  public static final String MVN_PREF_PREFIX =
      PreferencesIds.PREFIX + "ruby_";

  // The shared instance
  private static RubyActivator plugin;

  /**
   * The constructor
   */
  public RubyActivator() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static RubyActivator getDefault() {
    return plugin;
  }

  /////////////////////////////////////
  // Plugin Images 

  /**
   * {@link ImageDescriptor} for {@link ArtifactElement}s.
   */
  public static final ImageDescriptor IMAGE_DESC_RUBY =
      RubyActivator.getImageDescriptor("icons/eclipse/ruby_icon_16x16.png");

  /**
   * {@link ImageDescriptor} for {@link ClassElement}s.
   */
  public static final ImageDescriptor IMAGE_DESC_CLASS =
      RubyActivator.getImageDescriptor("icons/eclipse/class_16x16.png");

  /**
   * {@link ImageDescriptor} for {@link ClassMethodElement}s.
   */
  public static final ImageDescriptor IMAGE_DESC_CLASS_METHOD =
      RubyActivator.getImageDescriptor("icons/eclipse/class_mthd_16x16.png");

  /**
   * {@link ImageDescriptor} for {@link InstanceMethodElement}s.
   */
  public static final ImageDescriptor IMAGE_DESC_INSTANCE_METHOD =
      RubyActivator.getImageDescriptor("icons/eclipse/instance_mthd_16x16.png");

  /**
   * {@link ImageDescriptor} for {@link SingletonMethodElement}s.
   */
  public static final ImageDescriptor IMAGE_DESC_SINGLETON_METHOD =
      RubyActivator.getImageDescriptor("icons/eclipse/singleton_mthd_16x16.png");

  /**
   * {@link Image} object for {@link ClassElement}s.
   */
  public static final Image IMAGE_CLASS =
      RubyActivator.getImage(IMAGE_DESC_CLASS);

  /**
   * {@link Image} object for {@link ClassMethodElement}s.
   */
  public static final Image IMAGE_CLASS_METHOD =
      RubyActivator.getImage(IMAGE_DESC_CLASS_METHOD);

  /**
   * {@link Image} object for {@link InstanceMethodElement}s.
   */
  public static final Image IMAGE_INSTANCE_METHOD =
      RubyActivator.getImage(IMAGE_DESC_INSTANCE_METHOD);

  /**
   * {@link Image} object for {@link SingletonMethodElement}s.
   */
  public static final Image IMAGE_SINGLETON_METHOD =
      RubyActivator.getImage(IMAGE_DESC_SINGLETON_METHOD);

  /////////////////////////////////////
  // Local abbreviations

  private static Image getImage(ImageDescriptor descriptor) {
    return descriptor.createImage();
  }

  private static ImageDescriptor getImageDescriptor(String path) {
    return Resources.getImageDescriptor(BUNDLE, path);
  }
}
