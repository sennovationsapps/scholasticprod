$('#cancelVolunteer').click(function(){
	  $('#closeVolunteer').click();
});

function submitPfpForm() {
    alert('within submit pfp form')
    $.ajax({
        url:  jQuery('#pfpForm').attr('action'),
        type: jQuery('#pfpForm').attr('method'),
        data: jQuery('#pfpForm').serialize(),
        dataType : 'html',
        success: function (response) {
          $('#volunteerModalBody').empty();
          $('#volunteerModal').modal('hide');
          location.reload();
        },
        error: function (xhr, ajaxOptions, thrownError) {
          $('#volunteerModalBody').empty();
          $('#volunteerModalBody').html(xhr.responseText);
          $('#volunteerModalBody').scrollTop(0);
        }
    });
}