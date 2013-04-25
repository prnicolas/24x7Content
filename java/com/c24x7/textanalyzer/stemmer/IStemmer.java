package com.c24x7.textanalyzer.stemmer;

public interface IStemmer {
	  
		/**
		 * <p>Generate a stem for a characters String. This method returns 
		 * null if the stem is identical to the input.</p>
		 * @param input characters String from which to generate a stem
		 * @return word stem if a stem is different from input, null otherwise
		 * @throws IllegalArgumentException if the input string is null
		 */
	  public String stem(final String input);

		/**
		 * <p>Generate a stem for a characters Sequence. This method returns 
		 * null if the stem is identical to the input.</p>
		 * @param input characters sequence from which to generate a stem
		 * @return word stem if a stem is different from input, null otherwise
		 * @throws IllegalArgumentException if the sequence of characters is null
		 */
	  public String stem(char[] word);	
}

// ------------------------- EOF -------------------------------