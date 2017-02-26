package org.aksw.kbox.appel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

public class KNSTable {
	
	public final static String FILE_SERVER_TABLE_FILE_NAME = "table.kns";
	
	private final static Logger logger = Logger.getLogger(KNSTable.class);	
	
	private URL tableURL = null;
	
	public KNSTable(URL url) throws MalformedURLException {		
		this(url.toString());
	}
	
	public KNSTable(String url) throws MalformedURLException {
		tableURL = new URL(url + "/" + FILE_SERVER_TABLE_FILE_NAME);
	}
	
	/**
	 * Resolve a given resource with by the given KNS service.
	 * 
	 * @param resourceURL the URL of the resource that will be resolved by the given KNS service.
	 * @return the resolved URL.
	 * @throws IOException if any error occurs during the operation.
	 */
	public URL resolveURL(URL resourceURL) throws IOException {
		return resolve(resourceURL, tableURL);
	}
	
	/**
	 * Resolve a given resource with by the given KNS service.
	 * 
	 * @param knsServerURL the URL of KNS server that will resolve the given URL.
	 * @param resourceURL the URL of the resource that will be resolved by the given KNS service.
	 * @return the resolved URL.
	 * @throws IOException if any error occurs during the operation.
	 */
	public static URL resolve(URL resourceURL, URL knsServerURL) throws IOException {
		URL tableURL = new URL(knsServerURL.toString() + "/" + FILE_SERVER_TABLE_FILE_NAME);
		try(InputStream is = tableURL.openStream()) {
			try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while((line = in.readLine()) != null) {
				    KN kns = null;
					try {
						if(!line.isEmpty()) {
							kns = KN.parse(line);
							if(kns.getName().equals(resourceURL.toString())) {
							   return new URL(kns.getTarget());
							}
						}
					} catch (Exception e) {
						logger.error("KNS Table entry could not be parsed: " + line, e);
					}
				}
			}
		}
		return null;
	}
	
	public boolean visit(KNSVisitor visitor) throws IOException {
		InputStream is = tableURL.openStream();
		try(BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
			String line = null;
			while((line = in.readLine()) != null) {
			    KN kn = null;
				try {
					String url = tableURL.toString();
					url = url.replace("/" + FILE_SERVER_TABLE_FILE_NAME, "");
					kn = KN.parse(line);
					kn.setKNS(url);
					boolean keep = visitor.visit(kn);
					if(!keep) {
						return false;
					}
				} catch (Exception e) {
					logger.error("KNS Table entry could not be parsed: " + line, e);
				}
			}
			return true;
		}
	}
}
