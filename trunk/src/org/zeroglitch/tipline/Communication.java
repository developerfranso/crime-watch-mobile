/**
 * 
 */
package org.zeroglitch.tipline;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author jamie
 * 
 */
public class Communication extends Thread implements Runnable {

	List<NameValuePair> params = null;
	private String URL = "http://www.zeroglitch.org:9090/TiplineMobileServer/jaxrs/tip";
	private int RETURN_CODE = -1;
	public static int OK = 0;
	public static int FAILURE = 1;
	public static int PROCESSING = -1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public int getReturnCode() {
		return RETURN_CODE;
	}

	public void setReturnCode(int rETURN_CODE) {
		RETURN_CODE = rETURN_CODE;
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while (!postData()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//if (postData()) {
				RETURN_CODE = OK;
				// success = true;
				//this.stop();
				return;
		//	}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// success = false;
			RETURN_CODE = FAILURE;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// success = true;
			RETURN_CODE = FAILURE;

		}
		RETURN_CODE = FAILURE;
		//this.stop();
		return;
	}

	public List<NameValuePair> getParams() {
		return params;
	}

	public void setParams(List<NameValuePair> params) {
		this.params = params;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public boolean postData() throws ClientProtocolException, IOException {

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(URL);

		// Add your data

		httppost.setEntity(new UrlEncodedFormEntity(params));

		httpclient.execute(httppost);

		return true;

	}

}
