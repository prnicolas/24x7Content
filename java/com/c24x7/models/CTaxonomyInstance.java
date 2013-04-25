// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models;

import java.util.LinkedList;
import java.util.List;

import com.c24x7.semantics.lookup.CLookup;
import com.c24x7.textanalyzer.ngrams.CSemanticNGram;
import com.c24x7.textanalyzer.ngrams.CTaggedNGram;
import com.c24x7.textanalyzer.ngrams.CTaggedWord;
import com.c24x7.textanalyzer.tfidf.CTfIdfScore;
import com.c24x7.util.CEnv;



		/**
		 * <p>Class that encapsulates the structure of a taxonomyInstance or compound noun.
		 * A taxonomyInstance is a N-Gram composed of adjective, nouns and conjunction such
		 * as 'of, 'and'. A taxonomyInstance noun has a label (content), a normalized TF-IDF
		 * weight, a type (semantic definition or not) and a taxonomy defined as a
		 * list of sets of taxonomy classes (i.e 'Animals', 'Pets', 'Person',...)</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 02/16/2012
		 */

public final class CTaxonomyInstance extends ATaxonomyNode {
	private static final float MIN_COMPOSITE_WEIGHT 		= 1E-8F;
	public static final float NNP_TAG_BOOST_PARAM			= 1.2F;
	public static final float NGRAM_TERMS_INFLUENCE_PARAM 	= 2.1F;
	public static final int	NUM_TOPICS = 3;

	private static float nnpTagBoostParam 			= NNP_TAG_BOOST_PARAM;
	private static float nGramTermsInfluenceParam 	= NGRAM_TERMS_INFLUENCE_PARAM;
	
	
	public static void setParam(float newNnpTagBoostParam, float newNGramTermsFreqBoostParam) {
		nnpTagBoostParam = newNnpTagBoostParam;
		nGramTermsInfluenceParam = newNGramTermsFreqBoostParam;
	}

	private char  	_type 			= CLookup.DBPEDIA_UNDEFINED;
	private	int		_rank			= CTaggedNGram.ANY;
	private boolean	_isNormalized 	= false;
	private int		_boosting		= CTaggedWord.NO_BOOST;
	private List<ATaxonomyNode[]> _taxonomyNodesList = null;

	
			/**
			 * <p>Create a taxonomyInstance noun from a valid semantic record (extracted
			 * from the reference corpus database.</p>
			 * @param record semantic record associated with this taxonomyInstance noun
			 * @throws IllegalArgumentException if the semantic record is undefined.
			 */
	public CTaxonomyInstance(final CSemanticNGram semanticEntry, CTaggedNGram nGram) {
		this(semanticEntry);
		_rank = nGram.getRank();
		_boosting = nGram.getBoosting();
	}
	
	
			/**
			 * <p>Create a taxonomyInstance noun from a valid semantic record (extracted
			 * from the reference corpus database.</p>
			 * @param record semantic record associated with this taxonomyInstance noun
			 * @throws IllegalArgumentException if the semantic record is undefined.
			 */
	public CTaxonomyInstance(final CSemanticNGram semanticEntry) {
		super(semanticEntry.getLabel(), semanticEntry.getIdf());

		_type = semanticEntry.getState();
		if( _label == null) {
			_label = semanticEntry.getLabel();
		}
		
		_rank = CTaggedNGram.CONTAINS_NNP;
		_boosting = CTaggedWord.NO_BOOST;
	}

	
			/**
			 * <p>Compute the weight of this taxonomyInstance from the 
			 * normalized relative frequency [0, 1] of the taxonomyInstance
			 * with the document.</p> 
			 * @param relFrequency normalized relative frequency of this taxonomyInstance noun.
			 */
	public void computeWeight(double normalizedFreq) {
		_weight *= CTaggedWord.getWordWeight(_boosting);
		_weight =  CTfIdfScore.getInstance().computeTfIdf((float)normalizedFreq, _weight);
	}


	
	public char getType() {
		return _type;
	}	


	public void normalize(float maxWeight) { }


