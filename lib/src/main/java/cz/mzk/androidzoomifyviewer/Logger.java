package cz.mzk.androidzoomifyviewer;

import cz.mzk.androidzoomifyviewer.viewer.TiledImageView;
import android.R.anim;
import android.util.Log;

/**
 * 
 * @author Martin Řehánek
 * 
 *         Logger that creates log for given level only if {@link TiledImageView#DEV_MODE} is true or
 *         {@link #PRODUCTION_LOG_LEVEL} is smaller or equal to given level.
 */
@SuppressWarnings("unused")
public class Logger {

	/*
	 * Log level for production. I.e. logs with this level or higher will be still produced even if {@link
	 * TiledImageView#DEV_MODE} == true. To disable logging in production completely set {@link #PRODUCTION_LOG_LEVEL}= {@link
	 * android.util.Log#ASSERT} + 1
	 */
	public static final int PRODUCTION_LOG_LEVEL = Log.WARN;
	public static final boolean PRODUCTION_ASSERT = PRODUCTION_LOG_LEVEL <= Log.ASSERT;
	public static final boolean PRODUCTION_ERROR = PRODUCTION_LOG_LEVEL <= Log.ERROR;
	public static final boolean PRODUCTION_WARN = PRODUCTION_LOG_LEVEL <= Log.WARN;
	public static final boolean PRODUCTION_INFO = PRODUCTION_LOG_LEVEL <= Log.INFO;
	public static final boolean PRODUCTION_DEBUG = PRODUCTION_LOG_LEVEL <= Log.DEBUG;
	public static final boolean PRODUCTION_VERBOSE = PRODUCTION_LOG_LEVEL <= Log.VERBOSE;

	private final String mTag;

	public Logger(Class<?> loggerClass) {
		mTag = loggerClass.getSimpleName();
	}

	public Logger(String tag) {
		mTag = tag;
	}

	public void d(String msg) {
		if (PRODUCTION_DEBUG || TiledImageView.DEV_MODE) {
			Log.d(mTag, msg);
		}
	}

	public void d(String msg, Throwable e) {
		if (PRODUCTION_DEBUG || TiledImageView.DEV_MODE) {
			Log.d(mTag, msg, e);
		}
	}

	public void e(String msg) {
		if (PRODUCTION_ERROR || TiledImageView.DEV_MODE) {
			Log.e(mTag, msg);
		}
	}

	public void e(String msg, Throwable e) {
		if (PRODUCTION_ERROR || TiledImageView.DEV_MODE) {
			Log.e(mTag, msg, e);
		}
	}

	public void i(String msg) {
		if (PRODUCTION_INFO || TiledImageView.DEV_MODE) {
			Log.i(mTag, msg);
		}
	}

	public void i(String msg, Throwable e) {
		if (PRODUCTION_INFO || TiledImageView.DEV_MODE) {
			Log.i(mTag, msg, e);
		}
	}

	public void v(String msg) {
		if (PRODUCTION_VERBOSE || TiledImageView.DEV_MODE) {
			Log.v(mTag, msg);
		}
	}

	public void v(String msg, Throwable e) {
		if (PRODUCTION_VERBOSE || TiledImageView.DEV_MODE) {
			Log.v(mTag, msg, e);
		}
	}

	public void w(String msg) {
		if (PRODUCTION_WARN || TiledImageView.DEV_MODE) {
			Log.w(mTag, msg);
		}
	}

	public void w(String msg, Throwable e) {
		if (PRODUCTION_WARN || TiledImageView.DEV_MODE) {
			Log.w(mTag, msg, e);
		}
	}

	public void wtf(String msg) {
		if (PRODUCTION_ASSERT || TiledImageView.DEV_MODE) {
			Log.wtf(mTag, msg);
		}
	}

	public void wtf(String msg, Throwable e) {
		if (PRODUCTION_ASSERT || TiledImageView.DEV_MODE) {
			Log.wtf(mTag, msg, e);
		}
	}

}
