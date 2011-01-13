// Copyright (C) 2010-2011 Patrick Nicolas
package com.c24x7.util;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


import com.c24x7.nlservices.content.CStructuredOutput;


			/**
			 * <p>Cache for generated content.</p>
			 * @author Patrick Nicolas
			 * @date 01/10/2011
			 */
public final class CCache {
	private final static String DELIM_KEY 	= "[#]";
	private final static String DELIM_PAIR 	= "[##]";
	private final static String CACHE_FILE 	= "gencontent";
	private Map<String, CStructuredOutput> _cache = new HashMap<String, CStructuredOutput>();
	
	
	public CCache() throws IOException {
		load();
	}
	
	public final boolean uptodate() {
		return (_cache.size() > 1);
	}
	
			/**
			 * <p>Retrieve the content of the cache for this particular key.</p>
			 * @param key key or tag for the cached object.
			 * @return value object
			 */
	public CStructuredOutput get(final String key) {
		return _cache.get(key);
	}
	
	/**
	 * <p>Update the cache for a particular object</p>
	 * @param key key or tag for the cached object.
	 * @param output object to cache.
	 */
	public void put(final String key, CStructuredOutput output) {
		_cache.put(key, output);
	}
	
	
	/**
	 * <p>Persists the content of the cache.</p>
	 * @throws IOException if file system is not available.
	 */
	public void save() throws IOException {
		CFastIO.write(CEnv.cacheDir + CACHE_FILE, this.toString());
	}

	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for( String key : _cache.keySet()) {
			buf.append(key);
			buf.append(DELIM_KEY);
			buf.append(_cache.get(key).intern());
			buf.append(DELIM_PAIR);
		}
		
		return buf.toString();
	}
	
	
			/**
			 * <p>Load the content of the cache from file.</p>
			 * @throws IOException if file system cannot be accessed
			 */
	private void load() throws IOException {
		String content = CFastIO.read(CEnv.cacheDir + CACHE_FILE);
		
		int prevIndexPair = 0,
		    indexPair = 0,
		    indexField = 0;
		
		int delim_pair_len = DELIM_PAIR.length(),
			delim_key_len = DELIM_KEY.length();
		
		String key_values = null;
		while ( true ) {
			indexPair = content.indexOf(DELIM_PAIR, prevIndexPair);
		
			if( indexPair != -1) {
				key_values = content.substring(prevIndexPair, indexPair);
				indexField = key_values.indexOf(DELIM_KEY);
				if( indexField == -1 ) {
					break;
				}
				_cache.put(key_values.substring(0, indexField), new CStructuredOutput(key_values.substring(indexField + delim_key_len)));
				prevIndexPair = indexPair+delim_pair_len;
			}
			else {
				break;
			}
		}
	}
}
