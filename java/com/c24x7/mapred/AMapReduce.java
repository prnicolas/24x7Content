package com.c24x7.mapred;



public abstract class AMapReduce {
	protected int 			_numThreads 	= 1;
	protected IDataSets[] 	_setsArray 		= null;
	protected Thread[]		_mapReduceThreads = null;
	
	public AMapReduce() {
		this(1);
	}
	
	public AMapReduce(int numThreads) {
		_numThreads = numThreads;
	}
	
	abstract protected String getName();
	abstract public void map(int[] range, IDataSets dataSets);
	abstract public void reduce();

}

// -------------------------  EOF --------------------------------