package com.example.shop;

import com.example.shop.billing.InvoicesPage;
import dev.jbaby.wicket.oat.WicketOats;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.stereotype.Component;

@Component
public class WicketApplication extends WebApplication {

	@Override
	public Class<? extends Page> getHomePage() {
		return InvoicesPage.class;
	}

	@Override
	protected void init() {
		super.init();
		WicketOats.install(this);
	}
}
