<%@ include file="/WEB-INF/views/search/_overlay.jsp" %>
<%@ include file="/WEB-INF/views/search/temporal_widget_dialog.jsp" %>
<%@ include file="/WEB-INF/views/search/geospatial_widget_dialog.jsp" %>
<%@ include file="/WEB-INF/views/search/facetsidebar_widget_dialog.jsp" %>
<%-- 
<%@ include file="/WEB-INF/views/search/_select_tbl.jsp" %>
--%>

<style>
.distribbutton {
	font-size: 13px;
	color: white;
	border: 1px solid #9c9c9c;
	background: #838943;
	cursor: pointer;
}


.modal {
	background-color:#fff;
  	display:none;
  	width:175px;
  	padding:15px;
  	text-align:left;
  	border:2px solid #333;

  	opacity:0.9;
  	-moz-border-radius:6px;
  	-webkit-border-radius:6px;
  	-moz-box-shadow: 0 0 50px #ccc;
  	-webkit-box-shadow: 0 0 50px #ccc;
}

.modal h2 {
    margin:0px;
    padding:10px 0 10px 45px;
    font-size:20px;
}


.tip{
 display:none;
 background-color:whitesmoke; 
 width: 100px;
 height: 50px; 
}




</style>



<div class="span-18 last" >
        <div class="span-15">
	       <span id="search-box">
	               <input id="query" name="text" type="text" value="" alt="query text" />
	       </span>
	       <input id="search-button" type="submit" value="Search" alt="search submit" />
	       <br/>&nbsp; &nbsp;
	       Examples: <i>temperature</i>, <i>"surface temperature"</i>, <i>climate AND project:CMIP5 AND variable:hus</i>.
	       <br/>&nbsp; &nbsp;
	       To download data: add datasets to your Data Cart, then click on <i>Expand</i> or <i>wget</i>.
        </div>
	    <div class="span-3 last" style="margin-top:5px">
			<div id="temporal"><a href="#" id="temporal" style="font-size:10px">Temporal Search</a></div>
			<!--  
			<div id="geo"><a href="#" id="geospatial" style="font-size:10px">Geospatial Search</a></div>
<<<<<<< HEAD
			<div id="initializeSRM"><a href="#" id="initializeSRM" style="font-size:10px">Initialize SRM</a></div>
=======
			-->
			<div id="clearc"><a href="#" id="clearcache" style="font-size:10px">Clear search constraints and datacart</a></div>
			<div id="search_help"><a href="http://devel.esgf.org/wiki/ESGF_Web_Search_User_Guide" style="font-size:10px" >Search Help</a></div>
	    	<div id="search_cv"><a href="http://devel.esgf.org/wiki/ESGF_Search_CV" style="font-size:10px" >Search Controlled Vocabulary</a></div>
	    </div>
	  
		
		
		
</div>

<div class="span-18 last" style="margin-bottom:15px"> 
		<div class="span-12 last">
			
			<!--   <label style="display: block;float: left;padding-right: 10px;white-space: nowrap;"> -->
				<input type="checkbox" alt="distrib checkbox" class="distribcheckbox1" id="distribcheckbox" style="vertical-align: middle;margin-left:10px;padding-bottom:10px"/><span style="font-weight:bold;padding-top:10px;vertical-align: middle;">Search All Sites</span>
				<input type="checkbox" alt="replica checkbox" class="replicacheckbox1" id="replicacheckbox" style="vertical-align: middle;margin-left:10px;padding-bottom:10px"/> <span style="font-weight:bold;padding-top:10px;vertical-align: middle;">Show All Replicas</span>
				<input type="checkbox" alt="versions checkbox" class="versioncheckbox1" id="versioncheckbox" style="vertical-align: middle;margin-left:10px;padding-bottom:10px"/> <span style="font-weight:bold;padding-top:10px;vertical-align: middle;">Show All Versions</span>
				
		</div>
		
		
<!--  	
		<table id='gravity' cellspacing='5'>
  <tr>
    <td>
      <a id='north-west' href='#' title='This is an example of north-west gravity'>Northwest</a>
    </td>
    <td>
      <a id='north' href='#' title='This is an example of north gravity'>North</a>
    </td>
    <td>
      <a id='north-east' href='#' title='This is an example of north-east gravity'>Northeast</a>
    </td>
  </tr>
