var _ = require('underscore');
var _s = require('underscore.string');
var fs = require('fs');


var lines = fs.readFileSync('Affective-Features.tsv', 'utf-8');

var affectives = _.map(_s.lines(lines), function (line) {
  var keyword = _s.trim(_s.words(line, '\t')[0]);
  var words = _.map(_s.words(_s.words(line, '\t')[1], ','), function (w) { return _s.trim(w); });

  return {keyword: keyword, words: words};
});

var results = _.map(_.groupBy(affectives, function (a) { return a.keyword; }), function (words, keyword) {
  return {keyword: keyword, words: _.uniq(_.flatten(_.map(words, function (x) { return x.words; })))};
});

_.each(results, function (result) {
  console.log(_s.join('\t', result.keyword, result.words.join('\t')));
});