	public void addTaxonomyClasses(ATaxonomyNode[] taxonomyNodes) {
		if( _taxonomyNodesList == null) {
			_taxonomyNodesList = new LinkedList<ATaxonomyNode[]>();
		}
		_taxonomyNodesList.add(taxonomyNodes);
	}
	
	
	


	public final List<ATaxonomyNode[]> getTaxonomyNodesList() {
		return _taxonomyNodesList;
	}
	
	
	public ATaxonomyNode[] getLineageFromTopic(final String topicLabel) {
		ATaxonomyNode[] lineageFromTopic = null;
		
		if( _taxonomyNodesList != null) {
			if( _taxonomyNodesList.size() > 1) {
				for( ATaxonomyNode[] lineage : _taxonomyNodesList) {
					if(lineage[lineage.length-2].getLabel().compareTo(topicLabel) == 0) {
						lineageFromTopic = lineage;
						break;
					}
				}
			}
			else {
				lineageFromTopic = _taxonomyNodesList.get(0);
			}
		}
		
		return lineageFromTopic;
	}
	
	
	
	public ATaxonomyNode[] getPrimaryLineage() {
		ATaxonomyNode[] primaryLineage = null;
		
		if( _taxonomyNodesList != null) {
			if( _taxonomyNodesList.size() > 1) {
				float maxWeight = -1.0F,
				      weight 	= -1.0F;
				
				for( ATaxonomyNode[] lineage : _taxonomyNodesList) {
					weight =  lineage[lineage.length-2].getWeight();
					if(maxWeight < weight) {
						maxWeight = weight;
						primaryLineage = lineage;
					}
				}
			}
			else {
				primaryLineage = _taxonomyNodesList.get(0);
			}
		}
		
		return primaryLineage;
	}
	
	
	
	protected ATaxonomyNode[] getTaxonomyNodes(int order) {
		if( order < 1 || order > 4) {
			throw new IllegalArgumentException("Incorrect order of taxonomy class " + order);
		}
		
		ATaxonomyNode[] taxonomyOrderClass = null;
	
		if( _taxonomyNodesList != null) {
			taxonomyOrderClass = new ATaxonomyNode[_taxonomyNodesList.size()];
			int index = 0;
			for( ATaxonomyNode[] taxonomyClasses : _taxonomyNodesList) {
				taxonomyOrderClass[index++] = taxonomyClasses[taxonomyClasses.length - order -1];
			}
		}
		return taxonomyOrderClass;
	}
	
	
	@Override
	public float applyKirchoff(final float maxWeight) {
		float maxTaxonomyClassWeight = Float.MIN_VALUE;
		
		if( _taxonomyNodesList != null && maxWeight > MIN_COMPOSITE_WEIGHT) {
			_weight /= maxWeight;
			final float weight = _weight/_taxonomyNodesList.size();
			
			float taxonomyClassWeight = 0.0F;
			for( ATaxonomyNode[] taxonomyClasses : _taxonomyNodesList) {
				for( ATaxonomyNode taxonomyClass : taxonomyClasses) {
					taxonomyClassWeight = taxonomyClass.applyKirchoff(weight);
					
					if( taxonomyClassWeight > maxTaxonomyClassWeight) {
						maxTaxonomyClassWeight = taxonomyClassWeight;
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
	
		if( !_isNormalized ) {
			double maxNumTermsFactor = 0.0F;
			double nGramsRelFreq = (double)nGramNumOccurrences*inverseMaxFrequency;
			double termMaxRelFreq = (double)maxNGramTermOccurrences*inverseMaxFrequency;
			
			if(maxNGramTermOccurrences > nGramNumOccurrences ) {
				maxNumTermsFactor = nGramTermsInfluenceParam*(termMaxRelFreq - nGramsRelFreq);
			}
			_isNormalized = true;
			
			relFreq = (nGramsRelFreq*(_rank == 2 ? nnpTagBoostParam : 1.0) +  maxNumTermsFactor)* CTaggedWord.getWordWeight(_boosting);
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
					buf.append(taxonomyClasses[k].toString());
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

// ------------------------ EOF ---------------------------------
