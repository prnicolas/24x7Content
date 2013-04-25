package com.c24x7.models;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.c24x7.semantics.lookup.CLookup;
import com.c24x7.semantics.lookup.CLookupRecord;
import com.c24x7.textanalyzer.tfidf.CTfIdfScore;
import com.c24x7.util.CEnv;
import com.c24x7.math.utils.CFreqArray;





public class CTaxonomyObject extends ATaxonomyNode {
	private static final float MIN_COMPOSITE_WEIGHT 		= 1E-8F;
	private static final float NGRAM_TERMS_INFLUENCE_PARAM 	= 2.1F;
	private static final int   NUM_TOPICS = 3;

	private char  	_type 		= CLookup.DBPEDIA_UNDEFINED;
	private boolean	_tfComputed = false;
	private int		_count 		= 1;
	
	private List<ATaxonomyNode[]> 	_taxonomyNodesList = null;
	private CFreqArray 				_sentencesIndices = null;
	
	
			/**
			 * <p>Create a taxonomyInstance noun from a valid semantic record (extracted
			 * from the reference corpus database.</p>
			 * @param record semantic record associated with this taxonomyInstance noun
			 * @param label label for the taxonomy object.
			 * @throws IllegalArgumentException if the semantic record is undefined.
			 */
	public CTaxonomyObject(final CLookupRecord record, final String label) {
		super(label, record.getIdf());
		_type = record.getType();
	}

			/**
			 * <p>Compute the weight of this taxonomyInstance from the 
			 * normalized relative frequency [0, 1] of the taxonomyInstance
			 * with the document.</p> 
			 * @param relFrequency normalized relative frequency of this taxonomyInstance noun.
			 */
	public void computeWeight(double normalizedFreq) {
		_weight =  CTfIdfScore.getInstance().computeTfIdf((float)normalizedFreq, _weight);
	}
		
		
	public void incrCount() {
		_count++;
	}
	
	public void incrCount(int incr) {
		_count += incr;
	}
	
	public int getCount() {
		return _count;
	}
	
		
	public char getType() {
		return _type;
	}	
		
		
	public void normalize(float maxWeight) { }
		
	
	public void getTopTaxonomyClasses(Set<ATaxonomyNode> treeSet) {
		if( treeSet == null ) {
			throw new IllegalArgumentException("Cannot extract top classes into undefined set");
		}
		if( _taxonomyNodesList == null) {
			throw new NullPointerException("Cannot extract top classes from undefined lists");
		}
		
		for( ATaxonomyNode[] taxonomyClasses: _taxonomyNodesList) {
			treeSet.add(taxonomyClasses[0]);
		}
	}
		
			/**
			 * <p>Attach a new taxonomy lineage to this taxonomy object.
			 * lineages with a single node (root class) are discarded.</p>
			 * @param taxonomyNodes array of taxonomy classes extracted from the Wikipedia reference database
			 */
	public void addTaxonomyClasses(ATaxonomyNode[] taxonomyNodes) {
		if( taxonomyNodes == null || taxonomyNodes.length < 1) {
			throw new IllegalArgumentException("Cannot add undefined taxonomy classes to an object");
		}

		if( _taxonomyNodesList == null) {
			_taxonomyNodesList = new LinkedList<ATaxonomyNode[]>();
		}
		_taxonomyNodesList.add(taxonomyNodes);
	}
		
		
			
	public final List<ATaxonomyNode[]> getTaxonomyNodesList() {
		return _taxonomyNodesList;
	}
	
	public void addSentenceIndex(int sentenceIndex) {
		if( _sentencesIndices == null) {
			_sentencesIndices = new CFreqArray(sentenceIndex);
		}

		_sentencesIndices.add(sentenceIndex);
	}
	
	public final CFreqArray getSentencesIndices() {
		return _sentencesIndices;
	}
	
	
	
