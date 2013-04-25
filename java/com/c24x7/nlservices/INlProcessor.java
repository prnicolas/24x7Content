// Copyright (C) 2010 Patrick Nicolas
package com.c24x7.nlservices;

import java.io.IOException;

import com.c24x7.nlservices.content.AContent;


public interface INlProcessor {
	public AContent process(AContent content) throws IOException;
}

// ----------------------  EOF -------------------------------