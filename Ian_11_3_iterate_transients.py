#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Oct 20 15:59:30 2016

@author: ianphilips

outline:
    run a display transients script from rna heat
    option 1:
    1. allow user to flip through transients using a key, let's say the 'n' key for next
    2. return a zoom(x,y) for rnaheat to focus on 
    
    option 2: write a file
    1. call this python program
    2. this python program reads the transient helix file and writes new files according to an ordering scheme that includes:
    ## this is the header and only in the first script that rheat needs to load
          rheat.showHelixTag('T')
          # start color is actually lowest energy, end color is highest energy coloring
          # yellow to green high energy to low energy helices according to modified energy, ME
          rheat.setTemporaryPreference('SpectrumStartColor', '#006633')
          rheat.setTemporaryPreference('Spectrum50PercentColor', '#99FF00')
          rheat.setTemporaryPreference('SpectrumEndColor', '#FFFF66')
          # does setting helix tag color supersede helix spectrum for a shared tag?
          rheat.setHelixTagColor('T', '#00FF00') //transients are GREEN
          
       a. rheat.showHelixTag(Str(tags[i]))
       b. rheat.scrollTo(actual_bp_x, actual_bp_y)
       c. rheat.setHelixTagColor(str(tags[i]), '#0000FF') //actual helices are BLUE
          rheat.setHelixSpectrum('ME', .50, -1.5)
       d. rheat.setHelixTagLineWidth('T', 10.5)
          rheat.setHelixTagLineWidth('T', 5.5)
          rheat.setHelixTagLineWidth('T', 1.5)

    3. lists the tags in sequential order (p1,p2,p3,p4, then s1,s2,s3, etc.)
    4. call a rnaheat script that iterates through the new file's lines, displaying current line then hiding previous line
    5. (zooming tags too)
    6. allow for user interactivity to open next file with next set of comands

