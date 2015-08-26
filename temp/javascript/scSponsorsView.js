$('.donationAmount').click(function(){
	 var x = $(this).val();
  $('input[name=amount]').val(x);
});

$('#payByCreditCard').click(function(){
	$('#payByCreditCardDiv').show();
	$('#payByCheckDiv').hide();
	$('input[name=payByCheck]').val('false');
	$('input[name=paymentType]').val('CREDIT');
});

$('#payByCreditCardInstead').click(function(){
	$('#payByCreditCardDiv').show();
	$('#payByCheckDiv').hide();
	$('input[name=payByCheck]').val('false');
	$('input[name=paymentType]').val('CREDIT');
});

$('#payByCheck').click(function(){
	$('#payByCheckDiv').show();
	$('#payByCreditCardDiv').hide();
	$('input[name=payByCheck]').val('true');
	$('input[name=paymentType]').val('CHECK');
});