	var selDiv = "";
	var html ;
	var i=0;
	var jcrop_api;
	var cropImageWidth=400;
	var cropImageHeight=400;
	var canvasCropImageWidth=370;
    var canvasCropImageHeight=370;

	
	document.addEventListener("DOMContentLoaded", init, false);
	
	function init() {
		document.querySelector('#files').addEventListener('change', handleFileSelect, false);
		selDiv = document.querySelector("#selectedFiles");
	}
	
	
	function deleteImage(index){
		$('#ImgTumbNailDiv'+index).remove();
	}
	
	
	function handleFileSelect(e) {
		if(!e.target.files || !window.FileReader) return;
		var files = e.target.files;
		var filesArr = Array.prototype.slice.call(files);
		
		filesArr.forEach(function(f) {
			if(!f.type.match("image.*")) {
				return;
			}
			var reader = new FileReader();
			reader.onload = function (e) {
			if($(".img_thumbnail")!=undefined){
		//alert($(".img_thumbnail").length);
			if($(".img_thumbnail").length<5){
				html= "<div class='img_thumbnail' id='ImgTumbNailDiv" +i +"'><img id='imgupload" +i +"' src=\"" + e.target.result + "\">" + "<br clear='all'/><input type='button' class='icons icon-edit' title='Edit Image' value='Edit Image' onclick='editimage(\"" +i + "\")'\"><input type='button' class='icons icon-delete' title='Delete Image' value='Delete Image' onclick='deleteImage(\"" +i + "\")'\"></div>";
				selDiv.innerHTML += html;	
				i=i+1;
				}else{
					alert("Max uploaded image file is 5");
				}
				}else{
				if(i<5){
				html= "<div class='img_thumbnail' id='ImgTumbNailDiv" +i +"'><img id='imgupload" +i +"' src=\"" + e.target.result + "\">" + "<br clear='all'/><input type='button' class='icons icon-edit' title='Edit Image' value='Edit Image' onclick='editimage(\"" +i + "\")'\"><input type='button' class='icons icon-delete' title='Delete Image' value='Delete Image' onclick='deleteImage(\"" +i + "\")'\"></div>";
				selDiv.innerHTML += html;	
				i=i+1;
				}else{
					alert("Max uploaded image file is 5");
				}
				}
			}
			reader.readAsDataURL(f); 
			$('#FinalUploadButton').css('display','block');
		});
	}
	
	
	function initJcrop(val){
			jcrop_api=null;
			jcrop_api=null;
            			$("#cropbox1").attr("src",val);
            			jcrop_api = $.Jcrop('#cropbox1');

            var val1=$("#cropbox1").attr('src');
            jcrop_api.setOptions({
            										onChange: showCoords,
            										onSelect: showCoords
            										});
		/*jcrop_api=	$('#cropbox1').Jcrop({aspectRatio: 3/2,
                                          					onChange: showCoords,
                                          					onSelect: showCoords,
                                          					boxWidth:900},function(){
				jcrop_api = this;
				jcrop_api.animateTo([0,0,100,100]);
				jcrop_api.setImage(val,function(){
					this.setOptions({
					aspectRatio: 3/2,
					onChange: showCoords,
					onSelect: showCoords,
					boxWidth:900
				});
				});
			});*/
	}
				
				
    function showCoords(c) {
      // variables can be accessed here as
      // c.x, c.y, c.x2, c.y2, c.w, c.h
      showPreview(c);
			document.getElementById('x').value=c.x;
			document.getElementById('y').value=c.y;
			document.getElementById('w').value=c.w;
			document.getElementById('h').value=c.h;
	 
    };
	
	
	function saveCropImage(val){
		  //alert($("#imgupload"+val).attr('src'));
		  if($("#imgupload"+val).attr('src')!=null && $("#imgupload"+val).attr('src')!=undefined){
				var sourceImage=$("#ActCropImage").attr('src');
				$("#imgupload"+val).attr('src',sourceImage);
$('#myModal').modal('hide');
				closePopUp();
		  }else{
				alert("Image not found");
				$('#myModal').modal('hide');
				closePopUp();
		  }
	 }
  
  
  
	
	
