package com.c24x7.textanalyzer.stemmer;

import java.util.HashMap;
import java.util.Map;




		/**
		 * <p>Class to reduce terms with NNPS and NNS tags.<br>Example of reduction are
		 * cats(cat), dogs(dog), meetings(meeting),fixes(fix),uses(use),values(value),
		 * caresses(caress),misses(miss),pies(pie),ties(tie),skies(sky),ponies(pony),
		 * worries(worry)</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 03/16/2012
		 */
public final class CPluralStemmer implements IStemmer {
	private static Map<String, String> irregularForms = null;
	static {
		irregularForms = new HashMap<String, String>();
		irregularForms.put("mice", "mouse");
		irregularForms.put("women", "woman");
		irregularForms.put("men", "man");
		irregularForms.put("oxen", "ox");
		irregularForms.put("children", "child");
		irregularForms.put("feet", "foot");
		irregularForms.put("geese", "goose");
		irregularForms.put("teeth", "tooth");
		irregularForms.put("bacteria", "bacterium");
		irregularForms.put("corpora", "corpus");
		irregularForms.put("criteria", "criterium");
		irregularForms.put("data", "datum");
		irregularForms.put("media", "medium");
		irregularForms.put("phenomena", "phenomenon");
		irregularForms.put("strata", "stratum");
	}
	
	private static CPluralStemmer stemmer = new CPluralStemmer();
	
	public static CPluralStemmer getInstance() {
		return stemmer;
	}
	
		/**
		 * <p>Generate a stem for a characters String. This method returns 
		 * null if the stem is identical to the input.</p>
		 * @param input characters String from which to generate a stem
		 * @return word stem if a stem is different from input, null otherwise
		 * @throws IllegalArgumentException if the input string is null
		 */
	@Override
	public String stem(final String term) {
		if( term == null) {
			throw new IllegalArgumentException("Cannot reduce undefined term");
		}
		
		char[] charsSequence = term.toCharArray();
		String stem = charsStem(charsSequence);
		if( stem == null) {
			stem = irregularForms.get(term);
		}
		
		return stem;
	}
	
	
	
	@Override
	public String stem(final char[] charsSequence) {
		String stem = charsStem(charsSequence);
		if( stem == null) {
			stem = irregularForms.get(String.copyValueOf(charsSequence));
		}
		
		return stem;
	}

	
	
		/**
		 * <p>Generate a stem for a characters Sequence. This method returns 
		 * null if the stem is identical to the input.</p>
		 * @param input characters sequence from which to generate a stem
		 * @return word stem if a stem is different from input, null otherwise
		 * @throws IllegalArgumentException if the sequence of characters is null
		 */
	
	private String charsStem(final char[] charsSequence) {
		if( charsSequence == null) {
			throw new IllegalArgumentException("Cannot reduce undefined term");
		}
		
		String stem = null;
		int len_1 = charsSequence.length-1,
			len_2 = charsSequence.length-2,
		    len_3 = -1,
		    len_4 = -1,
		    newLen = charsSequence.length;
		
		if( len_2 > 0) {
			char lastChar =  charsSequence[len_1];
			if( lastChar == 's') {
				if( charsSequence[len_2] == '\'') {
					newLen = len_2 ;
				}
				
				else if( charsSequence[len_2] == 'e') {
					len_3 = len_2 -1;
					if(len_3 > 0) {
						if( charsSequence[len_3] == 'i') {
							len_4 = len_3 -1;
							if( len_4 > 0) {
								charsSequence[len_3] = 'y';
								newLen = len_2;
							}
							else {
								newLen = len_1;
							}
						}
						else if (charsSequence[len_3] == 's') {
							if( len_3 > 1) {
								if(charsSequence[len_3-1] == 's' ) {
									newLen = len_2;	
								}
								else {
									charsSequence[len_2] = 'i';
								}
							}
						}
						else {
							newLen = len_2+1;
						}
					}
				}
				else if( charsSequence[len_2] == 'i' || charsSequence[len_2] == 'u') {
					newLen = -1;
				}
				
				else {
					newLen = len_1;
				}
			
				if( newLen > 0) { 
					stem = String.copyValueOf(charsSequence, 0, newLen);
				}
			}
			
			/*
			 * If the last character is 'x' that extract nouns
			 * with eaux or aux terminals.
			 */
			else if (lastChar == 'x') {
				if( charsSequence[len_2] == 'u') {
					len_4 = len_2 -2;
					if( len_4 > 0 && charsSequence[len_4] == 'e' && charsSequence[len_4+1] == 'a') {
						charsSequence[len_1] = 'x';
						stem = String.copyValueOf(charsSequence);
					}
				}
			}
		}

		return stem;
	}
	
	
					// --------------------------
					// Private Supporting Methods
					// --------------------------
	
	private CPluralStemmer() { }
	
}

// ------------------------------  EOF --------------------------
