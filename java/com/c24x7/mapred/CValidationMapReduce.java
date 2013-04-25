package com.c24x7.mapred;


import com.c24x7.util.logs.CLogger;



public class CValidationMapReduce extends CTrainingMapReduce {
	
	public CValidationMapReduce() {
		super(null, 1);
	}
	
	public CValidationMapReduce(int numThreads) {
		super(null, numThreads);
	}
	
	@Override
	protected String getName() {
		return "Validation";
	}


	/**
	 * <p>Aggregate and store the statistics for the validation session.</p>
	 */
	@Override
	public void reduce() {
		if( _setsArray != null && _setsArray.length > 0) {
			int[] count = new int[2];	
			
			for( int k = 0; k < _numThreads; k++ ) {
				try {
					if( _numThreads > 1) {
						_mapReduceThreads[k].join();
					}
					

					_setsArray[k].collectResults(count);
					_setsArray[k].close();
				}
				catch( InterruptedException e) {
					CLogger.error("Failed to generate Lookup Map " + e.toString());
				}
			}
			
			_setsArray[0].saveResults(count);
		}
	}
}

// ----------------------------- EOF --------------------------
