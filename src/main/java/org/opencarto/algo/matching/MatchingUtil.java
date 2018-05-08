/**
 * 
 */
package org.opencarto.algo.matching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CSVUtil;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class MatchingUtil {
	private final static Logger LOGGER = Logger.getLogger(MatchingUtil.class.getName());

	public static int getLevenshteinDistance(String s1, String s2, boolean toLowerCase, boolean trim, boolean stripDiacritics, boolean stripWeirdCaracters) {
		String s1_=s1, s2_=s2;
		if(toLowerCase) { s1_=s1_.toLowerCase(); s2_=s2_.toLowerCase(); }
		if(trim) { s1_=s1_.trim(); s2_=s2_.trim(); }
		if(stripDiacritics) { s1_=Util.stripDiacritics(s1_); s2_=Util.stripDiacritics(s2_); }
		if(stripWeirdCaracters) { s1_=Util.stripWeirdCaracters(s1_); s2_=Util.stripWeirdCaracters(s2_); }
		return StringUtils.getLevenshteinDistance(s1_,s2_);
	}

	public static class Match {
		public String s1, s2;
		public double cost = 0;
	}

	public static Collection<Match> getMatchingMinLevenshteinDistance(Set<String> s1s, Collection<String> s2s, boolean toLowerCase, boolean trim, boolean stripDiacritics, boolean stripWeirdCaracters) {
		Collection<Match> out = new ArrayList<Match>();
		for(String s1 : s1s) {
			//evaluate distance to each s2, keeping the minimum one
			Match m = new Match(); m.cost = Integer.MAX_VALUE; m.s1 = s1;
			for(String s2 : s2s) {
				//evaluate distance
				int d = getLevenshteinDistance(s1, s2, toLowerCase, trim, stripDiacritics, stripWeirdCaracters);
				if(d>m.cost) continue;
				m.cost = d; m.s2 = s2;
			}
			out.add(m);
		}
		return out;
	}


	//make mathing based on a property and the Levenshtein distance
	public static Collection<Match> getMatchingMinLevenshteinDistance(Collection<Feature> f1s, String propF1, Collection<Feature> f2s, String propF2, boolean toLowerCase, boolean trim, boolean stripDiacritics, boolean stripWeirdCaracters) {
		return getMatchingMinLevenshteinDistance(FeatureUtil.getPropValues(f1s, propF1), FeatureUtil.getPropValues(f2s, propF2), true, true, true, true);
	}

	public static HashMap<String, Match> index(Collection<Match> ms) {
		HashMap<String,Match> msI = new HashMap<String,Match>();
		for(Match m : ms) msI.put(m.s1, m);
		return msI;
	}

	public static boolean override(HashMap<String,Match> msI, String sOld, String sNew) {
		Match m = msI.get(sOld);
		if(m == null) {
			LOGGER.warn("Could not override "+sOld+" into "+sNew);
			return false;
		}
		m.s2 = sNew;
		m.cost = 0;
		return true;
	}


	public static void save(HashMap<String, Match> msI, String outFile) {
		CSVUtil.save(data, outFile);
	}

}
