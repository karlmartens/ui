/**
 *   Copyright 2011 Karl Martens
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
package net.karlmartens.ui.action;

import net.karlmartens.ui.Images;
import net.karlmartens.ui.Messages;
import net.karlmartens.ui.widget.Table;

import org.eclipse.jface.action.Action;

public final class ResizeAllColumnsAction extends Action {

  private final Table _table;
  private final ResizeColumnAction _delegateAction;

  public ResizeAllColumnsAction(Table table) {
    _table = table;
    _delegateAction = new ResizeColumnAction(_table, -1);
    setText(Messages.RESIZE_COLUMN_ALL.string());
    setImageDescriptor(Images.RESIZE_ALL);
  }

  @Override
  public void run() {
    for (int i = 0; i < _table.getColumnCount(); i++) {
      _delegateAction.setColumnIndex(i);
      _delegateAction.run();
    }
  }
}
