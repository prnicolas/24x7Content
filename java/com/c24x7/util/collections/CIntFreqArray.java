package com.c24x7.util.collections;

import java.util.Arrays;



public final class CIntFreqArray {
	private final static int EXPANSION_SIZE = 5;
	
	public final class NData {
		private int _data = -1;
		private int _frequency = 1;
		
		private NData(int data) {
			_data = data;
		}
		
		private NData(int data, int frequency) {
			_data = data;
			_frequency = frequency;
		}
		
		public int getFrequency() {
			return _frequency;
		}
		
		public int getData() {
			return _data;
		}
		
		private boolean compareTo(int value) {
			boolean match = (_data == value);
			if( match ) {
				_frequency++;
			}
			
			return match;
		}
		
		private boolean compareTo(NData value) {
			boolean match = (_data == value.getData());
			if( match ) {
				_frequency += value.getFrequency();
			}
			
			return match;
		}
	}
	
	private int	  _expansionSize = EXPANSION_SIZE;
	private int   _index = -1;
	private NData[] _values = null;
	
	public CIntFreqArray() {	}
	
	public CIntFreqArray(final NData[] values, final int index) {
		_values = values;
		_index = index;
	}
	
	
	public void setExpansionSize(final int expansionSize) {
		_expansionSize = expansionSize;
	}
	
	public int size() {
		return _values.length;
	}
	
	public int getData(int index) {
		return _values[index].getData();
	}
	
	public int getFrequency(int index) {
		return _values[index].getFrequency();
	}
	
	
		/**
		 * <p>Add a new value to this array of integer.
		 * @param value value added to the array of integers.
		 */
	public void add(int value) {
		
		if(_index == -1) {
			_values = new NData[_expansionSize];
			_values[++_index] = new NData(value);
		}
		
		else if(!addValue(value)) {
			if( _index == _values.length-1) {
				NData[] oldArray = Arrays.copyOf(_values, _index + _expansionSize);
				_values = oldArray;
			}
			_values[++_index] = new NData(value);
		}
	}
	
	
	public void add(final CIntFreqArray values) {
		this.add(values.values());
	}

	
	public void add(NData[] values) {
		if( values == null) {
			throw new IllegalArgumentException("Cannot add an empty array");
		}
		
		if(_index == -1) {
			_values = new NData[_expansionSize + values.length];
			for(int k = 0; k < values.length; k++) {
				_values[++_index] = values[k];
			}
		}
		
		else {
			for( int k = 0; k < values.length; k++) {
				if(!addValue(values[k])) {
					if( _index == _values.length-1) { 
						NData[] oldArray = Arrays.copyOf(_values, _index + _expansionSize);
						_values = oldArray;
					}
					_values[++_index] = values[k];
				}
			}
		}	
	}

	
	private boolean addValue(int value) {
		boolean found = false;
		
		for(int k = 0; k <= _index; k++) {
			if(_values[k].compareTo(value)) {
				found = true;
				break;
			}
		}
		
		return found;
	}
	
	private boolean addValue(NData value) {
		boolean found = false;
		
		for(int k = 0; k <= _index; k++) {
			if(_values[k].compareTo(value)) {
				found = true;
				break;
			}
		}
		
		return found;
	}
	
	public boolean isEmpty() {
		return (_index == 0);
	}
	
	public CIntFreqArray clone() {
		NData[] values = new NData[_values.length];
		for( int k = 0; k <= _index; k++) {
			values[k] = new NData(_values[k].getData(), _values[k].getFrequency());
		}
		return new CIntFreqArray(values, _index);
	}
	
	
		/**
		 * <p>Retrieve the array of values (integers)
		 * @return array of values
		 */
	public final NData[] values() {
		return Arrays.copyOf(_values, _index+1);
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for( int k = 0; k < _index; k++) {
			buf.append(_values[k].getData());
			buf.append("/");
			buf.append(_values[k].getFrequency());
			buf.append(" ");
		}
		buf.append(_values[_index].getData());
		buf.append("/");
		buf.append(_values[_index].getFrequency());
		buf.append(" ");
		
		return buf.toString();
	}

}

// -------------------------  EOF --------------------------------
