$(document).ready(function(){
	// Activate tooltip
	$('[data-toggle="tooltip"]').tooltip();
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
                    +   '<a href="#deleteServiceModal" class="delete" data-toggle="modal"><i class="material-icons" data-toggle="tooltip" title="Delete">&#xE872;</i></a>'
                    + '</td>'
                  + '</tr>';
        }
        $( "#services" ).html(rows);
      }
    });
  }
  fetchServices();
});
