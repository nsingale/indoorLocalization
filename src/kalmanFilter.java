import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import Jama.Matrix;

/**
 * 
 * @author Ninad S. Ingale 
 * 
 * This program is responsible for consuming the preprocced input
 * file and then feed the desired attributes to the Kalman filter
 * for prediction.
 *
 */
public class kalmanFilter {

	static double[][] processedInputData;
	static double[][] newMagnetometer;
	static double[][] stateVector;
	static double[][] predictStateVector;
	
	// Reading the preprocessed file generated after executing 
	// the preprocessingFile.java file.
	private static void readProcessedFile() {
		// TODO Auto-generated method stub
		try 
		{
			Scanner inputScanner=new Scanner (new BufferedReader(new FileReader("E:/Independent Study/Output/output.csv")));
			
			inputScanner.nextLine();
			ArrayList <String> stringInput=new ArrayList<String>();
		
			while (inputScanner.hasNextLine()){
				String input=inputScanner.nextLine();
				stringInput.add(input);
			}
			
			int rowNumber=0;

			String[] inputString=new String[stringInput.size()];

			processedInputData=new double[stringInput.size()][stringInput.get(1).split(",").length+5];

			for (int i=0;i<stringInput.size();i++){
				inputString=stringInput.get(i).split(",");
				for (int j=0;j<inputString.length;j++){
					
					processedInputData[i][j]=Double.parseDouble(inputString[j]); 
				}
			}
		}		
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	/*
	 *  This method is responsible to calculate the step distance , pitch and Roll
	 *  at any particular instance of time. 
	 */
	private static void calculate() {
		// TODO Auto-generated method stub
		processedInputData[0][15]=0; 
//		
			for (int i=0;i<processedInputData.length-1;i++){			
		//s=ut+1/2at2
			processedInputData[i][15]=processedInputData[i][13]*processedInputData[i][14]+
			(0.5*(processedInputData[i][13]*processedInputData[i][14]*processedInputData[i][14]));
		// X axis angle
			double x_angle=0;
			x_angle=Math.atan2(processedInputData[i][8],processedInputData[i][9]);
			processedInputData[i][16]=(x_angle);
//			System.out.println(x_angle);
		// Y axis angle			
			double y_angle=0;
			y_angle=Math.atan2(processedInputData[i][7],processedInputData[i][9]);
			processedInputData[i][17]=(y_angle);
//			System.out.println(x_angle+"\t\t"+y_angle);			
		}		
  }

	/*
	 *  This method is one of the most important methods in the entire prediction 
	 *  process. The angle calculated in this method is then provided to the kalman 
	 *  filter predict method. 
	 */
	private static void headingDirectionDetection() {
		// TODO Auto-generated method stub
		
		for (int i=0;i<processedInputData.length;i++){
		double[][] m=new double[][]{
			{processedInputData[i][10]},
			{processedInputData[i][11]},
			{processedInputData[i][12]}
			};	
		// Current magnetometer reading at any particular instance.	
		Matrix magnetometer= new Matrix(m);
		
		// Rotational matrix for rotation around X-axis.
		double x_rotation[][]= new double[][]{
			{1,0,0},
			{0,Math.cos(processedInputData[i][12]),Math.sin(processedInputData[i][12])},
			{0,-1*Math.sin(processedInputData[i][12]),Math.cos(processedInputData[i][12])}
		};
		
		// Converting the double array to the matrix. 
		Matrix x_rotation_matrix= new Matrix(x_rotation);

		// Rotational matrix for rotation around Y-axis.
		double y_rotation[][]= new double[][]{
			{Math.cos(processedInputData[i][12]),0,-Math.sin(processedInputData[i][12])},
			{0,1,0},
			{Math.sin(processedInputData[i][12]),0,Math.cos(processedInputData[i][12])}
		};

		// Converting the double array to the matrix.
		Matrix y_rotation_matrix= new Matrix(y_rotation);
		
		// Resultant heading direction vector combining the gyroscope and magnetometer
		// sensor readings.
		Matrix Matrix_result= magnetometer.transpose().times(x_rotation_matrix).times(y_rotation_matrix);
		double[][] result_matrix=Matrix_result.getArray();
		
		// Predict the heading direction.		
		for (int u=0;u<result_matrix.length;u++){		
			double angle=0;
			processedInputData[i][18]=Math.atan2(result_matrix[u][1],result_matrix[u][0]);
		}

	   }
 }
	
	/*
	 *  This method is responsible for plotting the actual and predicted values. 
	 */
	private static void plotLineChart(){		
		XYSeries lineSeries = new XYSeries("ax Vs. ay");
		for(int i=0;i<processedInputData.length;i++){
			lineSeries.add(processedInputData[i][4],processedInputData[i][5]);	
		}
		
		// To plot SSE vs. Number of clusters Line chart. 
		XYSeriesCollection lineData = new XYSeriesCollection();
		lineData.addSeries(lineSeries);
		
		JFreeChart lineChart=ChartFactory.createXYLineChart(
				"Actual Values", 
				"ax", 
				"ay", 
				lineData, 
				PlotOrientation.VERTICAL, 
				true, 
				true, 
				false);
//	###################################################################### 
		
		
		XYSeries predictedLineSeries = new XYSeries("ax Vs. ay");
		for(int i=0;i<predictStateVector.length;i++){
			predictedLineSeries.add(predictStateVector[i][0],predictStateVector[i][1]);	
		}
		
		// To plot SSE vs. Number of clusters Line chart. 
		XYSeriesCollection predictLineData = new XYSeriesCollection();
		predictLineData.addSeries(predictedLineSeries);
		
		JFreeChart predictLineChart=ChartFactory.createXYLineChart(
				"Predicted Values", 
				"ax", 
				"ay", 
				predictLineData, 
				PlotOrientation.VERTICAL, 
				true, 
				true, 
				false);

		// Saving the JPEG file on local machine. 
		try {
			ChartUtilities.saveChartAsJPEG(new File("E:/Independent Study/Output/ActualLineChart.jpg"), lineChart, 500, 300);
			ChartUtilities.saveChartAsJPEG(new File("E:/Independent Study/Output/PredictedLineChart.jpg"), predictLineChart, 500, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart3.");
		}
		
	}

	/*
	 *  This is the most important method of this algorithm. 
	 *  This method implements the non linear kalman filter in order 
	 *  to predict the next location of the user at the next instance.  
	 */
	private static void predict(){
		stateVector=new double[processedInputData.length][3];
		predictStateVector=new double[processedInputData.length][3];
		
		for (int i=0;i<processedInputData.length;i++){
			
			// Current state is represented by this vector. 
			stateVector=new double[1][3];
				stateVector[0][0]=processedInputData[i][4];
				stateVector[0][1]=processedInputData[i][5];
				stateVector[0][2]=processedInputData[i][18];
				
		Matrix currentMatrix=new Matrix(stateVector);

		// Predicted heading direction is represented by this vector. 
		double[][] headingDirectoin=new double[][]{
			{Math.cos(stateVector[0][2])},
			{Math.sin(stateVector[0][2])},
			{0}
		};

			Matrix heading=new Matrix(headingDirectoin);
			
			// Non Linear Kalman filter matrix multiplication. 
			Matrix predict=currentMatrix.transpose().plus(heading);
			
			// Store the predicted results for plotting. 
			double[][] result=predict.getArray();
			
			predictStateVector[i][0]=result[0][0];
			predictStateVector[i][1]=result[1][0];
			predictStateVector[i][2]=result[2][0];
		}	
			
		// Console display for the predicted values. 	
		for (int i=0;i<predictStateVector.length;i++){
			for (int j=0;j<predictStateVector[0].length;j++){
				System.out.print(predictStateVector[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		readProcessedFile();
		calculate();
		headingDirectionDetection();
		predict();
		plotLineChart();
	}
}
