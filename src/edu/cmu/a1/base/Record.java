package edu.cmu.a1.base;
/******************************************************************************************************************
* File:SinkFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2012 Carnegie Mellon University
* Versions:
*	1.0 February 2012
*
* Description:
*
* This class is a simple base data structure to hold a flight data record.  Use the
* FilterFrameworkExtended class to create these from an input stream.
*
******************************************************************************************************************/
import java.util.HashMap;

public class Record
{
	private HashMap<Integer,Object> valueMap;
	private RecordDefinition recordDefinition;
	
	public Record(RecordDefinition recordDefinition) {
		if(recordDefinition == null)
			throw new NullPointerException("Null RecordDefinition");
		this.recordDefinition=recordDefinition;
		valueMap = new HashMap<Integer,Object>();
		
		//for(Integer code : recordDefinition.getFieldCodes())
		//	valueMap.put(code, null);
		
	}
	// Main provides a primitive incomplete standalone unit test.
	public static void main(String[] args)
	{
		RecordDefinition recordDef = new RecordDefinition();

		recordDef.addFieldDefinition(000, Long.TYPE, "Time");
		recordDef.addFieldDefinition(001, Double.TYPE, "Velocity");
		recordDef.addFieldDefinition(002, Double.TYPE, "Altitude");
		recordDef.addFieldDefinition(003, Double.TYPE, "Pressure");
		recordDef.addFieldDefinition(004, Double.TYPE, "Temperature");
		recordDef.addFieldDefinition(005, Double.TYPE, "Attitude");


		Record record = new Record(recordDef);
		System.out.println(record);
		
		record.setValueByCode(0,1);
		record.setValueByCode(1,1);
		record.setValueByCode(2,1);
		record.setValueByCode(3,1);
		record.setValueByCode(4,1);
		record.setValueByCode(5,1);
		
		System.out.println(record);
//		System.out.println(record.toColumns());

		System.out.println("Done");
	}

	// Getters
	public Object getValueByCode(Integer code) throws IllegalArgumentException
	{
		if(valueMap.containsKey(code))
			return valueMap.get(code);
		else
			throw new IllegalArgumentException("Code not in record.");
	}

	public void setValueByCode(Integer code, Object value)
	{
		this.valueMap.put(code, value);
	}
	
	public Integer [] getCodes() {
		return this.recordDefinition.getFieldCodes();
	}
	
	public Class<?> getTypeByCode(Integer code) {
		return this.recordDefinition.getFieldType(code);
	}
	public String getTitleByCode(Integer code) {
		return this.recordDefinition.getFieldTitle(code);
	}
	@Override
	public String toString()
	{
		String str = "";
		for(Integer code : this.recordDefinition.getFieldCodes())
			str += this.recordDefinition.getFieldTitle(code) + "("+code.toString()+"): " + 
					(this.valueMap.containsKey(code) ? valueMap.get(code) : "") + " " ;
		return str;
	}
	
	public String columnHeader()
	{
		String str = "";
		for(Integer code : this.recordDefinition.getFieldCodes())
			String.format("%24s, ", 
					this.recordDefinition.getFieldTitle(code));
		if(str.length() >= 2)
		str = str.substring(0, str.length()-2);
		return str;
	}
	
	/*
	// Left to the team...
	//public toByteStream
	
	public String toColumns()
	{
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss.SSS");
		return	String.format("%s, %16.6f, %16.6f, %16.6f, %16.6f, %16.6f", 
				TimeStampFormat.format(time_),
		 		velocity_, altitude_, temperature_, pressure_, attitude_);
	}


	// We always have time!
	protected long time_ = 0;
	protected Double velocity_ = null;
	protected Double altitude_ = null;
	protected Double temperature_ = null;
	protected Double pressure_ = null;
	protected Double attitude_ = null;
	*/
}
