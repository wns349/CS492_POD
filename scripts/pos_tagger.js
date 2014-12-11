var mysql = require('mysql');
var async = require('async');
var _ = require('underscore');
var _s = require('underscore.string');
var util = require('util');
var cluster = require('cluster');
var pos = require('pos');

var connection = mysql.createConnection({
  host     : '143.248.49.97',
  user     : 'cs492',
  password : 'aliceoh'
});

var LIMIT = 180000;
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
  
  connection.query('SELECT * FROM pod.author_doc_review LIMIT '+ Number(process.env.FROM) + ', ' + LIMIT, function(err, reviews, fields) {
  //connection.query('SELECT * FROM pod.author_doc_review LIMIT ?, ?', [1, 2], function(err, reviews, fields) {
    async.mapLimit(reviews, 1000, function (review, callback) {
    //async.mapSeries(reviews, function (review, callback) {
      count++;
      var words = new pos.Lexer().lex(review.post);
      var postLength = words.length;
      var taggedWords = new pos.Tagger().tag(words);
      var taggedPost = _.map(taggedWords, function (tw) { return tw[0] + '/' + tw[1]; }).join(" ");

      setImmediate(function () { callback(null, [review.author_id, review.doc_id, taggedPost]) });
    },
    function (err, results) {
      async.eachSeries(_.values(_.groupBy(results, function (a, b) { return Math.floor(b/1000);})), function (inputs, callback) {
        connection.query('INSERT INTO pod.author_doc_review_tagged VALUES ' + connection.escape(inputs), function (err, result) {
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
