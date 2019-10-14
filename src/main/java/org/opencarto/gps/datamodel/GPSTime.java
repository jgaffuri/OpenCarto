/**
 * 
 */
package org.opencarto.gps.datamodel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author julien Gaffuri
 *
 */
public class GPSTime implements Comparable<GPSTime>{
	//final static Logger logger = Logger.getLogger(GPSTime.class.getName());

	/**
	 * get the duration in seconds between two dates
	 * 
	 * @param start the start date
	 * @param end the end date
	 * @return
	 */
	public static double getDurationS(GPSTime start, GPSTime end) {
		Date end_ = end.getDate();
		if(end_==null) return -1;
		Date start_ = start.getDate();
		if(start_==null) return -1;
		return (end_.getTime() - start_.getTime()) * 0.001;
	}

	private static DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'");

	private static DateFormat dfToString = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static DateFormat dfNiceDate = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
	private static DateFormat dfHM = new SimpleDateFormat("HH:mm");
	//private static DateFormat dfDay = new SimpleDateFormat("EEEE", Locale.ENGLISH);

	// 2010-03-08T08:24:53Z
	// 2009-10-04T13:00:49.000Z
	private String timeString;

	public GPSTime(String timeString) {
		this.timeString = timeString;
	}

	private Date date;
	public Date getDate(){
		if (this.date == null) {
			try { this.date = df1.parse(this.timeString); }
			catch (ParseException e) {
				try { this.date = df2.parse(this.timeString); }
				catch (ParseException e1) {
					//logger.error("Impossible to parse date: " + this.timeString);
					System.err.println("Impossible to parse date: " + this.timeString);
					this.date = null;
					//e1.printStackTrace();
				}
			}
		}
		return this.date;
	}

	public String getNiceDate(){
		return dfNiceDate.format(getDate());
	}
	public String getHM(){
		return dfHM.format(getDate());
	}

	public int compareTo(GPSTime t) {
		return this.getDate().compareTo(t.getDate()); 
	}

	@Override
	public synchronized String toString() {
		return dfToString.format(getDate());
	}
}
