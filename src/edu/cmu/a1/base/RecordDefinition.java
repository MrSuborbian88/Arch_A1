package edu.cmu.a1.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class RecordDefinition {

	/******************************************************************************************************************
	 * File:Record.java
	 * 
	 * 
	 * Description:
	 *
	 * This class defines the format of a record stream, with field ids, class types, and names for
	 * each field.
	 *
	 ******************************************************************************************************************/

	private class Record {
		public Class<?> type;
		public String title;
		//Internal class to keep records (type & name) together
		private Record(Class<?> type,String title) {
			this.type = type;
			this.title=title;
		}
	}
	
	private HashMap<Integer,Record> records;
	
	public RecordDefinition() {
		records = new HashMap<Integer,Record>();
	}
	
	public void addFieldDefinition(Integer fieldID, Class<?> type, String title) {
		records.put(fieldID, new Record(type,title));
	}
	
	public Class<?> getFieldType(Integer fieldID) {
		if(records.containsKey(fieldID))
			return records.get(fieldID).type;
		else
			return null;
	}
	
	public String getFieldTitle(Integer fieldID) {
		if(records.containsKey(fieldID))
			return records.get(fieldID).title;
		else
			return null;
	}
	
	public Integer[] getFieldCodes() {
		Set<Integer> keys = records.keySet();
		Integer[] codes = keys.toArray(new Integer[keys.size()]);
		Arrays.sort(codes);
		return codes;
	}
	
	public boolean hasFieldCode(Integer fieldID) {
		return records.containsKey(fieldID);
	}
}
