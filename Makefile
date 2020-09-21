test-refresh:
	lein test-refresh :growl

membrane:
	clj -A:membrane -m feedly-membrane.view

cljfx:
	clj -A:cljfx -m feedly-cljfx.gene-feedly

uberjar:
	clj -A:cljfx:uberjar

runuberjar:
	java -cp dist/feedly.jar clojure.main -m feedly-cljfx.gene-feedly

package:
	jpackage @jpackage/common
