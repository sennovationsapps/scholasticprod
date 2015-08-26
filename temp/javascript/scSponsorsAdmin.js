
        //jQuery of dynamic elements is not working for me
        //$('.twipsies').on('click', '.removePrize', function() {
        	//alert("hello");
		    //event.preventDefault();
		  //$(this).parent().remove();
		  //renumber();
        //});
               
        $('.addSponsorItem').click(function(e) {
            var template = $('.sponsoritem_template');
            template.before('<div class="sponsoritem form-inline">' + template.html() + '</div>');
            renumber(e);
        })
        
        $('#pageForm').submit(function() {
            $('.sponsoritem_template').remove();
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
        
        var renumber = function(sponsoritem) {
            $('.sponsoritem').each(function(i) {
                $('input', this).each(function() {
                    $(this).attr('name', $(this).attr('name').replace(/sponsoritems\[.+?\]/g, 'sponsoritems[' + i + ']'))
                })
            })
        }

        $(document).on("click", ".removeSponsorItem", function(event){
        	event.preventDefault();
  			$(this).parent().remove();
  		  	renumber(event);
        });

        
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