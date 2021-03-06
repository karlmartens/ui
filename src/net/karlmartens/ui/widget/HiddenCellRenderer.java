/**
 *   Copyright 2012 Karl Martens
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *       
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   net.karlmartens.ui, is a library of UI widgets
 */
package net.karlmartens.ui.widget;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;

/**
 * @author karl
 *
 */
public class HiddenCellRenderer implements KTableCellRenderer {

  @Override
  public int getOptimalWidth(GC gc, int col, int row, Object content,
      boolean fixed, KTableModel model) {
    return 0;
  }

  @Override
  public void drawCell(GC gc, Rectangle rect, int col, int row, Object content,
      boolean focus, boolean header, boolean clicked, KTableModel model) {
    // Nothing to do
  }

}
