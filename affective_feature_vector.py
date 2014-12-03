from nltk.tokenize import RegexpTokenizer
import threading


def load_af(af_path):
  features = {}
  f_af = open(af_path, 'r');

  for line in f_af.readlines():
    line = line.strip()
    tokens = line.split('\t');
    features[tokens[0]] = tokens[1:]

  f_af.close()

  return features

##########
# MAIN
##########
af_path = './data/Affective-Features-Final.tsv'
#post_path = './data/Author-Doc-Review.tsv'

#af_path = './data/Affective-Features-Short.tsv'
post_path = './data/Author-Doc-Review-Short.tsv'

SUM_WORDS = 'sumWords'
SUM_POST_LENGTH = 'sumPostLength'

# Load affective feature
features = load_af(af_path)

# Tokenizer (by words only)
tokenizer = RegexpTokenizer(r'\w+')

results = {} # stores count

lock = threading.Lock()

#for line in f_post.readlines():
with open(post_path, 'r') as inf:
  read_line_count = 0
  for line in inf:
    print str(read_line_count) + ' ', 
    read_line_count += 1

    tokens = line.split('\t')
    user_id = tokens[0].strip()
    doctor_id = tokens[1].strip()
    post = tokens[2].strip()
    words = tokenizer.tokenize(post)

    lock.acquire()
    if not user_id in results:
      results[user_id] = {}
    lock.release()

    for keyword, affective_words in features.iteritems():
      lock.acquire()
      if not keyword in results[user_id]:
        results[user_id][keyword] = {}
        results[user_id][keyword][SUM_WORDS] = 0
        results[user_id][keyword][SUM_POST_LENGTH] = 0
      lock.release()

      for word in words:
        if word in affective_words:
          results[user_id][keyword][SUM_WORDS] += 1

      results[user_id][keyword][SUM_POST_LENGTH] += len(words)

# compute vector
output = {}
for user_id in results:
  output[user_id] = {}
  for keyword in results[user_id]:
    output[user_id][keyword] = float(results[user_id][keyword][SUM_WORDS]) / float(results[user_id][keyword][SUM_POST_LENGTH])

#print(results)

print('-----------')

print output

#f_post.close()