var Base64Binary = {
		_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
		
		/* will return a  Uint8Array type */
		decodeArrayBuffer: function(input) {
			var bytes = (input.length/4) * 3;
			var ab = new ArrayBuffer(bytes);
			this.decode(input, ab);
			
			return ab;
		},

		removePaddingChars: function(input){
			var lkey = this._keyStr.indexOf(input.charAt(input.length - 1));
			if(lkey == 64){
				return input.substring(0,input.length - 1);
			}
			return input;
		},

		    decode: function (input, arrayBuffer) {
			//get last chars to see if are valid
			input = this.removePaddingChars(input);
			input = this.removePaddingChars(input);

			var bytes = parseInt((input.length / 4) * 3, 10);
			
			var uarray;
			var chr1, chr2, chr3;
			var enc1, enc2, enc3, enc4;
			var i = 0;
			var j = 0;
			
			if (arrayBuffer)
				uarray = new Uint8Array(arrayBuffer);
			else
				uarray = new Uint8Array(bytes);
			
			input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
			
			for (i=0; i<bytes; i+=3) {	
				//get the 3 octects in 4 ascii chars
				enc1 = this._keyStr.indexOf(input.charAt(j++));
				enc2 = this._keyStr.indexOf(input.charAt(j++));
				enc3 = this._keyStr.indexOf(input.charAt(j++));
				enc4 = this._keyStr.indexOf(input.charAt(j++));
		
				chr1 = (enc1 << 2) | (enc2 >> 4);
				chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
				chr3 = ((enc3 & 3) << 6) | enc4;
		
				uarray[i] = chr1;			
				if (enc3 != 64) uarray[i+1] = chr2;
				if (enc4 != 64) uarray[i+2] = chr3;
			}
		
			return uarray;	
		}
	}

var img;
var imageObj;
var sourceX ;
var sourceY ;
var sourceWidth ;
var sourceHeight ;
var destWidth ;
var destHeight ;
var destX  ;
var destY  ;
var imgSrc;
var image = new Image();



function cropimage(val){
			//alert("val"+val);

            $("#CanvasDiv").attr("style","display:block;");

			imgSrc= document.getElementById('cropbox1').src;
			var left=document.getElementById('x').value;
			var top=document.getElementById('y').value;
			var width=document.getElementById('w').value;
			var height=document.getElementById('h').value;
			var naturalWidth=document.getElementById('cropbox1').naturalWidth;
            var naturalHeight=document.getElementById('cropbox1').naturalHeight;
            var width1=document.getElementById('cropbox1').width;
            var height1=document.getElementById('cropbox1').height;
            var w=naturalWidth/width1;
            var h=naturalHeight/height1;
	        if(width!=null && width!=0 && height!=null && height!=0){
				  var canvas = document.getElementById('myCanvas');
				  var context = canvas.getContext('2d');
				  canvas.width=canvasCropImageWidth;
				  canvas.height=canvasCropImageHeight;
				  context.clearRect(0,0,canvas.width,canvas.height);
				  imageObj = new Image();

				  imageObj.onload = function() {
					   sourceX = left*w;
                       sourceY = top*h;
                       sourceWidth = width*w;
                       sourceHeight = height*h;
					   destWidth = sourceWidth;
					   destHeight = sourceHeight;
					   destX = canvas.width / 2 - destWidth / 2;
					   destY = canvas.height / 2 - destHeight / 2;
					 
					   context.drawImage(imageObj, sourceX, sourceY, sourceWidth, sourceHeight, 0, 0, canvasCropImageWidth, canvasCropImageHeight);
					
					   var canvas1 = document.getElementById('myCanvas');
					   var url = canvas1.toDataURL();
					   var dataURL = canvas1.toDataURL("image/png");
					   var imageData = dataURL.replace(/data:image\/png;base64,/,'');
					  // image.src="";
					  // image.src = url;
					  // image.id="ActCropImage";
					 //  image.style.display="none";
					 $("#ActCropImage").attr('src','');
					 $("#ActCropImage").attr('src',url);
					  // document.body.appendChild(image);
				  };
				  
				  imageObj.src = imgSrc;
				  imageObj.id="cropImage";
				  imageObj.width =canvasCropImageWidth;
				 imageObj.height = canvasCropImageHeight;
				  $("#croppedImageText").hide();
			 
				var uintArray = Base64Binary.decode(imageData);
				var contents=0;
			   for(var j = 0; j <= uintArray.length-1 ; j++){
				  contents+=uintArray[j];
			   }
			   console.log(contents);

   }else{
                alert("Please select area to crop");
   }
}

 var $preview = $('#preview');
  // Our simple event handler, called from onChange and onSelect
  // event handlers, as per the Jcrop invocation above
  function showPreview(coords)
  {
    if (parseInt(coords.w) > 0)
    {
      var rx = 100 / coords.w;
      var ry = 100 / coords.h;

      $preview.css({
        width: Math.round(rx * 500) + 'px',
        height: Math.round(ry * 370) + 'px',
        marginLeft: '-' + Math.round(rx * coords.x) + 'px',
        marginTop: '-' + Math.round(ry * coords.y) + 'px'
      }).show();
    }
  }
