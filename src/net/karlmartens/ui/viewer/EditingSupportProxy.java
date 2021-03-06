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
package net.karlmartens.ui.viewer;

import net.karlmartens.platform.util.ReflectSupport;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ViewerCell;

class EditingSupportProxy extends EditingSupport {

  EditingSupport _base;

  EditingSupportProxy(ColumnViewer viewer) {
    super(viewer);
  }

  @Override
  protected CellEditor getCellEditor(Object element) {
    checkBase();
    return (CellEditor) ReflectSupport.invoke("getCellEditor", _base, Object.class, element);
  }

  @Override
  protected boolean canEdit(Object element) {
    checkBase();
    return Boolean.TRUE.equals(ReflectSupport.invoke("canEdit", _base, Object.class, element));
  }

  @Override
  protected Object getValue(Object element) {
    checkBase();
    return ReflectSupport.invoke("getValue", _base, Object.class, element);
  }

  @Override
  protected void setValue(Object element, Object value) {
    checkBase();
    ReflectSupport.invoke("setValue", _base, new Class[] { Object.class, Object.class }, new Object[] { element, value });
  }
  
  @Override
  protected void initializeCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
    checkBase();
    ReflectSupport.invoke("initializeCellEditorValue", _base, new Class[] { CellEditor.class, ViewerCell.class }, new Object[] { cellEditor, cell });
  }
  
  @Override
  protected void saveCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
    checkBase();
    ReflectSupport.invoke("saveCellEditorValue", _base, new Class[] { CellEditor.class, ViewerCell.class }, new Object[] { cellEditor, cell });
  }

  private void checkBase() {
    if (_base == null)
      throw new IllegalStateException();;
  }
}