/**
 *   Copyright 2013 Karl Martens
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author kmartens
 *
 */
public class FolderDragBehavior {
  
  private final CTabFolder _control;
  private int _fixedRightTabCount = 0;
  private CTabItem _item;

  public FolderDragBehavior(CTabFolder control) {
    _control = control;
    hook();
  }
  
  public void setFixedRightTabCount(int count) {
    _fixedRightTabCount = count;
  }

  private void hook() {
    _control.addListener(SWT.MouseDown, _listener);
    _control.addListener(SWT.MouseUp, _listener);
    _control.addListener(SWT.MouseMove, _listener);
  }
  
  private boolean isActive() {
    return _item != null;
  }
  
  private void cancel() {
    _item = null;
  }
  
  private void init(CTabItem item) {
    _item = item;
  }
  
  private void handleMouseDown(Event e) {
    if (e.button != 1) {
      cancel();
      return;
    }
    
    final Point pt = new Point(e.x, e.y);
    final CTabItem item = _control.getItem(pt);
    if (item == null) {
      cancel();
      return;
    }
        
    init(item);    
  }
  
  private void handleMouseUp(Event e) {
    if (!isActive())
      return;
    
    cancel();
  }
  
  private void handleMouseMove(Event e) {
    if (!isActive())
      return;
    
    final Point pt = new Point(e.x, e.y);
    final CTabItem item = _control.getItem(pt);
    if (item == _item || item == null)
      return;

    final int maxIdx = _control.getItemCount() - _fixedRightTabCount - 1;
    final int selectedIdx = _control.indexOf(_item);
    if (selectedIdx > maxIdx)
      return;
    
    final int otherIdx = _control.indexOf(item);
    if (otherIdx > maxIdx)
      return;
    
    
    final CTabItem newItem;
    if (selectedIdx < otherIdx) {
      newItem = new CTabItem(_control, _item.getStyle(), otherIdx + 1);
    } else {
      newItem = new CTabItem(_control, _item.getStyle(), otherIdx);
    }
    
    newItem.setControl(_item.getControl());
    newItem.setFont(_item.getFont());
    newItem.setImage(_item.getImage());
    newItem.setShowClose(_item.getShowClose());
    newItem.setText(_item.getText());
    newItem.setToolTipText(_item.getToolTipText());
    newItem.setData(_item.getData());
    
    _control.setSelection(_control.indexOf(newItem));
    
    _item.dispose();
    _item = newItem;
  }

  private final Listener _listener = new Listener() {
    @Override
    public void handleEvent(Event event) {
      if (_control == null || _control.isDisposed() || event.widget != _control)
        return;
      
      switch (event.type) {
        case SWT.MouseDown:
          handleMouseDown(event);
          return;
          
        case SWT.MouseUp:
          handleMouseUp(event);
          return;
          
        case SWT.MouseMove:
          handleMouseMove(event);
          return;
          
        default:
          return;
      }
    }    
  };
}
