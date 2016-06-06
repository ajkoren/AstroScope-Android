package com.astro.scope;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

public class WebServices 
{
	public static InputStream OpenHttpConnection(String urlString) 
	throws IOException 
	{
		InputStream in = null;
		int response = -1;
		
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		
		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");
		
		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		}
		catch (Exception ex) {
			throw new IOException("Error connecting");
		}
		return in;		
	}

	public static String getText(InputStream in)
	{
		int BUFFER_SIZE = 1000;
		InputStreamReader isr = new InputStreamReader(in);
		int charRead;
		String str = "";
		char[] inputBuffer = new char[BUFFER_SIZE];
		try {
			while ((charRead = isr.read(inputBuffer)) > 0) {
				String readString = 
					String.copyValueOf(inputBuffer, 0, charRead);
				str += readString;
				inputBuffer = new char[BUFFER_SIZE];
			}
			in.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return "";
		}
		return str;
	}
	
	public static String getXmlValue(String text, String tag) {
		String startTag = "<" + tag + ">";
		String endTag = "</" + tag + ">";
        Log.e("Geocoder", "startTag=" + startTag + ", endTag=" + endTag);
		int startOffset = text.indexOf(startTag) + startTag.length();
		int endOffset = text.indexOf(endTag);
        Log.e("Geocoder", "startOffset=" + startOffset + ", endOffset=" + endOffset);
		String xmlValue = text.substring(startOffset, endOffset);
		return xmlValue;
	}
}
