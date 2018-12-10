var addService = function (data) {
  $.ajax({
    url: "/api/service",
    type: "POST",
    data: JSON.stringify(data),
    dataType: 'json',
    success: function( result ) {
      fetchServices();
    },
    error: function (err) {
      console.log(err);
    }
  });
}
var deleteService = function (id) {
  $.ajax({
    url: "/api/service/" + id,
    type: "DELETE",
    success: function( result ) {
      fetchServices();
    },
    error: function (err) {
      console.log(err);
    }
  });
}
var bindFunction = function () {
  $('#addButton').click(function () {
    var data = {
      url: $('#url').val()
    };
    addService(data);
  });
  $('.delete').click(function () {
    alert($(this).data('serviceId'));
    deleteService($(this).data('serviceId'));
  });
}
var fetchServices = function () {
  $.ajax({
    url: "/api/service",
    success: function( result ) {
      var rows = '';
      for (i = 0; i < result.length; i++) {
        rows += '<tr>'
                  + '<td>' + result[i].id + '</td>'
                  + '<td>' + result[i].url + '</td>'
                  + '<td>' + result[i].status + '</td>'
                  + '<td>' + result[i].lastCheckedAt + '</td>'
                  + '<td>'
                  +   '<button onclick="deleteService( ' + result[i].id + ' )" class="delete"><i class="material-icons" data-toggle="tooltip" title="Delete">&#xE872;</i></button>'
                  + '</td>'
                + '</tr>';
      }
      $( "#services" ).html(rows);
    }
  });
}

$(document).ready(function(){
	// Activate tooltip
	$('[data-toggle="tooltip"]').tooltip();
  fetchServices();
  bindFunction();
});