var rotateRight=0;

function rotateImage(degree){
$("#croppedImageText").show();
if(degree==1)
{
if(rotateRight<270){
rotateRight=rotateRight+90;
}else{
rotateRight=0;
}
}else if(degree==2){
if(rotateRight>0){
rotateRight=rotateRight-90;
}else{
rotateRight=270;
}
}else{
rotateRight=0;
}
var myCanvas = document.getElementById('myCanvas');
					var myCanvascontext = myCanvas.getContext('2d');
					myCanvascontext.clearRect(0,0,1000,1000);


	   var img = document.getElementById('getRotatedImage');
	    var canvas=document.getElementById('hideCanvas');
	
        if(document.getElementById('hideCanvas')){
           var cContext = canvas.getContext('2d');
           var cw = img.width, ch = img.height, cx = 0, cy = 0;
           
           //   Calculate new canvas size and x/y coorditates for image
           switch(rotateRight){
                case 90:
                    cw = img.height;
                    ch = img.width;
                    cy = img.height * (-1);
                    break;
                case 180:
                    cx = img.width * (-1);
                    cy = img.height * (-1);
                    break;
                case 270:
                    cw = img.height;
                    ch = img.width;
                    cx = img.width * (-1);
                    break;
           }

            //  Rotate image            
			canvas.setAttribute('width', cw);
			canvas.setAttribute('height', ch);
			cContext.rotate(rotateRight * Math.PI / 180);
			cContext.drawImage(img, cx, cy,cropImageWidth,cropImageHeight);
			
			var canvas1 = document.getElementById('hideCanvas');
			var url = canvas1.toDataURL();
			$("#ActCropImage").attr('src',url);
			$("#cropbox1").attr('src',url);
			if(jcrop_api!=null ){
            					jcrop_api.destroy();
            					initJcrop(url);
            				}else{
            					jcrop_api = $.Jcrop('#cropbox1');

            var val1=$("#cropbox1").attr('src');
            jcrop_api.setOptions({
            										onChange: showCoords,
            										onSelect: showCoords
            										});
            										}
			/*if(jcrop_api!=null){
					jcrop_api.destroy();
					initJcrop(url);
				}else{
					jcrop_api = $.Jcrop('#cropbox1');
					jcrop_api.setImage(url,function(){
										this.setOptions({
										aspectRatio: 3/2,
										onChange: showCoords,
										onSelect: showCoords,
										boxWidth:900
									});
									});
					
				}
				jcrop_api.setImage(url);*/
        } else {
            //  Use DXImageTransform.Microsoft.BasicImage filter for MSIE
            switch(rotateRight){
                case 0: image.style.filter = 'progid:DXImageTransform.Microsoft.BasicImage(rotation=0)'; break;
                case 90: image.style.filter = 'progid:DXImageTransform.Microsoft.BasicImage(rotation=1)'; break;
                case 180: image.style.filter = 'progid:DXImageTransform.Microsoft.BasicImage(rotation=2)'; break;
                case 270: image.style.filter = 'progid:DXImageTransform.Microsoft.BasicImage(rotation=3)'; break;
            }
        }
	}
	
	function openPopup()
	{

$(".popUpDiv").css('display','block');
$("body").addClass('overlay');
	}
	
	
	function closePopUp()
		{
		$(".popUpDiv").css('display','none');
		$("body").removeClass('overlay');
		}
	
	
