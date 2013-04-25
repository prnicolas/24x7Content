/*
 * Copyright (C) 2010-2012  Patrick Nicolas
 */
package com.c24x7.semantics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.c24x7.exception.InitException;
import com.c24x7.models.ATaxonomyNode;
import com.c24x7.util.CEnv;
import com.c24x7.util.CFileUtil;
import com.c24x7.util.CFileUtil.IValueExtractor;



public class CTaxonomyRoots extends HashMap<String, Integer> {
	
	private static final long serialVersionUID = 3658766697133781714L;
	private static final String ROOT_CLASSES_FILE = CEnv.configDir + "root_taxonomy_classes";
	private static final int SHIFT = 9;

	/*
	public static class NRootClassesExtractor implements IValueExtractor {
		private CTaxonomyRoots _rootClassesMap = null;
		
		private NRootClassesExtractor(CTaxonomyRoots rootClassesMap) {
			_rootClassesMap = rootClassesMap;
		}
		
		@Override
		public void extract(final String key, final String value) {
			_rootClassesMap.put(key, value);
		}

	}
	*/
	
	private static CTaxonomyRoots instance = null;
	
	public static void init() throws InitException {
		instance = new CTaxonomyRoots();
		try {
			instance.load();
			System.out.println("Reference taxonomy graph ready");
		}
		catch (IOException e) {
			throw new InitException(e.toString());
		}
	}
	
	public static CTaxonomyRoots getInstance() {
		return instance;
	}
	
	/*
	public int distancex(final String from, final String to) {
		if( from == null || to == null) {
			throw new IllegalArgumentException("Cannot compare and score undefined taxonomy nodes");
		}
		int compareResult = 3;
		
		if(containsKey(from) && containsKey(to)) {
			int fromInt = get(from).intValue();
			int toInt = get(to).intValue();
			
			if( (fromInt & SHIFT) == (toInt & SHIFT) ) {
				compareResult = 1;
			}
			else if( (fromInt >> SHIFT) == (toInt >> SHIFT)) {
				compareResult = 2;
			}
		}
		
		return compareResult;
	}
	*/
	public boolean contains(final ATaxonomyNode node) {
		if( node == null ) {
			throw new IllegalArgumentException("Cannot get root from undefined node.");
		}
		return containsKey(node.getLabel());
	}
	
	
	
	
			// ---------------------------
			//  Private Supporting Methods
			// ----------------------------
		
	private CTaxonomyRoots() { } 
	
	private void load() throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		
		CFileUtil.readKeysValues(ROOT_CLASSES_FILE, map);
		int index = 0;
		for(String key : map.keySet() ) {
			this.put(key, Integer.valueOf(index));
			index++;
		}
	}

}
// -----------------  EOF -------------------------------------------------