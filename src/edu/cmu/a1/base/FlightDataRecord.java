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
import java.text.SimpleDateFormat;

public class FlightDataRecord
{
	// Main provides a primitive incomplete standalone unit test.
	public static void main(String[] args)
	{
		FlightDataRecord record = new FlightDataRecord();
		System.out.println(record);
		
		record.setTime(1);
		record.setVelocity(2.2);
		record.setAltitude(3.3);
		record.setTemperature(4.4);
		record.setPressure(5.5);
		record.setAttitude(6.6);

		System.out.println(record);
		System.out.println(record.toColumns());

		System.out.println("Done");
	}

	// Getters
	public long getTime()
	{
		return time_;
	}

	public Double setVelocity()
	{
		return velocity_;
	}
	
	public Double setAltitude()
	{
		return altitude_;
	}

	public Double setTemperature()
	{
		return temperature_;
	}

	public Double setPressure()
	{
		return pressure_;
	}

	public Double setAttitude()
	{
		return attitude_;
	}

	// Setters
	public void setTime(long time)
	{
		time_ = time;
	}

	public void setVelocity(Double velocity)
	{
		velocity_ = velocity;
	}
	
	public void setAltitude(Double altitude)
	{
		altitude_ = altitude;
	}

	public void setTemperature(Double temperature)
	{
		temperature_ = temperature;
	}

	public void setPressure(Double pressure)
	{
		pressure_ = pressure;
	}

	public void setAttitude(Double attitude)
	{
		attitude_ = attitude;
	}
	
		
	
	@Override
	public String toString()
	{
		return
			"Time: " + time_ +
	 		" Velocity: " + velocity_ +
	 		" Altitude: " + altitude_ +
			" Temperature: " + temperature_ +
	 		" Pressure: " + pressure_ +
	 		" Attitude: " + attitude_;
	}
	
	public static String columnHeader()
	{
		return	String.format("%24s, %16s, %16s, %16s, %16s, %16s", 
			"Time Stamp", "Velocity", "Altitude", "Temperature", "Pressure", "Attitude");
	}
	
	
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
}
