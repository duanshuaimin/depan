/*
 * Copyright 2007 The Depan Project Authors
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

package com.google.devtools.depan.eclipse.editors;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.google.devtools.depan.eclipse.plugins.SourcePluginRegistry;
import com.google.devtools.depan.tasks.MigrationRule;

/**
 * A Helper class for RemapTable, handling text labels and icons.
 *
 * @author ycoppel@google.com (Yohann Coppel)
 *
 */
public class RemapTableHelper extends LabelProvider
    implements ITableLabelProvider {

  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    if (element instanceof MigrationRule) {
      MigrationRule<?> rule = (MigrationRule<?>) element;
      switch (columnIndex) {
      case 0:
        return SourcePluginRegistry.getImage(rule.getSource());
      default:
        break;
      }
    }
    return null;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    if (element instanceof MigrationRule) {
      MigrationRule<?> rule = (MigrationRule<?>) element;
      switch (columnIndex) {
      case 0:
        return rule.getSource().toString();
      case 1:
        return rule.getTarget().toString();
      default:
        break;
      }
    }
    return "";
  }

}
