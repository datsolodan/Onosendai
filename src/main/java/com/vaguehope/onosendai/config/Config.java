package com.vaguehope.onosendai.config;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Environment;

import com.vaguehope.onosendai.C;
import com.vaguehope.onosendai.util.FileHelper;

public class Config {

	public Config () throws IOException, JSONException {
		File f = new File(Environment.getExternalStorageDirectory().getPath(), C.CONFIG_FILE_NAME);
		String s = FileHelper.fileToString(f);
		JSONObject o = (JSONObject) new JSONTokener(s).nextValue();
		System.err.println(o);
	}

}
