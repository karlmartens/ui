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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author kmartens
 *
 */
public class FolderTest {

  private final Display _display;
  private final Shell _shell;
  private final CTabFolder _folder;

  public FolderTest() {
    _display = Display.getDefault();
    _shell = new Shell(_display);
    _shell.setLayout(new GridLayout(1, false));
    
    _folder = new CTabFolder(_shell, SWT.NONE);
    _folder.setSimple(false);
    _folder.setLayoutData(GridDataFactory//
        .swtDefaults()//
        .grab(true, true)//
        .align(SWT.FILL, SWT.FILL)//
        .create());
    
    final CTabItem item1 = new CTabItem(_folder, SWT.CLOSE);
    item1.setText("Tab 1");
    
    final CTabItem item2 = new CTabItem(_folder, SWT.CLOSE);
    item2.setText("Tab 2");
    
    final CTabItem item3 = new CTabItem(_folder, SWT.CLOSE);
    item3.setText("Tab 3");    
    
    final FolderDragBehavior drag = new FolderDragBehavior(_folder);
    drag.setFixedRightTabCount(1);
  }

  private void run() {
    _shell.open();
    _shell.layout();
    while (!_shell.isDisposed()) {
      if (!_display.readAndDispatch())
        _display.sleep();
    }
  }
      
  public static void main(String[] args) {
    new FolderTest().run();
  }
  
}
