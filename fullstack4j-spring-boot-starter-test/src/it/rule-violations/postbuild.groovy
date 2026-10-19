// Every rule has to name its violation in the build log.
def log = new File(basedir, 'build.log').text

def expected = [
	// the fullstack4j rules
	'com.example.shop.shared.Money is in slice shared, which isn\'t named after a business capability',
	'com.example.shop.billing.BillingHelper is in the base package of billing',
	'Class <com.example.shop.billing.Note> does not reside in a package \'com.example.shop.*.internal..\'',
	'Field <com.example.shop.ordering.OrdersPage.repository> has type <com.example.shop.ordering.internal.OrderRepository>',
	'listens to com.example.shop.billing.internal.Invoice, which is a document',
	'creates com.example.shop.ordering.OrderShipped, an event of ordering',
	'Field <com.example.shop.ordering.OrdersPage.lastShipped> has raw type a document or a record that isn\'t Serializable',
	'com.example.shop.ordering.OrdersPage.refresh() is annotated with @Transactional',
	// Taikai's rules
	'Class <com.example.shop.ordering.internal.OrderService> has name matching \'.*(Service|Impl|Dto)\'',
	'com.example.shop.ordering.internal.OrderService.events',
	'calls com.example.shop.ordering.internal.OrderService.ship(java.lang.String)',
	'com.example.shop.ordering.OrdersTests.shipsOnce()',
]

def missing = expected.findAll { !log.contains(it) }
assert missing.isEmpty() : "Violations missing from the build log:\n${missing.join('\n')}"

// and nothing else: a rule that also flags correct code would raise the count
assert log.contains('Found 13 Taikai violations for 12 rules!')
