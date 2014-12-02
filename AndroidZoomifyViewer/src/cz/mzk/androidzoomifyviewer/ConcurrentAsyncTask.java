package cz.mzk.androidzoomifyviewer;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

/**
 * AsyncTask that uses thread pool if available. Android (since Honeycomb) executes all AsyncTask on single thread.
 * 
 * @see http ://www.jayway.com/2012/11/28/is-androids-asynctask-executing-tasks-serially -or-concurrently/
 * 
 * @author Martin Řehánek
 */

public abstract class ConcurrentAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	@SuppressLint("NewApi")
	public final AsyncTask<Params, Progress, Result> executeConcurrentIfPossible(Params... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			return execute(params);
		}
	}

}
