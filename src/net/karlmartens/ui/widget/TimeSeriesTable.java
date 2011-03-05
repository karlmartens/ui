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
package net.karlmartens.ui.widget;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.BitSet;

import net.karlmartens.platform.text.LocalDateFormat;
import net.karlmartens.platform.util.ArraySupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TypedListener;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableCellResizeListener;
import de.kupzog.ktable.KTableCellSelectionListener;
import de.kupzog.ktable.KTableDefaultModel;
import de.kupzog.ktable.SWTX;
import de.kupzog.ktable.renderers.CheckableCellRenderer;
import de.kupzog.ktable.renderers.DefaultCellRenderer;
import de.kupzog.ktable.renderers.FixedCellRenderer;
import de.kupzog.ktable.renderers.TextCellRenderer;

public final class TimeSeriesTable extends Composite {
	
	public enum ScrollDataMode {FOCUS_CELL, SELECTED_ROWS}; 

	private final GC _gc;
	private final TimeSeriesTableListener _listener;
	private final MoveColumnTableListener _moveColumnListener;
	private final Font _defaultFont;
	private final KTableImpl _table;
	private final SparklineScrollBar _hscroll;

	private boolean _showHeader = false;
	private ScrollDataMode _scrollDataMode = ScrollDataMode.FOCUS_CELL;
	private LocalDateFormat _dateFormat = new LocalDateFormat(DateTimeFormat.shortDate());
	private NumberFormat _numberFormat = NumberFormat.getNumberInstance();
	
	private int _columnCount = 0;
	private int _itemCount = 0;
	private TimeSeriesTableColumn[] _columns = {};
	private TimeSeriesTableItem[] _items = {};
	private LocalDate[] _periods = {};
	
	private int _lastFocusRow = -1;
	private int _lastFocusColumn = -1;
	private int[] _lastRowSelection = new int[0];
	private int _lastIndexOf = -1;
	
	public TimeSeriesTable(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FormLayout());
		
		final PassthoughEventListener passthroughListener = new PassthoughEventListener(this);	
		_gc = new GC(getShell());
		_defaultFont = new Font(getDisplay(), "Arial", 10, SWT.BOLD);
		_listener = new TimeSeriesTableListener();
		_moveColumnListener = new MoveColumnTableListener();
		
		_table = new KTableImpl(this, SWT.FLAT | SWT.V_SCROLL | SWT.MULTI | SWTX.MARK_FOCUS_HEADERS);
		_table.setBackground(getBackground());
		_table.setForeground(getForeground());
		_table.setModel(new TimeSeriesTableModel());
		
		_hscroll = new SparklineScrollBar(this);
		_hscroll.setMinimum(0);
		_hscroll.setMaximum(1);
		_hscroll.setSelection(0);
		_hscroll.setThumb(2);
		_hscroll.setLabelFont(_defaultFont);
		
		final FormData tableData = new FormData();
		tableData.top = new FormAttachment(0, 100, 0);
		tableData.left = new FormAttachment(_hscroll, 0, SWT.LEFT);
		tableData.bottom = new FormAttachment(_hscroll, -5, SWT.TOP);
		tableData.right = new FormAttachment(_hscroll, 0, SWT.RIGHT);
		
		final FormData scrollData = new FormData();
		scrollData.left = new FormAttachment(0,100, 0);
		scrollData.bottom = new FormAttachment(100, 100, 0);
		scrollData.right = new FormAttachment(100, 100, 0);
		scrollData.height = 40;		
		
		_table.setLayoutData(tableData);
		_hscroll.setLayoutData(scrollData);

