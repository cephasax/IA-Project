package core;

public enum ARFF implements Database {

	ADS("ADS.arff"),

	Annealing("Annealing.arff"),

	Arrhythmia("Arrhythmia.arff"),

	Audiology("Audiology.arff"),

	Automobile("Automobile.arff"),

	Balance("Balance.arff"),

	Balance2("Balance2.arff"),

	Breast_Cancer_Wisconsin_Diagnostic("Breast Cancer Wisconsin Diagnostic.arff"),

	Breast_Cancer_Wisconsin_Original("Breast Cancer Wisconsin Original.arff"),

	Breast_Cancer_Wisconsin_Prognostic("Breast Cancer Wisconsin Prognostic.arff"),

	Breast_Tissue("Breast Tissue.arff"),

	Car("Car.arff"),

	Climate_Model_Simulation_Crashes("Climate Model Simulation Crashes.arff"),

	Congressional_Voting_Records("Congressional Voting Records.arff"),

	Connectionist_Bench_Sonar("Connectionist Bench Sonar.arff"),

	Connectionist_Bench_Vowel("Connectionist Bench Vowel.arff"),

	Credit_Approval("Credit Approval.arff"),

	Cylinder_Bands("Cylinder Bands.arff"),

	Dermatology("Dermatology.arff"),

	Ecoli("Ecoli.arff"),

	Flags("Flags.arff"),

	Gaussian("Gaussian.arff"),

	Glass_Identification("Glass Identification.arff"),

	Heart_Disease_Cleveland("Heart Disease Cleveland.arff"),

	Heart_Disease_Hungrarian("Heart Disease Hungrarian.arff"),

	Heart_Disease_Long_Beach_VA("Heart Disease Long Beach VA.arff"),

	Heart_Disease_Witzerland("Heart Disease Witzerland.arff"),

	Hepatitis("Hepatitis.arff"),

	Horse_Colic("Horse Colic.arff"),

	Ionosphere("Ionosphere.arff"),

	Iris("Iris.arff"),

	Jude("Jude.arff"),

	KRKPA7("KRKPA7.arff"),

	Labor("Labor.arff"),

	Libras_Movement("Libras Movement.arff"),

	Lung_Cancer("Lung Cancer.arff"),

	Micromass("Micromass.arff"),

	Musk("Musk.arff"),

	Parkinsons("Parkinsons.arff"),

	Pima_Indians_Diabetes("Pima_Indians_Diabetes.arff"),

	Pittsburgh_Bridges_V1("Pittsburgh Bridges V1.arff"),

	Pittsburgh_Bridges_V2("Pittsburgh Bridges V2.arff"),

	Planning_Relax("Planning Relax.arff"),

	Promoter_Gene_Sequences("Promoter Gene Sequences.arff"),

	Protein("Protein.arff"),

	Segment_Challenge("Segment Challenge.arff"),

	Sick("Sick.arff"),

	Simulated("Simulated.arff"),

	SolarFlare1("SolarFlare1.arff"),

	Soybean_Large("Soybean Large.arff"),

	Spam_Base("Spam Base.arff"),

	SPECT_Heart("SPECT Heart.arff"),

	SPECTF_Heart("SPECTF Heart.arff"),

	Statlog_Australian_Credit_Approval("Statlog Australian Credit Approval.arff"),

	Statlog_German_Credit_Data("Statlog German Credit Data.arff"),

	Statlog_Heart("Statlog Heart.arff"),

	Transfusion("Transfusion.arff"),

	Vehicle("Vehicle.arff"),

	Waveform("Waveform.arff"),

	Wine("Wine.arff"),

	Zoo("Zoo.arff");

	private String location;

	ARFF(String location) {
		this.location = "resources/" + location;
	}
	
	public String getLocation(){
		return location;
	}
}
