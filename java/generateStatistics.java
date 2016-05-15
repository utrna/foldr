import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class generateStatistics {
	
	public static ArrayList<CompoundHelix> compHeliciesList;
	
	public static void main(String[] args) {
		
		//read in file with RNA sequence and store it as a string
		String rnaSeq = readRNAFile();
		
		//read in text file with helicies and returns an array list with all the helicies
		ArrayList<PeHelicies> helicies = readHeliciesFile(rnaSeq);
		
		//generate an array list of compound helicies
		compHeliciesList= generateCompoundHelicies(helicies, rnaSeq);
		
		//should be called generate statistics, but this essentially gets all the statistics and prints to stdout
		//generateDistances(compHeliciesList);
		
		
	}
	
	//method reads in file on helicies
	public static ArrayList<PeHelicies> readHeliciesFile(String rnaSeq) {
        // The name of the file to open.  5sEcoliHelicies.txt
        String fileName = "16sEcoliHelicies.txt";
        
        String line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        
        String helixName = "";
        String beg5 = "";
        int beg5Num = 0;
        String end5 = "";
        int end5Num = 0;
        String beg3 = "";
        int beg3Num = 0;
        String end3 = "";
        int end3Num = 0;
        
        //array list containing all the helicies and their data extracted from the helicies file
        ArrayList<PeHelicies> heliciesList = new ArrayList<PeHelicies>();
        
        //actual reading of the file
        try {
            FileReader fileReader = 
                new FileReader(fileName);

            
            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                
            	stringBuilder.append(line);
            	stringBuilder.append("\n");
            	/*
            	System.out.println(line);
            	System.out.println(line.substring(6, 10));
            	System.out.println(line.substring(11, 16));
            	System.out.println(line.substring(16, 21));
            	System.out.println(line.substring(21, 27));
            	System.out.println("sub:" + line.substring(27,32));
            	*/
            	//gets the name of the helix
            	helixName = line.substring(6, 10);
            	
            	//gets beginning 5' of helix
            	beg5 = line.substring(11, 16);
            	beg5 = beg5.trim();
            	beg5Num = Integer.parseInt(beg5);
            	
            	//gets end 5' of helix
            	end5 = line.substring(16, 21);
            	end5 = end5.trim();
            	end5Num = Integer.parseInt(end5);
            	
            	//gets beginning 3' of helix
            	beg3 = line.substring(21, 27);
            	beg3 = beg3.trim();
            	beg3Num = Integer.parseInt(beg3);
            	
            	//gets end 3' of helix
            	//System.out.println(line.substring(27,32));
            	end3 = line.substring(27, 32);
            	end3 = end3.trim();
            	end3Num = Integer.parseInt(end3);
            	
            	PeHelicies helix = new PeHelicies(beg5Num, end5Num, beg3Num, end3Num, rnaSeq);
            	helix.setName(helixName);
            	heliciesList.add(helix);
            	
            	
            }  
            bufferedReader.close();         
        }
        
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
        }
       
        return heliciesList;
    }
	
	//reads in RNA sequence from text file and returns as a String
		public static String readRNAFile() {
	        // The name of the file to open. C:\\Users\\Vishal\\git\\simple-distance-folding-project\\Ecoli5Sseq
	        String fileName = "RNAseq.txt";

	        // This will reference one line at a time
	        String line = null;
	        String rnaSeq = "";
	        StringBuilder  stringBuilder = new StringBuilder();

	        try {
	            FileReader fileReader = 
	                new FileReader(fileName);

	            
	            BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);

	            while((line = bufferedReader.readLine()) != null) {
	                
	            	stringBuilder.append( line );
	            }  
	            bufferedReader.close();         
	        }
	        
	        catch(FileNotFoundException ex) {
	            System.out.println(
	                "Unable to open file '" + 
	                fileName + "'");                
	        }
	        
	        catch(IOException ex) {
	            System.out.println(
	                "Error reading file '" 
	                + fileName + "'");                  
	        }
	       
	        return stringBuilder.toString();
	    }
		
		//organizes the list all helicies into different compound helicies
		public static ArrayList<CompoundHelix> generateCompoundHelicies(ArrayList<PeHelicies> heliciesList, String rnaSeq) {
			
			
			//generates an arraylist of compound helicies
			ArrayList<CompoundHelix> compHelixList = new ArrayList<CompoundHelix>();
			/*
			for(PeHelicies helix: heliciesList) {
				System.out.println(helix.getName());
				helix.printSequence();
			}
			*/
			
			for(PeHelicies helix : heliciesList) {
				
				String name = helix.getName();
				
				//decide whether it is a P or S helix
				char helixType = name.charAt(0);
				
				//gets the number of the helix
				int helixNum = Integer.parseInt(name.replaceAll("[\\D]", ""));
				
				//the name of the compound helix the current helix should be a part of
				String nameCompHelix = "" + helixType + helixNum;
				
				//if this is the first of the compound helix, create a new compound helix, and add the helix to it
				CompoundHelix compHelix = new CompoundHelix(rnaSeq);
				if(compHelixList.size()==0) {
					compHelix.setName(nameCompHelix);
					compHelix.addHelix(helix);
					compHelixList.add(compHelix);
				}

				//otherwise checks to see if this helix should be added to an existing compound helix or a new compound helix should be created and added to the list of compound helicies
				else {
					boolean isInList = false;
					//if the compound helix that the current helix needs to be added to is already in the list, add the helix to the compoundHelix
					for(CompoundHelix compoundHelix: compHelixList) {
						if((compoundHelix.getName()).equals(nameCompHelix)) {
							compoundHelix.addHelix(helix);
							isInList = true;
						}
					}
					//if the compound helix that the current helix needs to be added to is not in the list, create a new compound helix and add it to the list
					if(!isInList) {
						compHelix.setName(nameCompHelix);
						compHelix.addHelix(helix);
						compHelixList.add(compHelix);
					}
					
				}
				
			}
			
			return compHelixList;
			
		}
		
		//generates the statistics for all the compound helicies 
		public static void generateDistances(ArrayList<CompoundHelix> compHeliciesList) {
			
			double avg5Dist = 0;
			double avg3Dist = 0;
			double avgCondDist = 0;
			
			double avgCompA5 = 0;
			double avgCompC5 = 0;
			double avgCompG5 = 0;
			double avgCompU5 = 0;
			double avgCompA3 = 0;
			double avgCompC3 = 0;
			double avgCompG3 = 0;
			double avgCompU3 = 0;
			
			//max distances between helicies 
			int maxDist5 = 0;
			int maxDist3 = 0;
			
			
			for(CompoundHelix compHelix: compHeliciesList) {
				
				compHelix.genDistances();
				System.out.println();
				
				avg5Dist += compHelix.getAvgDist5();
				avg3Dist += compHelix.getAvgDist3();
				avgCondDist += compHelix.getAvgCondDist();
				
				avgCompA5 += compHelix.getAvgComp5()[0];
				avgCompC5 += compHelix.getAvgComp5()[1];
				avgCompG5 += compHelix.getAvgComp5()[2];
				avgCompU5 += compHelix.getAvgComp5()[3];
				
				avgCompA3 += compHelix.getAvgComp3()[0];
				avgCompC3 += compHelix.getAvgComp3()[1];
				avgCompG3 += compHelix.getAvgComp3()[2];
				avgCompU3 += compHelix.getAvgComp3()[3];
				
				
				if(compHelix.getMaxDist5() > maxDist5)
					maxDist5 = compHelix.getMaxDist5();
				if(compHelix.getMaxDist3() > maxDist3)
					maxDist3 = compHelix.getMaxDist3();
				
			}
			//gets the average 5' distance and the average 3' distance and the average conditional distance
			avg5Dist = avg5Dist / compHeliciesList.size();
			avg5Dist = Math.round(avg5Dist*100);
			avg5Dist = avg5Dist/100 ;
			avg3Dist = avg3Dist / compHeliciesList.size();
			avg3Dist = Math.round(avg3Dist*100);
			avg3Dist = avg3Dist/100 ;
			avgCondDist = avgCondDist / compHeliciesList.size();
			avgCondDist = Math.round(avgCondDist*100);
			avgCondDist = avgCondDist/100 ;
			
			
			//gets the average percentage compositions of the entire sequence for the 5' and 3' end for unpair regions
			avgCompA5 = avgCompA5/ compHeliciesList.size();
			avgCompA5 = Math.round(avgCompA5*100);
			avgCompA5 = avgCompA5/100 ;
			avgCompC5 = avgCompC5/ compHeliciesList.size();
			avgCompC5 = Math.round(avgCompC5*100);
			avgCompC5 = avgCompC5/100 ;
			avgCompG5 = avgCompG5/ compHeliciesList.size();
			avgCompG5 = Math.round(avgCompG5*100);
			avgCompG5 = avgCompG5/100 ;
			avgCompU5 = avgCompU5/ compHeliciesList.size();
			avgCompU5 = Math.round(avgCompU5*100);
			avgCompU5 = avgCompU5/100 ;
			
			avgCompA3 = avgCompA3/ compHeliciesList.size();
			avgCompA3 = Math.round(avgCompA3*100);
			avgCompA3 = avgCompA3/100 ;
			avgCompC3 = avgCompC3/ compHeliciesList.size();
			avgCompC3 = Math.round(avgCompC3*100);
			avgCompC3 = avgCompC3/100 ;
			avgCompG3 = avgCompG3/ compHeliciesList.size();
			avgCompG3 = Math.round(avgCompG3*100);
			avgCompG3 = avgCompG3/100 ;
			avgCompU3 = avgCompU3/ compHeliciesList.size();
			avgCompU3 = Math.round(avgCompU3*100);
			avgCompU3 = avgCompU3/100 ;
			
			double avgCompA = (avgCompA5 + avgCompA3);
			avgCompA = Math.round(avgCompA*100);
			avgCompA = avgCompA/100 ; 
			double avgCompC = (avgCompC5 + avgCompC3);
			avgCompC = Math.round(avgCompC*100);
			avgCompC = avgCompC/100 ; 
			double avgCompG = (avgCompG5 + avgCompG3);
			avgCompG = Math.round(avgCompG*100);
			avgCompG = avgCompG/100 ; 
			double avgCompU = (avgCompU5 + avgCompU3);
			avgCompU = Math.round(avgCompU*100);
			avgCompU = avgCompU/100 ; 
			
			
			
			
			
			System.out.println("");
			System.out.println("Whole Sequence Statistics:");
			System.out.println("Max Distance between helicies: 5' end: " + maxDist5 + " | 3' end: " + maxDist3);
			System.out.println("Average:  5' distance: " + avg5Dist + " | 3' distance: " + avg3Dist + " | CD: " + avgCondDist);
			System.out.println("Average:  5' composition: A: " + avgCompA5 + "% C: " + avgCompC5 + "% G: " + avgCompG5 + "% U: " + avgCompU5 + "  ||  " + "3' composition: A: " + avgCompA3 + "% C: " + avgCompC3 + "% G: " + avgCompG3 + "% U: " + avgCompU3);
			System.out.println("Average:     composition: A: " + avgCompA + "% C: " + avgCompC + "% G: " + avgCompG + "% U: " + avgCompU + "%");

		}
		
		

	
		
		
		
		
		
		
		
		
		
		
		
		
		
}
