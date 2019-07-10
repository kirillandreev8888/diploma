let socket = io.connect('http://192.168.1.68:3000');

$(function () {

    function getWindow(id) { //pastable to append
        return document.querySelector('link[rel=import]').import.querySelector(id).cloneNode(true);
    }

    //code

    $('#root').append(getWindow('#windowLogin'));

   $('#confirmLogin').click(function () {
       let data = {
           login : $('#login').val(),
           password : $('#password').val()
       };
       socket.emit('req.signin', JSON.stringify(data));
   });


    socket.on('ans.signin', function (data) {
        if (data=='CORRECT'){
            $('#root').empty();
        }
    })
});

