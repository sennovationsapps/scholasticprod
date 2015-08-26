$('.donationAmount').click(function(){
	 var x = $(this).val();
  $('input[name=amount]').val(x);
});
$('#payByCreditCard').click(function(){
	$('#payByCreditCardDiv').show();
	$('#payByCheckDiv').hide();
	$('#payByCashDiv').hide();
	$('input[name=payByCheck]').val('false');
	$('input[name=paymentType]').val('CREDIT');
});

$('#payByCreditCardInstead').click(function(){
	$('#payByCheckDiv').hide();
	$('#payByCreditCardDiv').show();
	$('#payByCashDiv').hide();
	$('input[name=payByCheck]').val('false');
	$('input[name=paymentType]').val('CREDIT');
});

$('#payByCheck').click(function(){
	$('#payByCheckDiv').show();
	$('#payByCreditCardDiv').hide();
	$('#payByCashDiv').hide();
	$('input[name=payByCheck]').val('true');
	$('input[name=paymentType]').val('CHECK');
});
$('#payByCash').click(function(){
	$('#payByCheckDiv').hide();
	$('#payByCreditCardDiv').hide();
	$('#payByCashDiv').show();
	$('input[name=payByCheck]').val('false');
	$('input[name=paymentType]').val('CASH');
});