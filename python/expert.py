import MySQLdb
import sys
import nltk
from nltk.corpus import wordnet as wn
from nltk.stem.porter import PorterStemmer

con = None
drugNames = ["Flagyl", "Ibuprofen", "Metformin", "Prilosec", "Tirosint", "Xanax"]
frequencyNames = ["Overdose", "More Common", "Less Common", "Not Known", "Rare"]
def connect():
  global con
  con = MySQLdb.connect('143.248.49.97', 'cs492', 'aliceoh', 'POD')
  return

def readTSV(file):
  global con, drugNames, frequencyNames
  cur = con.cursor()
  ps = PorterStemmer()
  
  dt = {}
  for d in drugNames : 
    dt[d] = {}
    for f in frequencyNames:
      dt[d][f] = {}
      dt[d][f][0] = "" # side effects
      dt[d][f][1] = "" # side effects tagged
      dt[d][f][2] = "" # side effects stemmed
      dt[d][f][3] = "" # side effects syn
  
  with open(file) as f:
    content = f.readlines()
    for line in content:
      tokens = line.split('\t')
      drugName = None
      for d in drugNames:
        if d.lower() in tokens[0]:
          drugName = d
          break
      if drugName == None:
        continue
      
      frequency = tokens[1].strip()
      sideEffects = tokens[2].strip()[1:-1]
      sideEffectsTagged = tokens[3].strip()[1:-1]
      
      # Stem
      stems = []
      sEffects = nltk.word_tokenize(sideEffects)
      for sEffect in sEffects:
        stems.append(ps.stem(sEffect))
      stems = set(stems)
      
      # Synonyms
      syns = []
      for sEffect in sEffects:
        if not sEffect: continue
        # Synset
        for synset in wn.synsets(sEffect):
          name = synset.name()
          if '.v.' in name: continue
          syns.append(name.encode('ascii', 'ignore'))
      syns = set(syns)
      
      side_effects_stemmed = " ".join(stems)
      side_effects_syn = " ".join(syns)
      dt[drugName][frequency][0] += sideEffects + " "
      dt[drugName][frequency][1] += sideEffectsTagged + " "
      dt[drugName][frequency][2] += side_effects_stemmed + " "
      dt[drugName][frequency][3] += side_effects_syn +  " "
  
  for d in drugNames:
    for f in frequencyNames:
      dt[d][f][0] = dt[d][f][0].replace("'", "\"");
      dt[d][f][1] = dt[d][f][1].replace("'", "\"");
      dt[d][f][2] = dt[d][f][2].replace("'", "\"");
      dt[d][f][3] = dt[d][f][3].replace("'", "\"");
      #query = "INSERT INTO expert_drug_side_effects (drug_family, frequency, side_effects, side_effects_tagged, side_effects_stemmed, side_effects_synset) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')" %(d, f, dt[d][f][0].strip(), dt[d][f][1].strip(), dt[d][f][2].strip(), dt[d][f][3].strip())
      query = "UPDATE expert_drug_side_effects SET side_effects='%s', side_effects_tagged='%s', side_effects_stemmed='%s', side_effects_synset='%s' WHERE drug_family = '%s' AND frequency = '%s'" %(dt[d][f][0].strip(), dt[d][f][1].strip(), dt[d][f][2].strip(), dt[d][f][3].strip(), d, f)
      print query
      cur.execute(query)

  con.commit()
  print "DONE"
  
try:
  drugName = "Flagyl"
  
  print "Hello"
  connect()
  print "Connection Established."
  
  readTSV("./Expert_Drug-SideEffects-Common.tsv")
 
  #for drugName in drugNames:
    #nlpSymptoms(drugName)
  
except Exception as e:
  print "Error %s" % (e)
  sys.exit(1)

finally:  
  if con:
    con.close()
