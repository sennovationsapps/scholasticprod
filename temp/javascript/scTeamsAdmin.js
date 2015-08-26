$('#saveAndAddBtn').click(function(){
	  $('#saveAndAdd').val('true');
	  $('form[id=pageForm]').submit();
	});

$('input[name=name]').change(function() {
    // if changed to, for example, the last option, then
    // $(this).find('option:selected').text() == D
    // $(this).val() == 4
    // get whatever value you want into a variable
    var x = $(this).val();
    // and update the hidden input's value
    $('input[name=title]').val("Welcome to the fundraising page for " + x);
});



$(document).ready(function(){
	$('#sampleTwo').collapse('hide');
	$('#sampleThree').collapse('hide');
	$('#sampleOne').collapse('hide');
	$('#sampleFour').collapse('hide');
	$('#sampleOne').collapse('show');
});
$('#modalAccordion1').click(function(){
	$('#sampleTwo').collapse('hide');
	$('#sampleThree').collapse('hide');
	$('#sampleFour').collapse('hide');
	$('#sampleOne').collapse('show');
});
$('#modalAccordion2').click(function(){
	$('#sampleOne').collapse('hide');
	$('#sampleThree').collapse('hide');
	$('#sampleFour').collapse('hide');
	$('#sampleTwo').collapse('show');
});
$('#modalAccordion3').click(function(){
	$('#sampleOne').collapse('hide');
	$('#sampleTwo').collapse('hide');
	$('#sampleFour').collapse('hide');
	$('#sampleThree').collapse('show');
});
$('#modalAccordion4').click(function(){
	$('#sampleOne').collapse('hide');
	$('#sampleTwo').collapse('hide');
	$('#sampleThree').collapse('hide');
	$('#sampleFour').collapse('show');
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
$('#sample4').click(function(){
	  $('textarea#content').code($('div#sampleFour>div.panel-body').html());
	  $('#closeSample').click();
});