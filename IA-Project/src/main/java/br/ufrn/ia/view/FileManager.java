package br.ufrn.ia.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

import br.ufrn.ia.fileManipulation.FiltraExtensoes;

public class FileManager {

	public File[] selecionarArquivos() {
		String url = ".";
		File[] files;

		JFileChooser chooser = null;
		chooser = new JFileChooser(url);
		chooser.addChoosableFileFilter(new FiltraExtensoes());
		chooser.setMultiSelectionEnabled(true);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			files = chooser.getSelectedFiles();
			return files;
		} else {
			return null;
		}
	}
	
	public File selecionarArquivo() {
		String url = ".";
		File file;

		JFileChooser chooser = null;
		chooser = new JFileChooser(url);
		chooser.addChoosableFileFilter(new FiltraExtensoes());
		chooser.setMultiSelectionEnabled(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
			return file;
		} else {
			return null;
		}
	}
	
	public void escreverArquivo(String nomeArquivo, String conteudo) throws IOException{
		//File saida = new File("Trip_kMeans_RCRAI_Si.txt");
		File saida = new File(nomeArquivo);
		
		FileOutputStream fos = new FileOutputStream(saida);    
        fos.write(conteudo.toString().getBytes());  
        fos.close();        
        System.out.println("Arquivo " + nomeArquivo + " salvo!");
	}
}
