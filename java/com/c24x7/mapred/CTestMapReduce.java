package com.c24x7.mapred;



public class CTestMapReduce extends CValidationMapReduce {
	public CTestMapReduce() {
		super();
	}
	
	public CTestMapReduce(int numThreads) {
		super(numThreads);
	}
	
	@Override
	protected String getName() {
		return "Test";
	}
}

// --------------------------------------  EOF ----------------------------