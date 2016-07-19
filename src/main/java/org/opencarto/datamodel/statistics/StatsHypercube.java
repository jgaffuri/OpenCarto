/**
 * 
 */
package org.opencarto.datamodel.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * @author julien Gaffuri
 *
 */
public class StatsHypercube {
	public ArrayList<Stat> stats;
	public ArrayList<String> dimLabels;

	public StatsHypercube(){
		stats = new ArrayList<Stat>();
		dimLabels = new ArrayList<String>();
	}

	//return all values for a dimension
	public HashSet<String> getDimValues(String dimLabel) {
		HashSet<String> dimValues = new HashSet<String>();
		for(Stat s : stats)
			dimValues.add(s.dims.get(dimLabel));
		return dimValues;
	}

	//get all stats having a dim value
	public ArrayList<Stat> get(String dimLabel, String dimValue){
		ArrayList<Stat> stats_ = new ArrayList<Stat>();
		if(!dimLabels.contains(dimLabel)){
			System.err.println("No dimension label: " + dimLabel);
			return stats_;
		}
		for(Stat stat : stats)
			for(Entry<String,String> dim:stat.dims.entrySet())
				if(dim.getKey().equals(dimLabel) && dim.getValue().equals(dimValue))
					stats_.add(stat);
		return stats_;
	}


	//get all stats with a geo id and time stamp
	public ArrayList<Stat> getGeoTime(String geo, String time) {
		ArrayList<Stat> stats_ = new ArrayList<Stat>();
		for(Stat s:stats){
			if(!geo.equals(s.dims.get("geo"))) continue;
			if(!time.equals(s.dims.get("time"))) continue;
			stats_.add(s);
		}
		return stats_;
	}

	//get one object maximum with a geo id and time stamp
	public Stat getGeoTimeMaxOne(String geo, String time) {
		ArrayList<Stat> stats_ = getGeoTime(geo, time);
		if(stats_.size()==0) return null;
		if(stats_.size()==1) return stats_.get(0);
		System.err.println("More than 1 stats found for "+geo+" "+time);
		for(Stat s:stats_) System.out.println("   "+s);
		return null;
	}

	//get all stats with a geo id and time stamp, and having a dim value
	public ArrayList<Stat> getGeoTime(String geo, String time, String dimLabel, String dimValue) {
		ArrayList<Stat> stats_ = get(dimLabel, dimValue);
		stats_.retainAll(getGeoTime(geo, time));
		return stats_;
	}

	//get one object maximum with a geo id and time stamp, and having a dim value.
	public Stat getGeoTimeMaxOne(String geo, String time, String dimLabel, String dimValue) {
		ArrayList<Stat> stats_ = getGeoTime(geo, time, dimLabel, dimValue);
		if(stats_.size()==0) return null;
		if(stats_.size()==1) return stats_.get(0);
		System.err.println("More than 1 stats found for "+geo+" "+time+" "+dimLabel+" "+dimValue);
		for(Stat s:stats_) System.out.println("   "+s);
		return null;
	}

	//get all stats having all dimensions values specified (all of them, with a logical "and")
	public ArrayList<Stat> get(String[] dimLabels, String[] dimValues) {
		ArrayList<Stat> stats_ = new ArrayList<Stat>();
		for(Stat s:stats){
			boolean keep=true;
			for(int i=0;i<dimLabels.length;i++){
				if(!dimValues[i].equals(s.dims.get( dimLabels[i] ))){
					keep=false;
					break;
				}
			}
			if(keep) stats_.add(s);
		}
		return stats_;
	}

	//get one stat maximum having all dimensions values specified (all of them, with a logical "and")
	public Stat getMaxOne(String[] dimLabels, String[] dimValues) {
		ArrayList<Stat> stats_ = get(dimLabels, dimValues);
		if(stats_.size()==0) return null;
		if(stats_.size()==1) return stats_.get(0);
		System.err.println("More than 1 stats found !");
		for(Stat s:stats_) System.out.println("   "+s);
		return null;
	}

	//delete a dimension
	public void delete(String dimLabel){
		for(Stat s:stats){
			String out = s.dims.remove(dimLabel);
			if(out==null)
				System.err.println("Error: dimension "+dimLabel+" not defined for "+s);
		}
		dimLabels.remove(dimLabel);
	}

	//delete all stats having a given value for a dimension
	public void delete(String dimLabel, String dimValue){
		stats.removeAll( get(dimLabel, dimValue) );
	}

