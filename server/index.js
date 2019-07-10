const ip = '192.168.1.68';
console.log("-------------------------------------------------------------");
let express = require('express');
let socket = require('socket.io');
let md5 = require('md5');
let MongoClient = require('mongodb').MongoClient;

let app = express();
app.use(express.static(__dirname + '/static'));
let db;

// db setup

//MongoClient.connect('mongodb+srv://nodejs:12345@myappdb-s6n3m.mongodb.net/test?retryWrites=true', { useNewUrlParser: true }, function(err, database){
MongoClient.connect('mongodb://nodejs:12345@myappdb-shard-00-00-s6n3m.mongodb.net:27017,myappdb-shard-00-01-s6n3m.mongodb.net:27017,myappdb-shard-00-02-s6n3m.mongodb.net:27017/test?ssl=true&replicaSet=myappdb-shard-0&authSource=admin&retryWrites=true', { useNewUrlParser: true }, function(err, database){	
	if (err) {
		return console.log(err)
	}
	db=database.db('mydb');
	console.log('DB connected successfully')
});

// server setup

let server = app.listen(3000, ip, function(){
	console.log('listening to requests on port 3000')

});




app.get("/test", function(request, response){
	db.collection('users').findOne({login : "karl"}, function (err, ans) {
		response.send(JSON.stringify(ans));
	});

});
app.get("*", function(request, response){
	response.sendFile(__dirname + '/static/404.html')
});


// socket setup

let io = socket(server);


