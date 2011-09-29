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

package v7views.mongo;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;

import v7views.v7db.BSONBackedObject;
import v7views.v7db.BSONBackedObjectLoader;

import com.mongodb.Mongo;
import com.mongodb.MongoURI;

public class InitMongo implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent sce) {
		getMongo(sce.getServletContext()).close();
	}

	public void contextInitialized(ServletContextEvent sce) {

		try {
			BSONBackedObject conf = BSONBackedObjectLoader.parse(FileUtils
					.readFileToString(new File("conf/v7views.json"), "UTF-8"),
					null);
			sce.getServletContext().setAttribute("v7views.config", conf);

			Mongo mongo = new Mongo();
			sce.getServletContext().setAttribute("mongo", mongo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Mongo getMongo(ServletContext context) {
		return (Mongo) context.getAttribute("mongo");
	}

	public static V7Collection getCollection(ServletContext context, String name) {
		BSONBackedObject conf = getConfig(context);
		Object data = conf.getField("collections." + name);
		if (data instanceof String) {
			MongoURI uri = new MongoURI((String) data);
			return new V7Collection(getMongo(context).getDB(uri.getDatabase())
					.getCollection(uri.getCollection()));
		}
		BSONBackedObject b = (BSONBackedObject) data;
		V7Collection base = getCollection(context, b
				.getStringField("collection"));
		return new V7Collection(base, b.getObjectField("filter"));
	}

	public static BSONBackedObject getConfig(ServletContext context) {
		return (BSONBackedObject) context.getAttribute("v7views.config");
	}

}
