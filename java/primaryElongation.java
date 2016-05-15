import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



//predicts p1-p32
public class primaryElongation {
	

	public static void main(String[] args) {
		
		
		generateStatistics.main(args);
		
		System.out.println("\n \n \n");
		System.out.println("Predicited Helicies");
		//arraylist of compound helicies
		ArrayList<CompoundHelix> compHelixList = generateStatistics.compHeliciesList;
		//arraylist of the Pi and Si helicies
		ArrayList<PeHelicies> initHelicies = genInitHelicies(compHelixList);
		
		
		/*
		//gets an arraylist of correct P compound helicies
		ArrayList<CompoundHelix> correctPCompHelicies = new ArrayList<CompoundHelix>();
		for(CompoundHelix compHelix: compHelixList) {
			if(!compHelix.getName().contains("S"))
				correctPCompHelicies.add(compHelix);
		}
		
		//arraylist containing only inital Pi helicies with no Si helicies
		ArrayList<PeHelicies> initPiHelicies = new ArrayList<PeHelicies>();
		*/
		//RNA sequence from file on one line
		String rnaSeq = readRNAFile();
		
		//array of the nucelotides in the sequence, if unpair nucleotide is 0, otherwise gives the nucleotide it is paired with
		int [] pairedBases = new int [rnaSeq.length() + 1];
		for(int i: pairedBases) {
			pairedBases[i] = 0;
		}
		

		
		//elongate each of the known Pi and Si helices for E.coli 16s rRNA and obtain an arraylist of the predicted compound helices
		ArrayList<CompoundHelix> predictedCompHeliciesList = elongateKnownPiSiHelices(initHelicies, rnaSeq, pairedBases);
		//check the correctness of the predicted helices
		checkPredictedHelicies checker = new checkPredictedHelicies(predictedCompHeliciesList, compHelixList, rnaSeq);
		checker.printCorrectness();
		//prints the percentage of bases correct
		System.out.println("Percentage of Nucleotides Correct: " + checker.generateNucleotideCorrectness());
		System.out.println();
		//print the predicted compound helicies
		printPredictedHelicies(predictedCompHeliciesList);
		
		
		/*
		//read in potential PI helices file and store in ArrayList
		ArrayList<PeHelicies> predPIHelicesList = readPiFile(rnaSeq);
		//elongate each of the potential PI helicies
		ArrayList<CompoundHelix> predCompPIHelices = elongatePotentialPIs(predPIHelicesList, rnaSeq, pairedBases);
		//printPredictedHelicesPI(predCompPIHelices);
		generateCsv.main(args, predCompPIHelices);
		*/

		/*
		for(PeHelicies helix: initHelicies) {
			if(!(helix.getName().contains("S"))) {
				initPiHelicies.add(helix);
			}
		}
		*/
		//enter all Pi and Si helices into pairedBases
		//for(PeHelicies helix: initHelicies) {
			//updatePairedBases(helix, pairedBases);
		//}


		
		
	}
	