function editimage(value){
$("#croppedImageText").show();
rotateRight=0;

   //$('#myCanvas').hide();
    var myCanvas = document.getElementById('myCanvas');
					var myCanvascontext = myCanvas.getContext('2d');
					myCanvascontext.clearRect(0,0,1000,1000);
	//openPopup();
	$('#myModal').modal('show');
				var myBtn = document.getElementById('ImgeEditButton');
				myBtn.innerHTML='';
				var html="<div class='image-btn-set '>"
				var html=html+"<input type='button' value='Crop Image' class='icons icon-crop' id='cropImageButton' onclick='cropimage(\"" +value + "\")'/>	<input type='button' value='Save Image' class='icons icon-save' id='saveImageButton' onclick='saveCropImage(\"" +value + "\")'/>";
					 
					 html=html+"<a  class='icons icon-reset' id='resetImage' onclick='rotateImage(0);'>Reset Image</a>"+
								"<a  class='icons icon-90' id='left' onclick='rotateImage(1);'>left</a>"+
                                "<a  class='icons icon-180' id='right' onclick='rotateImage(2);'>right</a>"+
								"</div>";
				myBtn.innerHTML = html;
				
				$("#ImgeEditButton").attr("style","display:block;");
				var val=$("#imgupload"+value).attr('src');
				
				var orgImage=document.getElementById("imgupload"+value);
				var height=orgImage.naturalHeight;
				var width=orgImage.naturalWidth;
				
				
				
				$("#cropbox1").attr("src",val);
				if(jcrop_api!=null  ){
                					jcrop_api.destroy();
                					initJcrop(val);
                				}else{
                					jcrop_api = $.Jcrop('#cropbox1');

                var val1=$("#cropbox1").attr('src');
                jcrop_api.setOptions({
                										onChange: showCoords,
                										onSelect: showCoords
                										});

                				}
				
				/*if(jcrop_api!=null){
					jcrop_api.destroy();
					initJcrop(val);

				}else{
					jcrop_api = $.Jcrop('#cropbox1');
					jcrop_api.setImage(val,function(){
										this.setOptions({
										aspectRatio: 3/2,
										onChange: showCoords,
										onSelect: showCoords,
boxWidth:900,
trueSize: [900,0],
                                                    minSize:[900,0],
                                                    allowResize:false



									});
									});
					
				}
*/

$("#ActCropImage").attr('src',val);
$("#getRotatedImage").attr('src',val);
				    var canvas = document.getElementById('hideCanvas');
					var context = canvas.getContext('2d');
					context.clearRect(0,0,1000,1000);
					var imageObj = new Image();
					imageObj.src=val;
					context.drawImage(imageObj,0,0,orgImage.naturalWidth,orgImage.naturalHeight);
					var canvas1 = document.getElementById('hideCanvas');
			    var url = canvas1.toDataURL();
				//alert("url"+url);					
				/*jcrop_api.setImage(val,function(){
                                       										this.setOptions({
                                       										aspectRatio: 3/2,
                                       										onChange: showCoords,
                                       										onSelect: showCoords,
                                       boxWidth:900,
                                       trueSize: [900,0],
                                       minSize:[900,0],
                                       allowResize:false



                                       									});
                                       									});*/
				
				
				
				
	    }
	function finalUpload(){
		$("#imgUrlText0").val('');
		$("#imgUrlText1").val('');
		$("#imgUrlText2").val('');
		$("#imgUrlText3").val('');
		$("#imgUrlText4").val('');

		$("#imgUrlText01").val('');
		$("#imgUrlText02").val('');
		$("#imgUrlText03").val('');
		$("#imgUrlText04").val('');
		$("#imgUrlText05").val('');


		$("#imgUrlText11").val('');
		$("#imgUrlText12").val('');
		$("#imgUrlText13").val('');
		$("#imgUrlText14").val('');
		$("#imgUrlText15").val('');

		$("#imgUrlText21").val('');
		$("#imgUrlText22").val('');
		$("#imgUrlText23").val('');
		$("#imgUrlText24").val('');
		$("#imgUrlText25").val('');

		$("#imgUrlText31").val('');
		$("#imgUrlText32").val('');
		$("#imgUrlText33").val('');
		$("#imgUrlText34").val('');
		$("#imgUrlText35").val('');

		$("#imgUrlText41").val('');
		$("#imgUrlText42").val('');
		$("#imgUrlText43").val('');
		$("#imgUrlText44").val('');
		$("#imgUrlText45").val('');
		var z=i;
		var a=0;
		for(var x=0;x<z;x++){
			if($("#imgupload"+x).attr('src')!=null && $("#imgupload"+x).attr('src')!=undefined){


				var my_colors=$("#imgupload"+x).attr('src');
				if ((my_colors=="") && (my_colors==null))
				{
					// alert("You have no favorite colors? Bummer.");
				}

				else
				{
					var part_num=0;
					var str1;
					var str2;
					var str3;
					var str4;
					var div = Math.floor(my_colors.length/5);
					var rem = my_colors.length % 5;
					var len1=div;
					var len2=len1*2;
					var len3=len1*3;
					var len4=len1*4;
					var len5 = len1 * 5;


					$("#imgUrlText"+a+"1").val(my_colors.substring(0,len1));
					$("#imgUrlText"+a+"2").val(my_colors.substring((len1),len2));
					$("#imgUrlText"+a+"3").val(my_colors.substring((len2),len3));
					$("#imgUrlText"+a+"4").val(my_colors.substring((len3),len4));
					$("#imgUrlText"+a+"5").val(my_colors.substring((len4), my_colors.length));

				}
				a=a+1;
			}

		}


	}
	/*function finalUpload() {
		$("#imgUrlText0").val('');
		$("#imgUrlText1").val('');
		$("#imgUrlText2").val('');
		$("#imgUrlText3").val('');
		$("#imgUrlText4").val('');
		$("#imgUrlText01").val('');
		$("#imgUrlText02").val('');
		$("#imgUrlText03").val('');
		$("#imgUrlText04").val('');
		$("#imgUrlText05").val('');

		var z = i;
		var a = 0;
		for (var x = 0; x < z; x++) {
			if ($("#imgupload" + x).attr('src') != null && $("#imgupload" + x).attr('src') != undefined) {
			var my_colors = $("#imgupload" + x).attr('src');
				if ((my_colors == "") && (my_colors == null)) {
					// alert("You have no favorite colors? Bummer.");
				}

				else {
					var part_num = 0;
					var str1;
					var str2;
					var str3;
					var str4;
					var div = Math.floor(my_colors.length / 5);
					var rem = my_colors.length % 5;
					var len1 = div;
					var len2 = len1 * 2;
					var len3 = len1 * 3;
					var len4 = len1 * 4;
					var len5 = len1 * 5;

					$("#imgUrlText" + a + "1").val(my_colors.substring(0, len1));
					$("#imgUrlText" + a + "2").val(my_colors.substring((len1), len2));
					$("#imgUrlText" + a + "3").val(my_colors.substring((len2), len3));
					$("#imgUrlText" + a + "4").val(my_colors.substring((len3), len4));
					$("#imgUrlText" + a + "5").val(my_colors.substring((len4), my_colors.length));
					alert("kkk1=> "+document.getElementById("imgUrlText01").value);
				}
				a = a + 1;
			}

		}
	}*/
/*
function finalUpload(){
$("#imgUrlText0").val('');
$("#imgUrlText1").val('');
$("#imgUrlText2").val('');
$("#imgUrlText3").val('');
$("#imgUrlText4").val('');
var z=i;
var a=0;
for(var x=0;x<z;x++){
 if($("#imgupload"+x).attr('src')!=null && $("#imgupload"+x).attr('src')!=undefined){

    alert("fffff");
	   var val=$("#imgupload"+x).attr('src');
	   $("#imgUrlText"+a).val(val);
	   alert("kkk1=> "+document.getElementById("imgUrlText"+a).value);
a=a+1;
	 }

}


  }*/
