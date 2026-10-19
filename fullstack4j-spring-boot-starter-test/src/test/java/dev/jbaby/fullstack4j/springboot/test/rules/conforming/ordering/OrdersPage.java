/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering;

import dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering.internal.Order;
import dev.jbaby.fullstack4j.springboot.test.rules.conforming.ordering.internal.Orders;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class OrdersPage extends WebPage {

	@SpringBean
	private Orders orders;

	public OrdersPage() {
		add(new ListView<>("orders", LoadableDetachableModel.of(() -> this.orders.open())) {

			@Override
			protected void populateItem(ListItem<Order> item) {
				Order order = item.getModelObject();
				String id = order.id();
				item.add(new Label("customer", order.customer()));
				item.add(new Link<Void>("ship") {

					@Override
					public void onClick() {
						OrdersPage.this.orders.ship(id);
					}
				});
			}
		});
	}
}