	//keep all stats having given values for a dimension
	public void keepOnly(String dimLabel, String... dimValues){
		Collection<String> dimVals = getDimValues(dimLabel);
		for(String dimValue:dimValues){
			if(!dimVals.contains(dimValue)){
				System.err.println("Error: Cannot keep only " + dimLabel + ":" + dimValue + ": No such value.");
				return;
			}
		}
		for(String s : dimVals){
			boolean delete=true;
			for(String dimValue:dimValues)
				if(s.equals(dimValue)) delete=false;
			if(delete) stats.removeAll( get(dimLabel, s) );
		}
	}

	//keep only the stats having a label value with a given length
	public void keepOnly(String dimLabel, int... sizes) {
		HashSet<String> values = getDimValues(dimLabel);
		for(String v:values){
			boolean delete=true;
			for(int size:sizes)
				if(v.length()==size) delete=false;
			if(delete) delete(dimLabel,v);
		}
	}

	//delete the stats having a label value with a given length
	public void delete(String dimLabel, int size) {
		HashSet<String> values = getDimValues(dimLabel);
		for(String v:values){
			if(v.length() != size) continue;
			delete(dimLabel,v);
		}
	}

	public void printDimensionInfo() {
		System.out.println("--- Dimension info of "+this+"---");
		System.out.println("--- "+dimLabels.size()+" dimensions.");
		for(String lbl:dimLabels){
			HashSet<String> vals = getDimValues(lbl);
			System.out.println("Dimension: "+lbl + " (with "+vals.size()+" values)");
			for(String val : vals)
				System.out.println("   "+val);
		}
	}


	public void exportAsCSV(String outFilePath) {
		exportAsCSV(outFilePath, "geo", "time");
	}

	public void exportAsCSV(String outFilePath, String geoLabel, String timeLabel) {
		exportAsCSV(outFilePath, geoLabel, timeLabel, null);
	}

	public void exportAsCSV(String outFilePath, String geoLabel, String timeLabel, String addLabel) {
		String sep="\t";
		try {
			new File(outFilePath).delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFilePath), true));

			//write header
			StringBuffer sb = new StringBuffer();
			sb.append(geoLabel).append(sep).append(timeLabel);
			if(addLabel!=null)
				sb.append(sep).append(addLabel);
			sb.append(sep).append("value").append("\n");
			bw.write(sb.toString());

			//write data
			for(Stat s : stats){
				sb = new StringBuffer();
				sb.append(s.dims.get(geoLabel)).append(sep).append(s.dims.get(timeLabel));
				if(addLabel!=null)
					sb.append(sep).append(s.dims.get(addLabel));
				sb.append(sep).append(s.value).append("\n");
				bw.write(sb.toString());
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//export all stats on objects, related to a dimension
	public void exportAsCSV(String outFilePath, String geoLabel, String timeLabel, String dimLabel, String[] dimValues, String sep) {
		try {
			new File(outFilePath).delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outFilePath), true));

			//write header
			StringBuffer sb = new StringBuffer();
			sb.append(geoLabel).append(sep).append(timeLabel).append(sep);
			for(int i=0;i<dimValues.length;i++){
				sb.append(dimValues[i]);
				if(i<dimValues.length-1) sb.append(sep);
			}
			sb.append("\n");
			bw.write(sb.toString());

			//write data
			for(String geo : getDimValues(geoLabel)){
				for(String time : getDimValues(timeLabel)){

					StringBuffer sbVal = new StringBuffer();
					for(int i=0; i<dimValues.length; i++){
						Stat s = getMaxOne(new String[]{geoLabel,timeLabel,dimLabel}, new String[]{geo,time,dimValues[i]});
						//System.out.println(s+" "+dimLabel+" "+dimValues[i]);
						if (s==null) {
							sbVal=null;
							break;
						};
						sbVal.append(s.value);
						if(i<dimValues.length-1) sbVal.append(sep);
					}
					if(sbVal==null) continue;

					sb = new StringBuffer();
					sb.append(geo).append(sep).append(time).append(sep).append(sbVal.toString()).append("\n");
					bw.write(sb.toString());

				}
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void checkGeoIds(Collection<String> nIds) {
		checkGeoIds("geo",nIds);
	}
	public void checkGeoIds(String geoLabel, Collection<String> nIds) {
		HashMap<String,Integer> missings = new HashMap<String,Integer>();
		//list and count missings
		for(Stat s : stats){
			String geoId = s.dims.get(geoLabel);
			if(nIds.contains(geoId)) continue;
			if(missings.get(geoId)==null)
				missings.put(geoId, 1);
			else
				missings.put(geoId, missings.get(geoId)+1);
		}
		//show result
		if(missings.size()>0) System.err.println("\t"+missings.size()+" geolocations missing");
		for(Entry<String,Integer> missing : missings.entrySet())
			System.err.println("\tUnknown geolocation id: "+missing.getKey()+" ("+missing.getValue()+" times)");
	}

}
