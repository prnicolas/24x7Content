package com.c24x7.mapred;

public interface IDataSets extends Runnable {
	public void map();
	public void close();
	public IDataSets create(int startIndex, int endIndex);
	public void collectResults(int[] counters);
	public void saveResults(int[] counters);
}

// -------------------  eof ----------------------------------------------