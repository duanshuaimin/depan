/*
 * Copyright 2015 The Depan Project Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.depan.eclipse.ui.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provide a common logger for the entire view/tools
 * package.  Simply import this class, and use the
 * available {@link #LOG} member.
 *
 * @author <a href="mailto:leeca@pnambic.com">Lee Carver</a>
 */
public class NodesUILogger {

  public static final Logger LOG =
      Logger.getLogger(NodesUILogger.class.getName());

  private NodesUILogger() {
    // Prevent instantiation.
  }

  /**
   * Bizarre that this is not part of standard java.util.logging.
   */
  public static final void logException(String msg, Exception err) {
    LOG.log(Level.SEVERE, msg, err);
  }
}