// Copyright (C) 2010-2012 Patrick Nicolas
package com.c24x7.models.tags;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.logs.CLogger;



		/**
		 * <p>Class that contains the features values for the NGram tag model.
		 * The tag model is built during training of the NGram tag classifier. The
		 * model is implemented as a singleton.</p>
		 * 
		 * @author Patrick Nicolas
		 * @date 03/120/2012
		 */
public final class CTagsModel {
	public static final int MAX_NGRAM_SIZE 	= 4;
	protected static final String MODEL_FILE =  CEnv.modelsDir + "semantic/ngrams_tag_model";

	
		/**
		 * <p>Class that defines the probability distribution tags.</p>
		 * @author Patrick Nicolas
		 * @date 03/11/2012
		 */
	private class NTagsDistribution extends HashMap<String, Integer> {
		private static final long serialVersionUID = 8573712763479234387L;
	}
	
	private static CTagsModel instance = null;
	
	private NTagsDistribution[] _tagsDistribution = null;
	
			/**
			 * <p>Initialize the NGrams tag model. This static method is called
			 * during the initialization of the application.</p>
			 * @see com.c24x7.util.CEnv.init()
			 * @return true if the model is correctly loaded and initialized, false otherwise.
			 */
	public static boolean init() {
		try {
			instance = new CTagsModel();
			instance.loadModel();
		}
		catch( IOException e) {
			CLogger.error("Cannot load the NGrams tag model");
		}
		catch( NumberFormatException e) {
			CLogger.error("Cannot load the NGrams tag model");
		}
		return (instance != null);
	}
	
			/**
			 * <p>Access the unique instance (Singleton) of the NGrams tag model.</p>
			 * @return singleton for the NGrams tag model
			 */
	public static CTagsModel getInstance() {
		return instance;
	}
	
	
	public boolean isValid(final String tag) {
		return (rank(tag)!= null);
	}
	
			/**
			 * <p>Retrieve the rank of a N-Gram with a specified tag, 
			 * (probability of any semantic N-Gram).</p>
			 * @param tag tag of the N-Gram,
			 * @return integer if N-Gram is semantically valid, null otherwise
			 */
	public Integer rank(final String tag) {
		return (_tagsDistribution[0].containsKey(tag)) ? _tagsDistribution[0].get(tag) : null;
	}
	
			/**
			 * <p>Retrieve the rank of the sub N-Gram with a specified tag,</p>
			 * @param tag tag of the N-Gram,
			 * @param index of the last tag
			 * @return integer if N-Gram is semantically valid, null otherwise
			 */
	public Integer rank(final String tag, int lastTagIndex) {
		return (lastTagIndex < MAX_NGRAM_SIZE && _tagsDistribution[lastTagIndex].containsKey(tag)) ? 
				_tagsDistribution[lastTagIndex].get(tag) : null;
	}

	
			// -------------------------
			// Private Supporting Methods
			// --------------------------
	
	private CTagsModel() throws IOException {
		_tagsDistribution = new NTagsDistribution[MAX_NGRAM_SIZE];
		for( int k = 0; k < MAX_NGRAM_SIZE; k++) {
			_tagsDistribution[k] = new NTagsDistribution();
		}
	}
	
	
	private boolean loadModel() throws IOException, NumberFormatException {
		List<String> entriesGroups = new LinkedList<String>();
		CFileUtil.readEntries(MODEL_FILE, CEnv.ENTRIES_DELIM, entriesGroups);
		
		String[] entries = null;
		String[] fields = null;
		int fieldCounter = 0;
		
		for( String entriesGroup : entriesGroups) {
			entries = entriesGroup.split("\n");
			for( String entry : entries) {
				fields = entry.split(CEnv.KEY_VALUE_DELIM);
				if(fields.length < 2) {
					throw new IOException("Incorrect NGram Tag Model file format");
				}
				_tagsDistribution[fieldCounter].put(fields[0], Integer.parseInt(fields[1]));
			}
			fieldCounter++;
		}
		return true;
	}
}

// ------------------------------  EOF --------------------------------