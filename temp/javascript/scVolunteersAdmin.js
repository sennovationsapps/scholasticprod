
  //jQuery of dynamic elements is not working for me
  //$('.twipsies').on('click', '.removePrize', function() {
  	//alert("hello");
	    //event.preventDefault();
	  //$(this).parent().remove();
	  //renumber();
  //});
         
  $('.addShift').click(function(e) {
      var template = $('.shift_template');
      template.before('<div class="shift form-inline">' + template.html() + '</div>');
      renumber(e);
      linkDate();
      var shiftCount = $('.shift').length;
      var shiftCurrent = shiftCount - 1;
      var shiftPrev = shiftCurrent - 1;
      if(shiftCount >= 1) {
    	  $("input[name='shifts[" + shiftCurrent + "].endTime']").val($("input[name='shifts[" + shiftPrev + "].endTime']").val());
    	  $("input[name='shifts[" + shiftCurrent + "].startTime']").val($("input[name='shifts[" + shiftPrev + "].startTime']").val());
    	  $("input[name='shifts[" + shiftCurrent + "].date']").val($("input[name='shifts[" + shiftPrev + "].date']").val());
    	  $("input[name='shifts[" + shiftCurrent + "].name']").val($("input[name='shifts[" + shiftPrev + "].name']").val());
    	  $("input[name='shifts[" + shiftCurrent + "].description']").val($("input[name='shifts[" + shiftPrev + "].description']").val());
    	  $("input[name='shifts[" + shiftCurrent + "].volunteerCount']").val($("input[name='shifts[" + shiftPrev + "].volunteerCount']").val());
      }
  })
  
  $('#pageForm').submit(function() {
      $('.shift_template').remove();
  })
  
  // -- renumber fields
  
  // Rename fields to have a coherent payload like:
  //
  // informations[0].label
  // informations[0].email
  // informations[0].phones[0]
  // informations[0].phones[1]
  // ...
  //
  // This is probably not the easiest way to do it. A jQuery plugin would help.
  
  var renumber = function(shift) {
      $('.shift').each(function(i) {
          $('input', this).each(function() {
              $(this).attr('name', $(this).attr('name').replace(/shifts\[.+?\]/g, 'shifts[' + i + ']'))
          })
      })
  }

  $(document).on("click", ".removeShift", function(event){
  	event.preventDefault();
		$(this).parent().remove();
	  	renumber(event);
  });

  
  $( document ).ready(function() {
      linkDate();
    });

  var linkDate = function() {
	  jQuery("input[id$='_date']").datetimepicker({
		  timepicker:false,
		  format:'m/d/Y',
		  formatDate:'m/d/Y',
		  closeOnDateSelect:true,
		  lazyInit:true
		});
		jQuery("input[id$='_startTime']").datetimepicker({
			  datepicker:false,
			  format:'g:i A',
			  formatTime:'g:i A',
			  //hours12:true,
			  lazyInit:true,
			  step:5
		});
		jQuery("input[id$='_endTime']").datetimepicker({
			  datepicker:false,
			  format:'g:i A',
			  formatTime:'g:i A',
			  //hours12:true,
			  lazyInit:true,
			  step:5
			});

  }
  
  $(document).ready(function(){
  	$('#sampleTwo').collapse('hide');
  	$('#sampleThree').collapse('hide');
  	$('#sampleOne').collapse('hide');
  	$('#sampleOne').collapse('show');
  });
  $('#modalAccordion1').click(function(){
  	$('#sampleTwo').collapse('hide');
  	$('#sampleThree').collapse('hide');
  	$('#sampleOne').collapse('show');
  });
  $('#modalAccordion2').click(function(){
  	$('#sampleOne').collapse('hide');
  	$('#sampleThree').collapse('hide');
  	$('#sampleTwo').collapse('show');
  });
  $('#modalAccordion3').click(function(){
  	$('#sampleOne').collapse('hide');
  	$('#sampleTwo').collapse('hide');
  	$('#sampleThree').collapse('show');
  });
  $('#sample1').click(function(){
  	  $('textarea#content').code($('div#sampleOne>div.panel-body').html());
  	  $('#closeSample').click();
  });
  $('#sample2').click(function(){
  	  $('textarea#content').code($('div#sampleTwo>div.panel-body').html());
  	  $('#closeSample').click();
  });
  $('#sample3').click(function(){
  	  $('textarea#content').code($('div#sampleThree>div.panel-body').html());
  	  $('#closeSample').click();
  });