</table>
-->	

</div>

<div class="span-20 last" id="search-summary" style="margin-top:0px;">
		
		<div class="span-18 last">
			<ul id="pager"></ul>
		    <div id="pager-header"> </div>
		</div>
		
		<!-- old -->    	  
		    <!-- <div class="span-7" id="search-speed">   -->
		    	<!--<div id="search-help" style="margin-left:15px;">(press ESC to close suggestions)</div>-->
			<!-- </div> -->
		    
		
			
</div>  



<div class="span-20 last" id="search-summary" style="margin-top:5px;">

	<div class="prepend-1 span-14 last">
	Display
			<select class="datasetCounter" name="datasetC" style="display:inline">
				<option id="" value="5">5</option>
				<option id="" value="10" selected="selected">10</option>
				<option id="" value="25">25</option>
				<option id="" value="50">50</option>
				<option id="" value="100">100</option>
				<option id="" value="200">200</option>
				<option id="" value="500">500</option>
			</select>
	datasets per page
	
		    </div>
	<div class="prepend-1 span-14 last">
		<a id="add_all" style="margin-left:20px;cursor:pointer">Add All Displayed to Datacart</a>
		<a id="remove_all" style="margin-left:20px;cursor:pointer">Remove All Displayed from Datacart</a>
	
	</div>
</div>


	
	
	
	
<div class="span-18 last" >


	<div id="myTabs" class="l2contentrightbox">
	
	    <ul>
	
	    <li><a href="#search-results"> Results</a></li>
	
	    <li><a href="#carts"> Data Cart </a></li>
	
	    </ul>
	
	    <div id="search-results"> </div>
	
	 
	    <div id="carts"> 
	    <!-- 
	    <table style="width:100%;table-layout: fixed">
	    <tbody id="datasetList">
	
	    </tbody>
	    </table>
	    -->
	    </div>
	</div>
</div>
 
<!-- user input dialog -->
<div class="modal" id="prompt" style="background-color:#ffffff;font-style:bold">
  <img src='/esgf-web-fe/images/ajax-loader.gif' style="float:left;margin-right:20px" alt="loader" /> 
  <div style="padding-left:20px">Processing...</div>

  <!--  <button class="f">Close</button>  -->
</div>
 

