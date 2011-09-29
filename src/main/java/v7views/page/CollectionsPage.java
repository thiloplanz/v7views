/**
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

package v7views.page;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.click.ActionResult;
import org.apache.click.Context;
import org.apache.click.Page;
import org.apache.click.control.AbstractLink;
import org.apache.click.control.Column;
import org.apache.click.control.PageLink;
import org.apache.click.control.Panel;
import org.apache.click.control.Table;
import org.apache.click.control.TextField;
import org.apache.click.element.Element;
import org.apache.click.element.JsImport;
import org.apache.click.element.JsScript;
import org.apache.click.extras.control.LinkDecorator;
import org.apache.click.extras.panel.TabbedPanel;
import org.apache.click.extras.tree.Tree;
import org.apache.click.util.HtmlStringBuffer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import v7views.mongo.DBCollectionDataProvider;
import v7views.mongo.InitMongo;
import v7views.v7db.BSONBackedObject;
import v7views.v7db.BSONBackedObjectLoader;
import v7views.v7db.LocalizedString;
import v7views.v7db.SchemaDefinition;

public class CollectionsPage extends Page {

	public LocalizedString title;

	public final Table table;

	public Tree tree;

	public String solr;

	public String filter;

	public BSONBackedObject[] views;

	public TabbedPanel tabs;

	public final TextField solrField = new TextField("solrField", "Search");

	private final BSONBackedObject demoView;

	private final BSONBackedObject schemaBSON;

	public CollectionsPage() throws IOException {

		BSONBackedObject config = InitMongo.getConfig(getContext()
				.getServletContext());

		String[] viewNames = config.getStringFieldAsArray("views");

		views = new BSONBackedObject[viewNames.length];
		int i = 0;
		for (String v : viewNames) {
			views[i++] = BSONBackedObjectLoader.parse(IOUtils.toString(
					getClass().getResourceAsStream(v), "UTF-8"), null);
		}

		tabs = new TabbedPanel("tabs");
		i = 0;
		Locale locale = getContext().getLocale();

		for (BSONBackedObject v : views) {
			tabs.add(new Panel(LocalizedString.get(v, "caption", locale),
					String.valueOf(i++), "list.htm"));
		}

		tabs.onInit();

		demoView = views[Integer.parseInt(tabs.getActivePanel().getId())];
		String s = demoView.getStringField("schema");
		schemaBSON = s == null ? null : BSONBackedObjectLoader.parse(IOUtils
				.toString(getClass().getResourceAsStream(s), "UTF-8"), null);

		SchemaDefinition demoSchema = schemaBSON == null ? null
				: new SchemaDefinition(schemaBSON);

		table = table(demoView, demoSchema, locale);
		// tree = tree(demoSchema);
		// form.add(tree);

		title = LocalizedString.get(demoView, "caption");

	}

	private Table table(BSONBackedObject viewDefinition, SchemaDefinition sd,
			Locale locale) {
		final Table table = new Table("listTable");
		for (BSONBackedObject field : viewDefinition
				.getObjectFieldAsArray("columns")) {
			final Column c = column(field, sd, locale);
			table.addColumn(c);
			String link = field.getStringField("link");
			if ("detailPage".equals(link)) {
				PageLink detailLink = new PageLink(DetailPage.class);

				c.setDecorator(new LinkDecorator(table, detailLink, "_id") {

					@Override
					protected void renderActionLink(HtmlStringBuffer buffer,
							AbstractLink link, Context context, Object row,
							Object value) {
						Object p = c.getProperty(row);
						if (p != null)
							link.setLabel(p.toString());
						else
							link.setLabel(c.getName());
						super.renderActionLink(buffer, link, context, row,
								value);
					}

				});
			}
		}
		Integer pageSize = viewDefinition.getIntegerField("pageSize");
		if (pageSize != null)
			table.setPageSize(pageSize);

		return table;
	}

	private Column column(BSONBackedObject vd, SchemaDefinition sd,
			Locale locale) {
		String fieldName = vd.getStringField("field");
		String caption = LocalizedString.get(vd, "caption", locale);
		if (caption == null)
			caption = StringUtils.defaultString(sd == null ? null : sd
					.getFieldCaption(fieldName, locale), fieldName);
		return new Column(fieldName, caption);
	}

	public ActionResult onReloadTable() {
		solrField.setValue(solr);
		table.onProcess();
		onGet();
		return new ActionResult(table.toString(), ActionResult.HTML);
	}

	@Override
	public void onGet() {

		try {

			String keywords = solrField.getValue();

			BSONBackedObject query = null;
			if (isNotBlank(filter)) {
				query = BSONBackedObjectLoader.parse(filter, null);
			}

			table.setDataProvider(new DBCollectionDataProvider(InitMongo
					.getCollection(getContext().getServletContext(), demoView
							.getStringField("collection")), table, demoView
					.getObjectField("orderBy"), query, null));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Element> getHeadElements() {
		if (headElements == null) {
			headElements = super.getHeadElements();
			headElements.add(new JsScript("var schema = " + schemaBSON));
			headElements.add(new JsImport("/assets/jquery-1.6.2.min.js"));
			headElements.add(new JsImport("/collections.js"));

		}
		return headElements;
	}

	@Override
	public String getTemplate() {
		return "/border-template.htm";
	}
}
