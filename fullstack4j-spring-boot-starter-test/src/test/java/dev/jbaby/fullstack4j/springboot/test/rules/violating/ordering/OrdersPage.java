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
package dev.jbaby.fullstack4j.springboot.test.rules.violating.ordering;

import dev.jbaby.fullstack4j.springboot.test.rules.violating.ordering.internal.Order;
import dev.jbaby.fullstack4j.springboot.test.rules.violating.ordering.internal.OrderRepository;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class OrdersPage extends WebPage {

	@SpringBean
	private OrderRepository repository;

	private Order lastShipped;

	public OrdersPage() {
		Order first = this.repository.findAll().get(0);
		add(new Label("customer", first.customer()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(first.id() != null);
			}
		});
	}
}
