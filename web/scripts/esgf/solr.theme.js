/*****************************************************************************
 * Copyright � 2011 , UT-Battelle, LLC All rights reserved
 *
 * OPEN SOURCE LICENSE
 *
 * Subject to the conditions of this License, UT-Battelle, LLC (the
 * �Licensor�) hereby grants to any person (the �Licensee�) obtaining a copy
 * of this software and associated documentation files (the "Software"), a
 * perpetual, worldwide, non-exclusive, irrevocable copyright license to use,
 * copy, modify, merge, publish, distribute, and/or sublicense copies of the
 * Software.
 *
 * 1. Redistributions of Software must retain the above open source license
 * grant, copyright and license notices, this list of conditions, and the
 * disclaimer listed below.  Changes or modifications to, or derivative works
 * of the Software must be noted with comments and the contributor and
 * organization�s name.  If the Software is protected by a proprietary
 * trademark owned by Licensor or the Department of Energy, then derivative
 * works of the Software may not be distributed using the trademark without
 * the prior written approval of the trademark owner.
 *
 * 2. Neither the names of Licensor nor the Department of Energy may be used
 * to endorse or promote products derived from this Software without their
 * specific prior written permission.
 *
 * 3. The Software, with or without modification, must include the following
 * acknowledgment:
 *
 *    "This product includes software produced by UT-Battelle, LLC under
 *    Contract No. DE-AC05-00OR22725 with the Department of Energy.�
 *
 * 4. Licensee is authorized to commercialize its derivative works of the
 * Software.  All derivative works of the Software must include paragraphs 1,
 * 2, and 3 above, and the DISCLAIMER below.
 *
 *
 * DISCLAIMER
 *
 * UT-Battelle, LLC AND THE GOVERNMENT MAKE NO REPRESENTATIONS AND DISCLAIM
 * ALL WARRANTIES, BOTH EXPRESSED AND IMPLIED.  THERE ARE NO EXPRESS OR
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE,
 * OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY PATENT, COPYRIGHT,
 * TRADEMARK, OR OTHER PROPRIETARY RIGHTS, OR THAT THE SOFTWARE WILL
 * ACCOMPLISH THE INTENDED RESULTS OR THAT THE SOFTWARE OR ITS USE WILL NOT
 * RESULT IN INJURY OR DAMAGE.  The user assumes responsibility for all
 * liabilities, penalties, fines, claims, causes of action, and costs and
 * expenses, caused by, resulting from or arising out of, in whole or in part
 * the use, storage or disposal of the SOFTWARE.
 *
 *
 ******************************************************************************/

/**
 * Presentation theme for Solr search results
 *
 * fwang2@ornl.gov
 *
 */

//alert('loading solr.theme.js');

