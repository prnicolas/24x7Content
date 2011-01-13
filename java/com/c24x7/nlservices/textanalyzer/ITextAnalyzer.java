// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices.textanalyzer;

import java.util.Map;

public interface ITextAnalyzer {
	public Map<String, Integer> getSignificantWords(final String inputText);
}

// ------------------------  EOF ----------------------------------