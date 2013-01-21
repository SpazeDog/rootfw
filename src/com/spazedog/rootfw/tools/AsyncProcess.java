/*
 * This file is part of the RootFW Project: https://github.com/spazedog/rootfw
 *  
 * Copyright (c) 2013 Daniel Bergløv
 *
 * RootFW is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RootFW is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with RootFW. If not, see <http://www.gnu.org/licenses/>
 */

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
