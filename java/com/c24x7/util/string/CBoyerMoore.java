// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.util.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.c24x7.util.CEnv;




	/**
	* <p>Implementation of the String matching Boyer-Moore algorithm.</p?
	* @author Patrick Nicolas
	* @author 07/11/2011
	*/
public final class CBoyerMoore {
	private static final int ALPHABET_SIZE = 256 ;
	
	private int[] 	_bmBC	= null;
	private int[] 	_bmGs	= null;
	private int 	_comparisons	= CEnv.UNINITIALIZED_INT;

	private String 	_pattern = null;
	private char[] 	_patternChars = null;
	private int 	_patternCharsLen = CEnv.UNINITIALIZED_INT;

	
			/**
			 * <p>Create an instance of Boyer-Moore String matching algorithm.</p>
			 * @param pattern pattern to be applied in the search.
			 */
	
	public CBoyerMoore ( final String pattern ) {
		_pattern = pattern;
		_patternChars = _pattern.toCharArray () ;
		_patternCharsLen = _patternChars.length; 
		preBmBc (_patternChars) ;
		preBmGs (_patternChars) ;
	}

	
			/**
			 * <p>Search for the occurrences of a pattern within an input text.</p>
			 * @param input input text 
			 * @return indexes of the occurrences of the pattern
			 * @throws IllegalArgumentException if argument is null
			 */
	public List<Integer> search (final String input ) {
		if( input == null || input.length() < 2) {
			throw new IllegalArgumentException("Cannot found BM pattern of undefined text");
		}
		
		char[] textChars = input.toCharArray () ;
		int textLen = textChars.length;
		List<Integer> resultsList = new ArrayList<Integer>() ;

		int j = 0 ;
		int i = 0 ;
		_comparisons = 0 ;

		while ( j <= textLen - _patternCharsLen ) {
			for (i = _patternCharsLen - 1 ; i >= 0 && _patternChars[i] == textChars[i+j] ; i-- ) {
				_comparisons++;
			}

			if (i < 0) {
				resultsList.add(Integer.valueOf(j)) ;
				j += _bmGs[0] ;
			} 
			else {
				j += Math.max(_bmGs [i], _bmBC [textChars[i+j]] - _patternCharsLen + 1 + i ) ;
			}
		}

		return resultsList;
	}

	
			/**
			 * <p>Attempt to find a pattern within an input text.</p>
			 * @param input text input
			 * @return true if pattern found, false otherwise
			 * @throws IllegalArgumentException if argument is null
			 */
	public boolean found( final String input) {
		if( input == null || input.length() < 2) {
			throw new IllegalArgumentException("Cannot found BM pattern of undefined text");
		}
		
		boolean found = false;
		char[] textChars = input.toCharArray () ;
		int textLen = textChars.length; 

		int j = 0 ;
		int i = 0 ;
		_comparisons = 0 ;

		while (j <= textLen - _patternCharsLen ) {
			for ( i = _patternCharsLen - 1 ; i >= 0 && _patternChars[i] == textChars [i+j] ; i-- ) {
				_comparisons++;
			}
			if (i < 0) {
				found = true;
				break;
			} 
			else {
				j += Math.max(_bmGs[i], _bmBC [textChars [i+j]] - _patternCharsLen+ 1 + i ) ;
			}
		}

		return found;
	}
	
	
	
							// -----------------------
							// Private Supporting Methods
							// -------------------------
	
	private void preBmBc ( char[] x ) {
		int m = x.length;
		_bmBC = new int [ ALPHABET_SIZE ] ;

		Arrays.fill ( _bmBC, m ) ;
		for ( int i = 0 ; i < m - 1 ; i++ ) {
			_bmBC [ x [ i ]] = m - i - 1 ;
		}
	}

	private int[] suffixes( char[] x ) {
		int m = x.length;
		int suff [] = new int [ m ] ;
		int g = m - 1 ;
		int f = m - 1 ;

		suff [ m - 1 ] = m;
		for ( int i = m - 2 ; i >= 0 ; --i ) {
			if ( i > g && ( i + m - 1 - f ) < m && suff [ i + m - 1 - f ] < i - g ) {
				suff [ i ] = suff [ i + m - 1 - f ] ;
			} 
			else {
				g = i;
				f = g;

				while ( g >= 0 && x [ g ] == x [ g + m - 1 - f ]) {
					--g;
				}
				suff [ i ] = f - g;
			}
		}

		return suff;
	}

	private void preBmGs (char[] x ) {
		int m = x.length;
		_bmGs = new int [ m ] ;

		int suff [] = suffixes ( x ) ;
		Arrays.fill ( _bmGs, m ) ;

		int j = 0 ;

		for ( int i = m - 1 ; i >= - 1 ; --i ) {
			if ( i == - 1 || suff [ i ] == i + 1 ) {
				for ( ; j < m - 1 - i; ++j ) {
					if ( _bmGs [ j ] == m ) {
						_bmGs [ j ] = m - 1 - i;
					}
				}
			}
		}

		for ( int i = 0 ; i < m - 1 ; i++ ) {
			_bmGs [ m - 1 - suff [ i ]] = m - 1 - i;
		}

	}

}


// --------------------------  EOF -----------------------------------