(function ($) {


AjaxSolr.theme.prototype.result = function (doc, snippetReplica, snippetVersion, snippet, actions) {
	
	
    var output = '';

    if (doc.title.length > 7000) {
        doc.title = doc.title.substring(0,7000) + "...";
    }


    var replId = (doc.id).replace('/','_');
    
    replId = replId.replace('/','_');
    
    var idStr = 'id="' + replId + '" ';
    var titleStr = 'title="' + doc.title + '" ';
    //if(idStr.search("ARM") > -1) 
    //	alert('idStr: ' + idStr);
    var urlStr = 'url="' + doc.url + '" ';
    var formatStr = 'format="' + doc.metadata_format + '" ';
    var metadataURLStr = 'metadata_url="' + doc.metadata_url + '" ';

    var allStr = idStr + titleStr + urlStr + formatStr +  metadataURLStr; //+ descriptionStr + westDegreesStr + eastDegreesStr + northDegreesStr + southDegreesStr + datetime_startStr + datetime_stopStr;

    output += '<div class="search-entry">';

      output += '<div style="font-size:14px;font-style:bold" class="desc">';//'<h4 class="desc">';

      //output += '<a href="#" style="text-decoration:none">';
      if(doc['replica']) {
    	  output += '<span style="font-size:9px;color:#7d5f45;font-weight:bold;font-style:italic;font-type:Trade Gothic;margin-right:3px"> Replica </span>';
      }
      output += '<span class="actionitem ai_meta">';
      //output += '<a href="metadataview?' + 'id=' + doc.id + '" ';
      //alert('')
      output += '<a href="metadataview/' + doc.id + '.html"' ;//?' + 'id=' + doc.id + '" ';
      //output += 'class="met" rel="#metadata_overlay"';
      output += allStr + '>';
      output += doc.title + '</a>';
      output += '</div>' ;
      output += '<p id="links_' + doc.id + '" class="links"></p>';
      output += "<p/><div>" + snippetReplica + "</div>" + "<div class='snippetVersion'>" + snippetVersion + "</div>" + "<div class='snippet'>" + snippet + "</div>" + actions + '</div>';
	
      return output;
};


AjaxSolr.theme.prototype.actions = function (doc) {
	
    var output = '<div class="actions" style="font-size:12px">',
        selectID = '',
        selected = ESGF.search.selected,
        carts = [];

    var idStr = 'id="' + doc.id + '" ';
    var titleStr = 'title="' + doc.title + '" ';
    var urlStr = 'url="' + doc.url + '" ';
    var formatStr = 'format="' + doc.metadata_format + '" ';
    var metadataURLStr = 'metadata_url="' + doc.metadata_url + '" ';
    var allStr = idStr + titleStr + urlStr + formatStr +  metadataURLStr;

    output += "Further options: ";
    
    
    selectID = 'ai_select_'+ doc.id.replace(/\./g, "_");

    selectID = selectID.replace("|", "_");
    //alert('selectID: ' + selectID);
    
    //selectID = ESGF.datacart.replaceChars(selectID);
    
    //selectID = selectID.replace("|","_");

    //for ARMBE
    //selectID = selectID.replace("/","_");
    //selectID = selectID.replace("/","_");
    //alert('selectID: ' + selectID);
    
    selectMetID = 'meta_select_'+ doc.id.replace(/\./g, "_");
    
    if(ESGF.localStorage.search('dataCart',doc.id)) {
    	output += '<span class="actionitem"> <a href="#" class="' + 'selections"' + ' id="' + selectID + '">Remove From Cart</a></span>';
    } else {
    	//alert('id=' + selectID.substring(0, 15));
    	output += '<span class="actionitem"> <a href="#" class="' + 'selections"' + ' id="' + selectID + '">Add To Cart</a></span>';
    }
    
    if(doc.url instanceof Array) {
    	for(var i=0;i<doc.url.length;i++) {
    		
    		var url = doc.url[i];
    		
    		if(url.search("LAS") > -1) {
    			var display = true;
    			
    			var restrictions = ESGF.setting.lasRestrictions;
    			for(var k=0;k<restrictions.length;k++) {
    				var regex = restrictions[k];
    				//alert('reg: ' + regex + ' search: ' + (doc.title).search(regex));
    				if((doc.id).search(regex) > -1) {
    					display = false;
    				}
    			}
    			if(display) {
    				var tuple = url.split("\|");
            	    output += '<span class="actionitem ai_las"><a href="' + tuple[0] + '" target="_blank">Visualize and Analyze</a></span>';	
    			}
    	    } else if(url.search("OPENDAP") > -1) {
    	    	var tuple = url.split("\|");
        	    output += '<span class="actionitem ai_las"><a href="' + tuple[0] + '" target="_blank">OPENDAP</a></span>';
            } else if(url.search("application/gis") > -1) {
                var tuple = url.split("\|");
                output += '<span class="actionitem ai_las"><a href="' + tuple[0] + '" target="_blank">'+tuple[2]+'</a></span>';
            } 
    	}
    }
    
    var projectStr = 'project="' + doc.project + '" ';
    var modelStr = 'model="' + doc.model + '" ';
    var instituteStr = 'institute="' + doc.institute + '" ';
    var experimentStr = 'experiment="' + doc.experiment + '" ';
    
    var datasetId = (new String(doc.id)).split('|')[0];
    //alert('datasetIdparse: ' + datasetId);
    var datasetIdStr = 'datasetID="' + datasetId + '" ';
    var cimStr = projectStr + modelStr + instituteStr + experimentStr + datasetIdStr;
    
    if(doc.project == 'CMIP5' || doc.project == 'cmip5') {
        output += '<span class="__actionitem__"> <a class="cim-model" href="#" ' + cimStr + '">Model Documentation</a></span>';
        
    }
    
    
    
    if(doc.xlink != undefined) {
        var techNote = doc.xlink;
        
        for (var i=0;i<techNote.length;i++) {
        	//url.length - 1 => "Technical Note"
        	//url.length - 2 => <type of technical note>
        	//url.length - 3 => <physical url of technical note>
        	
        	var url = techNote[i].split("|");
        	
    	    output += '<span class="actionitem ai_las"><a href="' + url[url.length-3] + '" target="_blank">' + url[url.length-2] + '</a></span>';
        } 
        
    }
    
    if (ESGF.setting.annotate === true) {

        output += '<span class="actionitem"><a class="annotate" href="/esgf-web-fe/scripts/esgf/annotation_overlay.html" rel="#annotator_overlay"> Annotate</a></span>';
        output += "</div>";
    }

    
    $("a#" + selectID).live('click', {doc:doc}, function (evt) {

    	//alert('clickiung');
        var metadataFormat = doc.metadata_format;
        
        //right now, we only support downloads through TDS
        //when we support others, this if guard will be removed
        if(metadataFormat === 'THREDDS') {

        	//alert('datasetId: ' + evt.data.doc.id);
        	//alert('number of files: ' + evt.data.doc['number_of_files']);
        	//var docInfo = 
        	//alert('access: ' + evt.data.doc['access']);
        	//evt.data.doc['access'] = undefined;
        	//alert('access: ' + evt.data.doc['access']);
        	
            selected[evt.data.doc.id] = doc;
            if ( jQuery.trim(this.innerHTML) == "Add To Cart") {
            	
            	if(ESGF.setting.datacartMax <= ESGF.localStorage.toKeyArr('dataCart').length) {
            		alert('Data cart contents exceeded.  Please remove a dataset from the datacart before adding a new one');
            	} else {

            		//alert('dataset has index: ' + evt.data.doc['index_node']);
            		var datasetInfo = '';
                	//alert('adding to cart');
                	if(evt.data.doc['xlink'] != undefined) {
                		//alert('xlink defined');
                		//alert(evt.data.doc['xlink']);
                		//add to the datacart localstorage
                		
                		
                    	if(evt.data.doc['index_node'] != undefined) {

                    		//alert('index defined');

                    		
                    		//var datasetInfo = {'numFiles' : evt.data.doc['number_of_files'], 'peer' : evt.data.doc['index_node'] , 'xlink' : evt.data.doc['xlink'], 'access' : undefined};//evt.data.doc['access']};
                    		datasetInfo = {'numFiles' : evt.data.doc['number_of_files'], 'peer' : evt.data.doc['index_node'] , 'xlink' : evt.data.doc['xlink'], 'access' : evt.data.doc['access']};

                    		
                    		//alert(datasetInfo['peer']);
                    		//alert(datasetInfo['access']);
                        	//ESGF.localStorage.put('dataCart',evt.data.doc.id,datasetInfo);
                    	
                    	
                    	} else {

                    		//alert('peer should be undefined');
                    		
                    		//var datasetInfo = {'numFiles' : evt.data.doc['number_of_files'], 'peer' : 'undefined' , 'xlink' : evt.data.doc['xlink'], 'access' : undefined};//evt.data.doc['access']};
                    		datasetInfo = {'numFiles' : evt.data.doc['number_of_files'], 'peer' : evt.data.doc['index_node'] , 'xlink' : evt.data.doc['xlink'], 'access' : evt.data.doc['access']};

                    		//alert('xlink: ' + datasetInfo['xlink']);
                    		

                    		//alert(datasetInfo['peer']);
                    		//alert(datasetInfo['access']);
                        	
                    		//ESGF.localStorage.put('dataCart',evt.data.doc.id,datasetInfo);
                    	
                    	
                    	}
                		
                	} else {
                		//alert('xlink undefined');
                	
                		//add to the datacart localstorage
                    	if(evt.data.doc['index_node'] != undefined) {

                    		
                    		//alert('index defined');
                    		
                    		//var datasetInfo = {'numFiles' : evt.data.doc['number_of_files'], 'peer' : evt.data.doc['index_node'] , 'xlink' : 'undefined',  'access' : undefined};//evt.data.doc['access'] };
                    		datasetInfo = {'numFiles' : evt.data.doc['number_of_files'], 'peer' : evt.data.doc['index_node'] , 'xlink' : evt.data.doc['xlink'], 'access' : evt.data.doc['access']};

                    		//alert(datasetInfo['peer']);
                    		//alert(datasetInfo['access']);
                        	
                    		//ESGF.localStorage.put('dataCart',evt.data.doc.id,datasetInfo);
                    	
                    	
                    	} else {
                    		//alert('peer should be undefined');
                    		//var datasetInfo = {'numFiles' : evt.data.doc['number_of_files'], 'peer' : 'undefined' , 'xlink' : 'undefined',  'access' : undefined };//evt.data.doc['access'] };
                    		datasetInfo = {'numFiles' : evt.data.doc['number_of_files'], 'peer' : evt.data.doc['index_node'] , 'xlink' : evt.data.doc['xlink'], 'access' : evt.data.doc['access']};

                    		//alert(datasetInfo['peer']);
                    		//alert(datasetInfo['access']);
                        	
                    	
                    	}
                	
                	}
                	

            		//alert('peer: ' + datasetInfo['peer'] + ' access: ' + datasetInfo['access']);
                	
                	var queryString = {
                			'dataset_id' : evt.data.doc.id
                	};
                	
                	/*
                	//put into datacart
                	var datacartControllerUrl = '/esgf-web-fe/datacartcontroller2/datacart';
                	$.ajax({
    					url: datacartControllerUrl,
    					global: false,
    					type: 'POST',
    					async: false,
    					data: queryString,
    					success: function(data) {
    						alert('success');
    					},
    					error: function(jqXHR) {
    						alert('error status: ' + jqXHR.status);
    					}
    				});
                	*/
                	
            		ESGF.localStorage.put('dataCart',evt.data.doc.id,datasetInfo);
            	
                	
                	//add to the datacart searchstates localstorage
                	
                    var key = ESGF.localStorage.toString('esgf_fq');
                    var value = evt.data.doc.id;

                	ESGF.localStorage.append('esgf_searchStates', key, value);
                	
                    this.innerHTML="Remove From Cart";
            		
            	}
            	

            } else {
            	//remove from super cookie
            	ESGF.localStorage.remove('dataCart',evt.data.doc.id);

            	/*
            	alert('removing: ' + evt.data.doc.id);
            	
            	var queryString = {
            			'dataset_id' : evt.data.doc.id
            	};
            	//remove the datacart from the session
            	var datacartControllerUrl = '/esgf-web-fe/datacartcontroller2/datacart';
            	$.ajax({
					url: datacartControllerUrl,
					global: false,
					type: 'DELETE',
					async: false,
					data: queryString,
					success: function(data) {
						alert('success');
					},
					error: function(jqXHR) {
						alert('error status: ' + jqXHR.status);
					}
				});
				*/
            	
            	//remove from stateful super cookie
                var key = ESGF.localStorage.toString('esgf_fq');
                var value = evt.data.doc.id;
            	ESGF.localStorage.removeFromValue('esgf_searchStates', key, value);
            	
            	
                this.innerHTML ="Add To Cart";
                delete selected[evt.data.doc.id];

            }

        } else {
            alert('Dataset: ' + doc.id + ' cannot be downloaded at this time');
        }
    //}

        return false;
    });
	

    return output;

};

AjaxSolr.theme.prototype.snippet = function (doc) {

    var output = '';

    if (doc.description != undefined)
        doc.text = doc.description[0];
    if (doc.text != undefined) {
    	output += 'Description: ';
        if (doc.text.length > 500) {
            output += doc.text.substring(0, 500);
            output += '<span style="display:none;">' + doc.text.substring(500);
            output += '</span> <a href="#" class="more"> ... more</a>';
        } else {
            output += doc.text;
        }
    } else {
        output = "No description available.";
    }

    return output;
};

AjaxSolr.theme.prototype.snippetReplica = function (doc) {

    var output = '';
    if(doc['replica']) {
        //output += 'Replica dataset at datanode: ' + doc['data_node'] + '<br />';
        output += 'Data Node: ' + doc['data_node'];
        
    } else {
        //output += 'Master dataset at datanode: ' + doc['data_node'];
        output += 'Data Node: ' + doc['data_node'];
    }

    return output;
};

AjaxSolr.theme.prototype.snippetVersion = function (doc) {

    var output = '<span style="font-style:italic;font-weight:bold">';
    //alert('latest: ' + doc['latest']);
    if(doc['latest'] == 'true') {
    	output += 'Version: ' + doc['version'] + ' (Most Recent)';
    } else {
        output += 'Version: ' + doc['version'] + ' ';
    }
    
    output += '</span>';

    return output;
};

AjaxSolr.theme.prototype.facet_browser_title = function(value) {
    return $('<div class="facet_browser_title">' + 'Facet Browser' + '<\div>');
};

AjaxSolr.theme.prototype.facet_title = function(value) {
    var title = $('<span class="facet_title"><h3>' + value + '</h3></span>');
    return title;
};

AjaxSolr.theme.prototype.tag = function (value, weight, handler) {
  return $('<a href="#" class="tagcloud_item"/>').text(value).addClass('tagcloud_size_' + weight).click(handler);
};

AjaxSolr.theme.prototype.facet_link = function (value, handler) {
  return $('<a href="#"/>').text(value).click(handler);
};


AjaxSolr.theme.prototype.no_items_found = function () {
  return 'no items found in current selection';
};


AjaxSolr.theme.prototype.prevLink = function (stopValue,objectedItems,divFieldId,thisObject) {
    var $facet_link = $('<a href="#" id="prev_' + divFieldId + '"> prev ' + thisObject.incrementValue + '...</a>').click(thisObject.prevClickHandler(divFieldId));
    return $facet_link;
};

AjaxSolr.theme.prototype.nextLink = function (divFieldId,thisObject) {
    var $facet_link = $('<a href="#" id="next_' + divFieldId + '"> next ' + thisObject.incrementValue + '...</a>').click(thisObject.nextClickHandler(divFieldId));
    return $facet_link;
};


AjaxSolr.theme.prototype.facet_content = function(stopValue,objectedItems,thisObject) {
    var $facet_content = $('<div></div>');
    if(thisObject.startingValue < objectedItems.length) {
        $facet_content.append('<p>');
        for(var i = thisObject.startingValue, l = stopValue; i < l; i++) {
            var facetTextValue = objectedItems[i].facet + ' (' + objectedItems[i].count + ')';
            var facet = objectedItems[i].facet;
            $facet_content.append($('<a href="#" class="tag_item" />').text(facetTextValue).click(thisObject.clickHandler(facet)));
            if(i != (stopValue - 1)) {
                $facet_content.append(' | ');
            }
        };
        $facet_content.append('</p>');

    }
    return $facet_content;
};


})(jQuery);


