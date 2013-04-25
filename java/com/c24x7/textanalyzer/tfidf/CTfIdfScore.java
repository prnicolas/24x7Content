// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.tfidf;

import com.c24x7.exception.InitException;



		/**
		 * <p>Singleton class that encapsulates the computation of the weight 
		 * (of score) of a term or N-Gram extracted from a Corpus or document. 
		 * The class implements a variant of Term Frequency Inverse Document 
		 * Frequency (TF-IDF) algorithm. The computation of the N-Gram relative
		 * frequency within a diagram depends on a discriminant function that
		 * can be specified by the client application (Linear, Square Root or
		 * Logarithmic).</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 12/08/2011
		 */
public final class CTfIdfScore {
	private final static long 	serialVersionUID = -695361762757129204L;

		/**
		 * <p>Interface that defines the discriminant function for scoring
		 * an semantically valid N-Gram within a document.</p>
		 * @author Patrick Nicolas
		 * @date 03/15/2012
		 */
	public static interface ITfDiscriminant {
			/**
			 * <p>Retrieve the name of the discriminant function.</p>
			 * @return name of mathematics function
			 */
		String getName();
		/**
		 * <p>Applies a math function to compute the relative frequency
		 * of a term or N-Gram within a document.</p>
		 * @param normalized frequency of a N-Gram [0,1] values
		 * @return relative frequency of the N-Gram
		 */
		double compute(double tfValue);
	}
	
	
	/**
	 * <p>Class that implements a linear discriminant function for scoring
	 * an semantically valid N-Gram within a document.</p>
	 * @author Patrick Nicolas
	 * @date 03/15/2012
	 */
	public static class NTfLinearDiscriminant implements ITfDiscriminant {
		private static final String NAME = "Lin";
		
		/**
		 * <p>Retrieve the name of the discriminant function.</p>
		 * @return name of mathematics function
		 */
		@Override
		public String getName() {
			return NAME;
		}
		
		/**
		 * <p>Applies a linear function to compute the relative frequency
		 * of a term or N-Gram within a document.</p>
		 * @param normalized frequency of a N-Gram [0,1] values
		 * @return relative frequency of the N-Gram
		 */
		@Override
		public double compute(double tfValue) {
			return tfValue;
		}
	}
	
	/**
	 * <p>Class that implements a Square Root discriminant function for scoring
	 * an semantically valid N-Gram within a document.</p>
	 * @author Patrick Nicolas
	 * @date 03/15/2012
	 */
	public static class NTfSqrtDiscriminant implements ITfDiscriminant {
		private static final String NAME = "Sqrt";
		
		/**
		 * <p>Retrieve the name of the discriminant function.</p>
		 * @return name of mathematics function
		 */
		@Override
		public String getName() {
			return NAME;
		}

		/**
		 * <p>Applies a Square Root function to compute the relative frequency
		 * of a term or N-Gram within a document.</p>
		 * @param normalized frequency of a N-Gram [0,1] values
		 * @return relative frequency of the N-Gram
		 */
		@Override
		public double compute(double tfValue) {
			return Math.sqrt(tfValue);
		}
	}
	
	/**
	 * <p>Class that implements a Logaritmic discriminant function for scoring
	 * an semantically valid N-Gram within a document.</p>
	 * @author Patrick Nicolas
	 * @date 03/15/2012
	 */
	public static class NTfLogDiscriminant implements ITfDiscriminant {
		private static final double INV_LOG_2 = 1.0/0.6931;
		private static final String NAME = "Log";
		
		/**
		 * <p>Retrieve the name of the discriminant function.</p>
		 * @return name of mathematics function
		 */
		@Override
		public String getName() {
			return NAME;
		}

		/**
		 * <p>Applies a logarithm function to compute the relative frequency
		 * of a term or N-Gram within a document.</p>
		 * @param normalized frequency of a N-Gram [0,1] values
		 * @return relative frequency of the N-Gram
		 */
		@Override
		public double compute(double tfValue) {
			return Math.log(1.0+tfValue)*INV_LOG_2;
		}
	}
	
	
	
	
	private static CTfIdfScore relativeTF = null;

			/**
			 * <p>Static initialization of the TF vector, loaded from the configuration file. The
			 * instance is not created if the vector is not properly initialized.</p>
			 * @return true if initialization succeed, false otherwise.
			 */
	public static void init() throws InitException {
		if(getInstance() == null) {
			throw new InitException("Cannot initialize TfIdf Score");
		}
	}
	
	
			/**
			 * <p>Return the instance of the TF vector singleton.</p>
			 * @return the singleton instance if initialization succeeds, null otherwise.
			 */
	public static CTfIdfScore getInstance() {
		if( relativeTF == null ) {
			relativeTF = new CTfIdfScore();
		}
		
		return relativeTF;
	}
	
	private ITfDiscriminant _discriminant = null; 
	
	
	
				/**
				 * <p>Set the discriminant function for the computation 
				 * of the relative frequency of a term or N-Gram within a document.
				 * The default discriminant function is Square root.</p>
				 * @param discriminant discriminant function applied to the computation of term frequency.
				 */
	public void setDiscriminant(final ITfDiscriminant discriminant) {
		_discriminant = discriminant;
	}
	
	
	public final ITfDiscriminant getTfDiscrimant() {
		return _discriminant;
	}

		/**
		 * <p>Computes the relative frequency of a term within a document or
		 * a document of document.
		 * @param relativeFrequency relative frequency of a term within a document
		 * @param idfValue inverse document frequency for a term, extracted from database.
		 * @return the relative normalized frequency of a term.
		 */
	public float computeTfIdf(float relFreq, float idfValue) {
		return (float)(_discriminant.compute(relFreq)*idfValue);
	}
	
	
	private CTfIdfScore() {
		_discriminant = new NTfSqrtDiscriminant();
	}
}


// ----------------------  EOF -------------------------------------------
