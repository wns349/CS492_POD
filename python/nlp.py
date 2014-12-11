import MySQLdb
import sys
from nltk.corpus import wordnet as wn
from nltk.stem.porter import PorterStemmer

con = None

def connect():
  global con
  con = MySQLdb.connect('143.248.49.97', 'cs492', 'aliceoh', 'POD')
  return

def nlpSymptoms(drugName):
  ps = PorterStemmer()  
  global con
  query = "SELECT * FROM pod.specific_drug_side_effects WHERE type='%s'" % (drugName)
  cur = con.cursor()
  cur.execute(query)
  
  records = cur.fetchall()

  for record in records:
    authorId = record[0]
    docId = record[1]
    symptom = record[2]
    tokens = symptom.split()
    
    if len(tokens) <= 0:
      continue
    
    syns = []
    stems = []
    for token in tokens:
      # Stem
      stems.append(ps.stem(token))
      
      # Synset
      for synset in wn.synsets(token):
        name = synset.name()
        if '.v.' in name: continue
        syns.append(name.encode('ascii', 'ignore'))
    stems = set(stems)
    syns = set(syns)
    symptom_syn = " ".join(syns)
    symptom_stem = " ".join(stems)
    queryUpdate = 'UPDATE pod.specific_drug_side_effects SET symptom_syn="%s", symptom_stem="%s" WHERE author_id=%d AND doc_id=%d AND type="%s" AND symptom="%s"' %(symptom_syn, symptom_stem, authorId, docId, drugName, symptom)
    print queryUpdate
    cur.execute(queryUpdate)
  con.commit()
  print "DONE"
  return
  
try:
  drugName = "Flagyl"
  
  print "Hello"
  connect()
  print "Connection Established."
  
  drugNames = ["Flagyl", "Ibuprofen", "Metformin", "Prilosec", "Tirosint", "Xanax"]
  for drugName in drugNames:
    nlpSymptoms(drugName)
  
except Exception as e:
  print "Error %s" % (e)
  sys.exit(1)

finally:  
  if con:
    con.close()
