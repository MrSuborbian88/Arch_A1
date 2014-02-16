package edu.cmu.a1.base;

import java.util.HashMap;
import java.util.Set;

public class RecordDefinition {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	private class Record {
		public Class<?> type;
		public String title;
		public Record(Class<?> type,String title) {
			this.type = type;
			this.title=title;
		}
	}
	
	private HashMap<Integer,Record> records;
	
	public RecordDefinition() {
		records = new HashMap<Integer,Record>();
		addFieldDefinition(1,Integer.TYPE,"aaa");
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
		return keys.toArray(new Integer[keys.size()]);
	}
}