io.on('connection', function(socket){
	console.log(socket.id + ' made socket connection');

	socket.on('chat', function(data){
		let messagedata = JSON.parse(data);
		io.sockets.emit('chat', messagedata.login+": "+messagedata.message);
	});

	socket.on('req.signup', function(data){
		let userdata = JSON.parse(data);
		userdata.password=md5(userdata.password);
		db.collection('users').findOne( { login : userdata.login} , function(err, answer){
			if (err){
				console.log(err);
				return;
			}
			if (answer){
				socket.emit('ans.signup', "Такой логин уже занят")
				//console.log("No such nickname, incorrect input data")
			}else{
				let user = {
					login : userdata.login,
					password : userdata.password
				};
				db.collection('users').insert(user, function(err){
					if (err){
						console.log(err);
						return;
					}
					socket.emit('ans.signup', "SUCCESS");
					socket.login = userdata.login;
					socket.currentuser = userdata.login;
					console.log(socket.id+" registered as "+ socket.login);
					socket.join(socket.currentuser);
					socket.join("login"+socket.currentuser);
					db.collection('users').updateOne( { login : socket.login }, { $set: { isOnline : true }}, function (err) {
						if (err){
							console.log(err);
						}
					});
				})
			}
		})
	});

	socket.on('req.signin', function(data){
		let userdata = JSON.parse(data);
		userdata.password=md5(userdata.password);
		db.collection('users').findOne( { login : userdata.login} , function(err, answer){
			if (err){
				console.log(err);
				return;
			}
			if (!answer){
				socket.emit('ans.signin', "Пользователь с таким никнемом не найден")
				//console.log("No such nickname, incorrect input data")
			}else{
				db.collection('users').findOne( { login : userdata.login, password : userdata.password}, function(err, answerpass){
					if (err){
						console.log(err);
						return;
					}
					if (!answerpass){
						socket.emit('ans.signin', "Неверный пароль")
						//console.log("incorrect password for that nickname")
					}else{
						socket.emit('ans.signin', "CORRECT");
						socket.login = userdata.login;
						socket.currentuser = userdata.login;
						socket.join(socket.currentuser);
						socket.join("login"+socket.currentuser);
						db.collection('users').updateOne( { login : socket.login }, { $set: { isOnline : true }}, function (err) {
							if (err){
								console.log(err);
							}
						});
						console.log(socket.id+" logged in as "+socket.login)
					}
				})
			}
		})

	});

	socket.on('req.eventlist', function(data){
		//var reqdata = JSON.parse(data)
		db.collection('events').find( {nickname : data}).toArray(function(err, answer){
			if (err){
				console.log(err);
				return;
			}
			//io.sockets.emit('ans.eventlist', JSON.stringify(answer));
			io.sockets.to(socket.currentuser).emit('ans.eventlist', JSON.stringify(answer))
		})

	});

	socket.on('req.addevent', function(data){
		let reqdata = JSON.parse(data);
		console.log('added');
		db.collection('events').insert(reqdata, function(err){
			if (err) {
				console.log(err);
			}
			let date = new Date();
			let tempnotif = {
				n_eventName: reqdata.eventname,
				n_eventDate: reqdata.date,
				n_author: reqdata.author,
				n_e_time: date.getHours()+':'+date.getMinutes()+' '+date.getDate()+'/'+date.getMonth()+'/'+date.getFullYear()
			};
			db.collection('users').updateOne({ login : reqdata.nickname}, { $addToSet : { 'notifications.eventNotifications' : tempnotif }}, function (err) {
				if (err)
					console.log(err)
			});
			db.collection('events').find( {nickname : reqdata.nickname}).toArray(function(err, answer){
				if (err){
					console.log(err);
					return;
				}
				io.sockets.to(socket.currentuser).emit('upd.eventlist', JSON.stringify(answer));
				io.sockets.to(socket.currentuser).emit('ans.eventlist', JSON.stringify(answer))
			})
		})

	});

	socket.on('req.delevent', function(data){
		let reqdata = JSON.parse(data);
		console.log('deleted');
		db.collection('events').remove(reqdata, function(err){
			if (err) {
				console.log(err);
			}
			db.collection('events').find( {nickname : reqdata.nickname}).toArray(function(err, answer){
				if (err){
					console.log(err);
					return;
				}
				io.sockets.to(socket.currentuser).emit('upd.eventlist', JSON.stringify(answer));
				io.sockets.to(socket.currentuser).emit('ans.eventlist', JSON.stringify(answer))
			})
		})

	});

	socket.on('req.editevent', function(data){
		let reqdata = JSON.parse(data);
		db.collection('events').update(reqdata[0], reqdata[1], function(err){
			if (err) {
				console.log(err);
			}
			db.collection('events').find( {nickname : reqdata[0].nickname}).toArray(function(err, answer){
				if (err){
					console.log(err);
					return;
				}
				io.sockets.to(socket.currentuser).emit('upd.eventlist', JSON.stringify(answer));
				io.sockets.to(socket.currentuser).emit('ans.eventlist', JSON.stringify(answer))
			})
		})

	});

	socket.on('req.addallowed', function(req) {
		let messagedata = JSON.parse(req);
		let data = messagedata.name;
		if (data == socket.login){
			socket.emit('upd.allowedlist', "SELF");
		}else {
			db.collection('users').findOne( { login : data} , function (err, answer) {
				if (err){
					console.log(err);
					return;
				}
				if (!answer){
					socket.emit('upd.allowedlist', "NOTFOUND");
				}else{
					db.collection('users').findOne( {login : socket.login, friends:{ $elemMatch: {name: data}}}, function (err, thisanswer) {
						if (err) {
							console.log(err);
							return;
						}
						if (!thisanswer){
							db.collection('users').update( { login : socket.login } , { $addToSet: { friends: messagedata } } , function (err){
								if (err) {
									console.log(err);
									return;
								}
								let date = new Date();
								let tempnotif = {
									n_username: socket.login,
									n_permission: messagedata.permission,
									n_p_time: date.getHours()+':'+date.getMinutes()+' '+date.getDate()+'/'+date.getMonth()+'/'+date.getFullYear()
								};
								db.collection('users').updateOne({ login : data}, { $addToSet : { 'notifications.permissionNotifications' : tempnotif }}, function (err) {
									if (err)
										console.log(err)
								});
								db.collection('users').findOne( {login : socket.login}, function(err, answer){
									if (err){
										console.log(err);
										return;
									}
									socket.emit('upd.allowedlist', JSON.stringify(answer.friends));
								})
							})
						}else{
							socket.emit('upd.allowedlist', "ALREADY");
						}
					})
				}
			})
		}
	});

	socket.on('req.allowedlist', function() {
		// if (!socket.login)
		// 	socket.login = "";
		db.collection('users').findOne( {login : socket.login}, function(err, answer){
			if (err){
				console.log(err);
				return;
			}
			if(!answer.friends){
				let temp = [{
					name : "Нет пользователей в списке",
					permission: "допущенных пользователей"
				}];
				socket.emit('upd.allowedlist', JSON.stringify(temp));
				return;
			}
			//console.log(JSON.stringify(answer.friends))
			if (answer.friends&&answer.friends.length!=0)
				socket.emit('upd.allowedlist', JSON.stringify(answer.friends));
			else{
				let temp = [{
					name : "Нет пользователей в списке",
					permission: "допущенных пользователей"
				}];
				socket.emit('upd.allowedlist', JSON.stringify(temp));
			}
		})
		//console.log(socket.login + " : " + data);
	});

	socket.on('req.delallowed', function(data) {
		// if (!socket.login)
		// 	socket.login = "";

		db.collection('users').update( { login : socket.login } , { $pull: { friends: {name : data} } } , function (err, result){
			if (err) {
				console.log(err);
				return;
			}
			db.collection('users').findOne( {login : socket.login}, function(err, answer){
				if (err){
					console.log(err);
					return;
				}
				//console.log(JSON.stringify(answer.friends))
				io.sockets.to("login"+data).emit('upd.permissiondrop',socket.login);
				if (answer.friends&&answer.friends.length!=0)
					socket.emit('upd.allowedlist', JSON.stringify(answer.friends));
				else{
					let temp = [{
						name : "Нет пользователей в списке",
						permission: "допущенных пользователей"
					}];
					socket.emit('upd.allowedlist', JSON.stringify(temp));
				}
			})
		})
		//console.log(socket.login + " : " + data);
	});

	socket.on('req.editallowed', function(req) {
		let messagedata = JSON.parse(req);
		let data = messagedata.name;

		db.collection('users').update( { login : socket.login, 'friends.name' : data } , { $set: { 'friends.$.permission' : messagedata.permission} } , function (err){
			if (err) {
				console.log(err);
				return;
			}
			db.collection('users').findOne( {login : socket.login}, function(err, answer){
				if (err){
					console.log(err);
					return;
				}
				//if (answer.friends&&answer.friends.length!=0)
				socket.emit('upd.allowedlist', JSON.stringify(answer.friends));
				let tempPermission = {
					name : socket.login,
					permission : messagedata.permission
				};
				io.sockets.to("login"+data).emit('upd.permission', JSON.stringify(tempPermission));
			})
		})
	});

	socket.on('req.friendslist', function() {
		db.collection('users').find( {friends : {$elemMatch : { name : socket.login}}}).toArray( function(err, answer){
			if (err){
				console.log(err);
				return;
			}
			let structAnswer = [];
			answer.forEach(function (value) {
				let tempAnswer = {
					name : value.login,
					permission : "",
					isOnline : value.isOnline
				};
				value.friends.forEach(function (person) {
					if (person.name == socket.login)
						tempAnswer.permission=person.permission;
				});
				structAnswer.push(tempAnswer);
			});
			//console.log(JSON.stringify(structAnswer))
			socket.emit('ans.friendslist', JSON.stringify(structAnswer));
		})
	});

	socket.on('req.changelogin', function (data) {
		socket.leave(socket.currentuser);
		socket.currentuser = data;
		socket.join(socket.currentuser)
	});

	socket.on('req.notificationslist', function () {
		db.collection('users').findOne({ login : socket.login }, function (err, ans) {
			if (err){
				console.log(err);
				return
			}
			if (!ans.notifications){
				return;
			}
			let arr = [];
			if (ans.notifications.eventNotifications){
				ans.notifications.eventNotifications.forEach(function (not) {
					let temp = {
						title: "Новое событие",
						author: not.n_author,
						description: "Когда добавлено: "+not.n_e_time+"\n"+"Имя события: "+not.n_eventName+"\n"+"Дата нового события события: "+not.n_eventDate
					};
					arr.push(temp)
				});
			}
			if (ans.notifications.permissionNotifications){
				ans.notifications.permissionNotifications.forEach(function (not) {
					let qwe ="";
					switch (not.n_permission) {
						case "FULL":
							qwe="Полный доступ";
							break;
						case "ADD":
							qwe="Только добавление";
							break;
						case "READ":
							qwe="Только просмотр";
							break;
					}
					let temp = {
						title: "Вам предоставили права доступа",
						author: not.n_username,
						description: "Когда произошло: "+not.n_p_time+"\n"+"Уровень доступа: "+qwe
					};
					arr.push(temp)
				});
			}

			socket.emit('ans.notificationslist', JSON.stringify(arr));
			//console.log(arr);
		})
	});


	socket.on('disconnect', function(){
		console.log(socket.id + ' disconnected');
		db.collection('users').updateOne( { login : socket.login }, { $set: { isOnline : false }}, function (err) {
			if (err){
				console.log(err);
			}
		});
	})
});


