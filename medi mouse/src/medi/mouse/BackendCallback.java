package medi.mouse;

import org.json.JSONObject;

public abstract class BackendCallback {
	public abstract void onPostExecute(JSONObject value);
}
