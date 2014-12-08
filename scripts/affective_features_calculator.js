var mysql = require('mysql');
var async = require('async');
var _ = require('underscore');
var _s = require('underscore.string');
var util = require('util');
var cluster = require('cluster');

var connection = mysql.createConnection({
  host     : '143.248.49.97',
  user     : 'cs492',
  password : 'aliceoh'
});

var LIMIT = 110000;
//if (false) {
if (cluster.isMaster) {
  for (var i = 0; i < require('os').cpus().length; i++) {
  //for (var i = 0; i < 3; i++) {
    cluster.fork({FROM: i * LIMIT});
  }

  cluster.on('exit', function(worker, code, signal) {
    console.log('worker ' + worker.process.pid + ' died', code, signal);
  });
} else {
  var count = 0;
  connection.connect();
  
  connection.query('SELECT author_id, doc_id, GROUP_CONCAT(post) AS post FROM pod.author_doc_review GROUP BY doc_id, author_id ORDER BY doc_id ASC LIMIT '+ Number(process.env.FROM) + ', ' + LIMIT, function(err, reviews, fields) {
  //connection.query('SELECT * FROM pod.author_doc_review LIMIT ?, ?', [1, 2], function(err, reviews, fields) {
    async.eachLimit(reviews, 1000, function (review, callback) {
      count++;
      var words = _.map(_s.words(review.post, /[!@#$%^&*()\-_=+\[\]\{\}\\\|\;\:\'\"\,\<\.\>\/\?\s]/), function (w) { return w.toLowerCase(); });
      var wordsCount = _.countBy(words);

      connection.query('SELECT * FROM pod.affective_features WHERE word IN (?)', _.map(_.uniq(words), function (word) { return word.toLowerCase(); }), function (err, features, fields) {
        if (_.isEmpty(features)) {
          callback();
          return;
        }

        features = _.map(_.groupBy(_.map(features, function (feature) { 
          feature['frequency'] = wordsCount[feature.word.toLowerCase()];
          return feature;
        }), function (feature) {
          return feature.feature;
        }), function (objs, feature) {
          return {feature: feature, frequency: _.reduce(objs, function (sum, o) { return sum + o.frequency; }, 0)};
        });

        //console.log(review.author_id, review.doc_id, features);

        connection.query('INSERT INTO pod.affective_feature_values VALUES ' + connection.escape(_.map(features, function (f) { return [review.author_id, review.doc_id, f.feature, f.frequency, review.post.length]; })), function (err, result) {
          console.log(process.pid, JSON.stringify(err), 'result:', review.author_id, review.doc_id, _.isEmpty(result) ? 'null' : result.affectedRows, connection.escape(_.map(features, function (f) { return [review.author_id, review.doc_id, f.feature, f.frequency, review.post.length]; })));
          callback();
        });
      });
    },
    function (err, results) {
      console.log('worker', process.pid, 'end', count);
      connection.end();
      process.exit(0)
    });
  });
}
