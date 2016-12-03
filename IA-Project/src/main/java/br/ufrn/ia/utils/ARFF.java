package br.ufrn.ia.utils;

public enum ARFF {
	
	Iris("../Iris.arff"),
	Balance("../Balance.arff"),
	Weather("../weather.nominal.arff"),
	Binary("../Binary.arff");

	public String location;
	
	ARFF (String location){
		this.location = location;
	}
}
