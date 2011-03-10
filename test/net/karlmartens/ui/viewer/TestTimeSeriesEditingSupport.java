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

import java.text.NumberFormat;

final class TestTimeSeriesEditingSupport implements TimeSeriesEditingSupport {

	private final int _index;
	private final NumberFormat _format;

	public TestTimeSeriesEditingSupport(NumberFormat format, int index) {
		_index = index;
		_format = format;
	}

	@Override
	public NumberFormat getNumberFormat() {
		return _format;
	}

	@Override
	public boolean canEdit(Object element) {
		return true;
	}

	@Override
	public void setValue(Object element, int index, double value) {
		final double[] series = (double[])((Object[])element)[_index];
		series[index] = value;
	}

}