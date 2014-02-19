package Filters;

/******************************************************************************************************************
* File:SinkFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
* filter reads some input from the filter's input port and does the following:
*
*	1) It parses the input stream and "decommutates" the measurement ID
*	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
*
* This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
* the stream: namely time (long type) and measurements (double type).
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/

import java.util.*;						// This class is used to interpret time words
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

import Templates.BaseFilter;


public class SinkFilter extends BaseFilter
{
	Calendar TimeStamp = null;
	SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");

	double defaultValue = Double.MIN_VALUE;		// This is important and should be something that will never be confused with a possible value.  0 is bad because that could be a valid temp.  Standard practice is to use the min value and hope that that's not a valid value; 
	
	double lastRecievedVelocity = defaultValue;
	double lastRecievedAltitude = defaultValue;
	double lastRecievedPressure = defaultValue;
	double lastRecievedTemperature = defaultValue;
	double lastRecievedAttitude = defaultValue;
	
	Calendar LastOutputtedTimeStamp = null;
	
	StringBuffer outputBuffer = new StringBuffer();
	
	@Override public void DoInnerWork()
	{
		switch(id)
				{
				case 0:
					
					//first output the previous frame					
					OutputLastRecievedFrame();
					
					//now start the parsing process for the next frame;
					TimeStamp = Calendar.getInstance();
					TimeStamp.setTimeInMillis(this.measurement);
					
					//important to clear them out when a new time comes in
					lastRecievedVelocity = defaultValue;
					lastRecievedAltitude = defaultValue;
					lastRecievedPressure = defaultValue;
					lastRecievedTemperature = defaultValue;
					lastRecievedAttitude = defaultValue;
					
					break;
				case 1:
					lastRecievedVelocity = this.GetMeasurementAsDouble();
					break;
				case 2:
					lastRecievedAltitude = this.GetMeasurementAsDouble();
					break;
				case 3:
					lastRecievedPressure = this.GetMeasurementAsDouble();
					break;
				case 4:
					lastRecievedTemperature = this.GetMeasurementAsDouble();
					break;
				case 5:
					lastRecievedAttitude = this.GetMeasurementAsDouble();
					break;
				default:
					
					//throw new Exception("bad kitty!");
					break;
				}
		
	}
	
	@Override public void DoFinalWork()
	{
		// This ensures that the last record is outputted
		// there's a bug here somewhere but I don't have time this morning to debug it
		//if(TimeStamp != LastOutputtedTimeStamp)
		{
			// this ensures that if some odd reason the last two frame datum's were timestamps, it doesn't get outputted twice
			OutputLastRecievedFrame();
			
            System.out.print( "And hey, look at that, we're done!");
		}
		
		WriteOutputToFile();		
		
	}
	
	@Override public void SendIdAndMeasurementToOutputStream()
	{
		// This gets overridden because there's nowhere else to send the data... so poof!
	}
	
	private void OutputLastRecievedFrame()
	{
		LastOutputtedTimeStamp = TimeStamp;
		
		if(TimeStamp != null)
		{
			System.out.print( "\n");
					
			System.out.print( 
								"Time = " + TimeStampFormat.format(TimeStamp.getTime()) +							 
								"; Vel = " + lastRecievedVelocity +
								"; Alt = " + lastRecievedAltitude +
								"; Pre = " + lastRecievedPressure +
								"; Temp = " + lastRecievedTemperature +
								"; Att = " + lastRecievedAttitude
								);
			
			// also create the output file entry
			
			String entry = TimeStampFormat.format(TimeStamp.getTime()) + "\t" + lastRecievedTemperature + "\t" + lastRecievedAltitude + "\r\n";
			outputBuffer.append(entry);
		}
	}
	
	
	private void WriteOutputToFile()
	{			
		String filename = "//Users/binky/Sandbox/eclipse/PipelineTest1/src/DataFiles/output.txt";

		try 
		{
		  BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		  
		  String outText = outputBuffer.toString();
		  
		  out.write(outText);		  
		  out.close();
		   
		}
		catch (IOException e)		    
		{
		    e.printStackTrace();		    
		}

	}
	
	

} // SingFilter