	//reads in RNA sequence from text file and returns as a String
	public static String readRNAFile() {
        // The name of the file to open.  C:\\Users\\Vishal\\git\\simple-distance-folding-project\\Ecoli5Sseq
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
	
	//reads in potential pi helices file and returns an ArrayList of the potential PI helices
	public static ArrayList<PeHelicies> readPiFile(String rnaSeq) {
        String fileName = "C:/Users/Vishal/Desktop/16s Pi predictions/ 16sPotentialPiHelices.pi";
        
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
                //System.out.println(line);
            	stringBuilder.append(line);
            	stringBuilder.append("\n");
            	
            	line.trim();
            	String[] values = line.split("   ");
            	beg5Num = Integer.parseInt(values[0]);
            	end5Num = Integer.parseInt(values[1]);
            	beg3Num = Integer.parseInt(values[2]);
            	end3Num = Integer.parseInt(values[3]);
            	
            	
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


	
	//given the position of 2 nucleotides, returns true if the two nucleotides can base pair and they haven't already paired to any other nucleotides, else it returns false
	public static boolean isBasePair(int pos1, int pos2, String seq, int[] pairedBases) {
		char a = seq.charAt(pos1);
		char b = seq.charAt(pos2);
		/*
		//if pos1 or pos 2 is already paired, return false
		if(!(pairedBases[pos1+1]==0 && pairedBases[pos2+1]==0))
			return false;
			*/
		// A:U
		if(a=='A' & b=='U')
			return true;
		// U:A
		else if(a=='U' & b=='A')
			return true;
		// G:C
		else if(a=='G' & b=='C')
			return true;
		// C:G
		else if(a=='C' & b=='G')
			return true;
		// U:G
		else if(a=='U' & b=='G')
			return true;
		// G:U
		else if(a=='G' & b=='U')
			return true;
		//if the nucleotides do not pair, returns false
		else
			return false;
	}
	
	//predicts elongation of all potential PI helices
	public static ArrayList<CompoundHelix> elongatePotentialPIs(ArrayList<PeHelicies> potentialPIs, String rnaSeq, int[] pairedBases) {
		
		//arraylist of the predicted compound helicies
		ArrayList<CompoundHelix> predictedCompHeliciesList = new ArrayList<CompoundHelix>();
		
		//runs through the initial helicies and generates predicted compound helix and adds to the list above
		int counter = 0;
		int helicesCounter = 1;
		for(PeHelicies initialHelix: potentialPIs) {
			
			CompoundHelix predictedCompHelix = new CompoundHelix(rnaSeq);
			//set the ME of the initial helix to be the energy of the helix/ hairpin loop size and the name
			PeHelicies currentHelix = initialHelix;
			currentHelix.setHelixME(currentHelix.getHelixEnergy()/currentHelix.getSimpleDist());
			currentHelix.setName("P" + helicesCounter + "a");
			predictedCompHelix.addHelix(currentHelix);
			PeHelicies competingHelix;

			
			//set the name of the compound helix
			predictedCompHelix.setName("P" + helicesCounter);

			double minME = 0.0;
			//keep elongation compound helix until the ratio of paired bases to total helix length falls below 0.6
			double ratio = 0.6;
			
			boolean loop = true;
						
			//total compound helix window size
			int totalWindow = 30;
			//continue building compound helix while total compound helix size is within the totalWindow size
			helixLoop:
			//while(loop & (predictedCompHelix.getTotalHelixLength()<= totalWindow)) {
			
			//elongate compound helix until the ratio of paired bases to total helix length falls below 0.6
			while(loop && ((double)(predictedCompHelix.getTotalNumPairedNucleotides())/(double)(predictedCompHelix.getTotalHelixLength()) > ratio)) {
				Result res = findNextHelix(rnaSeq,currentHelix, pairedBases);
				//currentHelix contains the helix based on shortest CD
				currentHelix = res.getHelix();
				//competition, determines if there is a more stable helix based on the previous helix just predicted above
				Result resCompetingHelix = findsCompetingHelix( currentHelix,rnaSeq, pairedBases, predictedCompHelix);
				competingHelix = resCompetingHelix.getHelix();
				
				
				//calculates the ME for the current helix , ME = OE/ CD
				double currentHelixME = currentHelix.getHelixME();
				//calculates the ME for the competing helix
				double competingHelixME = competingHelix.getHelixME();

				if(!(currentHelix.isEqual(predictedCompHelix.getHelix(predictedCompHelix.getSize()-1)))) {
					//if the competing helix has a more stable ME, it becomes the current helix, chooses the helix with the larger ME, ie. less negative
					if(competingHelixME < currentHelixME) {
						currentHelix = competingHelix;
						currentHelixME = competingHelixME;
						//if it is more stable, keep searching for a more stable helix
						resCompetingHelix = findsCompetingHelix( currentHelix, rnaSeq, pairedBases,predictedCompHelix);
						competingHelixME = resCompetingHelix.getHelix().getHelixME();
						
						while(competingHelixME < currentHelixME) {
						
							currentHelix = resCompetingHelix.getHelix();
							currentHelixME = competingHelixME;
							resCompetingHelix = findsCompetingHelix (currentHelix, rnaSeq, pairedBases, predictedCompHelix);
							competingHelixME = resCompetingHelix.getHelix().getHelixME();
							
						}
					}	
					
					
					//if the predicted helix is more stable than any previous competing helicies or there are no competing helicies, add to the predicted helicies list
					//isMoreStable(currentHelix, predictedCompHeliciesList, rnaSeq, potentialPIs) &&
					//predicted compound helix must be within 30 nucleotides in length
					//(predictedCompHelix.getTotalHelixLength()<= totalWindow)
					if( (currentHelix.getHelixME() < 0.0) && (currentHelix.getCD()<=18) && calcPredRatio(predictedCompHelix, currentHelix, ratio)) {
						
						counter++;
						currentHelix.setName(currentHelix.generateName(predictedCompHelix.getName(), counter));
						predictedCompHelix.addHelix(currentHelix);
					}
					//if the predicted helix is not more stable than a previously predicted helix, than do not add the helix to the list, and end elongation of compound helix
					else
						break helixLoop;
				}
			loop = res.getBoolean();

			}
			counter = 0;
			predictedCompHeliciesList.add(predictedCompHelix);
			helicesCounter++;
		}
		//printPredictedHelicies(predictedCompHeliciesList);
		return predictedCompHeliciesList;
	}
	

	
	//elongates PI and SI helices from the known E.coli 16s helicies file and returns an arraylist of the predicted compound helicies
	public static ArrayList<CompoundHelix> elongateKnownPiSiHelices(ArrayList<PeHelicies> initHelicies, String rnaSeq, int[] pairedBases) {
		//arraylist of the predicted compound helicies
				ArrayList<CompoundHelix> predictedCompHeliciesList = new ArrayList<CompoundHelix>();
				
				//runs through the initial helicies and generates predicted compound helix and adds to the list above
				int counter = 0;
				for(PeHelicies initialHelix: initHelicies) {
					
					CompoundHelix predictedCompHelix = new CompoundHelix(rnaSeq);
					PeHelicies currentHelix = initialHelix;
					PeHelicies competingHelix;
					predictedCompHelix.addHelix(currentHelix);
					
					//set the name of the compound helix
					String name = currentHelix.getName();
					//decide whether it is a P or S helix
					char helixType = name.charAt(0);
					//gets the number of the helix
					int helixNum = Integer.parseInt(name.replaceAll("[\\D]", ""));	
					//the name of the compound helix the current helix should be a part of
					predictedCompHelix.setName("" + helixType + helixNum);
					//ratio of paired nucleotides to total length of compound helix
					double ratio = 0.8;
					
					double minME = 0.0;
					
					boolean loop = true;
					helixLoop:
					while(loop) {
						PeHelicies initialHelix2 = currentHelix;

						Result res = findNextHelix(rnaSeq,currentHelix, pairedBases);

						//currentHelix contains the helix based on shortest CD
						currentHelix = res.getHelix();
						//competition, determines if there is a more stable helix based on the previous helix just predicted above
						Result resCompetingHelix = findsCompetingHelix( currentHelix,rnaSeq, pairedBases, predictedCompHelix);
						competingHelix = resCompetingHelix.getHelix();
						
						
						//calculates the ME for the current helix , ME = OE/ CD
						double currentHelixME = currentHelix.getHelixME();
						//calcualtes the ME for the competing helix
						double competingHelixME = competingHelix.getHelixME();
						/*
						System.out.println(predictedCompHelix.getName());
						System.out.println("Competing helix: " + competingHelix.getName()+ " : " + competingHelix.getCD());
						competingHelix.printSequence();
						System.out.println("current helix: " + currentHelix.getName()+ " : " + currentHelix.getCD() );
						currentHelix.printSequence();
						System.out.println();
						*/

						
						if(!(currentHelix.isEqual(predictedCompHelix.getHelix(predictedCompHelix.getSize()-1)))) {
							//if the competing helix has a more stable ME, it becomes the current helix, chooses the helix with the larger ME, ie. less negative
							if(competingHelixME < currentHelixME) {
								currentHelix = competingHelix;
								currentHelixME = competingHelixME;
								//if it is more stable, keep searching for a more stable helix
								resCompetingHelix = findsCompetingHelix( currentHelix, rnaSeq, pairedBases,predictedCompHelix);
								competingHelixME = resCompetingHelix.getHelix().getHelixME();
								
								while(competingHelixME < currentHelixME) {
								
									currentHelix = resCompetingHelix.getHelix();
									currentHelixME = competingHelixME;
									resCompetingHelix = findsCompetingHelix (currentHelix, rnaSeq, pairedBases, predictedCompHelix);
									competingHelixME = resCompetingHelix.getHelix().getHelixME();
									//System.out.println("Competing helix: " + competingHelix.getName()+ " : " + competingHelix.getCD());
									//competingHelix.printSequence();
								}
							}	
							
							
							//if the predicted helix is more stable than any previous competing helicies or there are no competing helicies, add to the predicted helicies list
							if(isMoreStable(currentHelix, predictedCompHeliciesList, rnaSeq, initHelicies) && currentHelix.getHelixME() < 0.0 && currentHelix.getCD()<=18) {
								counter++;
								currentHelix.setName(currentHelix.generateName(predictedCompHelix.getName(), counter));
								predictedCompHelix.addHelix(currentHelix);
								//updatePairedBases(currentHelix, pairedBases);
								/*
								System.out.println("Helix added: " + currentHelix.getName());
								currentHelix.printSequence();
								*/
							}
							//if the predicted helix is not more stable than a previously predicted helix, than do not add the helix to the list, and end elongation of compound helix
							else
								break helixLoop;
							/*
							System.out.println(currentHelix.getName());
							currentHelix.printSequence();
							System.out.println();
							*/
							
							/*
							if(test)
								System.out.println(currentHelix.getName() + ": competing " + resCompetingHelix.getCondDist());
							else
								System.out.println(currentHelix.getName() + ": " + res.getCondDist());
								*/
							/*
							System.out.println("current helix: " + currentHelix.getName() + ": " + res.getCondDist());
							currentHelix.printSequence();
							 */
						}
						//res = findNextHelix(rnaSeq, currentHelix);
					loop = res.getBoolean();

					}
					counter = 0;
					predictedCompHeliciesList.add(predictedCompHelix);
			}

				
				//hardcoding p3b in because it has a non canonical bp and removes predicted p3c
				PeHelicies p3b = new PeHelicies(146 , 147   ,175 , 176, rnaSeq);
				//updatePairedBases(p3b, pairedBases);
				p3b.setName("P3b");
				predictedCompHeliciesList.get(2).removeHelix(2);
				predictedCompHeliciesList.get(2).removeHelix(1);
				predictedCompHeliciesList.get(2).addHelix(p3b);
				
				//remove unstable helices, for now remove helicies that have a CD>20
				removeUnstableHelicies(predictedCompHeliciesList);
				//printPredictedHelicies(predictedCompHeliciesList);
				return(predictedCompHeliciesList);
				
				
				//check correctness of predictions
				//checkPredictedHelicies checker = new checkPredictedHelicies(predictedCompHeliciesList, compHelixList, rnaSeq);
				//checker.printCorrectness();
				//prints the percentage of bases correct
				//System.out.println("Percentage of Nucleotides Correct: " + checker.generateNucleotideCorrectness());
				//System.out.println();
				
				//print the predicted compound helicies
				
				
				
	}
	
	
	
	//finds the next helix given a previous helix, returns true if a helix is found, return false otherwise
	public static Result findNextHelix(String seq, PeHelicies p2a, int[] pairedBases) {
		
		//5' end start number
		int start5 = p2a.getPre5() - 2;
		//3' end start number
		int start3 = p2a.getEnd3();
		
		//counter for 5' end
		int counter5 = 0;
		//counter for 3' end
		int counter3 = 0;
		
		//current nucleotide number 5' end
		int cur5 = start5;
		//current nucleotide number 3' end
		int cur3 = start3;
		
		//max distance a 5' nucleotide can pair with another 3' nucleotide 
		int window = 4;
		
		int balance = 6;
		
		//5' start for the new helix
		int newHelixBeg_5 = 0;
		//5' end for the new helix
		int newHelixEnd_5 = 0;
		//3' start for the new helix
		int newHelixBeg_3 = 0;
		//3' end for the new helix
		int newHelixEnd_3 = 0;
		
		//current helix length (number of base pairs)
		int helixLen = 0;
		//minimum helix length + 1, if you want min helix length to be 2, enter 1, below
		int minHelixLen = 1;
		
		boolean isPair = false;
		
		PeHelicies potentialNextHelix3;
		
		while(!(helixLen > 0 & isPair == false)) {
			
			//if at the edges of the sequence, and the method runs out of bounds,break the loop
			if( cur5<0 || cur3<0 ) {
				potentialNextHelix3 = p2a;
				break;
			}
			if((cur3>=(seq.length()-1) )|| (cur5>=(seq.length()-1))) {
				potentialNextHelix3 = p2a;
				break;
			}
			
			isPair = isBasePair(cur5, cur3, seq, pairedBases);
			
			//if it is not a base pair, isPair = false, and it is the beginning of a new helix,
			//move down 1 nucleotide on 3' end, 5' end nucleotide remains the same,
			if(isPair==false & helixLen==0) {
				counter3++;
				//if it cannot find a base pair within the window, move backwards one nucleotide on the 5' end,
				//and start search from the beginning of the 3' end
				if(counter3 >= window) {
					cur5--;
					cur3 = start3;
					counter3 = 0;
					counter5++;
				}
				else
					cur3++;	
			}
			
			//if it a base pair, isPair==True, move backwards down 5' end by 1 nucleotide, move down 1 nucleotide on the 3' end 
			else if(isPair==true){
				//if it is the start of a new helix
				if(helixLen==0){
					newHelixEnd_5 = cur5;
					newHelixBeg_3 = cur3;					
				}
				cur5--;
				cur3++;
				helixLen++;	
			}
			
			//if helixLen > 0, and the next nucleotides do not pair, this is the end of the helix
			else{
				//if the helix is too short in length, start over
				if(helixLen <= minHelixLen & minHelixLen > 0) {
					helixLen = 0;
					cur5 = newHelixEnd_5 -1;
					cur3 = start3;
					counter3 = 0;
					isPair = false;
				}
				else {
					newHelixBeg_5 = cur5 + 1;
					newHelixEnd_3 = cur3 - 1;
					break;
				}
			}
			
		}
		
		//right here I have a potential helix from a search initiating on the 3' end
		//store information on potential helix
		potentialNextHelix3 = new PeHelicies(newHelixBeg_5+1,newHelixEnd_5+1,newHelixBeg_3+1,newHelixEnd_3+1,seq);

		
		//5' end search
		
		//5' end start number
		start5 = p2a.getPre5() - 2;
		//3' end start number
		start3 = p2a.getEnd3();
		
		//counter for 5' end
		counter5 = 0;
		//counter for 3' end
		counter3 = 0;
		
		//current nucleotide number 5' end
		cur5 = start5;
		//current nucleotide number 3' end
		cur3 = start3;
		
		//max distance a 5' nucleotide can pair with another 3' nucleotide 
		window = 4;
		
		
		//5' start for the new helix
		newHelixBeg_5 = 0;
		//5' end for the new helix
		newHelixEnd_5 = 0;
		//3' start for the new helix
		newHelixBeg_3 = 0;
		//3' end for the new helix
		newHelixEnd_3 = 0;
		
		//current helix length (number of base pairs)
		helixLen = 0;
		
		isPair = false;
		
		PeHelicies potentialNextHelix5;
		
		while(!(helixLen > 0 & isPair == false)) {
			
			
			//if at the edges of the sequence, and the method runs out of bounds,break the loop
			if( cur5<0 || cur3<0 ) {
				potentialNextHelix5 = p2a;
				break;
			}
			if((cur3>=(seq.length()-1) )|| (cur5>=(seq.length()-1))) {
				potentialNextHelix3 = p2a;
				break;
			}
			
			isPair = isBasePair(cur5, cur3, seq, pairedBases);
			//System.out.println((cur5) + ": "+ seq.charAt(cur5) + " " + (cur3)  + ": " + seq.charAt(cur3) + " " + isPair);
			
			//if it is not a base pair, isPair = false, and it is the beginning of a new helix,
			//move down 1 nucleotide on 3' end, 5' end nucleotide remains the same,
			if(isPair==false & helixLen==0) {
				counter5++;
				//System.out.println(cur5 + " " + cur3);
				//System.out.println(counter5);

				//if it cannot find a base pair within the window, move backwards one nucleotide on the 5' end,
				//and start search from the beginning of the 3' end
				if(counter5 >= window) {
					cur3++;
					cur5 = start5;
					counter5 = 0;
				}
				else {
					cur5--;	
				}
			}
			
			//if it a base pair, isPair==True, move backwards down 5' end by 1 nucleotide, move down 1 nucleotide on the 3' end 
			else if(isBasePair(cur5, cur3, seq, pairedBases)){
				//if it is the start of a new helix
				if(helixLen==0){
					newHelixEnd_5 = cur5;
					newHelixBeg_3 = cur3;					
				}
				//System.out.println(seq.charAt(cur5) + " " + seq.charAt(cur3));
				cur5--;
				cur3++;
				helixLen++;	

			}
			
			//if helixLen > 0, and the next nucleotides do not pair, this is the end of the helix
			else{
				//if the helix is too short in length, start over
				if(helixLen <= minHelixLen & helixLen > 0) {
					helixLen = 0;
					cur5 = start5;
					cur3 = newHelixBeg_3 + 1;
					counter5 = 0;
					isPair = false;
				}
				else {
					newHelixBeg_5 = cur5 + 1;
					newHelixEnd_3 = cur3 -1;
					break;
				}
			}
			
		}

		potentialNextHelix5 = new PeHelicies(newHelixBeg_5 +1,newHelixEnd_5+1,newHelixBeg_3+1,newHelixEnd_3+1,seq);
		
		
		//conditional distance
		int cdNextHelix3 = getCondDist(p2a, potentialNextHelix3);
		int cdNextHelix5 = getCondDist(p2a, potentialNextHelix5);

		
		//finds the helix with the shorter simple distance
		//3' search helix simple distance
		int simpleDist_3 = potentialNextHelix3.getSimpleDist();
		//5' search helix simple distance
		int simpleDist_5 = potentialNextHelix5.getSimpleDist();
		//distance between predicted helix and the previous helix on the 5' end and the 3'end for both of the predicted helicies
		int potentialNextHelix3dist5 = p2a.getPre5() - potentialNextHelix3.getEnd5();
		int potentialNextHelix3dist3 = potentialNextHelix3.getPre3() - p2a.getEnd3();
		int potentialNextHelix5dist5 = p2a.getPre5() - potentialNextHelix5.getEnd5();
		int potentialNextHelix5dist3 = potentialNextHelix5.getPre3() - p2a.getEnd3();
		//the difference between the distances on the 5' and the 3' end
		int difPotentialNextHelix3 = Math.abs(potentialNextHelix3dist5 - potentialNextHelix3dist3);
		int difPotentialNextHelix5 = Math.abs(potentialNextHelix5dist5 - potentialNextHelix5dist3);
		
		//minimum ME for a helix to form
		double minME = 1;
		
		
		potentialNextHelix3.setHelixME(potentialNextHelix3.getHelixEnergy()/cdNextHelix3);
		potentialNextHelix5.setHelixME(potentialNextHelix5.getHelixEnergy()/cdNextHelix5);
		
		potentialNextHelix3.setCD(cdNextHelix3);
		potentialNextHelix5.setCD(cdNextHelix5);
		/*
		System.out.println("Pt competing helix 3:" );
		potentialNextHelix3.printSequence();
		System.out.println("Pt competing helix 5:");
		potentialNextHelix5.printSequence();
		*/
		//if the predicted helix is too far from the previous helix
		if(Math.min(cdNextHelix3, cdNextHelix5) >= 18) {
			Result res = new Result(false, p2a,cdNextHelix3);
			return res;
		}
		//if there are no other helicies because at the edge of the sequence
		else if (potentialNextHelix3.isEqual(p2a)|| potentialNextHelix5.isEqual(p2a)){
			Result res = new Result(false, p2a,cdNextHelix3);
			return res;
		}
		// ( && (difPotentialNextHelix3 <= difPotentialNextHelix5) simpleDist_3 < simpleDist_5
		else if((cdNextHelix3 < cdNextHelix5 ) && ( (difPotentialNextHelix3 < balance) ) && potentialNextHelix3.getHelixME()<= minME ) {
			potentialNextHelix3.setHelixME(potentialNextHelix3.getHelixEnergy()/cdNextHelix3);
			Result res = new Result(true, potentialNextHelix3,cdNextHelix3);
			//System.out.println(p2a.getName() + " dif:" + difPotentialNextHelix3);
			return res;
		}
		// && (difPotentialNextHelix3 >= difPotentialNextHelix5) simpleDist_5 < simpleDist_3 
		else if((cdNextHelix5 < cdNextHelix3) && ((difPotentialNextHelix5 < balance) )  && potentialNextHelix5.getHelixME()<= minME ) {
			potentialNextHelix5.setHelixME(potentialNextHelix5.getHelixEnergy()/cdNextHelix5);
			Result res = new Result(true, potentialNextHelix5,cdNextHelix5);
			//System.out.println(p2a.getName() + " dif:" + difPotentialNextHelix5);
			return res;
		}
		else if(( cdNextHelix3 == cdNextHelix5 ) && ( (difPotentialNextHelix3 < balance) )) {
			if((difPotentialNextHelix3 < difPotentialNextHelix5)  && potentialNextHelix3.getHelixME()<= minME) {
				potentialNextHelix3.setHelixME(potentialNextHelix3.getHelixEnergy()/cdNextHelix3);
				return new Result(true, potentialNextHelix3,cdNextHelix3);
			}
			else if ( potentialNextHelix5.getHelixME()<= minME) {
				potentialNextHelix5.setHelixME(potentialNextHelix5.getHelixEnergy()/cdNextHelix5);
				return new Result(true, potentialNextHelix5,cdNextHelix5);
			}
			//if the dif between the helicies are the same and the cd of the helicies are the same, choose the helicies with a lower ME
			else {
				if(potentialNextHelix3.getHelixME() < potentialNextHelix5.getHelixME())
					return new Result(true, potentialNextHelix3,cdNextHelix3);
				else
					return new Result(true, potentialNextHelix5,cdNextHelix5);

			}
				
		}
		//does not satisfy other requirements, no other helicies predicted
		else {
			Result res = new Result(false, p2a,cdNextHelix3);
			return res;
		}
		
	}

	
	//calculates the conditional distance between 2 helicies
	public static int getCondDist(PeHelicies helixA, PeHelicies helixB) {
		int dist5 = 0;
		int dist3 = 0;	
		dist5 = Math.abs(helixA.getPre5()-helixB.getEnd5());
		dist3 = Math.abs(helixB.getPre3()-helixA.getEnd3());
		int cd = dist5+dist3;
		return cd;
	}
	
	//generates all the primary initiation helicies
	public static ArrayList<PeHelicies> genInitHelicies(ArrayList<CompoundHelix> compHelixList) {
		
		ArrayList<PeHelicies> initHelicies = new ArrayList<PeHelicies>();
		for(CompoundHelix compHelix: compHelixList) {
			initHelicies.add(compHelix.getHelix(0));
		}
		return initHelicies;
		
	}
	
	//prints all the predicted compound helicies
	public static void printPredictedHelicies(ArrayList<CompoundHelix> predictedCompHelicies) {
		for(CompoundHelix compHelix: predictedCompHelicies) {
			System.out.println(compHelix.getName());
			compHelix.printCompHelix();
			System.out.println();
		}
	}
	
	//prints all the predicted compound helices from the potential pi helices list
	public static void printPredictedHelicesPI(ArrayList<CompoundHelix> predCompHelices) {
		for(CompoundHelix compHelix: predCompHelices) {
			System.out.println(compHelix.getName());
			System.out.println("Compound Helix ME: " + compHelix.getCompHelixEnergy());
			compHelix.printCompHelix();
			System.out.println();
			
		}
	}
	
	
	//finds if another more stable helix can form once a helix has formed based on shorted conditional distance
	//searches for a more stable helix starting from the first base pair of the predicted helix
	public static Result findsCompetingHelix(PeHelicies p2a, String seq, int[] pairedBases, CompoundHelix predictedCompHelix) {
		
		//5' end start number
		//starts at the first base pair of the predicted helix based on CD
				/*
				int start5 = p2a.getEnd5()-1;
				//3' end start number
				int start3 = p2a.getPre3();
				*/
				int start5 = p2a.getEnd5() - 1;
				//3' end start number
				int start3 = p2a.getPre3();
				
				//counter for 5' end
				int counter5 = 0;
				//counter for 3' end
				int counter3 = 0;
				
				//current nucleotide number 5' end
				int cur5 = start5;
				//current nucleotide number 3' end
				int cur3 = start3;
				
				//max distance a 5' nucleotide can pair with another 3' nucleotide 
				int window = 4;
				
				int balance = 6;
				
				//5' start for the new helix
				int newHelixBeg_5 = 0;
				//5' end for the new helix
				int newHelixEnd_5 = 0;
				//3' start for the new helix
				int newHelixBeg_3 = 0;
				//3' end for the new helix
				int newHelixEnd_3 = 0;
				
				//current helix length (number of base pairs)
				int helixLen = 0;
				//minimum helix length + 1, if you want min helix length to be 2, enter 1, below
				int minHelixLen = 1;
				
				boolean isPair = false;
				
				PeHelicies potentialNextHelix3;
				
				while(!(helixLen > 0 & isPair == false) && counter5<=window) {
					
					//if at the edges of the sequence, and the method runs out of bounds,break the loop
					if( cur5<0 || cur3<0 ) {
						potentialNextHelix3 = p2a;
						break;
					}
					if((cur3>=(seq.length()-1) )|| (cur5>=(seq.length()-1))) {
						potentialNextHelix3 = p2a;
						break;
					}
					
					isPair = isBasePair(cur5, cur3, seq, pairedBases);
					
					//if it is not a base pair, isPair = false, and it is the beginning of a new helix,
					//move down 1 nucleotide on 3' end, 5' end nucleotide remains the same,
					if(isPair==false & helixLen==0) {
						counter3++;
						//if it cannot find a base pair within the window, move backwards one nucleotide on the 5' end,
						//and start search from the beginning of the 3' end
						if(counter3 >= window) {
							cur5--;
							cur3 = start3;
							counter3 = 0;
							counter5++;
						}
						else
							cur3++;	
					}
					
					//if it a base pair, isPair==True, move backwards down 5' end by 1 nucleotide, move down 1 nucleotide on the 3' end 
					else if(isPair==true){
						//if it is the start of a new helix
						if(helixLen==0){
							newHelixEnd_5 = cur5;
							newHelixBeg_3 = cur3;					
						}
						cur5--;
						cur3++;
						helixLen++;	
					}
					
					//if helixLen > 0, and the next nucleotides do not pair, this is the end of the helix
					else{
						//if the helix is too short in length, start over
						if(helixLen <= minHelixLen && minHelixLen > 0) {
							helixLen = 0;
							/*
							cur5 = newHelixEnd_5 -1;
							cur3 = start3;
							counter3 = 0;
							*/
							if(counter3 >= window) {
								cur5 = newHelixEnd_5-1;
								cur3 = start3;
								counter3 = 0;
								isPair = false;
							}
							else {
								cur5 = newHelixEnd_5;
								cur3 = newHelixBeg_3 + 1;
								counter3= Math.abs(cur3-start3);
								isPair = false;
							}
						}
						else {
							newHelixBeg_5 = cur5 + 1;
							newHelixEnd_3 = cur3 - 1;
							break;
						}
					}
					
				}
				
				//right here I have a potential helix from a search initiating on the 3' end
				//store information on potential helix
				potentialNextHelix3 = new PeHelicies(newHelixBeg_5+1,newHelixEnd_5+1,newHelixBeg_3+1,newHelixEnd_3+1,seq);

				//5' search
				/*
				start5 = p2a.getEnd5()-2;
				start3 = p2a.getPre3()-1;
				*/
				//5' end start number
				start5 = p2a.getEnd5() - 2;
				//3' end start number
				start3 = p2a.getPre3()-1;
				//counter for 5' end
				counter5 = 0;
				//counter for 3' end
				counter3 = 0;
				
				//current nucleotide number 5' end
				cur5 = start5;
				//current nucleotide number 3' end
				cur3 = start3;
				
				
				
				//5' start for the new helix
				newHelixBeg_5 = 0;
				//5' end for the new helix
				newHelixEnd_5 = 0;
				//3' start for the new helix
				newHelixBeg_3 = 0;
				//3' end for the new helix
				newHelixEnd_3 = 0;
				
				//current helix length (number of base pairs)
				helixLen = 0;
				
				isPair = false;
				
				PeHelicies potentialNextHelix5;
				
				while(!(helixLen > 0 && isPair == false) && counter3 <= window) {
					
					
					//if at the edges of the sequence, and the method runs out of bounds,break the loop
					if( cur5<0 || cur3<0 ) {
						potentialNextHelix5 = p2a;
						break;
					}
					if((cur3>=(seq.length()-1) )|| (cur5>=(seq.length()-1))) {
						potentialNextHelix3 = p2a;
						break;
					}
					
					isPair = isBasePair(cur5, cur3, seq, pairedBases);
					//System.out.println((cur5) + ": "+ seq.charAt(cur5) + " " + (cur3)  + ": " + seq.charAt(cur3) + " " + isPair);
					
					//if it is not a base pair, isPair = false, and it is the beginning of a new helix,
					//move down 1 nucleotide on 3' end, 5' end nucleotide remains the same,
					if(isPair==false && helixLen==0) {
						counter5++;
						//System.out.println(cur5 + " " + cur3);
						//System.out.println(counter5);

						//if it cannot find a base pair within the window, move backwards one nucleotide on the 5' end,
						//and start search from the beginning of the 3' end
						if(counter5 >= window) {
							cur3++;
							counter3++;
							cur5 = start5;
							counter5 = 0;
						}
						else {
							cur5--;	
						}
					}
					
					//if it a base pair, isPair==True, move backwards down 5' end by 1 nucleotide, move down 1 nucleotide on the 3' end 
					else if(isBasePair(cur5, cur3, seq, pairedBases)){
						//if it is the start of a new helix
						if(helixLen==0){
							newHelixEnd_5 = cur5;
							newHelixBeg_3 = cur3;					
						}
						//System.out.println(seq.charAt(cur5) + " " + seq.charAt(cur3));
						cur5--;
						cur3++;
						helixLen++;	

					}
					
					//if helixLen > 0, and the next nucleotides do not pair, this is the end of the helix
					else{
						//if the helix is too short in length, start over
						if(helixLen <= minHelixLen && helixLen > 0) {
							helixLen = 0;

							if(counter5 >= window) {
								cur3 = newHelixBeg_3+1;
								cur5 = start5;
								counter5 = 0;
								isPair = false;
							}
							else {
								cur3 = newHelixBeg_3;
								cur5 = newHelixEnd_5 - 1;
								counter5 = Math.abs(start5-cur5);
								isPair = false;
							}
						}
						else {
							newHelixBeg_5 = cur5 + 1;
							newHelixEnd_3 = cur3 -1;
							break;
						}
					}
					
				}

				potentialNextHelix5 = new PeHelicies(newHelixBeg_5 +1,newHelixEnd_5+1,newHelixBeg_3+1,newHelixEnd_3+1,seq);
				
				
				//conditional distance
				int cdNextHelix3 = getCondDist(predictedCompHelix.getHelix(predictedCompHelix.getSize()-1), potentialNextHelix3);
				int cdNextHelix5 = getCondDist(predictedCompHelix.getHelix(predictedCompHelix.getSize()-1), potentialNextHelix5);
				
				//finds the helix with the shorter simple distance
				//3' search helix simple distance
				int simpleDist_3 = potentialNextHelix3.getSimpleDist();
				//5' search helix simple distance
				int simpleDist_5 = potentialNextHelix5.getSimpleDist();
				//distance between predicted helix and the previous helix on the 5' end and the 3'end for both of the predicted helicies
				int potentialNextHelix3dist5 = p2a.getPre5() - potentialNextHelix3.getEnd5();
				int potentialNextHelix3dist3 = potentialNextHelix3.getPre3() - p2a.getEnd3();
				int potentialNextHelix5dist5 = p2a.getPre5() - potentialNextHelix5.getEnd5();
				int potentialNextHelix5dist3 = potentialNextHelix5.getPre3() - p2a.getEnd3();
				//the difference between the distances on the 5' and the 3' end
				int difPotentialNextHelix3 = Math.abs(potentialNextHelix3dist5 - potentialNextHelix3dist3);
				int difPotentialNextHelix5 = Math.abs(potentialNextHelix5dist5 - potentialNextHelix5dist3);
				
				//ME of the potential helicies
				double potentialNextHelix3ME = potentialNextHelix3.getHelixEnergy()/ cdNextHelix3;
				double potentialNextHelix5ME = potentialNextHelix5.getHelixEnergy()/ cdNextHelix5;
				
				//minimum ME required to form a helix
				double minME = 1;

				potentialNextHelix3.setHelixME(potentialNextHelix3.getHelixEnergy()/cdNextHelix3);
				potentialNextHelix5.setHelixME(potentialNextHelix5.getHelixEnergy()/cdNextHelix5);
				
				potentialNextHelix3.setCD(cdNextHelix3);
				potentialNextHelix5.setCD(cdNextHelix5 );
				/*
				System.out.println("Pt competing helix 3:" );
				potentialNextHelix3.printSequence();
				System.out.println("Pt competing helix 5:");
				potentialNextHelix5.printSequence();	
				*/
				
				//if the predicted helix is too far from the previous helix
				if(Math.min(cdNextHelix3, cdNextHelix5) >= 18) {
					Result res = new Result(false, p2a, cdNextHelix3);
					return res;
				}
				//if there are no other helicies because at the edge of the sequence
				else if (potentialNextHelix3.isEqual(p2a) && potentialNextHelix5.isEqual(p2a)){
					Result res = new Result(false, p2a, cdNextHelix5);
					return res;
				}
				// ( && (difPotentialNextHelix3 <= difPotentialNextHelix5) simpleDist_3 < simpleDist_5
				else if((potentialNextHelix3ME < potentialNextHelix5ME ) && potentialNextHelix3.getHelixME()<= minME) {
					//System.out.println(p2a.getName() + " dif:" + difPotentialNextHelix3);
					Result res = new Result(true, potentialNextHelix3, cdNextHelix3);
					return res;
				}
				// && (difPotentialNextHelix3 >= difPotentialNextHelix5) simpleDist_5 < simpleDist_3 
				else if((potentialNextHelix3ME > potentialNextHelix5ME) && potentialNextHelix5.getHelixME()<= minME) {
					Result res = new Result(true, potentialNextHelix5, cdNextHelix5);
					//System.out.println(p2a.getName() + " dif:" + difPotentialNextHelix5);

					return res;
				}
				else if(( cdNextHelix3 == cdNextHelix5 ) && ( (difPotentialNextHelix3 < balance) ) && potentialNextHelix3.getHelixME()<= minME) {
					if((potentialNextHelix3ME < potentialNextHelix5ME) && (difPotentialNextHelix3 < balance) ) {
						return new Result(true, potentialNextHelix3, cdNextHelix3); 
					}
					else if(  (difPotentialNextHelix3 < balance) && potentialNextHelix5.getHelixME()<= minME) {
						return new Result(true, potentialNextHelix5, cdNextHelix5);
					}
					else {
						return new Result(false, p2a, cdNextHelix3);
					}
				}
				//does not satisfy other requirements, no other helicies predicted
				else {
					Result res = new Result(false, p2a, cdNextHelix3);
					return res;
				}
	}
	/*
	//pass in a helix and pairs the bases the bases in the paired bases array, the array is augment plus 1
	public static void updatePairedBases(PeHelicies helix, int[] pairedBases) {
		int count = 0;
		//adds in pais for 5' end
		for(int i = helix.getPre5() ; i <= helix.getEnd5(); i++) {
			pairedBases[i] = helix.getEnd3()  - count;
			count++;
		}
		//adds in pairs for 3' end
		count = 0;
		for(int i = helix.getPre3() ; i <= helix.getEnd3(); i++) {
			pairedBases[i] = helix.getEnd5() - count;
			count++;
		}
	}
	*/
	//pass in a helix and checks to see whether it is valid and does not include base pairs that have already been paired previously, returns True if the helix, otherwise returns false
	//need to update, if a base is paired cannot just remove helix, but should just remove the base, next helix search should just move to the nucleotide, should do this in the counter method,
	//or just try removing the array containing pairedBases
	/*
	public static boolean validHelix(PeHelicies helix, int[] pairedBases) {
		//5' check
		for(int i=helix.getPre5(); i<=helix.getEnd5(); i++) {
			if(pairedBases[i] != 0)
				return false;
		}
		//3' check
		for(int i=helix.getPre3(); i<=helix.getEnd3(); i++) {
			if(pairedBases[i] != 0)
				return false;
		}
		return true;
	}
	*/
	//takes in a potential helix and a list of predicted compound helices, if another helix has already formed that inhibits the formation of this helix, the method determines which of two helicies is more stable, and then removes 
	//the helix that is less stable, returns true if the helix passed in as the argument is more stable than the competing helix or if there is no competing helix
	//if the method returns false and the helix cannot be replaced, this should be the end of elongation for this compound helix
	public static boolean isMoreStable(PeHelicies helix, ArrayList<CompoundHelix> predictedCompHeliciesList, String seq, ArrayList<PeHelicies> initialHeliciesList) {
		int helix5Start = helix.getPre5();
		int helix5End = helix.getEnd5();
		int helix3Start = helix.getPre3();
		int helix3End = helix.getEnd3();
		//if the helix passed in cannot replace any helix it needs to, the helix cannot be added to predictedHelicies, and return false
		boolean canReplace = true;
		CompoundHelix competingCompHelix;
		PeHelicies competingHelicies = helix;
		int counter = 0;
		int listNum = 0;
		int counter2 = 0;
		int heliciesCounter = 0;
		
		//if no compound helicies have been predicted yet, there can be no competing helicies, return true
		if(predictedCompHeliciesList.size()==0)
			return true;
		
		//reverse the list so that compound helicies nearest will be checked first
		Collections.reverse(predictedCompHeliciesList);
		
		//list of all the conflicting helicies
		ArrayList<PeHelicies> competingHeliciesList = new ArrayList<PeHelicies>();
		//runs through all the helicies that have been predicted so far to determine if any are competing
		mainLoop:
		for(CompoundHelix compHelix: predictedCompHeliciesList) {
			ArrayList<PeHelicies> heliciesList = compHelix.getAllHelicies();
			//reverse the heliciesList so that the nearest helicies are checked first
			Collections.reverse(heliciesList);
			for(PeHelicies competingHelix: heliciesList) {
				int competingHelix5Start = competingHelix.getPre5();
				int competingHelix5End = competingHelix.getEnd5();
				int competingHelix3Start = competingHelix.getPre3();
				int competingHelix3End = competingHelix.getEnd3();
				
				//if there is another helix that conflicts with the helix passed in, isCompeting = true
				boolean isCompeting = false;
				
				//if either the start or end of the helix passed in is within the competing helix, isCompeting = true
				if((helix5Start>=competingHelix5Start) && (helix5Start<=competingHelix5End)) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum = counter;
					heliciesCounter = counter2;
				}
				if((helix5Start>=competingHelix3Start) && (helix5Start<=competingHelix3End)) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum = counter;
					heliciesCounter = counter2;
				}
				if(helix5End>=competingHelix5Start && helix5End<=competingHelix5End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("2");
				}
				if((helix5End>=competingHelix3Start) && (helix5End<=competingHelix3End)) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum = counter;
					heliciesCounter = counter2;
				}
				if(helix3Start>=competingHelix3Start && helix3Start<=competingHelix3End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("3");
				}
				if(helix3Start>=competingHelix5Start && helix3Start<=competingHelix5End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("3");
				}
				if(helix3End>=competingHelix3Start && helix3End<=competingHelix3End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum = counter;
					heliciesCounter = counter2;
					//System.out.println("4");
				}
				if(helix3End>=competingHelix5Start && helix3End<=competingHelix5End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("3");
				}
				//if the competing helix is within the passed in helix, isCompeting = true;
				if(competingHelix5Start>=helix5Start && competingHelix5Start<=helix5End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("3");
				}
				if(competingHelix5Start>=helix3Start && competingHelix5Start<=helix3End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("3");
				}
				if(competingHelix5End>=helix5Start && competingHelix5End<=helix5End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("6");
				}
				if(competingHelix5End>=helix3Start && competingHelix5End<=helix3End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("6");
				}
				if(competingHelix3Start>=helix3Start && competingHelix3Start<=helix3End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum = counter;
					heliciesCounter = counter2;
					//System.out.println("7");
				}
				if(competingHelix3Start>=helix5Start && competingHelix3Start<=helix5End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum = counter;
					heliciesCounter = counter2;
					//System.out.println("7");
				}
				if(competingHelix3End>=helix3Start && competingHelix3End<=helix3End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("8");
				}
				if(competingHelix3End>=helix5Start && competingHelix3End<=helix5End) {
					isCompeting = true;
					/*
					competingCompHelix = compHelix;
					competingHelicies = competingHelix;
					*/
					listNum =  counter;
					heliciesCounter = counter2;
					//System.out.println("8");
				}
				counter2++;
				
				//if there is a competing helix add to the competing helicies list
				if(isCompeting) {
					competingHeliciesList.add(competingHelix);
				}
			}
			counter2 = 0;
			counter++;
			//put the helicies list back in order
			Collections.reverse(heliciesList);
			//once a helix has had base pairs removed or a helix had been removed from the compound helix, the CDs for helicies need to be updated
			//updateCDandMEs(predictedCompHeliciesList.get(listNum));
		}
		//put the compound helicies list back in order
		Collections.reverse(predictedCompHeliciesList);
		
