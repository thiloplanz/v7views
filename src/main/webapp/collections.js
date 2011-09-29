/*
 * Copyright (c) 2011, Thilo Planz. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

(function() {
  var findFieldDefinition, makeOptions, reloadTable, solr, solrClear, toggleFilterItem;
  findFieldDefinition = function(x) {
    var caption, id, item, _ref;
    caption = x.caption;
    if (!caption) {
      return {};
    }
    _ref = schema.fields.itemData.fields;
    for (id in _ref) {
      item = _ref[id];
      if (item.caption === caption && item.possibleValues) {
        return {
          id: "itemData." + id,
          schema: item
        };
      }
    }
    return {};
  };
  solr = function() {
    return jQuery("#solrField");
  };
  solrClear = function() {
    return jQuery("#clearSolr");
  };
  reloadTable = function(event) {
    var cb, f, filter, id, link, params, _i, _j, _len, _len2, _ref, _ref2;
    link = jQuery(event != null ? event.currentTarget : void 0).attr('href') || '';
    params = {
      solr: solr().val(),
      pageAction: 'onReloadTable'
    };
    filter = {};
    _ref = jQuery("b.toggleFilterItem");
    for (_i = 0, _len = _ref.length; _i < _len; _i++) {
      f = _ref[_i];
      id = f.getAttribute('v7filter');
      _ref2 = jQuery("input:checkbox[name='" + id + "']:checked");
      for (_j = 0, _len2 = _ref2.length; _j < _len2; _j++) {
        cb = _ref2[_j];
        if (!filter[id]) {
          filter[id] = {
            '$in': []
          };
        }
        filter[id]['$in'].push(cb.value);
      }
    }
    params.filter = JSON.stringify(filter);
    if (params.solr) {
      solrClear().show();
    } else {
      solrClear().hide();
    }
    jQuery.get(link, params, function(data) {
      return jQuery("#tableContainer").html(data);
    });
    return false;
  };
  makeOptions = function(id, schema) {
    var x, _i, _len, _ref, _results;
    _ref = schema.possibleValues;
    _results = [];
    for (_i = 0, _len = _ref.length; _i < _len; _i++) {
      x = _ref[_i];
      if (x.disabled !== 'true') {
        _results.push("<label style='float:left; width: 20%'><input type='checkbox' name='" + id + "' value='" + x.caption + "' />" + x.caption + "</label>		");
      }
    }
    return _results;
  };
  toggleFilterItem = function(event) {
    var existing, id, item, schema, _ref;
    item = jQuery(event != null ? event.currentTarget : void 0).text() || event;
    _ref = findFieldDefinition({
      caption: item
    }), id = _ref.id, schema = _ref.schema;
    if (schema) {
      existing = jQuery("b.toggleFilterItem[v7filter='" + id + "']");
      if (existing.length) {
        existing.parent().remove();
        return reloadTable();
      }
      jQuery("#tableContainer").parent().before("<div>			<b class='toggleFilterItem' v7filter='" + id + "'>" + schema.caption + "</b>				<img onclick='closeFilterItem(this)' src='/click/colorpicker/images/close.png' />			<br/>" + (makeOptions(id, schema).join('')) + "			<div style='clear: both'/></div>");
    }
    return false;
  };
  window.closeFilterItem = function(img) {
    return toggleFilterItem(jQuery(img).prev().text());
  };
  jQuery(document).ready(function() {
    jQuery(".pagelinks-nobanner a").live('click', reloadTable);
    solr().bind('change', reloadTable);
    solrClear().bind('click', function() {
      solr().val('');
      return reloadTable();
    });
    jQuery("#tableContainer th").live('click', toggleFilterItem);
    return jQuery("input:checkbox").live('change', reloadTable);
  });
}).call(this);
