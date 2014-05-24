var http = require('http');
var port = 32200;
//var io = require('socket.io');
http.createServer(function (req, res) {
  res.writeHead(200, {'Content-Type': 'text/html'});
  res.end('<html><head><link href="styl.css" rel="stylesheet" type="text/css"></head><body><h1>Projekt SMART-MOUSE by KMK</h1></body><br><br>Serwer</html>');
}).listen(port);

//var io.listen(server);
console.log('Serwer działa');