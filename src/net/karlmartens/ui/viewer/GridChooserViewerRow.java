/**
 *  net.karlmartens.ui, is a library of UI widgets
 *  Copyright (C) 2010,2011
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

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import net.karlmartens.ui.widget.GridChooser;
import net.karlmartens.ui.widget.GridChooserItem;

final class GridChooserViewerRow extends ViewerRow {

	private GridChooserItem _item;

	GridChooserViewerRow(GridChooserItem item) {
		_item = item;
	}

	void setItem(GridChooserItem item) {
		_item = item;
	}

	@Override
	public Rectangle getBounds(int columnIndex) {
		return _item.getBounds(columnIndex);
	}

	@Override
	public Rectangle getBounds() {
		return _item.getBounds();
	}

	@Override
	public Widget getItem() {
		return _item;
	}

	@Override
	public int getColumnCount() {
		return _item.getParent().getColumnCount();
	}

	@Override
	public Image getImage(int columnIndex) {
		return _item.getImage(columnIndex);
	}

	@Override
	public void setImage(int columnIndex, Image image) {
		_item.setImage(columnIndex, image);
	}

	@Override
	public String getText(int columnIndex) {
		return _item.getText(columnIndex);
	}

	@Override
	public void setText(int columnIndex, String text) {
		_item.setText(columnIndex, text);
	}

	@Override
	public Color getBackground(int columnIndex) {
		return _item.getBackground(columnIndex);
	}

	@Override
	public void setBackground(int columnIndex, Color color) {
		_item.setBackground(columnIndex, color);
	}

	@Override
	public Color getForeground(int columnIndex) {
		return _item.getForeground(columnIndex);
	}

	@Override
	public void setForeground(int columnIndex, Color color) {
		_item.setForeground(columnIndex, color);
	}

	@Override
	public Font getFont(int columnIndex) {
		return _item.getFont(columnIndex);
	}

	@Override
	public void setFont(int columnIndex, Font font) {
		_item.setFont(columnIndex, font);
	}

	@Override
	public Control getControl() {
		return _item.getParent();
	}

	@Override
	public ViewerRow getNeighbor(int direction, boolean sameLevel) {
		final GridChooserItem item;
		if (ViewerRow.ABOVE == direction) {
			item = getNeighbor(-1);
		} else if (ViewerRow.BELOW == direction) {
			item = getNeighbor(1);
		} else {
			throw new IllegalArgumentException();
		}

		if (item == null)
			return null;

		return new GridChooserViewerRow(item);
	}

	private GridChooserItem getNeighbor(int direction) {
		final GridChooser parent = _item.getParent();
		if (_item.isSelected()) {
			final GridChooserItem[] selection = parent.getSelection();
			final int index = _item.getSelectionOrder() + direction;
			if (index < 0 || index >= selection.length) {
				return null;
			}
			return selection[index];
		}

		int index = parent.indexOf(_item);
		for (;;) {
			index += direction;
			if (index < 0 || index >= parent.getItemCount()) {
				return null;
			}

			final GridChooserItem item = parent.getItem(index);
			if (!item.isSelected()) {
				return item;
			}
		}
	}

	@Override
	public TreePath getTreePath() {
		return new TreePath(new Object[] { _item.getData() });
	}

	@Override
	public Object clone() {
		return new GridChooserViewerRow(_item);
	}

	@Override
	public Object getElement() {
		return _item.getData();
	}
}