		passthroughListener.addSource(_table);
		hookControls();
	}
	
	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		_table.setBackground(color);
	}
	
	@Override
	public void setForeground(Color color) {
		super.setForeground(color);
		_table.setForeground(color);
	}

	public int getColumnCount() {
		checkWidget();
		return _columnCount;
	}
	
	public int getPeriodCount() {
		checkWidget();
		return _periods.length;
	}
	
	public int getItemCount() {
		checkWidget();
		return _itemCount;
	}
	
	public int indexOf(TimeSeriesTableItem item) {
		checkWidget();
		if (item == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		if (_lastIndexOf >= 1 && _lastIndexOf < _itemCount - 1) {
			if (_items[_lastIndexOf] == item) return _lastIndexOf;
			if (_items[_lastIndexOf+1] == item) return ++_lastIndexOf;
			if (_items[_lastIndexOf-1] == item) return --_lastIndexOf;
		}
		
		if (_lastIndexOf < _itemCount / 2) {
			for (int i=0; i<_itemCount; i++) {
				if (_items[i] == item) {
					_lastIndexOf = i;
					return i;
				}
			}
		} else {
			for (int i=_itemCount-1; i >= 0; i--) {
				if (_items[i] == item) {
					_lastIndexOf = i;
					return i;
				}
			}
		}
		
		return -1;
	}
	
	public TimeSeriesTableItem[] getItems() {
		checkWidget();
		final TimeSeriesTableItem[] items = new TimeSeriesTableItem[_itemCount];
		System.arraycopy(_items, 0, items, 0, items.length);
		return items;
	}
	
	public TimeSeriesTableItem getItem(int index) {
		checkWidget();
		if (index < 0 || index >= _itemCount)
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		return _items[index];
	}
	
	public TimeSeriesTableItem getItem(Point point) {
		checkWidget();
		if (point == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		final Point location = _table.getLocation();
		final Point clientPoint = new Point(point.x - location.x, point.y - location.y);
		final Point cell = _table.getCellForCoordinates(clientPoint.x, clientPoint.y);
		final int row = computeModelRow(cell.y);
		if (row < 0)
			return null;
		
		return _items[row];
	}
	
	public int[] getSelectionIndices() {
		checkWidget();
		
		final BitSet selectedRows = new BitSet();
		for (Point selection : _table.getCellSelection()) {
			if (_showHeader && selection.y == 0)
				continue;
			
			selectedRows.set(computeModelRow(selection.y));
		}
		
		int index = 0;
		final int[] selected = new int[selectedRows.cardinality()];
		for (int i=selectedRows.nextSetBit(0); i >= 0; i = selectedRows.nextSetBit(i+1)) {
			selected[index++] = i;
		}
		
		return selected;
	}
	
	public TimeSeriesTableItem[] getSelection() {
		checkWidget();
		
		final int[] indices = getSelectionIndices();
		final TimeSeriesTableItem[] selected = new TimeSeriesTableItem[indices.length];
		for (int i=0; i<indices.length; i++) {
			selected[i] = _items[i];
		}
		
		return selected;
	}
	
	public Point getFocusCell() {
		checkWidget();
		
		final int row = computeModelRow(_lastFocusRow);
		final int col = computeModelColumn(_lastFocusColumn);
		if (row < 0 || col < 0 || col >= (_columnCount + _periods.length))
			return null;
		
		return new Point(col, row);
	}
	
	private TimeSeriesTableColumn _periodColumn = new TimeSeriesTableColumn(this);
	
	public TimeSeriesTableColumn getColumn(int index) {
		checkWidget();
		if (index < 0 || index >= (_columnCount + _periods.length))
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		if (index < _columnCount)
			return _columns[index];
			
		return _periodColumn;
	}
	
	public void setHeaderVisible(boolean show) {
		checkWidget();
		_showHeader = show;
		_table.redraw();
	}
	
	public void setPeriods(LocalDate[] periods) {
		checkWidget();
		if (periods == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		final LocalDate[] newPeriods = new LocalDate[periods.length];
		System.arraycopy(periods, 0, newPeriods, 0, newPeriods.length);
		Arrays.sort(newPeriods);
		_periods = periods;
		_hscroll.setMaximum(Math.max(1, _periods.length - 1));
		_table.redraw();
	}
	
	public void setDateFormat(LocalDateFormat format) {
		checkWidget();
		if (format == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		_dateFormat = format;
		_table.redraw();
	}
	
	public void setNumberFormat(NumberFormat format) {
		checkWidget();
		if (format == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		_numberFormat = format;
		_table.redraw();
	}
	
	public void setScrollDataMode(ScrollDataMode mode) {
		checkWidget();
		if (mode == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		_scrollDataMode = mode;
		doUpdateScrollData();
	}
	
	public void deselectAll() {
		checkWidget();
		_table.clearSelection();
	}
	
	public void setSelection(TimeSeriesTableItem[] items) {
		checkWidget();
		if (items == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		final int[] indices = new int[items.length];
		int i=0;
		for (TimeSeriesTableItem item : items) {
			final int index = indexOf(item);
			if (index < 0)
				continue;
			
			indices[i++] = index; 
		}
		
		final int[] result = new int[i];
		System.arraycopy(indices, 0, result, 0, i);
		setSelection(result);
	}
	
	public void setSelection(int[] indices) {
		checkWidget();
		if (indices == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);

		final int width = _columnCount + _periods.length;
		final Point[] selections = new Point[indices.length * width];
		for (int i=0; i<indices.length; i++) {
			for (int j=0; j<width; j++) {
				selections[i*width+j] = new Point(j, computeTableRow(indices[i]));
			}
		}
		_table.setSelection(selections, false);
	}
	
	public void select(int[] indices) {
		checkWidget();
		if (indices == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		final BitSet selected = new BitSet();
		for (int index : getSelectionIndices()) {
			selected.set(index);
		}
		
		for (int index : indices) {
			selected.set(index);
		}
		
		final int[] newSelection = new int[selected.cardinality()];
		int index=0;
		for (int i=selected.nextSetBit(0); i >= 0; i=selected.nextSetBit(i+1)) {
			newSelection[index++] = i;
		}
		setSelection(newSelection);
	}

	public void setCellSelection(int col, int row) {
		checkWidget();
		if (col < 0 || col >= (_columnCount + _periods.length) 
				|| row < 0 || row >= _itemCount)
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		_table.setSelection(computeTableColumn(col), computeTableRow(row), true);
	}
	
	public void showSelection() {
		checkWidget();
		if (_lastFocusRow < 0 || _lastFocusColumn < 0)
			return;
		
		_table.scroll(_lastFocusColumn, _lastFocusRow);
	}
	
	public void showItem(TimeSeriesTableItem item) {
		checkWidget();
		if (item == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		final int index = indexOf(item);
		if (index < 0)
			return;
		
		_table.scroll(Math.max(0, _lastFocusColumn), computeTableRow(index));
	}

	public void scrollTo(LocalDate date) {
		checkWidget();
		if (date == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		int index = Arrays.binarySearch(_periods, date);
		if (index < 0) {
			index = -(index + 1);
		}

		_hscroll.setSelection(index);
	}

	public void setItemCount(int count) {
		checkWidget();
		final int c = Math.max(0, count);
		if (c == _itemCount)
			return;
		
		if (c > _itemCount) {
			for (int i=_itemCount; i<c; i++) {
				new TimeSeriesTableItem(this, i);
			}
			return;
		}
		
		for (int i=c; i<_itemCount; i++) {
			final TimeSeriesTableItem item = _items[i];
			if (item != null && !item.isDisposed())
				item.release();
			_items[i] = null;
		}
		
		final int length = Math.max(4, (c + 3) / 4 * 4);
		final TimeSeriesTableItem[] newItems = new TimeSeriesTableItem[length];
		System.arraycopy(_items, 0, newItems, 0, c);
		_items = newItems;
		_itemCount = c;
		_table.redraw();
	}
	
	public void remove(int start, int end) {
		checkWidget();
		if (start < 0 || start > end || end >= _itemCount)
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		for (int i=end; i>start; i--) {
			doRemove(i);
		}
		_table.redraw();
	}
	
	public void remove(int[] indices) {
		checkWidget();
		if (indices == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (indices.length == 0)
			return;
		
		final int[] idxs = new int[indices.length];
		System.arraycopy(indices, 0, idxs, 0, idxs.length);
		Arrays.sort(idxs);
		
		for (int i=idxs.length-1; i >= 0; i--) {
			doRemove(i);
		}
		_table.redraw();
	}
	
	public void removeAll() {
		checkWidget();
		for (int i=0; i<_itemCount; i++) {
			_items[i].release();
			_items[i] = null;
		}
		_itemCount = 0;
		_table.redraw();
	}
	
	public void clear(int index) {
		checkWidget();
		if (index < 0 || index >= _itemCount)
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		_items[index].clear();
		_table.redraw();
	}
	
	public void clearAll() {
		checkWidget();
		for (int i=0; i<_itemCount; i++) {
			_items[i].clear();
		}
		_table.redraw();
	}
	
	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		final TypedListener tListener = new TypedListener(listener);
		addListener(SWT.Selection, tListener);
		addListener(SWT.DefaultSelection, tListener);
	}
	
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		final TypedListener tListener = new TypedListener(listener);
		removeListener(SWT.Selection, tListener);
		removeListener(SWT.DefaultSelection, tListener);
	}
	
	void createItem(TimeSeriesTableColumn item, int index) {
		if (index < 0 || index > _columnCount)
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		if (_columns.length == _columnCount) {
			final TimeSeriesTableColumn[] newColumns = new TimeSeriesTableColumn[_columns.length + 4];
			System.arraycopy(_columns, 0, newColumns, 0, _columns.length);
			_columns = newColumns;
		}
		
		System.arraycopy(_columns, index, _columns, index+1, _columnCount++-index);
		_columns[index] = item;
	}
	
	void createItem(TimeSeriesTableItem item, int index) {
		if (index < 0 || index > _itemCount)
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		if (_items.length == _itemCount) {
			final int length = Math.max(4, _items.length * 3 / 2);
			final TimeSeriesTableItem[] newItems = new TimeSeriesTableItem[length];
			System.arraycopy(_items, 0, newItems, 0, _items.length);
			_items = newItems;
		}
		
		System.arraycopy(_items, index, _items, index+1, _itemCount++-index);
		_items[index] = item;
	}
	
	Rectangle getBounds(TimeSeriesTableItem item, int index) {
		checkWidget();
		if (item == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (index < 0 || index >= (_columnCount + _periods.length))
			SWT.error(SWT.ERROR_INVALID_RANGE);
				
		final Rectangle r = _table.getCellRect(computeTableColumn(index), computeTableRow(indexOf(item)));
		final Point p = _table.getLocation();
		return new Rectangle(r.x + p.x + 1, r.y + p.y + 2, r.width, r.height);
	}
	
	Rectangle getBounds(TimeSeriesTableItem item) {
		checkWidget();
		if (item == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		
		final int row = computeTableRow(indexOf(item));
		final Point p = _table.getLocation();
		final Rectangle bounds = new Rectangle(p.x, p.y, 0, 0);
		for (int i=0; i<_columnCount; i++) {
			final Rectangle r  = _table.getCellRect(i, row);
			if (i == 0) {
				bounds.x += r.x;
				bounds.y += r.y;
			}
			
			bounds.width += r.width;
			bounds.height = Math.max(bounds.height, r.height);
		}
		
		return bounds;
	}
	
	Rectangle getImageBounds(TimeSeriesTableItem item, int index) {
		checkWidget();
		if (item == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (index < 0 || index >= (_columnCount + _periods.length))
			SWT.error(SWT.ERROR_INVALID_RANGE);
		
		final Rectangle r = getBounds(item, index);
		r.width = 0;
		r.height = 0;
		return r;		
	}

    Composite getTableComposite() {
		return _table;
	}
    
    private int computeTableRow(int row) {
    	if (_showHeader)
    		return row + _table.getModel().getFixedHeaderRowCount();
    	
    	return row;
    }
    
    private int computeTableColumn(int modelColumn) {
    	return modelColumn;
    }
    
    private int computeModelRow(int row) {
    	if (_showHeader)
    		return row - _table.getModel().getFixedHeaderRowCount();
    	
    	return row;
    }
    
    private int computeModelColumn(int column) {
    	return column;
    }

	private void hookControls() {
		_table.addCellSelectionListener(_listener);
		_table.addCellResizeListener(_listener);
		_table.addPaintListener(_listener);
		_table.addMouseListener(_moveColumnListener);
		_table.addMouseMoveListener(_moveColumnListener);
		_hscroll.addSelectionListener(_listener);
		addDisposeListener(_listener);
	}
	
	private void releaseControls() {
		_table.removeCellSelectionListener(_listener);
		_table.removeCellResizeListener(_listener);
		_table.removePaintListener(_listener);
		_table.removeMouseListener(_moveColumnListener);
		_table.removeMouseMoveListener(_moveColumnListener);
		_hscroll.removeSelectionListener(_listener);
		removeDisposeListener(_listener);
	}
	
	private Rectangle doGetVisibleDataCells() {
		final Rectangle r = _table.getVisibleCells();

		if (r.width > 0 && (_showHeader || r.y < _itemCount)) {
			final int y = _showHeader ? 0 : r.y;
			if (!_table.isCellFullyVisible(r.x + r.width - 1, y)) {
				r.width--;
			}
		}
		
		if (r.height > 0 && (_columnCount > 0 || r.x < _periods.length)) {
			final int x = (_columnCount > 0) ? 0 : r.x;
			if (!_table.isCellFullyVisible(x, r.y + r.height - 1)) {
				r.height--;
			}
		}
		
		return r;
	}

	private void doUpdateScrollSelection() {
		if (_lastFocusColumn < _columnCount)
			return;
		
		final Rectangle r = doGetVisibleDataCells();
		if (_lastFocusColumn >= _columnCount && _lastFocusColumn < r.x) {
			_hscroll.setSelection(_lastFocusColumn - _columnCount);
		}
		
		if (_lastFocusColumn >= _columnCount && _lastFocusColumn >= r.x + r.width) {
			final int delta = _lastFocusColumn - r.x - r.width + 1;
			_hscroll.setSelection(r.x + delta - _columnCount);
		}
	}

	private void doUpdateScrollHighlights() {
		final BitSet selectedColumns = new BitSet();
		for (Point p : _table.getCellSelection()) {
			if (p.x < _columnCount)
				continue;
			
			if (_showHeader && p.y == 0)
				continue;
			
			selectedColumns.set(p.x);
		}
		
		final int[] indices = new int[selectedColumns.cardinality()];
		int i = 0;
		for (int index = selectedColumns.nextSetBit(0); index >= 0; index = selectedColumns.nextSetBit(index+1)) {
			indices[i++] = index - _columnCount;
		 }
		_hscroll.setHighlights(indices);
	}
	
	private void doUpdateScrollData() {
		final double[] data = new double[_periods.length];
		Arrays.fill(data, 0.0);
		
		if (ScrollDataMode.FOCUS_CELL.equals(_scrollDataMode)) {
			if (_lastFocusRow >= 0 && (!_showHeader || _lastFocusRow > 0)) {
				final int row = computeModelRow(_lastFocusRow);
				for (int j=0; j<data.length; j++) {
					data[j] += _items[row].getValue(j);
				}
			}
		}
		
		if (ScrollDataMode.SELECTED_ROWS.equals(_scrollDataMode)) {
			for (int index : getSelectionIndices()) {
				for (int j=0; j<data.length; j++) {
					data[j] += _items[index].getValue(j);
				}
			}
		}
		_hscroll.setDataPoints(data);
	}
	
	private void doUpdateRows(int[] indices) {
		Arrays.sort(indices);
		
		int previous = -1;
		for (int index : indices) {
			if (index == previous)
				continue;
			
			final int width = doGetVisibleDataCells().width + _columnCount + 1;
			_table.redraw(0, computeTableRow(index), width, 1);
		}
	}
	
	private void doRemove(int index) {
		_items[index].release();
		
		System.arraycopy(_items, index+1, _items, index, --_itemCount-index);
		_items[_itemCount] = null;
	}
	
	private void doSwapColumns(int firstIndex, int secondIndex) {
		if (!_columns[firstIndex].isMoveable() || !_columns[secondIndex].isMoveable())
			return;
		
		final TimeSeriesTableColumn t = _columns[firstIndex];
		_columns[firstIndex] = _columns[secondIndex];
		_columns[secondIndex] = t;
		
		for (int i=0; i<_itemCount; i++) {
			_items[i].swapColumns(firstIndex, secondIndex);
		}
		
		_columns[firstIndex].notifyListeners(SWT.Move, new Event());
		_columns[secondIndex].notifyListeners(SWT.Move, new Event());
		
		_table.redraw();
	}
	
	private final class TimeSeriesTableModel extends KTableDefaultModel {

		@Override
		public int getFixedHeaderColumnCount() {
			return 0;
		}

		@Override
		public int getFixedHeaderRowCount() {
			return _showHeader ? 1 : 0;
		}

		@Override
		public int getFixedSelectableColumnCount() {
			return _columnCount;
		}

		@Override
		public int getFixedSelectableRowCount() {
			return 0;
		}

		@Override
		public int getRowHeightMinimum() {
			return 0;
		}

		@Override
		public boolean isColumnResizable(int col) {
			return col < _columnCount;
		}

		@Override
		public boolean isRowResizable(int row) {
			return false;
		}

		@Override
		public KTableCellEditor doGetCellEditor(int col, int row) {
			// Not used
			return null;
		}
		
		private final FixedCellRenderer _headerRenderer = new FixedCellRenderer(SWT.BOLD | DefaultCellRenderer.INDICATION_FOCUS_ROW);
		private final TextCellRenderer _renderer = new TextCellRenderer(DefaultCellRenderer.INDICATION_FOCUS);
		private final CheckableCellRenderer _checkRenderer = new CheckableCellRenderer(DefaultCellRenderer.INDICATION_FOCUS);
		private final Color _selectionColor = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);

		@Override
		public KTableCellRenderer doGetCellRenderer(int col, int row) {
			if (_showHeader && row == 0) {
				_headerRenderer.setDefaultBackground(getBackground());
				_headerRenderer.setDefaultForeground(getForeground());
				_headerRenderer.setFont(getFont());
				return _headerRenderer;
			}
			
			final int modelRow = computeModelRow(row);
			final int modelCol = computeModelColumn(col);
			final TimeSeriesTableItem item = getItem(modelRow);
			final DefaultCellRenderer renderer;
			if (modelCol < _columnCount) {	
				if ((SWT.CHECK & _columns[modelCol].getStyle()) > 0) {
					renderer = _checkRenderer;
					_checkRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_CENTER | SWTX.ALIGN_VERTICAL_CENTER);
				} else {
					renderer = _renderer;
					_renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_LEFT | SWTX.ALIGN_VERTICAL_CENTER);
				}
				
				renderer.setDefaultBackground(item.getBackground(modelCol));
				renderer.setDefaultForeground(item.getForeground(modelCol));
				renderer.setFont(item.getFont(modelCol));
			} else {
				renderer = _renderer;
				_renderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
				_renderer.setDefaultBackground(item.getBackground());
				_renderer.setDefaultForeground(item.getForeground());
				_renderer.setFont(item.getFont());
			}
			
			if (ScrollDataMode.FOCUS_CELL.equals(_scrollDataMode) && row == _lastFocusRow) {
				renderer.setBackground(_selectionColor);
			} else if (ScrollDataMode.SELECTED_ROWS.equals(_scrollDataMode) && Arrays.binarySearch(_lastRowSelection, modelRow) >= 0) {
				renderer.setBackground(_selectionColor);
			} else {
				renderer.setBackground(null);
			}
			
			return renderer;
		}

		@Override
		public int doGetColumnCount() {
			return _columnCount + _periods.length;
		}

		@Override
		public Object doGetContentAt(int col, int row) {
			final int modelCol = computeModelColumn(col);
			if (_showHeader && row == 0) {
				if (modelCol < _columnCount) {
					return _columns[modelCol].getText();
				}
				
				final LocalDate date = _periods[modelCol - _columnCount];
				if (date == null)
					return "";
				
				return _dateFormat.format(date);
			}
			
			final TimeSeriesTableItem item = _items[computeModelRow(row)];
			if (modelCol < _columnCount) {
				final String text = item.getText(modelCol);
				if ((SWT.CHECK & _columns[modelCol].getStyle()) > 0)
					return Boolean.valueOf(text);
				
				if (text == null)
					return "";
				
				return text;
			}
			
			final double value = item.getValue(modelCol - _columnCount);
			if (value == 0.0)
				return "";
			
			return _numberFormat.format(value);
		}

		@Override
		public int doGetRowCount() {
			return computeTableRow(_itemCount);
		}

		@Override
		public void doSetContentAt(int col, int row, Object newValue) {
			// Not used
		}

		@Override
		public int getInitialColumnWidth(int col) {
			final int modelCol = computeModelColumn(col);
			if (modelCol < _columnCount) {
				final TimeSeriesTableColumn column = getColumn(modelCol);
				return column.getWidth();
			}
			
			_gc.setFont(getFont());
			return _gc.getCharWidth('W') * 8;
		}

		@Override
		public int getInitialRowHeight(int row) {
			if ((_showHeader && row == 0) || row < 0) {
				_gc.setFont(getFont());
			} else {
				final TimeSeriesTableItem item = getItem(computeModelRow(row));
				_gc.setFont(item.getFont());
			}

			return _gc.getFontMetrics().getHeight() + 10;
		}		
	}
	
	private final class TimeSeriesTableListener implements KTableCellSelectionListener, KTableCellResizeListener, 
	  SelectionListener, PaintListener, DisposeListener {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() != _hscroll)
				return;

			final int selection = _hscroll.getSelection();
			if (selection < 0 || selection >= _periods.length)
				return;
			
			_hscroll.setLabel(_dateFormat.format(_periods[selection]));
			
			final Rectangle visible = doGetVisibleDataCells();
			final int x = _hscroll.getSelection() + _columnCount; 
			final int y = Math.max(0, Math.min(visible.y, _itemCount - _table.getVisibleRowCount()));
			_table.scroll(x, y);
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Ignore event
		}
		
		@Override
		public void cellSelected(int col, int row, int statemask) {
			_lastFocusRow = row;
			_lastFocusColumn = col;
			
			final int[] selectedRows = getSelectionIndices();
			if (!Arrays.equals(_lastRowSelection, selectedRows)) {
				final int[] oldSelection = _lastRowSelection;
				_lastRowSelection = getSelectionIndices();
				
				final int[] update = ArraySupport.minus(oldSelection, _lastRowSelection);
				doUpdateRows(update);
			}
			
			doUpdateScrollSelection();
			doUpdateScrollHighlights();
			doUpdateScrollData();
			notifyListeners(SWT.Selection, new Event());
		}

		@Override
		public void fixedCellSelected(int col, int row, int statemask) {
			// Ignore event
		}
		
		@Override
		public void paintControl(PaintEvent e) {
			if (e.getSource() != _table) 
				return;

			final Rectangle visible = doGetVisibleDataCells();
			if (visible.width <= 0) {
				_hscroll.setThumb(_hscroll.getMaximum() + 1);
				_hscroll.setEnabled(false);
				return;
			}
			
			_hscroll.setThumb(Math.max(1, visible.width));
			_hscroll.setEnabled(true);
		}
		
		@Override
		public void columnResized(int col, int newWidth) {
			final int modelCol = computeModelColumn(col);
			if (modelCol < _columnCount) {
				_columns[modelCol].notifyListeners(SWT.Resize, new Event());
			}
		}
		
		@Override
		public void rowResized(int row, int newHeight) {
			_items[computeModelRow(row)].notifyListeners(SWT.Resize, new Event());
		}

		@Override
		public void widgetDisposed(DisposeEvent e) {
			releaseControls();
			_defaultFont.dispose();
			_gc.dispose();
		}
	}

	private final class MoveColumnTableListener implements MouseListener, MouseMoveListener {

		private Integer _columnIndex;
		private Image _image;
		private Shell _shell;

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// ignore
		}
		
		@Override
		public void mouseDown(MouseEvent e) {
			if (_table != e.getSource())
				return;

			final Point cellCord = _table.getCellForCoordinates(e.x, e.y);
			if (cellCord.y >= _table.getModel().getFixedHeaderRowCount())
				return;
			
			final int colIndex = computeModelColumn(cellCord.x);
			if (colIndex < 0 || colIndex >= _columnCount)
				return;
			
			if (!_columns[colIndex].isMoveable())
				return;
			
			initColumnMove(colIndex);
		}

		@Override
		public void mouseUp(MouseEvent e) {
			if (!isColumnMoveActive())
				return;
				
			if (_table != e.getSource()) {
				cancelColumnMove();
				return;
			}

			final Point cellCord = _table.getCellForCoordinates(e.x, e.y);
			final int newColIndex = computeModelColumn(cellCord.x);
			final int oldColumnIndex = _columnIndex.intValue();
			if (newColIndex < 0 || newColIndex >= _columnCount || oldColumnIndex == newColIndex) {
				cancelColumnMove();
				return;
			}
			
			performColumnMove(oldColumnIndex, newColIndex);
		}
		
		@Override
		public void mouseMove(MouseEvent e) {
			if (!isColumnMoveActive())
				return;
			
			if (_table != e.getSource()) {
				cancelColumnMove();
				return;
			}
			
			
			if (_shell == null) {
				openColumnWindow();
			}
			
			final Rectangle rLastCol = _table.getCellRect(_columnCount - 1, 0);
			final Point p = TimeSeriesTable.this.toDisplay(_table.getLocation());
			p.x += Math.max(Math.min(e.x, rLastCol.x), 0);
			_shell.setLocation(p);
		}

		private void initColumnMove(int colIndex) {
			_columnIndex = colIndex;
			_table._ignoreMouseMove = true;
	
			final GC gc = new GC(_table);
			
			final Rectangle cellCords = _table.getCellRect(computeTableColumn(colIndex), 0);
			final int height = _table.getClientArea().height;
			
			if (_image != null)
				_image.dispose();
			_image = new Image(getDisplay(), new Rectangle(0, 0, cellCords.width, height));
			_image.getImageData().alpha = 0;
			gc.copyArea(_image, cellCords.x, cellCords.y);
		}
		
		private void openColumnWindow() {
			_shell = new Shell(getDisplay(), SWT.NO_TRIM);
			_shell.setAlpha(200);
			_shell.setLayout(new FillLayout());
			_shell.setBounds(_image.getBounds());
			
			final Label l = new Label(_shell, SWT.NONE);
			l.setImage(_image);
			l.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

			_shell.open();
		}
		
		private void cancelColumnMove() {
			_columnIndex = null;
			_table._ignoreMouseMove = false;
			
			if (_image != null && !_image.isDisposed())
				_image.dispose();
			_image = null;
			
			if (_shell != null  && !_shell.isDisposed())
				_shell.dispose();
			_shell = null;
		}
		
		private void performColumnMove(int oldColumnIndex, int newColumnIndex) {
			doSwapColumns(oldColumnIndex, newColumnIndex);
			cancelColumnMove();
		}
		
		private boolean isColumnMoveActive() {
			return _columnIndex != null;
		}
	}

	private final class KTableImpl extends KTable {
		
		private boolean _ignoreMouseMove = false;

		public KTableImpl(Composite parent, int style) {
			super(parent, style);
		}
		
		@Override
		protected void onMouseMove(MouseEvent e) {
			if (_ignoreMouseMove)
				return;
			
			super.onMouseMove(e);
		}
		
	}
}
