/**
 *  net.karlmartens.ui, is a library of UI widgets
 *  Copyright (C) 2011
 *  Karl Martens
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation, either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.karlmartens.ui.viewer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static net.karlmartens.ui.widget.ClipboardStrategy.OPERATION_COPY;
import static net.karlmartens.ui.widget.ClipboardStrategy.OPERATION_CUT;
import static net.karlmartens.ui.widget.ClipboardStrategy.OPERATION_PASTE;

import java.text.DecimalFormat;
import java.util.Arrays;

import net.karlmartens.platform.text.LocalDateFormat;
import net.karlmartens.platform.util.NumberStringComparator;
import net.karlmartens.ui.SwtTester;
import net.karlmartens.ui.SwtTester.Initializer;
import net.karlmartens.ui.SwtTester.Task;
import net.karlmartens.ui.widget.TimeSeriesTable;
import net.karlmartens.ui.widget.TimeSeriesTable.ScrollDataMode;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

public final class TimeSeriesTableViewerTest {

  private final LocalDate[] _dates;
  private final Object[][] _input;

  public TimeSeriesTableViewerTest() {
    _dates = generateDates();
    final int seriesLength = _dates.length;
    _input = new Object[500][];
    for (int i = 0; i < _input.length; i++) {
      _input[i] = new Object[] { "Item " + Integer.toString(i), Boolean.valueOf(i % 3 == 0), "stuff", generateSeries(seriesLength) };
    }
  }

  @Test
  public void testRemove() {
    SwtTester//
        .test(_initializer)//
        .add(new Task<TimeSeriesTableViewer>() {
          @Override
          public void run(TimeSeriesTableViewer context) {
            final TimeSeriesTable control = context.getControl();

            assertEquals(500, control.getItemCount());

            control.setCellSelections(new Point[] { new Point(5, 499) });
            context.remove((Object) _input[499]);
            assertEquals(499, control.getItemCount());
            assertNull(context.getElementAt(499));
            assertEquals(0, control.getCellSelections().length);
            for (int i = 0; i < 499; i++) {
              assertEquals("Element " + Integer.toString(i), (Object) _input[i], context.getElementAt(i));
            }

            final Point[] expectedTopSelection = new Point[] { new Point(5, 0) };
            control.setCellSelections(expectedTopSelection);
            context.remove((Object) _input[0]);
            assertEquals(498, control.getItemCount());
            for (int i = 1; i < 499; i++) {
              assertEquals("Element " + Integer.toString(i - 1), (Object) _input[i], context.getElementAt(i - 1));
            }
            assertTrue(Arrays.equals(expectedTopSelection, control.getCellSelections()));

            context.remove(new Object[] { _input[10], _input[20], _input[30] });
            assertEquals(495, control.getItemCount());
            int index = 0;
            for (int i = 1; i < 499; i++) {
              if (i == 10 || i == 20 || i == 30)
                continue;

              assertEquals("Element " + Integer.toString(index), (Object) _input[i], context.getElementAt(index));
              index++;
            }
            assertTrue(Arrays.equals(expectedTopSelection, control.getCellSelections()));

            context.setInput(null);
            assertEquals(0, control.getItemCount());
            assertEquals(0, control.getCellSelections().length);
          }
        }).run();
  }

  public static void main(String[] args) throws Exception {
    final TimeSeriesTableViewerTest test = new TimeSeriesTableViewerTest();

    final Shell shell = new Shell();
    shell.setLayout(new FillLayout());

    final TimeSeriesTableViewer viewer = test._initializer.run(shell);
    viewer.setComparator(new ViewerComparator(new NumberStringComparator()));

    final TimeSeriesTable table = viewer.getControl();
    table.getColumn(0).addSelectionListener(new TestSelectionListener("Period Column"));

    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        System.out.println("Selection changed event");
      }
    });

    new ViewerClipboardManager(viewer, OPERATION_COPY | OPERATION_CUT | OPERATION_PASTE);
    new TimeSeriesTableComparator(viewer);

    final Display display = shell.getDisplay();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }

  private final Initializer<TimeSeriesTableViewer> _initializer = new Initializer<TimeSeriesTableViewer>() {

    @Override
    public TimeSeriesTableViewer run(Shell shell) {
      final Display display = shell.getDisplay();

      final TimeSeriesTableViewer viewer = new TimeSeriesTableViewer(shell);
      viewer.setContentProvider(new TestTimeSeriesContentProvider(_dates, 3));
      viewer.setLabelProvider(new TestColumnLabelProvider(0));
      viewer.setEditingSupport(new TestTimeSeriesEditingSupport(new DecimalFormat("#,##0.0000"), 3));

      final TimeSeriesTable table = viewer.getControl();
      table.setHeaderVisible(true);
      table.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
      table.setFont(new Font(display, "Arial", 10, SWT.NORMAL));
      table.setDateFormat(new LocalDateFormat(DateTimeFormat.forPattern("MMM yyyy")));
      table.setNumberFormat(new DecimalFormat("#,##0.00"));
      table.setScrollDataMode(ScrollDataMode.SELECTED_ROWS);

      final TimeSeriesTableViewerColumn c1 = new TimeSeriesTableViewerColumn(viewer, SWT.NONE);
      c1.setLabelProvider(new TestColumnLabelProvider(0));
      c1.setEditingSupport(new TestTextEditingSupport(viewer, 0));
      c1.getColumn().setText("Test");
      c1.getColumn().setWidth(75);

      final TimeSeriesTableViewerColumn c2 = new TimeSeriesTableViewerColumn(viewer, SWT.CHECK);
      c2.setLabelProvider(new TestColumnLabelProvider(1));
      c2.setEditingSupport(new TestBooleanEditingSupport(viewer, 1));
      c2.getColumn().setText("Test 2");
      c2.getColumn().setWidth(60);

      viewer.setInput(_input);

      return viewer;
    }
  };

  private static LocalDate[] generateDates() {
    final LocalDate initialDate = new LocalDate(2011, 1, 1);
    final LocalDate[] dates = new LocalDate[12 * 15];
    for (int i = 0; i < dates.length; i++) {
      dates[i] = initialDate.plusMonths(i);
    }
    return dates;
  }

  private static double[] generateSeries(int length) {
    final double[] values = new double[length];
    for (int i = 0; i < values.length; i++) {
      values[i] = Math.random() * 100000;
    }
    return values;
  }
}
