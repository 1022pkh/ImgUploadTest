# 안드로이드 - node.js 서버 간의 파일업로드 

서버와의 합동세미나 전 연습 코드입니다.
node.js 모듈 formidable을 사용하였습니다.

----

node.js를 처음 사용해보아서 상당히 어려운 점이 많았지만, 공부하면서 진행해보니 node.js로 재미있는 녀석이었습니다.

node.js는 중요 부분의 코드만 올립니다.

----

처음에는 multer 모듈을 사용하여 시도했지만, 웹페이지 내에서 파일업로드는 잘 작동하였지만 안드로이드앱,postman을 통해 시도할 때 안되었음
그래서 안드로이드 앱 - node.js 서버로 파일 업로드를 구현했던 경험자에게 조언을 구했음.
그 결과 자신도 multer모듈로 했을 때 안되어서 formidable 모듈을 사용했다고 함. 
multer 모듈은 웹페이지내에 적합한 모듈이라고 함

----

    app.post('/upload',function(req,res){ 

    console.log("Request upload!");

    var name = "";
    var filePath = "";
    var form = new formidable.IncomingForm();

    form.parse(req, function(err, fields, files) {
        name = fields.name;
    });

    form.on('end', function(fields, files) {
      for (var i = 0; i < this.openedFiles.length; i++) {
        var temp_path = this.openedFiles[i].path;
        var file_name = this.openedFiles[i].name;
        var index = file_name.indexOf('/'); 
        var new_file_name = file_name.substring(index + 1);
         
        var new_location = 'uploads/'+name+'/';

        fs.copy(temp_path, new_location + file_name, function(err) { // 이미지 파일 저장하는 부분임
          if (err) {
            console.error(err);

            console.log("upload error!");
          }
          else{      
            res.setHeader('Content-Type', 'application/json');
            res.send(JSON.stringify({ result : "success", url : new_location+file_name }, null, 3));

            console.log("upload success!");
          }
        });
      }

    });
    });