		/**
		 * <p>Normalize the weight of the taxonomy objects and classes using
		 * the Kirchoff's electrical current law. The weight of the taxonomy object is 
		 * normalized by the maximum weight of any taxonomy objects in the document. The
		 * weight is then propagated along the lineage using the Kirchoff's law. The
		 * weight of each taxonomy class is normalized by the number of lineages
		 * associated with this taxonomy object.</p>
		 * @param maxWeight maximum weight of any taxonomy object within this document.
		 */
	@Override
	public float applyKirchoff(final float maxWeight) {
		float maxTaxonomyClassWeight = Float.MIN_VALUE;
		
		if( _taxonomyNodesList != null && maxWeight > MIN_COMPOSITE_WEIGHT) {
			_weight /= maxWeight;
			final float weight = _weight/_taxonomyNodesList.size();
			
			float taxonomyClassWeight = 0.0F;
			for( ATaxonomyNode[] taxonomyClasses : _taxonomyNodesList) {
				for( ATaxonomyNode taxonomyClass : taxonomyClasses) {
					if(taxonomyClass != null) {
						taxonomyClassWeight = taxonomyClass.applyKirchoff(weight);
						
						if( taxonomyClassWeight > maxTaxonomyClassWeight) {
							maxTaxonomyClassWeight = taxonomyClassWeight;
						}
					}
				}
			}
		}
		
		return maxTaxonomyClassWeight;
	}	
		


		/**
		 * <p>Compute the normalized frequency of a n-Gram within a document. The
		 * value is normalized [0, 1] by the maximum frequency or number of occurrences of any
		 * terms in this document.</p>
		 * 
		 * @param oneGramNumOccurrences Number of occurrences of this n-Gram in the document
		 * @param maxNGramTermOccurrences Maximum number of occurrences of any term composing this N-Gram
		 * @param inverseMaxFrequency inverse of the maximum frequency of any term in the document
		 * @return normalized frequency of this N-Gram in a document
		 */
	public double computeTf(int 	nGramNumOccurrences, 
							int 	maxNGramTermOccurrences, 
							double 	inverseMaxFrequency) {
		double relFreq =  CEnv.UNINITIALIZED_DOUBLE;
	
			/*
			 * Make sure we compute the relative term frequency only once.
			 */
		if( !_tfComputed ) {
			double maxNumTermsFactor = 0.0F;
			double nGramsRelFreq = (double)nGramNumOccurrences*inverseMaxFrequency;
			double termMaxRelFreq = (double)maxNGramTermOccurrences*inverseMaxFrequency;
			
			if(maxNGramTermOccurrences > nGramNumOccurrences ) {
				maxNumTermsFactor = NGRAM_TERMS_INFLUENCE_PARAM*(termMaxRelFreq - nGramsRelFreq);
			}
			relFreq = nGramsRelFreq +  maxNumTermsFactor;
			
			_tfComputed = true;
		}
		
		return relFreq;
	}
	
	
	
	public String printSummary() {
		StringBuilder buf = new StringBuilder(_label);
		buf.append(" W=");
		buf.append(_weight);
		buf.append(" T=");
		buf.append(_type);
		
		return buf.toString();
	}
	
	
	public String printTopics() {
		StringBuilder buf = new StringBuilder(_label);
		buf.append(" W=");
		buf.append(_weight);
		buf.append(" T=");
		buf.append(_type);
		
		if( _taxonomyNodesList != null) {
			for( ATaxonomyNode[] taxonomyClasses : _taxonomyNodesList) {
				
				int highestTopicIndex = taxonomyClasses.length-NUM_TOPICS;
				if(highestTopicIndex < 3)  {
					highestTopicIndex = 3;
				}
				buf.append("\n");
				for(int k = highestTopicIndex; k < taxonomyClasses.length; k++) {
					buf.append(taxonomyClasses[k].getWeight());
					buf.append(" ");
				}
			}
		}
	
		return buf.toString();
	}
	
	
	public String printDetails() {
		StringBuilder buf = new StringBuilder(_label);
		buf.append(" W=");
		buf.append(_weight);
		buf.append(" T=");
		buf.append(_type);
		
		if( _taxonomyNodesList != null) {
			for( ATaxonomyNode[] taxonomyClasses: _taxonomyNodesList) {
				buf.append("\n");
				for(int k = 0; k < taxonomyClasses.length; k++) {
					buf.append("/");
					if(taxonomyClasses[k] == null ) {
						buf.append("null");
					}
					else {
						buf.append(taxonomyClasses[k].toString());
					}
				}
			}
		}
				
		return buf.toString();
	}
	
	@Override 
	public String toString() {
		return printSummary();
	}
	
}


 //------------------------------- EOF -----------------------------------