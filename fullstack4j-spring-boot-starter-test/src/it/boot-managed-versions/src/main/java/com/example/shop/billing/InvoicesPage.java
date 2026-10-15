package com.example.shop.billing;

import java.util.Iterator;
import java.util.List;

import com.example.shop.billing.internal.Invoice;
import com.example.shop.billing.internal.InvoiceRepository;
import dev.jbaby.wicket.oat.components.OatDataTable;
import dev.jbaby.wicket.oat.components.OatEmptyState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class InvoicesPage extends WebPage {

	@SpringBean
	private InvoiceRepository invoices;

	public InvoicesPage() {
		List<IColumn<Invoice, String>> columns = List.of(
				new LambdaColumn<>(Model.of("Customer"), Invoice::customer),
				new LambdaColumn<>(Model.of("Amount"), Invoice::amount));

		add(new OatDataTable<>("invoices", columns, new SortableDataProvider<Invoice, String>() {

			@Override
			public Iterator<? extends Invoice> iterator(long first, long count) {
				return invoices.findAll().stream().skip(first).limit(count).iterator();
			}

			@Override
			public long size() {
				return invoices.count();
			}

			@Override
			public IModel<Invoice> model(Invoice invoice) {
				return Model.of(invoice);
			}
		}, 10).setEmptyState(id -> new OatEmptyState(id, "No invoices yet")));
	}
}