<script type="text/javascript">

    $(function(){
    	

  	  $("#openoverlay").click(function() {
  		  //alert('click');
  	      $("#prompt").overlay().load();
  	  });
  	  
  	  
  	  /*var triggers =*/ 
  	  $("#prompt").overlay({

 		 top: '40%',
 		 mask: '#FFFFFF',
 		 speed: 'slow',
 	     closeOnClick: false
 	  	
  	  });
 	  
  		$('a#initializeSRM').click(function() {
  			
  			
  			var initialize_url = '/esgf-web-fe/initializeSRMEntryList';
  			
  			$.ajax({
				url: initialize_url,
				global: false,
				type: "POST",
				//data: queryStr,
				//dataType: 'xml',
				success: function(data) {
					alert(data);
					//alert('Your data has been staged and an email has been sent to your account.  Please follow the instructions included.');
					// $('#srm_response').append("Staging successfully launched"); 
				},
				error: function (request, status, error) {
			        //alert("SRM Request error: " + request.responseText);
				}
				
	    	});
  			
  			
  		});
    	
    	//event is trigger on both logout and login links (for now)
    	//there is a little disconnect with the header.jsp file so this can be seen as a temporary fix until a main page clean up is performed 
    	$('li.resetLocalStorage').live('click', function() {
    		//reset the localStorage to querying over the dataset type
      	  	ESGF.localStorage.removeAll('esgf_fq');
      	  
      	  	//reset the contents of the datacart
      	  	ESGF.localStorage.removeAll('dataCart');
      	  	

      	  	//reset the contents of the datacart
      	  	ESGF.localStorage.removeAll('esgf_queryString');

      	  	//reset the contents of the GO_Credential 
      	  	ESGF.localStorage.removeAll('GO_Credential');
      	  	
      	  	
      	  	
    	});
    	
    	
    	$('li#showConstraints').live('click',function () {
    		var text = $(this).html();
    		var constraintsText = '';
    		if(text === 'Show more constraints') {
    			constraintsText = 'Hide more constraints';
    		} else {
    			constraintsText = 'Show more constraints';
    		}
			$('li#showConstraints').html(constraintsText);
			$('fieldset#showConstraintsBox').toggle('slow');
    		
    	});
    	
    	//$('#myform input[type=checkbox]:checked')
    	$('#constraintChoices input[type=checkbox]').live('click', function () {
    		//alert($('form#my').html());
    		/*
    		if ($('input#showConstraints').attr('checked'))
    		{
        		alert('show constraints');
    		}
    		else
    		{
        		alert('hide constraints');
    		}
    		*/
		});
    	
    	
    	$('#myTabs').tabs();
        
    	
    	var urlParams = ESGF.datacart.getURLParams();
		
    	if(urlParams['tab'] == undefined) {
    		$('#myTabs').tabs('select' , 0);
    	} else if(urlParams['tab'] == 'datacart') {
    		$('#myTabs').tabs('select' , 1);
    	} else {
    		$('#myTabs').tabs('select' , 0);
    	}
    	
    	
    	
        /*Facet overlay
        */
      //scroll wheel for facet overlay 
        $(".scrollable").scrollable({ vertical: true, mousewheel: true });

        $("div#facet_browser_overlay a[rel]").overlay({
        		 
        		mask: {opacity: 0.5, color: '#000'},
        		effect: 'apple',
        		left: "30%",
        		top: "2%",
        		
        		onBeforeLoad: function() {
        		
        			$('.apple_overlay').css({'width' : '700px'});
        		},

        		onLoad: function() {
        			 //radio buttons for sorting facets 
        		    $("#facetSort").buttonset();
        			$(".overlay_header").show();
        			$(".overlay_content").show();
        			$(".overlay_footer").show();
        			$(".overlay_border").show();
        	},
        	
        	onClose: function() {
        			$(".overlay_header").hide();
        			$(".overlay_content").hide();
        			$(".overlay_footer").hide();
        			$(".overlay_border").hide();
        		}
        	
        });  
         //event trigger for facet sorting buttons 
        $("input[name='sorter']").change(function() {
            if ($("input[name='sorter']:checked").val() == 'sortbyabc') {
                Manager.sortType = 'sortbyabc';
                Manager.doRequest(0);
            } else {
                Manager.sortType = 'sortbycount';
                Manager.doRequest(0);
            }
        });
         
         /*
        $('#somecontent a').mouseover(function(){
            if(!$('#somecontent .tip').exists())
            {
                // add your tip to the dom
                $('#somecontent').append('<div class="tip">Tool Tip Stuff Here</div>');
                
                // animate the display if you wish
                $('#somecontent .tip').fadeIn('slow', function() {
                });
                
                // remove element on mouse out
                $('.tip').mouseout(function(){
                   $(this).remove();
                })
            }
        });
		*/
         
        
		/*
        $('#example-1').tipsy();
        
        $('#auto-gravity').tipsy({gravity: $.fn.tipsy.autoNS});
        
        $('#example-fade').tipsy({fade: true});
        
        $('#example-custom-attribute').tipsy({title: 'id'});
        $('#example-callback').tipsy({title: function() { return this.getAttribute('original-title').toUpperCase(); } });
        $('#example-fallback').tipsy({fallback: "Where's my tooltip yo'?" });
        
        $('#example-html').tipsy({html: true });
        
        //$('.tipsy').bind('mouseout',function() {alert('here')});
            
            $('#south').tipsy({gravity: 's'});
            $('#east').tipsy({gravity: 'e'});
            $('#west').tipsy({gravity: 'w'});
            $('#north-west').tipsy({gravity: 'nw'});
            $('#north-east').tipsy({gravity: 'ne'});
            $('#south-west').tipsy({gravity: 'sw'});
            $('#south-east').tipsy({gravity: 'se'});
          
            $('#north').hover(function() {
            	var flag = true;
            	if(flag) {
                	$('#north').tipsy({delayOut: 10000,gravity: 'n'});
            	}
            	
            });
        */
    });

</script>

