var mysql = require('mysql');
var async = require('async');
var _ = require('underscore');
var _s = require('underscore.string');
var util = require('util');
var cluster = require('cluster');
var pos = require('pos');

var RULE = {
  'QS': {type: 'tag', rules: ['W']},
  'Neg': {type: 'word', rules: ['no', 'not', 'neither', 'nor', 'never']},
  'Following Conjunctions': {type: 'word', rules: ['but', 'however', 'nevertheless', 'otherwise', 'yet', 'still', 'nonetheless']},
  'Contrasting Conjunctions': {type: 'word', rules: ['till', 'until', 'despite', 'inspite', 'though', 'although']},
  'Inferential Conjunctions': {type: 'word', rules: ['therefore', 'furthermore', 'consequently', 'thus', 'as', 'subsequently', 'eventually', 'hence']},
  'Strong Modals': {type: 'word', rules: ['might', 'could', 'can', 'would', 'may']},
  'Weak Modals': {type: 'word', rules: ['should', 'ought', 'need', 'shall', 'will', 'must']},
  'Conditionals': {type: 'word', rules: ['if']},
  'Adverbs': {type: 'tag', rules: ['RB']},
  'Adjectives': {type: 'tag', rules: ['JJ']},
  'Proper Nouns': {type: 'tag_exact', rules: ['NNP', 'NNPS']},
  'Common Nouns': {type: 'tag_exact', rules: ['NN', 'NNS']}, 
  'First Person': {type: 'word', rules: ['i', 'we', 'me', 'us', 'my', 'mine', 'our', 'ours']},
  'Second Person': {type: 'word', rules: ['you', 'your', 'yours']},
  'Third Person': {type: 'word', rules: ['he', 'she', 'him', 'her', 'his', 'it', 'its', 'hers', 'they', 'them', 'their', 'theirs']},
  'Definitie Determiner': {type: 'tag_word', rules: ['DT', 'th']}, 
  'Indefinite Determiner': {type: 'tag_word', rules: ['DT', 'th']}
};

var FEATURE = {
  'QS': 0,
  'Neg': 0,
  'Following Conjunctions': 0,
  'Contrasting Conjunctions': 0,
  'Inferential Conjunctions': 0,
  'Strong Modals': 0,
  'Weak Modals': 0,
  'Conditionals': 0,
  'Adverbs': 0,
  'Adjectives': 0,
  'Proper Nouns': 0,
  'Common Nouns': 0, 
  'First Person': 0,
  'Second Person': 0,
  'Third Person': 0,
  'Definitie Determiner': 0, 
  'Indefinite Determiner': 0
};

var connection = mysql.createConnection({
  host     : '143.248.49.97',
  user     : 'cs492',
  password : 'aliceoh'
});

var LIMIT = 110000;
//if (false) {
if (cluster.isMaster) {
  for (var i = 0; i < require('os').cpus().length; i++) {
  //for (var i = 0; i < 1; i++) {
    cluster.fork({FROM: i * LIMIT});
  }

  cluster.on('exit', function(worker, code, signal) {
    console.log('worker ' + worker.process.pid + ' died', code, signal);
  });
} else {
  var count = 0;
  connection.connect();
  
  connection.query('SELECT author_id, doc_id, GROUP_CONCAT(pos_tagged SEPARATOR \' \') AS post FROM pod.author_doc_review_tagged GROUP BY doc_id, author_id ORDER BY doc_id ASC LIMIT '+ Number(process.env.FROM) + ', ' + LIMIT, function(err, reviews, fields) {
  //connection.query('SELECT * FROM pod.author_doc_review LIMIT ?, ?', [1, 2], function(err, reviews, fields) {
    async.mapLimit(reviews, 1000, function (review, callback) {
      count++;
      var words = _s.words(review.post);
      var wordsCount = _.countBy(words);
      var postLength = words.length;

      var feature = _.clone(FEATURE);

      _.each(words, function (w) {
        var tokens = _s.words(w, '/');
        var word = tokens.length === 1 ? '/' : tokens[0];
        var tag = tokens.length === 1 ? tokens[0] : tokens[1];

        _.each(RULE, function (value, key) {
          if (value.type === 'word') {
            if (_.contains(value.rules, word.toLowerCase())) feature[key]++;
          } else if (value.type === 'tag') {
            if (_.any(value.rules, function (t) { return _s.startsWith(tag, t); })) feature[key]++;
          } else if (value.type === 'tag_exact') {
            if (_.contains(value.rules, tag)) feature[key]++;
          } else if (value.type === 'tag_word') {
            if (_s.startsWith(tag, value.rules[0])) {
              if (_s.startsWith(word, value.rules[1])) feature['Definitie Determiner']++;
              else feature['Indefinite Determiner']++;
            } 
          }
        });
      });

      setImmediate(function () { callback(null, _.map(feature, function (value, key) { return [review.author_id, review.doc_id, key, value, postLength];}));});
    },
    function (err, results) {
      async.eachSeries(_.values(_.groupBy(_.flatten(results, true), function (a, b) { return Math.floor(b/1000);})), function (inputs, callback) {
        connection.query('INSERT INTO pod.stylistic_feature_values VALUES ' + connection.escape(inputs), function (err, result) {
          console.log(process.pid, JSON.stringify(err), 'result:', _.isEmpty(result) ? 'null' : result.affectedRows);
          callback();
        });
      },
      function (err) {
        console.log('worker', process.pid, 'end', count);
        connection.end();
        process.exit(0)
      });
    });
  });
}
