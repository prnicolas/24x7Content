// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.textanalyzer.ngrams;

import java.util.HashMap;
import java.util.Map;

import com.c24x7.exception.InitException;
import com.c24x7.textanalyzer.tfidf.CTfVector;
import com.c24x7.util.string.CStringUtil;


		/**
		 * <p>Class that defines a tagged word where tag are extracted from a document
		 * using a classifier. The tag or type of a tagged word are NN (noun), 
		 * NNP (proper noun), JJ (adjective..... A tagged word is composed of its label
		 * (single word), its tag and the number of references within the document.
		 * 
		 * @author Patrick Nicolas
		 * @date 2/17/2012
		 */
public final class CTaggedWord {
	
	public static final int	 NO_BOOST			 			= 0;
	public static final int  FIRST_SENTENCE_BOOST 			= 1;
	public static final int  FIRST_SUBJECT_ATTRIBUTE_BOOST 	= 2;
	
	public static final float NO_BOOST_WEIGHT 						= 1.0F;
	public static final float FIRST_SENTENCE_BOOST_WEIGHT 			= 1.7F;
	public static final float FIRST_SUBJECT_ATTRIBUTE_BOOST_WEIGHT 	= 2.0F;
	
	private static Map<String, ETAG_TYPES> 	tagsTypes = null;
	private static Map<String, ETAG_TYPES> 	tokensTypes = null;
	
			/**
			 * <p>Define the tag for terms that are used in extracting
			 * bags of words and ultimately part of speech.
			 * @author Patrick Nicolas
			 * @date 12/05/2011
			 */
	public enum ETAG_TYPES {
		JJ("JJ"), OA("OA"), NN("NN"), NNS("NN"), NNP("NNP"), NNPS("NNP"), POS("POS"), TH("TH"), FI("FI");
		
		protected String _tag = null;
		ETAG_TYPES(final String tag) {
			_tag = tag;
		}
				
		public final String getTag() {
			return _tag;
		}
		
		
		public boolean isNoun() {
			return (_tag.charAt(0) == 'N');
		}
		
		public boolean isNNP() {
			return (_tag.length() == 3 && _tag.charAt(2) =='P');
		}
		
		
		public boolean isNN() {
			return (_tag.charAt(0) == 'N' &&  _tag.length() == 2);
		}
		
		public static boolean isPlural(final String tag) {
			return (tag.charAt(tag.length()-1) == 'S');
		}
		
		public static ETAG_TYPES getTagType(final String tag) {
			return tagsTypes.get(tag);
		}
		
		public static ETAG_TYPES getTagType(final String tag, String token) {
			ETAG_TYPES tagType = getTagType(tag);
			
			if ( tagType == null && tokensTypes.containsKey(token)) {
				tagType = tokensTypes.get(token);
			}
			
			return tagType;
		}
	}
			
		
	public static class NETagTypes {
		private ETAG_TYPES[] _eTagTypes = null;
		
		public NETagTypes(int numTags) {
			_eTagTypes = new ETAG_TYPES[numTags];
		}
		
		public void set(int index, ETAG_TYPES tagType) {
			_eTagTypes[index] = tagType;
		}
		
		public int getNumTags() {
			return _eTagTypes.length;
		}
		
		public final ETAG_TYPES[] getTagTypes() {
			return _eTagTypes;
		}
	}
		
	public static void init() throws InitException {
		tagsTypes = new HashMap<String, ETAG_TYPES>();
		tagsTypes.put("JJ", ETAG_TYPES.JJ);
		tagsTypes.put("NN", ETAG_TYPES.NN);
		tagsTypes.put("NNS", ETAG_TYPES.NN);
		tagsTypes.put("NNP", ETAG_TYPES.NNP);
		tagsTypes.put("NNPS", ETAG_TYPES.NNP);
		tagsTypes.put("POS", ETAG_TYPES.POS);
		
		tokensTypes = new HashMap<String, ETAG_TYPES>();
		tokensTypes.put("of", ETAG_TYPES.OA);
		tokensTypes.put("and", ETAG_TYPES.OA);
		tokensTypes.put("the", ETAG_TYPES.TH);
		tokensTypes.put("in", ETAG_TYPES.FI);
		tokensTypes.put("for", ETAG_TYPES.FI);
	}
	
	
	public static float getWordWeight(int boosting) {
		return (boosting == FIRST_SUBJECT_ATTRIBUTE_BOOST) ?
				FIRST_SUBJECT_ATTRIBUTE_BOOST_WEIGHT :
				(boosting == FIRST_SENTENCE_BOOST) ? FIRST_SENTENCE_BOOST_WEIGHT : NO_BOOST_WEIGHT;
	}
	
