package com.spazedog.rootfw.tools;

import com.spazedog.rootfw.RootFW;
import com.spazedog.rootfw.containers.ShellResult;

import android.os.AsyncTask;
import android.os.Bundle;

public final class AsyncProcess extends AsyncTask<String, Void, ShellResult> {
	
	public static interface AsyncProcessReceiver {
		public void onAsyncProcessStart(Bundle argBundle);
		public void onAsyncProcessStop(ShellResult argResult, Bundle argBundle);
	}
	
	private AsyncProcessReceiver RECIEVER;
	private String[] CMD;
	private Boolean USEROOT;
	private Bundle BUNDLE;
	
	public static AsyncProcess injectInstance(String[] argCmd, Boolean argUseroot, Bundle argBundle, AsyncProcessReceiver argReciever) {
		return RootFW.injectAsyncProcess(new AsyncProcess(argCmd, argUseroot, argBundle, argReciever));
	}
	
	private AsyncProcess(String[] argCmd, Boolean argUseroot, Bundle argBundle, AsyncProcessReceiver argReciever) {
		CMD = argCmd;
		BUNDLE = argBundle;
		USEROOT = argUseroot;
		RECIEVER = (AsyncProcessReceiver) argReciever;
	}

	@Override
	protected ShellResult doInBackground(String... params) {
		RootFW rootfw = new RootFW(USEROOT);
		
		if (rootfw.isConnected()) {
			ShellResult result = rootfw.runShell(CMD);
			rootfw.close();
			
			return result;
		}

		return null;
	}
	
	@Override
	protected void onPreExecute() {
		if (RECIEVER != null) {
			RECIEVER.onAsyncProcessStart(BUNDLE);
		}
	}
	
	@Override
	protected void onPostExecute(ShellResult argResult) {
		if (RECIEVER != null) {
			RECIEVER.onAsyncProcessStop(argResult, BUNDLE);
		}
	}
}
