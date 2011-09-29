######## Schema definition handling

findFieldDefinition = (x) -> 
	{caption} = x 
	return {} unless caption
	for id, item of schema.fields.itemData.fields
		if (item.caption == caption and item.possibleValues)
			return id : "itemData.#{id}" , schema : item
	return {}
	


########
reloadTable = (event) -> 
    link = jQuery(event?.currentTarget).attr('href') or ''
    params = 
    	pageAction : 'onReloadTable'
   
    filter = {}
   	
   	for f in jQuery "b.toggleFilterItem"
    	id = f.getAttribute 'v7filter'
    	for cb in jQuery "input:checkbox[name='#{id}']:checked"
    		filter[id] = { '$in' : []  } unless filter[id]
    		filter[id]['$in'].push cb.value
    
    params.filter = JSON.stringify filter
   
    jQuery.get link, params, (data) ->
        jQuery("#tableContainer").html data;
    return false
    
makeOptions = (id, schema) ->
	for x in schema.possibleValues when x.disabled != 'true'
		"<label style='float:left; width: 20%'><input type='checkbox' name='#{id}' value='#{x.caption}' />#{x.caption}</label>
		"
    	 
toggleFilterItem = (event) ->
	item = jQuery(event?.currentTarget).text() or event
	{id, schema} = findFieldDefinition caption : item
	if (schema)
		existing = jQuery("b.toggleFilterItem[v7filter='#{id}']")
		if (existing.length)
			existing.parent().remove()
			return reloadTable()
		jQuery("#tableContainer").parent().before "<div>
			<b class='toggleFilterItem' v7filter='#{id}'>#{schema.caption}</b>
				<img onclick='closeFilterItem(this)' src='/click/colorpicker/images/close.png' />
			<br/>#{makeOptions(id, schema).join('')}
			<div style='clear: both'/></div>"
	return false
   
window.closeFilterItem = (img) ->
	toggleFilterItem jQuery(img).prev().text()
   
jQuery(document).ready  ->
	jQuery(".pagelinks-nobanner a").live 'click', reloadTable
	jQuery("#tableContainer th").live 'click', toggleFilterItem
	jQuery("input:checkbox").live 'change', reloadTable