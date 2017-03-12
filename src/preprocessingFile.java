import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Ninad S. Ingale
 *	Date : 07/30/2016
 *	This program caters the need of data preprocessing for the indoor
 *	localization challenge. 
 *
 */
public class preprocessingFile {

	static double [][] inputData;
	static double [][] accelerometer;
	static double [][] gyroScope;
	static double [][] magnetometer;
	static double gyroCaclculation;
	static double accel;
	static int columnNumber;
	static int shortColumnNumber;
	static int count;
	static int rowCount;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			readFile();
			populateSensorData();
			gyroScopeCalc();
			acceleroCalc();
			writeNewFile();
	}

	
	/*
	 *  This method is responsible for converting the 3 dimensional values of  
	 *  angular velocity to a single value for the sake of simplicity. 
	 */
	// 3D to single gyro reading in deg/sec. 
	private static void gyroScopeCalc() {
		// TODO Auto-generated method stub
		double[] gyroCaclculation=new double[inputData.length];
		for (int i=0;i<inputData.length;i++){
			double value=Math.sqrt(gyroScope[i][0]*gyroScope[i][0]+
						 		   gyroScope[i][1]*gyroScope[i][1]+
						 		   gyroScope[i][2]*gyroScope[i][2]);
			double degPerSec=value*57.295;
			gyroCaclculation[i]=degPerSec;			
		}		
		
	}

	/*
	 *  This method is responsible for converting the 3 dimensional values of  
	 *  linear acceleration to a single value for the sake of simplicity. 
	 */
	
	//combines accelerometer values of ax, ay, az.
	private static void acceleroCalc() {
		// TODO Auto-generated method stub
		double[] accCaclculation = new double[inputData.length];
			
		for (int i=0;i<inputData.length;i++){
			double accvalue=Math.sqrt(accelerometer[i][0]*accelerometer[i][0]+
								   accelerometer[i][1]*accelerometer[i][1]+
						 		   accelerometer[i][2]*accelerometer[i][2]);
			// Store the magnitude of acceleration for further steps.
			accelerometer[i][3] = accvalue;
		}		
		
	}
	
	// This method is responsible for calculating additional derived attributes 
	// and then writing the results to a file for further processing. 
	private static void writeNewFile() {
		// TODO Auto-generated method stub
		double[][] modifiedData=new double[inputData.length][inputData[0].length+5];
		
		//System.out.println("Input Length"+inputData.length);
		
		for (int i=0;i<modifiedData.length;i++){
			for (int j=0;j<4;j++){
				modifiedData[i][j]=inputData[i][j];
			}
		}
				
		for (int i=0;i<inputData.length;i++){
			modifiedData[i][4]=accelerometer[i][0];
			modifiedData[i][5]=accelerometer[i][1];
			modifiedData[i][6]=accelerometer[i][2];
			
			modifiedData[i][7]=gyroScope[i][0];
			modifiedData[i][8]=gyroScope[i][1];
			modifiedData[i][9]=gyroScope[i][2];
			
			modifiedData[i][10]=magnetometer[i][0];
			modifiedData[i][11]=magnetometer[i][1];
			modifiedData[i][12]=magnetometer[i][2];
			
			modifiedData[i][13]=accelerometer[i][3];
		}
		
		// Create array to hold the preprocessed data. 
		for (int i=1;i<modifiedData.length-1;i++){
			if ((modifiedData[i][13]-modifiedData[i-1][13])>0 || (modifiedData[i][13]-modifiedData[i+1][13])<0){
				count++;	
			}
		}
		
		// Form a string array in order to write to a text file.
		String[][] writeData=new String[count+1][modifiedData[0].length];
			for (int j=0;j<modifiedData[0].length;j++){
				writeData[0][j]=String.valueOf(modifiedData[0][j]);
			}
		
		rowCount=1;
		
			for (int i=1;i<modifiedData.length-1;i++){
				if ((modifiedData[i][13]-modifiedData[i-1][13])>0 || (modifiedData[i][13]-modifiedData[i+1][13])<0){
					for (int j=0;j<modifiedData[0].length;j++){
					writeData[rowCount][j]=String.valueOf(modifiedData[i][j]);	
					}
					rowCount++;
				}
			}
			
		// Write contents of the input file and derived attributes to another file					
		try {
			FileWriter writer=new FileWriter("E:/Independent Study/Output/output.csv");
			
			for (int i=0;i<writeData.length;i++){
				for (int j=0;j<writeData[0].length;j++){
					writer.write(String.valueOf(writeData[i][j])+",");
				}
				writer.write("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
 }

	// Reading the input file. 
	public static void readFile() {
		// TODO Auto-generated method stub
		try 
		{
			Scanner inputScanner=new Scanner (new BufferedReader(new FileReader("E:/Independent Study/Input/device319.csv")));			
			inputScanner.nextLine();
			ArrayList <String> stringInput=new ArrayList<String>();
		
			while (inputScanner.hasNextLine()){
				String input=inputScanner.nextLine();
				stringInput.add(input);
			}
			
			int rowNumber=0;

			String[] inputString=new String[stringInput.size()];

			inputData=new double[stringInput.size()][stringInput.get(1).split(",").length];

			for (int i=0;i<stringInput.size();i++){
				inputString=stringInput.get(i).split(",");
				for (int j=0;j<inputString.length;j++){
					String str=inputString[j].trim();
					inputData[i][j]=Double.parseDouble(str); 
				}
			}
		}
		
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 	This method is responsible for segregating the individual sensor data.  	
	 */
	public static void populateSensorData(){
		
		accelerometer=new double[inputData.length][4];
		
		for (int i=0;i<inputData.length;i++){
				accelerometer[i][0]=inputData[i][4];
				accelerometer[i][1]=inputData[i][5];
				accelerometer[i][2]=inputData[i][6];
			}
		
		for (int i=1;i<accelerometer.length;i++){
			for (int j=0;j<accelerometer[0].length;j++){
				if (accelerometer[i][j]==0)
				accelerometer[i][j]=accelerometer[i-1][j];
			}
		}
		
		gyroScope=new double[inputData.length][3];
		
		for (int i=0;i<inputData.length;i++){
				gyroScope[i][0]=inputData[i][7];
				gyroScope[i][1]=inputData[i][8];
				gyroScope[i][2]=inputData[i][9];
			}
		
		for (int i=1;i<accelerometer.length;i++){
			for (int j=0;j<gyroScope[0].length;j++){
				if (gyroScope[i][j]==0)
				gyroScope[i][j]=gyroScope[i-1][j];
			}
		}
		
		magnetometer=new double[inputData.length][3];
		
		for (int i=0;i<inputData.length;i++){
			magnetometer[i][0]=inputData[i][10];
			magnetometer[i][1]=inputData[i][11];
			magnetometer[i][2]=inputData[i][12];
			}

		for (int i=1;i<magnetometer.length;i++){
			for (int j=0;j<magnetometer[0].length;j++){
				if (magnetometer[i][j]==0)
				magnetometer[i][j]=magnetometer[i-1][j];
			}
		}		
	} 
}