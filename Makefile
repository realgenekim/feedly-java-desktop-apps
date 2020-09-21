test-refresh:
	lein test-refresh :growl

membrane:
	clj -A:membrane -m feedly-membrane.view

cljfx:
	clj -A:cljfx -m feedly-cljfx.gene-feedly
