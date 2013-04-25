package com.c24x7.mapred;


import com.c24x7.util.logs.CLogger;



public class CTrainingMapReduce extends AMapReduce {
	private IModelStats _record = null;
	
	public CTrainingMapReduce(IModelStats record) {
		this(record, 1);
	}
	
	public CTrainingMapReduce(IModelStats record, int numThreads) {
		super(numThreads);
		_record = record;
	}
	

	protected String getName() {
		return "Training";
	}
	

	public void map(int[] range, IDataSets dataSets) {
		
		IDataSets dataSet = null;
		if( _numThreads == 1) {
			dataSet = dataSets.create(range[0], range[1]);
			_setsArray = new IDataSets[] { dataSet };
			dataSet.map();
		}
		
			/*
			 * Launch all the threads used in training..
			 */
		else {
			final int interval = (int)((float)(range[1]-range[0])/_numThreads);
			_mapReduceThreads = new Thread[_numThreads];
			_setsArray = new IDataSets[_numThreads];
			
			for( int k = 0; k < _numThreads; k++) {
				range[1] = range[0] + interval;
				_setsArray[k] = dataSets.create(range[0], range[1]);
			
				_mapReduceThreads[k] = new Thread(dataSet);
				_mapReduceThreads[k].start();
				range[0] += interval;
			}
		}
	}
	
		/**
		 * <p>Aggregate and store the statistics for the training session.</p>
		 */
	public void reduce() {
		
			/*
			 * Aggregate or reduce the list of observations.
			 */
		for( int k = 0; k < _numThreads; k++) {
			try {
				if(_numThreads > 1) {
					_mapReduceThreads[k].join();
				}
				_setsArray[k].close();
			}
			catch( InterruptedException e) {
				CLogger.error("Failed to generate Lookup Map " + e.toString());
			}
		}
			
		/*
		 * If a minimum number of observations are valid, then compute
		 * the statistics (mean, standard deviation) of the model features
		 * or parameters and save them into the model file.
		 */

		if( _record != null && !_record.compute(_setsArray[0]) ) {
			CLogger.error("Cannot create model");
		}
	}

}

// ------------------------  EOF ------------------------------