	private String 		_word 		= null;
	private String		_stem 		= null;
	private ETAG_TYPES  _type		= null;	
	private boolean		_unknownTag = false;
	private int			_boosting	= NO_BOOST;
	
		/**
		 * <p>Create a tagged word as defined by its content (single term) and its
		 * type (or tag). By default the number of references is set to 1. </p>
		 * @param word single term for this word
		 * @param type tag associated to the word.
		 */
	public CTaggedWord(final String word, final ETAG_TYPES type) {
		_word = word;
		_type = type;
	}
	
	public void setBoosting(int boosting) {
		_boosting = boosting;
	}
	
	
	public void setStem(final String stem) {
		_stem = stem;
	}
	
	
	/*
	public float getWeight() {
		return _weight;
	}
	*/
	
	public boolean isBoosted() {
		return (_boosting != NO_BOOST);
	}
	
	public final int getBoosting() {
		return _boosting;
	}

	public String getStem() {
		return _stem;
	}

	public int getRank() {
		return (_type.isNNP() ? 2 : _type.isNN() ? 1 : 0);
	}

		/**
		 * <p>Retrieve the content or term of this tagged word.</p>
		 * @return word or content of this tagged word.
		 */
	public final String getWord() {
		return _word;
	}
	
	public boolean isNoun() {
		return _type.isNoun();
	}
	
	
	public boolean isNNP() {
		return _type.isNNP();
	}
	
	public boolean isNN() {
		return _type.isNN();
	}
	
	
	
	public void updateTfVector(CTfVector tfVector) {
		if(isNoun() || _type == ETAG_TYPES.JJ) {
			if( _unknownTag) {
				tfVector.putUnknownCase(_word, _stem);
			}
			else {
				tfVector.put(_word, _stem);
			}
		}
	}
	
	public boolean isValidFirstWord() {
		char firstCharacter = _type.getTag().charAt(0);
		return firstCharacter == 'N' || firstCharacter == 'J'; 
	}
	
	public boolean isValidLastWord() {
		char firstCharacter = _type.getTag().charAt(0);
		return firstCharacter == 'N';
	}
	
	
		/**
		 * <p>Fast implementation to conversion of the label of this N-Gram to lower case.</p>
		 */
	public void convertToLowerCase() {
		_word = CStringUtil.convertFirstCharToLowerCase(_word);
	}
	

		/**
		 * <p>Retrieve the type or tag of this word.</p>
		 * @return tag of the word.
		 */
	public final ETAG_TYPES getTagType() {
		return _type;
	}
	
	
	public void setTagType(ETAG_TYPES eType) {
		_type = eType;
	}
	
	
	public boolean isTagUnknown() {
		return _unknownTag;
	}
	
	public void resetTagType() {
		if( _type == ETAG_TYPES.NNP) {
			_unknownTag = true;
		}
		else {
			convertToLowerCase();
		}
	}
	
		/**
		 * <p>Retrieve the tag of this word as a string.</p>
		 * @return textual form of the tag.
		 */
	public final String getTag() {
		return _type.toString();
	}
	

		/**
		 * <p>Test if this word shares the same tag as another word.</p>
		 * @param otherWord word for which is tag is evaluated.
		 * @return true if both words have the same tag, false otherwise.
		 */
	public final boolean compareTag(final CTaggedWord word) {
		return (_type == word.getTagType());
	}
	
	
		/**
		 * <p>Generate a textual representation of this tagged word.</p>
		 * return string representation.
		 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(_word);
		buf.append("(");
		buf.append(_type);
		buf.append(")");
		
		return buf.toString();
	}
}

// ------------------------------ EOF --------------------------------
