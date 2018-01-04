package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class Divide {

	public static void main(String[] args) throws IOException {
		File file = new File("C:\\Users\\Antonino\\Documents\\Experiments Clustering\\Resultados\\ResultsAll.part");

		File[] divide = new File[10];
		for (int i = 0; i < divide.length; i++)
			divide[i] = new File(file.getParent(), "Results.part" + i);

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();

		int count = 0;
		int index = 0;
		while(line != null){
			
			PrintStream output = new PrintStream(new FileOutputStream(divide[index], true));
			output.println(line);
			output.close();
			
			count = count+1;
			if(count==63){
				count = 0;
				index = (index + 1) % 10;
			}
			line = reader.readLine();
		}
		reader.close();
		System.out.println("END");
	}

}