		//runs through the initial helicies to see if any are conflicing 
		for(PeHelicies competingHelix: initialHeliciesList) {
			int competingHelix5Start = competingHelix.getPre5();
			int competingHelix5End = competingHelix.getEnd5();
			int competingHelix3Start = competingHelix.getPre3();
			int competingHelix3End = competingHelix.getEnd3();
			
			//if there is another helix that conflicts with the helix passed in, isCompeting = true
			boolean isCompeting = false;
			
			//if either the start or end of the helix passed in is within the competing helix, isCompeting = true
			if((helix5Start>=competingHelix5Start) && (helix5Start<=competingHelix5End)) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum = counter;
				heliciesCounter = counter2;
			}
			if((helix5Start>=competingHelix3Start) && (helix5Start<=competingHelix3End)) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum = counter;
				heliciesCounter = counter2;
			}
			if(helix5End>=competingHelix5Start && helix5End<=competingHelix5End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("2");
			}
			if((helix5End>=competingHelix3Start) && (helix5End<=competingHelix3End)) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum = counter;
				heliciesCounter = counter2;
			}
			if(helix3Start>=competingHelix3Start && helix3Start<=competingHelix3End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("3");
			}
			if(helix3Start>=competingHelix5Start && helix3Start<=competingHelix5End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("3");
			}
			if(helix3End>=competingHelix3Start && helix3End<=competingHelix3End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum = counter;
				heliciesCounter = counter2;
				//System.out.println("4");
			}
			if(helix3End>=competingHelix5Start && helix3End<=competingHelix5End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("3");
			}
			//if the competing helix is within the passed in helix, isCompeting = true;
			if(competingHelix5Start>=helix5Start && competingHelix5Start<=helix5End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("3");
			}
			if(competingHelix5Start>=helix3Start && competingHelix5Start<=helix3End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("3");
			}
			if(competingHelix5End>=helix5Start && competingHelix5End<=helix5End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("6");
			}
			if(competingHelix5End>=helix3Start && competingHelix5End<=helix3End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("6");
			}
			if(competingHelix3Start>=helix3Start && competingHelix3Start<=helix3End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum = counter;
				heliciesCounter = counter2;
				//System.out.println("7");
			}
			if(competingHelix3Start>=helix5Start && competingHelix3Start<=helix5End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum = counter;
				heliciesCounter = counter2;
				//System.out.println("7");
			}
			if(competingHelix3End>=helix3Start && competingHelix3End<=helix3End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("8");
			}
			if(competingHelix3End>=helix5Start && competingHelix3End<=helix5End) {
				isCompeting = true;
				/*
				competingCompHelix = compHelix;
				competingHelicies = competingHelix;
				*/
				listNum =  counter;
				heliciesCounter = counter2;
				//System.out.println("8");
			}
			counter2++;
			
			//if there is a competing helix add to the competing helicies list
			if(isCompeting) {
				competingHeliciesList.add(competingHelix);
			}
		}
		/*
		System.out.println("Current Helix: ");
		helix.printSequence();
		for(PeHelicies competingHelix: competingHeliciesList) {
			System.out.println("Competing helix: " + competingHelix.getName());
			competingHelix.printSequence();
			helix.competingBasePairs(competingHelix);
		}
		System.out.println();
		*/
		
		
		
		//iterate through the competing helicies, if the helix passed in is not more stable than any of these, the helix can not be added
		PeHelicies competingHelix;
		/*
		//return false if any initial helices are in the competing helicies list
		for(Iterator<PeHelicies> iterator = competingHeliciesList.iterator(); iterator.hasNext();) {
			competingHelix = iterator.next();
			if(competingHelix.isInitialHelix())
				return false;
		}
		
				//return false if any of the competing helicies are more stable than the helix passed in
		for(Iterator<PeHelicies> iterator = competingHeliciesList.iterator(); iterator.hasNext();) {
			competingHelix = iterator.next();
			if(competingHelix.getHelixME()< helix.getHelixME())
				return false;
		}
		*/
		
		//new code that removes part of the competing helix on a base pair by base pair basis
		//if the helix is conflicting with an initial helix, only part of the helix can form, if it is too short do not add to the predicited helicies list
		for(Iterator<PeHelicies> iterator = competingHeliciesList.iterator(); iterator.hasNext();) {
			competingHelix = iterator.next();
			if(competingHelix.isInitialHelix()) {
				//return false if the helix becomes too short
				if(helix.removeBasePairs(competingHelix) ) {
					//don't add the helix to the predicted helicies list
					return false;
				}
					
			}
		}
		
		//if any of the conflicting helicies are more stable, remove parts of the helix passed, if the helix becomes too short, do not add the helix to the predicted helicies list
		for(Iterator<PeHelicies> iterator = competingHeliciesList.iterator(); iterator.hasNext();) {
			competingHelix = iterator.next();
			//compare the conflicting base pairs, if the competing helix base pairs are more stable, remove base pairs from the helix  !helix.competingBasePairs(competingHelix)
			if(helix.getHelixME() > competingHelix.getHelixME()) {
				//if the helix becomes to short the helix can not be added to the predicted helicies list, return false
				if(helix.removeBasePairs(competingHelix))
					return false;
			}
		}
	
		//remove/alter all conflicting helicies
		for(CompoundHelix compHelix: predictedCompHeliciesList) {
			ArrayList<PeHelicies> heliciesList = compHelix.getAllHelicies();
			for(Iterator<PeHelicies> iterator = heliciesList.iterator(); iterator.hasNext();) {
				PeHelicies curHelix = iterator.next();
				for(PeHelicies conflictingHelix: competingHeliciesList) {
					if(curHelix.isEqual(conflictingHelix)) {
						/*
						System.out.println("Current helix:" );
						helix.printSequence();
						System.out.println("Competing helix: " + curHelix.getName());
						curHelix.printSequence();
						*/
						
						boolean remove = curHelix.removeBasePairs(helix);
						if(remove) {
							//System.out.println(curHelix.getName() + ": removed");
							iterator.remove();
							compHelix.decreaseSizeByOne();
						}
						updateCDandMEs(predictedCompHeliciesList);
					}
					
				}
			}
			canReplace = true;
		}
		return canReplace;
	}
	
	//updates the conditional distances and MEs of all the helicies predicted
	public static void updateCDandMEs(ArrayList<CompoundHelix> predictedCompHeliciesList) {
		for(CompoundHelix compHelix: predictedCompHeliciesList) {
			ArrayList<PeHelicies> heliciesList = compHelix.getAllHelicies();
			if(heliciesList.size() > 1) {
				for(int i=1; i<heliciesList.size(); i++) {
					heliciesList.get(i).setCD(getCondDist(heliciesList.get(i-1), heliciesList.get(i)));
					heliciesList.get(i).setHelixME(heliciesList.get(i).getHelixEnergy()/heliciesList.get(i).getCD());
					int dist5 = Math.abs(heliciesList.get(i-1).getPre5() - heliciesList.get(i).getEnd5());
					int dist3 = Math.abs(heliciesList.get(i-1).getEnd3() - heliciesList.get(i).getPre3());
					heliciesList.get(i).setDistances(dist5, dist3);

				}
			}
		}
	}
	//given the list of predicted compound helicies, this method removes all the helicies that have a CD > 20 and a unbalanced unpaired region greater than 5
	public static void removeUnstableHelicies(ArrayList<CompoundHelix> predictedCompHeliciesList) {
		for(CompoundHelix compHelix: predictedCompHeliciesList) {
			ArrayList<PeHelicies> heliciesList = compHelix.getAllHelicies();
			PeHelicies currentHelix;
			for(Iterator<PeHelicies> iterator = heliciesList.iterator(); iterator.hasNext();) {
				currentHelix = iterator.next();
				int dif = Math.abs(currentHelix.getDistance3()  - currentHelix.getDistance5());
				if((!currentHelix.isInitialHelix()) && (currentHelix.getCD() > 20 || dif > 5)) {
					iterator.remove();
					compHelix.decreaseSizeByOne();
					updateCDandMEs(predictedCompHeliciesList);
				}
			}
		}
	}
	
	//checks whether the helix passed in is added to the compound helix the ratio of pair to total length remains above 0.6
	//returns true if the ratio is above 0.6, otherwise false, end elongation of the compound helix
	public static boolean calcPredRatio(CompoundHelix compHelix, PeHelicies potHelix, Double ratio) {
		//number of paired bases already in the compound helix
		int pairedBasesCompHelix = compHelix.getTotalNumPairedNucleotides();
		//total compound helix length
		int totalCompHelixLen = compHelix.getTotalHelixLength();
		
		
		//the number of paired bases in the potential helix
		int pairedBasesPotHelix = potHelix.getHelixLength() *2;
		
		//determine the number of unpaired bases between the last helix in the compound helix and the next potential helix
		int unPairedBasesToAdd = CompoundHelix.distBetweenHelicies5(compHelix.getAllHelicies().get(compHelix.getAllHelicies().size()-1), potHelix);
		unPairedBasesToAdd +=  CompoundHelix.distBetweenHelicies3(compHelix.getAllHelicies().get(compHelix.getAllHelicies().size()-1), potHelix);

		
		int newPairedBases = pairedBasesCompHelix + pairedBasesPotHelix;
		int newTotalLength = totalCompHelixLen + unPairedBasesToAdd + pairedBasesPotHelix;
		
		double compHelixRatio = (double)newPairedBases / (double)newTotalLength;
		//if ratio of the compound helix with the new helix is still above 0.6, keep extending the helix
		if(compHelixRatio >= ratio)
			return true;
		else
			return false;
	}	
	
	
	
}

	