"""
import sys, os
from operator import itemgetter, attrgetter
from collections import OrderedDict
transient_directory_name='transient_script_sequence'
# pass these args to this script: <file containting transient info from Vishal> <experiment directory>
def is_Number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

def until_Next_Tag(tag, me_and_tag_list):
    me_return_list=[]
    i = me_and_tag_list.index(tag)+1
    tag=me_and_tag_list[i]
    while is_Number(tag):
        me_return_list.append(float(tag))
        i+=1
        if not i> len(me_and_tag_list)-1:
            tag=me_and_tag_list[i]
        else:
            return(me_return_list)
    return(me_return_list)
    
    

def main():
    
    #return(1000)
    os.chdir(sys.argv[2])
    print("I'm in this path", os.path.abspath('./'))
    #make the transient scripts directory
    if not transient_directory_name in os.listdir():
        os.mkdir(transient_directory_name)
    #file where all the transients are written is first argument
    transient_file= sys.argv[1]
    transient_group=[]
    pre_sequence_file=open(transient_directory_name+'/'+'file_0.js','w')
    pre_sequence_file.write("rheat.showHelixTag('T')"+'\n')
    pre_sequence_file.write("rheat.setTemporaryPreference('SpectrumStartColor', '#0000FF')"+'\n')
    pre_sequence_file.write("rheat.setTemporaryPreference('Spectrum50PercentColor', '#CCFFFF')"+'\n')
    pre_sequence_file.write("rheat.setTemporaryPreference('SpectrumEndColor', '#FFFFFF')"+'\n')
    pre_sequence_file.write("rheat.setHelixTagColor('T', '#00FF00')"+'\n')
    with open (transient_file) as tfile:
        
        # open the transient file, read each line, split into list
        used_tags_list=[]
        ME_bounds_list=[]
        t_line=tfile.readline().strip()
        while t_line!='':
#split transient helix line into a list
            #0th element is 3', 1st is 5', 2nd is helix tag 'P' for primary elongation, 'S'for secondary elongation
            t_split=t_line.split()
            # add a unique tag
            if not t_split[2] in ME_bounds_list: 
                ME_bounds_list.append(t_split[2])
            # add all modified energies after its correpsonding tag
            if t_split[3]=='T':
                t_split_ME = t_split[4].split('=')[1]
            else:
                t_split_ME = t_split[3].split('=')[1]
 # find where the tag is in the list in case the transient helices are out of order
            # add the modified energy that corresponds to its unique tag
            ME_bounds_list.insert(ME_bounds_list.index(t_split[2])+1, t_split_ME)
            
            
            t_tuple=[]
            number_index=''
            for i in range(len(t_split[2])):
                # split the p1a etc. tag into parts
                if not t_split[2][i].isdigit(): #if a string
                    #check for duplicates
                    if number_index!='':
        # split the tag into the number
                        t_tuple.append(int(number_index))
                    number_index=''
        # split the tag into string P or S, then number (done above), then string a,b,c,d,e,f, etc...
                    t_tuple.append(t_split[2][i])

                else:
                    number_index+=(t_split[2][i])
        # add the bp3', bp5' ends so you can call location later when writing the .js files
            t_split_3prime=t_split[0].split('-')
            #get earliest 3' base location
            t_tuple.append(t_split_3prime[0])
            t_split_5prime=t_split[1].split('-')
            #get largest 5' location, the first number in the #-#
            t_tuple.append(t_split_5prime[0])
            # get the modified energy

           # t_tuple.append(t_split_ME)
            # read the next line            
            
            t_line_next=tfile.readline().strip()
# you reach the end of the file
            if t_line_next=='':
# make sure to get the current tag before exiting
                #check for duplicates
                if not t_split[2] in used_tags_list:
                    #print(t_split[2] +"is the tag adding to transient group")
                    # find max and min for unique tag and add to tuple
                    t_tuple_min=min(until_Next_Tag(t_split[2],ME_bounds_list))
                    t_tuple_max=max(until_Next_Tag(t_split[2],ME_bounds_list))
                    t_tuple.append(t_tuple_min)
                    t_tuple.append(t_tuple_max)
                                        
                    #TAG ADDED HERE
                    transient_group.append(t_tuple)
                    used_tags_list.append(t_split[2])
                break
            t_split_next=t_line_next.split()
            # while the next line's group is the same as the current, add the current
            #print(t_line)
            if t_split[2]!=t_split_next[2]:
                if not t_split[2] in used_tags_list:
                    #print(t_split[2] +"is the tag adding to transient group")
                    t_tuple_min=min(until_Next_Tag(t_split[2],ME_bounds_list))
                    t_tuple_max=max(until_Next_Tag(t_split[2],ME_bounds_list))
                    t_tuple.append(t_tuple_min)
                    t_tuple.append(t_tuple_max)
                    
                    #TAG ADDED HERE
                    transient_group.append(t_tuple)
                    used_tags_list.append(t_split[2])
                
                
            t_line=t_line_next
    #order elements of transient_group list by letter (P,S, possible T termination later on) and then number
    #for elt in transient_group:
     #   print(elt)
    print('sorting!')
    
    sorted_transient_group=sorted(transient_group,key=itemgetter(0,1,2,5))
    counter=0
    script_sequence_file = open("script_sequence.txt",'w')
    show_all_tags_file=open("show_all_tags.js",'w')
    #unique_sorted_transient_group=map(list, OrderedDict.fromkeys(map(tuple,sorted_transient_group)))
    #write the file that lists all the files to iterate through
    for elt in sorted_transient_group:
        script_sequence_file.write("file_"+str(counter)+".js \n")
        counter+=1
    script_sequence_file.close()
    counter=0
    
    for tag in sorted_transient_group:
        print(tag)
        rheat_tag="'"
        #convert tag 'P' '12' 'b' to 'P12b'
        for i in range(3):
            rheat_tag+=str(tag[i])
        rheat_tag+="'"
        if counter!=0:
            
            #write a script for every tag in the transient helix group list
            new_script_file=open(transient_directory_name+'/'+'file_'+str(counter)+'.js','w')
            #new_script_file.write("rheat.setTemporaryPreference('SpectrumStartColor', '#0000FF')"+'\n')
            #new_script_file.write("rheat.setTemporaryPreference('Spectrum50PercentColor', '#CCFFFF')"+'\n')
            #new_script_file.write("rheat.setTemporaryPreference('SpectrumEndColor', '#FFFFFF')"+'\n')
            new_script_file.write("rheat.showHelixTag("+rheat_tag+")\n")
            new_script_file.write("rheat.zoomTo(5.0)\n")
            new_script_file.write("rheat.scrollTo("+tag[3]+', '+tag[4]+')\n') #add 3' base and 5' base locations
            
            new_script_file.write("rheat.setHelixTagColor("+rheat_tag+ ", '#0000FF') \n")
            # write the max and min ME specific for that tag
            new_script_file.write("rheat.setHelixSpectrum('ME', "+str(tag[6])+", "+str(tag[5])+")"+'\n')
#            new_script_file.write("rheat.setHelixTagLineWidth('T', 10.5)"+'\n')
            new_script_file.write("rheat.setHelixTagLineWidth('T', 5.5)"+'\n')
 #           new_script_file.write("rheat.setHelixTagLineWidth('T', 1.5)"+'\n')
            #hide everything until you show it
            pre_sequence_file.write("rheat.setHelixTagLineWidth("+rheat_tag+", 0.1)"+'\n')
            show_all_tags_file.write("rheat.showHelixTag("+rheat_tag+")"+'\n')
            new_script_file.close()
        else:
            #write the first file's info here
            pre_sequence_file.write("rheat.showHelixTag("+rheat_tag+")\n")
            pre_sequence_file.write("rheat.zoomTo(1.5)\n")
            pre_sequence_file.write("rheat.scrollTo("+tag[3]+', '+tag[4]+')\n') #add 3' base and 5' base locations
           
            pre_sequence_file.write("rheat.setHelixTagColor("+rheat_tag+ ", '#0000FF') \n")
            # write the max and min ME specific for that tag

            pre_sequence_file.write("rheat.setHelixSpectrum('ME', "+str(tag[6])+", "+str(tag[5])+")"+'\n')
            pre_sequence_file.write("rheat.zoomTo(5.0)\n")
            pre_sequence_file.write("rheat.setHelixTagLineWidth('T', 10.5)"+'\n')
            pre_sequence_file.write("rheat.setHelixTagLineWidth('T', 5.5)"+'\n')
            pre_sequence_file.write("rheat.setHelixTagLineWidth('T', 1.5)"+'\n')
            
        
        counter+=1
    pre_sequence_file.close()
    show_all_tags_file.close()

    
    
    
main()


        
    
    
    
