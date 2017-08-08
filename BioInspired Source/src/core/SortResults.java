package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SortResults {

	public static void main(String[] args) throws IOException{
		File file = new File("Results.txt");
		
		int numBases = 7;
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		// metric, fitness, eval
		String[][][] res = new String[4][3][8];
		
		for(int i=0;i<res.length;i++){
			for(int j=0;j<res[0].length;j++){
				reader.readLine();
				for(int k=0;k<res[0][0].length;k++){
					reader.readLine();
					reader.readLine();
					reader.readLine();
					reader.readLine();
					StringBuilder str = new StringBuilder(1000);
					
					for(int z=0;z<numBases;z++){
						str.append(reader.readLine() + "\n");
					}
					res[i][j][k] = str.toString();
				}
			}
		}
		reader.close();
		
		String[] messages = { "Robustez", "Novidade(Distance)", "Novidade(Similarity)", "Estabilidade" };
		String[][] metricMessages = { { "Menor", "Média" }, { "Menor", "Média" }, { "Menor", "Média" }, { "Média", "Desvio" } };
		
		String[] metrics = {"CalinskiHarabasz", "Jaccard", "Dunn", "Dom"};
		String[] fitness = {"CorrectRand","DaviesBouldin","MX"};
		
		int count = 0;
		String[] text = new String[8]; 
		for(int i=0;i<messages.length;i++){
			for(int j=0;j<metricMessages[i].length;j++){
				text[count++] = messages[i] + "("+ metricMessages[i][j] +")";
			}
		}
		
		for(int i=0;i<text.length;i++){
			System.out.println(); //TODO
			System.out.println(text[i]); //TODO
			
			for(int j=0;j<metrics.length;j++){
				System.out.println(); //TODO
				System.out.println(metrics[j]); //TODO
				System.out.println("Base	ACO	AG	BCO	CRO1	CRO2	CRO3	PSO		ACO	AG	BCO	CRO1	CRO2	CRO3	PSO");
				for(int k=0;k<fitness.length;k++){
					System.out.println(fitness[k]); //TODO
					System.out.print(res[j][k][i]); //TODO
				}
			}
		}
	}
}
