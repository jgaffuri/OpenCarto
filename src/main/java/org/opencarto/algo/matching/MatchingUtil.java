/**
 * 
 */
package org.opencarto.algo.matching;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class MatchingUtil {

	public static int getLevenshteinDistance(String s1, String s2, boolean toLowerCase, boolean trim, boolean stripDiacritics, boolean stripWeirdCaracters) {
		String s1_=s1, s2_=s2;
		if(toLowerCase) { s1_=s1_.toLowerCase(); s2_=s2_.toLowerCase(); }
		if(trim) { s1_=s1_.trim(); s2_=s2_.trim(); }
		if(stripDiacritics) { s1_=Util.stripDiacritics(s1_); s2_=Util.stripDiacritics(s2_); }
		if(stripWeirdCaracters) { s1_=Util.stripWeirdCaracters(s1_); s2_=Util.stripWeirdCaracters(s2_); }
		return StringUtils.getLevenshteinDistance(s1_,s2_);
	}


	public static class Match {
		public Feature f1, f2;
		public double cost = 0;
	}

	//make mathing based on a property and the Levenshtein distance
	public static Collection<Match> getMatchingMinLevenshteinDistance(Collection<Feature> f1s, String propF1, Collection<Feature> f2s, String propF2, boolean toLowerCase, boolean trim, boolean stripDiacritics, boolean stripWeirdCaracters) {
		Collection<Match> out = new ArrayList<Match>();
		for(Feature f1 : f1s) {
			//evaluate distance to each f2, keeping the minimum one
			Match map = new Match(); map.cost = Integer.MAX_VALUE; map.f1 = f1;
			for(Feature f2 : f2s) {
				//evaluate distance f/u
				int d = getLevenshteinDistance(f1.get(propF1).toString(),f2.get(propF2).toString(), toLowerCase, trim, stripDiacritics, stripWeirdCaracters);
				if(d>map.cost) continue;
				map.cost = d; map.f2 = f2;
			}
			out.add(map);
		}
		return out;
	}

}
