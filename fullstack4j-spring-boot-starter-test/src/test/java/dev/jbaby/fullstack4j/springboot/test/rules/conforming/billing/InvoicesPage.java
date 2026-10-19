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
package dev.jbaby.fullstack4j.springboot.test.rules.conforming.billing;

import java.io.Serializable;

import dev.jbaby.fullstack4j.springboot.test.rules.conforming.billing.internal.Invoice;
import dev.jbaby.fullstack4j.springboot.test.rules.conforming.billing.internal.Invoices;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class InvoicesPage extends WebPage {

	@SpringBean
	private Invoices invoices;

	private Choice selected = new Choice("all", "All invoices");

	public InvoicesPage() {
		add(new Label("filter", this.selected.label()));
		add(new ListView<>("invoices", LoadableDetachableModel.of(() -> this.invoices.all())) {

			@Override
			protected void populateItem(ListItem<Invoice> item) {
				item.add(new Label("order", item.getModelObject().orderId()));
			}
		});
	}

	record Choice(String id, String label) implements Serializable {
	}
}
